package org.symade.kiev.gradle.internal.tasks.compile;

import org.gradle.api.internal.tasks.compile.ApiCompilerResult;
import org.gradle.api.internal.tasks.compile.CompilationFailedException;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.WorkResult;
import org.gradle.workers.internal.DefaultWorkResult;
import org.symade.kiev.gradle.api.plugins.KievPlugin;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class InProcessKievCompiler implements KievCompiler<KievJavaJointCompileSpec>, Serializable {

    private static final Logger LOGGER = Logging.getLogger(KievPlugin.class);

    @Override
    public WorkResult execute(KievJavaJointCompileSpec spec) {
        LOGGER.quiet("Initializing Kiev compiler in JVM: "+org.gradle.internal.jvm.Jvm.current());

        List<File> allSourceFiles = new ArrayList<>();
        for (File f : spec.getSourceFiles())
            allSourceFiles.add(f);
        if (allSourceFiles.isEmpty())
            return new DefaultWorkResult(false, null);


        Object kievCompiler = null;

        ArrayList<URL> classLoaderURLs = new ArrayList<>();
        for (File f : spec.getKievClasspath()) {
            try {
                classLoaderURLs.add(f.toURI().toURL());
            } catch (MalformedURLException e) {
                // ignore
            }
        }
        try (URLClassLoader kievCompilerClassLoader = new URLClassLoader(
                classLoaderURLs.toArray(new URL[0]),
                this.getClass().getClassLoader()
        ))
        {
            try {
                kievCompiler = kievCompilerClassLoader.loadClass("kiev.Compiler").getDeclaredConstructor().newInstance();
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                LOGGER.error("Class kiev.Compiler not found", e);
                throw new RuntimeException(e.getClass() + ": kiev.Compiler not on classpath: " + e);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            List<String> sourceRoots = spec.getSourceRoots().stream()
                    .map(File::getAbsolutePath)
                    .collect(Collectors.toList());

            boolean isDebug = true; //LOGGER.isDebugEnabled();
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Kiev compiler using the following source roots:");
                sourceRoots.forEach(LOGGER::info);
            }

            LinkedHashSet<String> classPaths = new LinkedHashSet<>();
            for (File f : spec.getKievClasspath()) {
                String path = f.getAbsolutePath();
                if (!classPaths.contains(path))
                    classPaths.add(path);
            }
            for (File f : spec.getCompileClasspath()) {
                String path = f.getAbsolutePath();
                if (!classPaths.contains(path))
                    classPaths.add(path);
            }
            StringBuilder classpath = new StringBuilder();
            for (String path : classPaths) {
                if (!classpath.isEmpty())
                    classpath.append(File.pathSeparator);
                classpath.append(path);
            }

            List<String> args = new ArrayList<>();
            args.add("-ide");
            if (!classpath.isEmpty()) {
                args.add("-classpath");
                args.add(classpath.toString());
            }
            args.add("-javacerrors");
            args.add("-no-btd");
            args.add("-d");
            args.add(spec.getDestinationDir().getAbsolutePath());
            args.add("-target");
            args.add("8");

            args.addAll(spec.getCompileOptions().getCompilerArgs());

            File listOfFiles = new File(spec.getTempDir(), "files.txt");
            try (FileWriter wr = new FileWriter(listOfFiles, StandardCharsets.UTF_8)) {
                for (File f : allSourceFiles) {
                    wr.write(f.getAbsolutePath());
                    wr.write('\n');
                }
            }
            args.add("@" + listOfFiles.getPath());

            Method kievRunMethod = null;
            Field kievSourceMapping = null;
            Field kievErrorCount = null;
            try {
                LOGGER.quiet("kievCompiler.getClass(): " + kievCompiler.getClass());
                kievRunMethod = kievCompiler.getClass().getMethod("run", String[].class);
                kievSourceMapping = kievCompiler.getClass().getField("sourceToClassMapping");
                kievErrorCount = kievCompiler.getClass().getField("errorCount");
            } catch (NoSuchMethodException | NoSuchFieldException e) {
                LOGGER.error("Cannot resolve kiev.Compiler.run(String[] args) or kiev.Compiler.getSourceToClassMapping()", e);
                return new DefaultWorkResult(false, e);
            }

            {
                StringBuilder sb = new StringBuilder("kievCompiler args:");
                for (String a : args)
                    sb.append(' ').append(a);
                LOGGER.quiet(sb.toString());
            }

            if (LOGGER.isInfoEnabled()) {
                int fileCount = allSourceFiles.size();
                LOGGER.quiet("Compiling " + fileCount + " source file(s)" + " to " + spec.getDestinationDir().getAbsolutePath());
            }

            Map<String, Set<String>> sourceClassesMapping = null;
            int exitCode = 0;
            int errorCount = 0;
            try {
                Object argsArr = args.toArray(new String[0]);
                try {
                    exitCode = (Integer) kievRunMethod.invoke(kievCompiler, argsArr);
                }
                catch (InvocationTargetException e) {
                    if (e.getCause() == null) {
                        LOGGER.error("Unexpected error during kiev.Compiler.run(String[] args)", e);
                        throw new CompilationFailedException(e);
                    }
                    if (!e.getCause().getClass().getName().contains("CompilationAbortError")) {
                        LOGGER.error("Unexpected error during kiev.Compiler.run(String[] args)", e.getCause());
                        throw new CompilationFailedException(e.getCause());
                    }
                    // ignore CompilationAbortError, it's a normal completition
                }
                //noinspection unchecked
                sourceClassesMapping = (Map<String, Set<String>>)kievSourceMapping.get(null);
                errorCount = (Integer)kievErrorCount.get(null);
            } catch (IllegalAccessException e) {
                LOGGER.error("Cannot access error counts", e);
                throw new CompilationFailedException(e);
            }
            if (exitCode != 0)
                throw new CompilationFailedException(exitCode);


            ApiCompilerResult result = new ApiCompilerResult();
            if (sourceClassesMapping != null) {
                result.getSourceClassesMapping().putAll(sourceClassesMapping);
            }
            if (errorCount > 0 || sourceClassesMapping == null) {
                LOGGER.error("Got errors during compilation");
                throw new CompilationFailedException(result);
            }
            LOGGER.quiet("Successfully compiled "+sourceClassesMapping.size()+" files");
            return result;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

//        Method hasErrorsMethod, getWarningsMethod, getErrorsMethod;
//        boolean errorsInCompilation = false;
//        List<String> warnings = null;
//        List<String> errors = null;
//        try {
//            hasErrorsMethod = driver.getClass().getMethod("hasErrors");
//            getWarningsMethod = driver.getClass().getMethod("getWarnings");
//            getErrorsMethod = driver.getClass().getMethod("getErrors");
//            errorsInCompilation = (boolean) hasErrorsMethod.invoke(driver);
//            warnings = (List<String>) getWarningsMethod.invoke(driver);
//            errors = (List<String>) getErrorsMethod.invoke(driver);
//        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
//            Throwable cause = e.getCause(); //FIXME
//            LOGGER.error(cause.getMessage());
//            LOGGER.error(e.getMessage());
//        }
//
//        List<String> warningMessages = new ArrayList<>();
//        List<String> errorMessages = new ArrayList<>();
//
//        warnings.forEach(warning -> warningMessages.add("[WARNING] " + warning));
//        int numWarnings = warningMessages.size();
//
//        int numErrors = 0;
//        if (errorsInCompilation) {
//            errors.forEach(error -> errorMessages.add("[ERROR] " + error));
//            numErrors = errorMessages.size();
//        }
//
//        boolean hasWarningsOrErrors = numWarnings > 0 || errorsInCompilation;
//        StringBuilder sb;
//        sb = new StringBuilder();
//        sb.append("Gosu compilation completed");
//        if (hasWarningsOrErrors) {
//            sb.append(" with ");
//            if (numWarnings > 0) {
//                sb.append(numWarnings).append(" warning").append(numWarnings == 1 ? "" : 's');
//            }
//            if (errorsInCompilation) {
//                sb.append(numWarnings > 0 ? " and " : "");
//                sb.append(numErrors).append(" error").append(numErrors == 1 ? "" : 's');
//            }
//        } else {
//            sb.append(" successfully.");
//        }
//
//        if (LOGGER.isInfoEnabled()) {
//            sb.append(hasWarningsOrErrors ? ':' : "");
//            LOGGER.info(sb.toString());
//            warningMessages.forEach(LOGGER::info);
//            errorMessages.forEach(LOGGER::info);
//        } else {
//            if (hasWarningsOrErrors) {
//                sb.append("; rerun with INFO level logging to display details.");
//                LOGGER.quiet(sb.toString());
//            }
//        }

//        if (errorsInCompilation) {
//            if (spec.getCompileOptions().isFailOnError()) {
//                throw new GosuCompilationFailedException();
//            } else {
//                LOGGER.info("Gosu Compiler: Ignoring compilation failure as 'failOnError' was set to false");
//            }
//        }
    }

}

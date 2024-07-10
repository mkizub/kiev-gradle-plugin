/*
 * Copyright 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.symade.kiev.gradle.internal.tasks.compile;

import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.WorkResult;
import org.gradle.api.tasks.WorkResults;
import org.gradle.language.base.internal.compile.Compiler;
import org.gradle.util.internal.CollectionUtils;
import org.symade.kiev.gradle.api.plugins.KievPlugin;

import java.io.File;

/**
 * A Groovy {@link Compiler} which does some normalization of the compile configuration and behaviour before delegating to some other compiler.
 */
public class NormalizingKievCompiler implements Compiler<KievJavaJointCompileSpec> {
    private static final Logger LOGGER = Logging.getLogger(KievPlugin.class);
    private final Compiler<KievJavaJointCompileSpec> delegate;

    public NormalizingKievCompiler(Compiler<KievJavaJointCompileSpec> delegate) {
        this.delegate = delegate;
    }

    @Override
    public WorkResult execute(KievJavaJointCompileSpec spec) {
        resolveAndFilterSourceFiles(spec);
        resolveNonStringsInCompilerArgs(spec);
        logSourceFiles(spec);
        logCompilerArguments(spec);
        return delegateAndHandleErrors(spec);
    }

    private void resolveAndFilterSourceFiles(final KievJavaJointCompileSpec spec) {
//        final List<String> fileExtensions = CollectionUtils.collect(spec.getKievCompileOptions().getFileExtensions(), extension -> '.' + extension);
//        Iterable<File> filtered = Iterables.filter(spec.getSourceFiles(), new Predicate<File>() {
//            @Override
//            public boolean apply(File element) {
//                for (String fileExtension : fileExtensions) {
//                    if (hasExtension(element, fileExtension)) {
//                        return true;
//                    }
//                }
//                return false;
//            }
//        });
//
//        spec.setSourceFiles(ImmutableSet.copyOf(filtered));

        spec.setSourceFiles(spec.getSourceFiles());
    }

    private void resolveNonStringsInCompilerArgs(KievJavaJointCompileSpec spec) {
        // in particular, this is about GStrings
        spec.getCompileOptions().setCompilerArgs(CollectionUtils.toStringList(spec.getCompileOptions().getCompilerArgs()));
    }

    private void logSourceFiles(KievJavaJointCompileSpec spec) {
        if (!spec.getKievCompileOptions().isListFiles()) {
            return;
        }

        StringBuilder builder = new StringBuilder();
        builder.append("Source files to be compiled:");
        for (File file : spec.getSourceFiles()) {
            builder.append('\n');
            builder.append(file);
        }

        LOGGER.quiet(builder.toString());
    }

    private void logCompilerArguments(KievJavaJointCompileSpec spec) {
//        if (!LOGGER.isDebugEnabled()) {
//            return;
//        }
//
//        List<String> compilerArgs = new JavaCompilerArgumentsBuilder(spec).includeLauncherOptions(true).includeSourceFiles(true).build();
//        String joinedArgs = Joiner.on(' ').join(compilerArgs);
//        LOGGER.debug("Java compiler arguments: {}", joinedArgs);
    }

    private WorkResult delegateAndHandleErrors(KievJavaJointCompileSpec spec) {
        try {
            return delegate.execute(spec);
        } catch (RuntimeException e) {
            // in-process Groovy compilation throws a CompilationFailedException from another classloader, hence testing class name equality
            // TODO:pm Prefer class over class name for equality check once using WorkerExecutor for in-process groovy compilation
            if ((spec.getCompileOptions().isFailOnError() && spec.getKievCompileOptions().isFailOnError())
//                || !CompilationFailedException.class.getName().equals(e.getClass().getName())
            ) {
                throw e;
            }
            LOGGER.debug("Ignoring compilation failure.");
            return WorkResults.didWork(false);
        }
    }
}

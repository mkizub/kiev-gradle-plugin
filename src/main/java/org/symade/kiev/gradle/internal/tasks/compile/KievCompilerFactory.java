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

import org.gradle.api.internal.ClassPathRegistry;
import org.gradle.api.internal.tasks.compile.processing.AnnotationProcessorDetector;
//import org.gradle.api.problems.internal.InternalProblems;
import org.gradle.initialization.ClassLoaderRegistry;
import org.gradle.initialization.layout.ProjectCacheDir;
import org.gradle.internal.jvm.inspection.JvmVersionDetector;
import org.gradle.language.base.internal.compile.Compiler;
import org.gradle.language.base.internal.compile.CompilerFactory;
import org.gradle.process.internal.JavaForkOptionsFactory;
import org.gradle.process.internal.worker.child.WorkerDirectoryProvider;
import org.gradle.workers.internal.ActionExecutionSpecFactory;
import org.gradle.workers.internal.IsolatedClassloaderWorkerFactory;
import org.gradle.workers.internal.WorkerDaemonFactory;

public class KievCompilerFactory implements CompilerFactory<KievJavaJointCompileSpec> {
    private final WorkerDaemonFactory workerDaemonFactory;
    private final IsolatedClassloaderWorkerFactory inProcessWorkerFactory;
    private final JavaForkOptionsFactory forkOptionsFactory;
    private final AnnotationProcessorDetector processorDetector;
    private final JvmVersionDetector jvmVersionDetector;
    private final WorkerDirectoryProvider workerDirectoryProvider;
    private final ClassPathRegistry classPathRegistry;
    private final ClassLoaderRegistry classLoaderRegistry;
    private final ActionExecutionSpecFactory actionExecutionSpecFactory;
    private final ProjectCacheDir projectCacheDir;
//    private final InternalProblems problems;

    public KievCompilerFactory(WorkerDaemonFactory workerDaemonFactory, IsolatedClassloaderWorkerFactory inProcessWorkerFactory, JavaForkOptionsFactory forkOptionsFactory, AnnotationProcessorDetector processorDetector, JvmVersionDetector jvmVersionDetector, WorkerDirectoryProvider workerDirectoryProvider, ClassPathRegistry classPathRegistry, ClassLoaderRegistry classLoaderRegistry, ActionExecutionSpecFactory actionExecutionSpecFactory, ProjectCacheDir projectCacheDir/*, InternalProblems problems*/) {
        this.workerDaemonFactory = workerDaemonFactory;
        this.inProcessWorkerFactory = inProcessWorkerFactory;
        this.forkOptionsFactory = forkOptionsFactory;
        this.processorDetector = processorDetector;
        this.jvmVersionDetector = jvmVersionDetector;
        this.workerDirectoryProvider = workerDirectoryProvider;
        this.classPathRegistry = classPathRegistry;
        this.classLoaderRegistry = classLoaderRegistry;
        this.actionExecutionSpecFactory = actionExecutionSpecFactory;
        this.projectCacheDir = projectCacheDir;
//        this.problems = problems;
    }

    @Override
    public Compiler<KievJavaJointCompileSpec> newCompiler(KievJavaJointCompileSpec spec) {
//        MinimalKievCompileOptions kievOptions = spec.getKievCompileOptions();
//        CompilerWorkerExecutor compilerWorkerExecutor =
//                new ClassloaderIsolatedCompilerWorkerExecutor(inProcessWorkerFactory, actionExecutionSpecFactory, projectCacheDir);

        return new InProcessKievCompiler();
//        Compiler<KievJavaJointCompileSpec> kievCompiler = new DaemonGroovyCompiler(workerDirectoryProvider.getWorkingDirectory(), DaemonSideCompiler.class, classPathRegistry, compilerWorkerExecutor, classLoaderRegistry, forkOptionsFactory, jvmVersionDetector, problems.getInternalReporter());
//        return new AnnotationProcessorDiscoveringCompiler<>(new NormalizingKievCompiler(kievCompiler), processorDetector);
    }

//    public static class DaemonSideCompiler implements Compiler<KievJavaJointCompileSpec> {
//        private final ProjectLayout projectLayout;
//        private final List<File> javaCompilerPlugins;
//        private final InternalProblems problemsService;
//
//        @Inject
//        public DaemonSideCompiler(ProjectLayout projectLayout, List<File> javaCompilerPlugins, InternalProblems problemsService) {
//            this.projectLayout = projectLayout;
//            this.javaCompilerPlugins = javaCompilerPlugins;
//            this.problemsService = problemsService;
//        }
//
//        @Override
//        public WorkResult execute(KievJavaJointCompileSpec spec) {
//            Compiler<JavaCompileSpec> javaCompiler = new JdkJavaCompiler(new JavaHomeBasedJavaCompilerFactory(javaCompilerPlugins), problemsService);
//            Compiler<KievJavaJointCompileSpec> kievCompiler = new ApiKievCompiler(javaCompiler, projectLayout);
//            return kievCompiler.execute(spec);
//        }
//    }

}

/*
 * Copyright 2014 the original author or authors.
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

import org.gradle.api.internal.tasks.compile.AbstractJavaCompileSpecFactory;
import org.gradle.api.internal.tasks.compile.CommandLineJavaCompileSpec;
import org.gradle.api.internal.tasks.compile.ForkingJavaCompileSpec;
import org.gradle.api.tasks.compile.CompileOptions;
import org.gradle.jvm.toolchain.JavaInstallationMetadata;

import java.io.File;

public class DefaultKievJavaJointCompileSpecFactory extends AbstractJavaCompileSpecFactory<DefaultKievJavaJointCompileSpec> {
    public DefaultKievJavaJointCompileSpecFactory(CompileOptions compileOptions, JavaInstallationMetadata javaInstallationMetadata) {
        super(compileOptions, javaInstallationMetadata);
    }

    @Override
    protected DefaultKievJavaJointCompileSpec getForkingSpec(File javaHome) {
        return new DefaultForkingKievJavaJointCompileSpec(javaHome);
    }

    @Override
    protected DefaultKievJavaJointCompileSpec getDefaultSpec() {
        return new DefaultKievJavaJointCompileSpec();
    }

    @Override
    protected DefaultKievJavaJointCompileSpec getCommandLineSpec(File executable) {
        return new DefaultCommandLineKievJavaJointCompileSpec(executable);
    }

    //@Override
    protected DefaultKievJavaJointCompileSpec getInProcessSpec() {
        return new DefaultKievJavaJointCompileSpec();
    }

    private static class DefaultCommandLineKievJavaJointCompileSpec extends DefaultKievJavaJointCompileSpec implements CommandLineJavaCompileSpec {
        private final File executable;

        private DefaultCommandLineKievJavaJointCompileSpec(File executable) {
            this.executable = executable;
        }

        @Override
        public File getExecutable() {
            return executable;
        }
    }

    private static class DefaultForkingKievJavaJointCompileSpec extends DefaultKievJavaJointCompileSpec implements ForkingJavaCompileSpec {
        private final File javaHome;

        private DefaultForkingKievJavaJointCompileSpec(File javaHome) {
            this.javaHome = javaHome;
        }

        @Override
        public File getJavaHome() {
            return javaHome;
        }
    }
}

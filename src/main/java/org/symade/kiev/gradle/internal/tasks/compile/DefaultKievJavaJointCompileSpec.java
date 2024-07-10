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

import java.io.File;
import java.util.List;
import org.gradle.api.internal.tasks.compile.DefaultJavaCompileSpec;

public class DefaultKievJavaJointCompileSpec extends DefaultJavaCompileSpec implements KievJavaJointCompileSpec {
    private MinimalKievCompileOptions kievCompileOptions;
    private List<File> kievClasspath;

    @Override
    public MinimalKievCompileOptions getKievCompileOptions() {
        return kievCompileOptions;
    }

    public void setKievCompileOptions(MinimalKievCompileOptions kievCompileOptions) {
        this.kievCompileOptions = kievCompileOptions;
    }

    @Override
    public List<File> getKievClasspath() {
        return kievClasspath;
    }

    @Override
    public void setKievClasspath(List<File> groovyClasspath) {
        this.kievClasspath = groovyClasspath;
    }
}

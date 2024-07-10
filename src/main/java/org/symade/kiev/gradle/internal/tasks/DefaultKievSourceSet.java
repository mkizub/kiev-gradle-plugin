/*
 * Copyright 2023 the original author or authors.
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
package org.symade.kiev.gradle.internal.tasks;

import groovy.lang.Closure;
import org.gradle.api.Action;
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.reflect.HasPublicType;
import org.gradle.api.reflect.TypeOf;
import org.symade.kiev.gradle.api.tasks.KievSourceDirectorySet;
import org.symade.kiev.gradle.api.tasks.KievSourceSet;

import javax.annotation.Nullable;
import javax.inject.Inject;

import static org.gradle.api.reflect.TypeOf.typeOf;
import static org.gradle.util.internal.ConfigureUtil.configure;

public class DefaultKievSourceSet implements KievSourceSet, HasPublicType {
    private final KievSourceDirectorySet kiev;
//    private final SourceDirectorySet allKiev;

    @Inject
    public DefaultKievSourceSet(String name, String displayName, ObjectFactory objectFactory) {
        this.kiev = createKievSourceDirectorySet(name, displayName, objectFactory);
//        allKiev = objectFactory.sourceDirectorySet("all" + name, displayName + " Kiev source");
//        allKiev.source(kiev);
//        allKiev.getFilter().include("**/*.kj");
    }

    private static KievSourceDirectorySet createKievSourceDirectorySet(String name, String displayName, ObjectFactory objectFactory) {
        KievSourceDirectorySet kievSourceDirectorySet = objectFactory.newInstance(DefaultKievSourceDirectorySet.class, objectFactory.sourceDirectorySet(name, displayName + " Kiev source"));
        kievSourceDirectorySet.getFilter().include("**/*.java", "**/*.kj", "**/*.xml");
        return kievSourceDirectorySet;
    }

    @Override
    public KievSourceDirectorySet getKiev() {
        return kiev;
    }

    @Override
    public KievSourceSet kiev(@SuppressWarnings("rawtypes") @Nullable Closure configureClosure) {
        configure(configureClosure, getKiev());
        return this;
    }

    @Override
    public KievSourceSet kiev(Action<? super SourceDirectorySet> configureAction) {
        configureAction.execute(getKiev());
        return this;
    }

//    @Override
//    public SourceDirectorySet getAllKiev() {
//        return allKiev;
//    }

    @Override
    public TypeOf<?> getPublicType() {
        return typeOf(KievSourceSet.class);
    }
}

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
package org.symade.kiev.gradle.api.tasks;

import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import org.gradle.api.Action;
import org.gradle.api.file.SourceDirectorySet;

import javax.annotation.Nullable;

public interface KievSourceSet {
    /**
     * Returns the source to be compiled by the Kiev compiler for this source set. Any Java source present in this set
     * will be passed to the Kiev compiler for joint compilation.
     *
     * @return The Kiev/Java source. Never returns null.
     */
    SourceDirectorySet getKiev();

    /**
     * Configures the Kiev source for this set.
     *
     * <p>The given closure is used to configure the {@link SourceDirectorySet} which contains the Kiev source.
     *
     * @param configureClosure The closure to use to configure the Kiev source.
     * @return this
     */
    KievSourceSet kiev(@SuppressWarnings("rawtypes") @Nullable @DelegatesTo(SourceDirectorySet.class) Closure configureClosure);

    /**
     * Configures the Kiev source for this set.
     *
     * <p>The given action is used to configure the {@link SourceDirectorySet} which contains the Kiev source.
     *
     * @param configureAction The action to use to configure the Kiev source.
     * @return this
     */
    KievSourceSet kiev(Action<? super SourceDirectorySet> configureAction);

    /**
     * All Kiev source for this source set.
     *
     * @return the Kiev source. Never returns null.
     */
    SourceDirectorySet getAllKiev();
}

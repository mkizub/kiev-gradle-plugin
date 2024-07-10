package org.symade.kiev.gradle.internal.tasks.compile;

import org.gradle.api.tasks.WorkResult;
import org.gradle.language.base.internal.compile.Compiler;

/**
 * Copied from {@link org.gradle.language.base.internal.compile.Compiler}
 * @param <T> an implementation of GosuCompileSpec
 */
public interface KievCompiler<T extends KievCompileSpec> extends Compiler<T> {
    WorkResult execute(T var1);
}
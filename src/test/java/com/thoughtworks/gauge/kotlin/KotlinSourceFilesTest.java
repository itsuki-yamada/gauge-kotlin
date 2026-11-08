/*----------------------------------------------------------------
 *  Copyright (c) Itsuki Yamada
 *  Licensed under the Apache License, Version 2.0
 *  See LICENSE.txt in the project root for license information.
 *----------------------------------------------------------------*/
package com.thoughtworks.gauge.kotlin;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Checks that a Kotlin class can be called from Java tests.
 */
public class KotlinSourceFilesTest {

    @Test
    public void kotlinSourceFileIsDetected() {
        assertTrue(KotlinSourceFiles.isKotlinFile(Path.of("src", "test", "kotlin", "StepImplementation.kt")));
    }

    @Test
    public void otherFilesAreNotKotlinSources() {
        assertFalse(KotlinSourceFiles.isKotlinFile(Path.of("StepImplementation.java")));
        assertFalse(KotlinSourceFiles.isKotlinFile(Path.of("build.gradle.kts")));
        assertFalse(KotlinSourceFiles.isKotlinFile(Path.of("kotlin")));
    }

    @Test
    public void pathWithoutFileNameIsNotKotlinSource() {
        assertFalse(KotlinSourceFiles.isKotlinFile(Path.of("/")));
    }
}

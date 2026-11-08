/*----------------------------------------------------------------
 *  Copyright (c) Itsuki Yamada
 *  Licensed under the Apache License, Version 2.0
 *  See LICENSE.txt in the project root for license information.
 *----------------------------------------------------------------*/
package com.thoughtworks.gauge.kotlin

import java.nio.file.Path

/**
 * Helpers for Kotlin source files (.kt).
 */
object KotlinSourceFiles {
    private const val EXTENSION = ".kt"

    /**
     * Returns true when [path] points to a Kotlin source file (.kt).
     * Kotlin script files (.kts) are not step sources, so they return false.
     */
    @JvmStatic
    fun isKotlinFile(path: Path): Boolean =
        path.fileName?.toString()?.endsWith(EXTENSION) ?: false
}

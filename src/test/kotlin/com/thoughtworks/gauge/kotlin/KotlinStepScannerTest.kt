/*----------------------------------------------------------------
 *  Copyright (c) Itsuki Yamada
 *  Licensed under the Apache License, Version 2.0
 *  See LICENSE.txt in the project root for license information.
 *----------------------------------------------------------------*/
package com.thoughtworks.gauge.kotlin

import com.thoughtworks.gauge.StepRegistryEntry
import com.thoughtworks.gauge.registry.StepRegistry
import com.thoughtworks.gauge.scan.StaticScanner
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Checks that Kotlin steps are registered with the same values as the equivalent Java steps.
 * Each pair of sources has the same layout, so positions can be compared directly.
 */
class KotlinStepScannerTest {

    private fun scan(fileName: String, contents: String): StepRegistry {
        val scanner = StaticScanner()
        scanner.addStepsFromFileContents(fileName, contents)
        return scanner.registry
    }

    private fun assertSameEntry(java: StepRegistryEntry, kotlin: StepRegistryEntry) {
        assertEquals(java.stepText, kotlin.stepText)
        assertEquals(java.stepValue, kotlin.stepValue)
        assertEquals(java.fullyQualifiedName, kotlin.fullyQualifiedName)
        assertEquals(java.span, kotlin.span)
        assertEquals(java.hasAlias, kotlin.hasAlias)
        assertEquals(java.aliases, kotlin.aliases)
        assertEquals(java.parameters.map { it.nameAsString }, kotlin.parameters.map { it.nameAsString })
    }

    @Test
    fun registersStepLikeJava() {
        val java = scan(
            "/project/src/test/java/com/example/Steps.java",
            """
            package com.example;

            public class Steps {
                @Step("Greet <name> with <greeting>")
                public void greet(String name, String greeting) {
                }
            }
            """.trimIndent(),
        )
        val kotlin = scan(
            "/project/src/test/kotlin/com/example/Steps.kt",
            """
            package com.example

            class Steps {
                @Step("Greet <name> with <greeting>")
                fun greet(name: String, greeting: String) {
                }
            }
            """.trimIndent(),
        )

        val stepText = "Greet {} with {}"
        assertTrue(kotlin.contains(stepText))
        val entry = kotlin.get(stepText)
        assertSameEntry(java.get(stepText), entry)
        assertEquals("com.example.Steps.greet", entry.fullyQualifiedName)
        assertEquals("/project/src/test/kotlin/com/example/Steps.kt", entry.fileName)
        assertEquals(4, entry.span.begin.line)
        assertEquals(5, entry.span.begin.column)
        assertEquals(6, entry.span.end.line)
        assertEquals(5, entry.span.end.column)
    }

    @Test
    fun registersAliasesLikeJava() {
        val java = scan(
            "Steps.java",
            """
            public class Steps {
                @Step({"First alias <x>", "Second alias <x>"})
                public void aliased(String x) {
                }
            }
            """.trimIndent(),
        )
        val varargs = scan(
            "Steps.kt",
            """
            class Steps {
                @Step("First alias <x>", "Second alias <x>")
                fun aliased(x: String) {
                }
            }
            """.trimIndent(),
        )
        val arrayLiteral = scan(
            "Steps.kt",
            """
            class Steps {
                @Step(value = ["First alias <x>", "Second alias <x>"])
                fun aliased(x: String) {
                }
            }
            """.trimIndent(),
        )

        for (stepText in listOf("First alias {}", "Second alias {}")) {
            assertSameEntry(java.get(stepText), varargs.get(stepText))
            assertSameEntry(java.get(stepText), arrayLiteral.get(stepText))
        }
        assertEquals(listOf("First alias <x>", "Second alias <x>"), varargs.get("First alias {}").aliases)
    }

    @Test
    fun registersConcatenatedStepTextLikeJava() {
        val java = scan(
            "Steps.java",
            """
            public class Steps {
                @Step("A long step " + "text")
                public void concatenated() {
                }
            }
            """.trimIndent(),
        )
        val kotlin = scan(
            "Steps.kt",
            """
            class Steps {
                @Step("A long step " + "text")
                fun concatenated() {
                }
            }
            """.trimIndent(),
        )

        assertSameEntry(java.get("A long step text"), kotlin.get("A long step text"))
    }

    @Test
    fun acceptsFullyQualifiedAnnotation() {
        val kotlin = scan(
            "Steps.kt",
            """
            class Steps {
                @com.thoughtworks.gauge.Step("Qualified step")
                fun qualified() {
                }
            }
            """.trimIndent(),
        )

        assertTrue(kotlin.contains("Qualified step"))
    }

    @Test
    fun ignoresStepsOutsideRegularClasses() {
        val kotlin = scan(
            "Steps.kt",
            """
            @Step("Top level step")
            fun topLevel() {
            }

            object ObjectSteps {
                @Step("Object step")
                fun inObject() {
                }
            }

            class CompanionSteps {
                companion object {
                    @Step("Companion step")
                    fun inCompanion() {
                    }
                }
            }

            class Steps {
                @Step("Class step")
                fun inClass() {
                    @Step("Local step")
                    fun local() {
                    }
                }
            }
            """.trimIndent(),
        )

        assertTrue(kotlin.contains("Class step"))
        for (stepText in listOf("Top level step", "Object step", "Companion step", "Local step")) {
            assertFalse(kotlin.contains(stepText), stepText)
        }
    }

    @Test
    fun ignoresStepTextThatIsNotAPlainString() {
        val kotlin = scan(
            "Steps.kt",
            """
            const val NAME = "world"

            class Steps {
                @Step("Hello ${'$'}NAME")
                fun template() {
                }

                @Step(STEP_TEXT)
                fun constant() {
                }
            }
            """.trimIndent(),
        )

        assertTrue(kotlin.keys().isEmpty())
    }

    @Test
    fun appliesPackageToScan() {
        val contents = """
            package com.example

            class Steps {
                @Step("Packaged step")
                fun packaged() {
                }
            }
        """.trimIndent()

        val other = StepRegistry()
        KotlinStepScanner.addSteps(other, "Steps.kt", contents, "com.other")
        assertFalse(other.contains("Packaged step"))

        val matching = StepRegistry()
        KotlinStepScanner.addSteps(matching, "Steps.kt", contents, "com.other, com.example")
        assertTrue(matching.contains("Packaged step"))
    }

    @Test
    fun computesPositionsWithWindowsLineSeparators() {
        val kotlin = scan(
            "Steps.kt",
            "class Steps {\r\n    @Step(\"Windows step\")\r\n    fun windows() {\r\n    }\r\n}\r\n",
        )

        val span = kotlin.get("Windows step").span
        assertEquals(2, span.begin.line)
        assertEquals(5, span.begin.column)
        assertEquals(4, span.end.line)
        assertEquals(5, span.end.column)
    }
}

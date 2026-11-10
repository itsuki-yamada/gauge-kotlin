/*----------------------------------------------------------------
 *  Copyright (c) Itsuki Yamada
 *  Licensed under the Apache License, Version 2.0
 *  See LICENSE.txt in the project root for license information.
 *----------------------------------------------------------------*/
package com.thoughtworks.gauge.kotlin

import com.github.javaparser.Position
import com.github.javaparser.Range
import com.github.javaparser.ast.body.Parameter
import com.github.javaparser.ast.type.ClassOrInterfaceType
import com.thoughtworks.gauge.StepRegistryEntry
import com.thoughtworks.gauge.StepValue
import com.thoughtworks.gauge.registry.StepRegistry
import com.thoughtworks.gauge.scan.StepsUtil
import org.jetbrains.kotlin.CoreEnvironmentDeprecation
import org.jetbrains.kotlin.cli.extensionsStorage
import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.com.intellij.openapi.util.Disposer
import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtAnnotationEntry
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtClassBody
import org.jetbrains.kotlin.psi.KtCollectionLiteralExpression
import org.jetbrains.kotlin.psi.KtEscapeStringTemplateEntry
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtLiteralStringTemplateEntry
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtParenthesizedExpression
import org.jetbrains.kotlin.psi.KtPsiFactory
import org.jetbrains.kotlin.psi.KtStringTemplateExpression
import org.jetbrains.kotlin.psi.psiUtil.collectDescendantsOfType

/**
 * Finds `@Step` annotated functions in Kotlin source files (.kt) and adds them to a [StepRegistry],
 * in the same shape as the JavaParser based scanner does for .java files.
 *
 * Only functions declared directly in a class body are registered (policy B): top-level functions,
 * `object` and `companion object` members cannot be executed by gauge-java yet.
 */
object KotlinStepScanner {
    private const val STEP_ANNOTATION = "Step"
    private const val STEP_ANNOTATION_FQN = "com.thoughtworks.gauge.Step"

    /**
     * Creating the compiler environment takes a while, so it is created once, on the first .kt file.
     * Java-only projects never pay for it.
     *
     * KotlinCoreEnvironment is the way ktlint and detekt get PSI as well. The Kotlin team plans to rework
     * this API, so the opt-ins below may need changes when the Kotlin version is upgraded.
     */
    @OptIn(CompilerConfiguration.Internals::class, CoreEnvironmentDeprecation::class, ExperimentalCompilerApi::class)
    private val psiFactory: KtPsiFactory by lazy {
        val configuration = CompilerConfiguration()
        // No compiler plugins are used, but the environment requires a (possibly empty) extension storage.
        configuration.extensionsStorage = CompilerPluginRegistrar.ExtensionStorage()
        val environment = KotlinCoreEnvironment.createForProduction(
            Disposer.newDisposable("gauge-kotlin"),
            configuration,
            EnvironmentConfigFiles.JVM_CONFIG_FILES,
        )
        KtPsiFactory(environment.project, markGenerated = false)
    }

    /**
     * Parses [contents] of the Kotlin file [fileName] and registers its steps.
     *
     * @param packagesToScan value of the `package_to_scan` environment variable (comma separated), or null
     */
    @JvmStatic
    fun addSteps(registry: StepRegistry, fileName: String, contents: String, packagesToScan: String?) {
        val ktFile = parse(fileName, contents)
        if (!shouldScan(ktFile, packagesToScan)) {
            return
        }
        for (function in ktFile.collectDescendantsOfType<KtNamedFunction>()) {
            val ktClass = containingClass(function) ?: continue
            for (annotation in function.annotationEntries.filter(::isStepAnnotation)) {
                addStep(registry, fileName, contents, ktFile, ktClass, function, annotation)
            }
        }
    }

    private fun parse(fileName: String, contents: String): KtFile = synchronized(this) {
        // PSI expects "\n" line separators.
        psiFactory.createFile(fileName.substringAfterLast('/').substringAfterLast('\\'), contents.replace("\r\n", "\n"))
    }

    private fun shouldScan(ktFile: KtFile, packagesToScan: String?): Boolean {
        val packageName = ktFile.packageFqName.asString()
        if (packagesToScan.isNullOrEmpty() || packageName.isEmpty()) {
            return true
        }
        return packagesToScan.split(",").map { it.trim() }.contains(packageName)
    }

    /** Returns the class when [function] is declared directly in a class body (not in an object), otherwise null. */
    private fun containingClass(function: KtNamedFunction): KtClass? {
        val body = function.parent as? KtClassBody ?: return null
        return body.parent as? KtClass
    }

    private fun isStepAnnotation(annotation: KtAnnotationEntry): Boolean {
        val name = annotation.typeReference?.text ?: return false
        return name == STEP_ANNOTATION || name == STEP_ANNOTATION_FQN
    }

    private fun addStep(
        registry: StepRegistry,
        fileName: String,
        contents: String,
        ktFile: KtFile,
        ktClass: KtClass,
        function: KtNamedFunction,
        annotation: KtAnnotationEntry,
    ) {
        val arguments = annotation.valueArguments.mapNotNull { it.getArgumentExpression() }
        val expressions = arguments.flatMap { (it as? KtCollectionLiteralExpression)?.getInnerExpressions() ?: listOf(it) }
        // A step text that is not a plain string (for example "$x" or a constant) cannot be read statically.
        val stepTexts = expressions.map { stringValue(it) ?: return }
        val hasAlias = stepTexts.size > 1 || arguments.any { it is KtCollectionLiteralExpression }
        val aliases = if (hasAlias) stepTexts else emptyList()

        val packageName = ktFile.packageFqName.asString()
        val className = if (packageName.isEmpty()) ktClass.name else "$packageName.${ktClass.name}"
        val stepsUtil = StepsUtil()

        for (parameterizedStep in stepTexts) {
            val stepValue = StepValue(stepsUtil.getStepText(parameterizedStep), parameterizedStep)
            val entry = StepRegistryEntry()
            entry.setName(declaration(function))
            entry.setFullyQualifiedName("$className.${function.name}")
            entry.setStepText(parameterizedStep)
            entry.setStepValue(stepValue)
            entry.setParameters(parameters(function))
            entry.setSpan(range(contents, function))
            entry.setHasAlias(hasAlias)
            entry.setAliases(aliases.toMutableList())
            entry.setFileName(fileName)
            registry.addStep(stepValue, entry)
        }
    }

    /** Evaluates a string literal or a concatenation of string literals. Returns null for anything else. */
    private fun stringValue(expression: KtExpression?): String? = when (expression) {
        is KtStringTemplateExpression -> {
            val builder = StringBuilder()
            for (entry in expression.entries) {
                when (entry) {
                    is KtLiteralStringTemplateEntry -> builder.append(entry.text)
                    is KtEscapeStringTemplateEntry -> builder.append(entry.unescapedValue)
                    else -> return null
                }
            }
            builder.toString()
        }
        is KtBinaryExpression -> {
            if (expression.operationToken != KtTokens.PLUS) {
                null
            } else {
                val left = stringValue(expression.left)
                val right = stringValue(expression.right)
                if (left == null || right == null) null else left + right
            }
        }
        is KtParenthesizedExpression -> stringValue(expression.expression)
        else -> null
    }

    /** For example "fun greet(name: String)". Used in debug logs, like JavaParser's declaration string. */
    private fun declaration(function: KtNamedFunction): String =
        "fun ${function.name}${function.valueParameterList?.text ?: "()"}"

    /** Parameters as JavaParser nodes, because [StepRegistryEntry] stores them in that form. */
    private fun parameters(function: KtNamedFunction): List<Parameter> =
        function.valueParameters.map { parameter ->
            val typeName = parameter.typeReference?.text ?: "Any"
            Parameter(ClassOrInterfaceType(null, typeName), parameter.name ?: "")
        }

    /**
     * The function's range in JavaParser's convention: 1-based lines and columns, from the first
     * annotation to the last character of the function (inclusive).
     */
    private fun range(contents: String, element: PsiElement): Range {
        val text = contents.replace("\r\n", "\n")
        val start = element.textRange.startOffset
        val end = maxOf(start, element.textRange.endOffset - 1)
        return Range(position(text, start), position(text, end))
    }

    private fun position(text: String, offset: Int): Position {
        val lineStart = text.lastIndexOf('\n', offset - 1) + 1
        val line = text.substring(0, offset).count { it == '\n' } + 1
        return Position(line, offset - lineStart + 1)
    }
}

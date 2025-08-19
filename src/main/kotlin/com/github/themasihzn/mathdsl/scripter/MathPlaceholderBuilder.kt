package com.github.themasihzn.mathdsl.scripter

import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtNamedFunction
import kotlin.script.experimental.api.*
import kotlin.script.experimental.host.*
import kotlin.script.experimental.jvm.*
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost



object MathPlaceholderBuilder {
    fun build(callExpr: KtCallExpression, root: PsiElement): String? {
        return buildMathPlaceholder(callExpr, root)
    }

    private fun buildMathPlaceholder(expr: KtCallExpression, root: PsiElement, includeSymbol: Boolean = true): String? {
        val calleeName = expr.calleeExpression?.text ?: return null

        val function = PsiTreeUtil.findChildrenOfType(root, KtNamedFunction::class.java)
            .find { it.name == calleeName }

        val liveAnnotation = function?.annotationEntries?.find {
            it.shortName?.asString() == "Live"
        }
        val symbolArg = liveAnnotation?.valueArguments?.firstOrNull()?.getArgumentExpression()?.text
        val code = symbolArg?.removeSurrounding("\"") ?: return null

        val args = expr.valueArguments
        val params = function.valueParameters

        return buildString {
            if (includeSymbol) evalKotlinCode(code)
            for ((i, param) in params.withIndex()) {
                val innerArg = args.getOrNull(i)?.getArgumentExpression() ?: continue
                val annots = param.annotationEntries.mapNotNull { it.shortName?.asString() }

                val innerText = when (innerArg) {
                    is KtCallExpression -> {
                        val innerFolded = buildMathPlaceholder(innerArg, root, includeSymbol = true)
                        "(${innerFolded ?: innerArg.text})"
                    }
                    else -> innerArg.text
                }

                when {
                    "Super" in annots -> append(ScriptBuilder.toSuperscript(innerText))
                    "Sub" in annots -> append(ScriptBuilder.toSubscript(innerText))
                    else -> append(evalKotlinCode(innerText)) // fallback
                }
            }
        }
    }
    private fun evalKotlinCode(code: String): String {
        val script = code.trimIndent()
        val host = BasicJvmScriptingHost()
        val result = host.eval(
            script.toScriptSource(),
            ScriptCompilationConfiguration { jvm { } },
            ScriptEvaluationConfiguration {}
        )
        return when (val ret = result.valueOrNull()?.returnValue) {
            is ResultValue.Value -> ret.value.toString()
            else -> "error"
        }
    }

}

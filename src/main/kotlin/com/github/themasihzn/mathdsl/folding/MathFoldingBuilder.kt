package com.github.themasihzn.mathdsl.folding

import com.github.themasihzn.mathdsl.scripter.ScriptBuilder
import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilderEx
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiRecursiveElementWalkingVisitor
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtNamedFunction

class MathFoldingBuilder : FoldingBuilderEx() {
    override fun buildFoldRegions(root: PsiElement, document: Document, quick: Boolean): Array<FoldingDescriptor> {
        val descriptors = mutableListOf<FoldingDescriptor>()

        root.accept(object : PsiRecursiveElementWalkingVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element is KtCallExpression) {
                    val calleeName = element.calleeExpression?.text ?: return

                    // Try to find the function definition manually from current file
                    val function = PsiTreeUtil.findChildrenOfType(root, KtNamedFunction::class.java)
                        .find { it.name == calleeName }

                    val mathAnnotation = function?.annotationEntries?.find {
                        it.shortName?.asString() == "Math"
                    }
                    val symbolArg = mathAnnotation?.valueArguments?.firstOrNull()?.getArgumentExpression()?.text
                    val symbol = symbolArg?.removeSurrounding("\"") ?: return

                    val arguments = element.valueArguments
                    val parameterList = function.valueParameters

                    val scriptSuffix = buildString {
                        for ((index, param) in parameterList.withIndex()) {
                            val argExpr = arguments.getOrNull(index)?.getArgumentExpression()?.text ?: "?"
                            val annotations = param.annotationEntries.mapNotNull { it.shortName?.asString() }

                            when {
                                "Super" in annotations -> append(ScriptBuilder.toSuperscript(argExpr))
                                "Sub" in annotations -> append(ScriptBuilder.toSubscript(argExpr))
//                                else -> append(argExpr)
                                else -> {}
                            }
                        }
                    }

                    val placeholder = "$symbol$scriptSuffix"
                    descriptors.add(FoldingDescriptor(element.node, element.textRange, null, placeholder))
                }

                super.visitElement(element)
            }
        })

        return descriptors.toTypedArray()
    }

    override fun getPlaceholderText(node: ASTNode): String = "..."
    override fun isCollapsedByDefault(node: ASTNode): Boolean = true
}

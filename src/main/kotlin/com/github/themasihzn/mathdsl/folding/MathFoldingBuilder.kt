package com.github.themasihzn.mathdsl.folding

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

                    val annotation = function?.annotationEntries?.find {
                        it.shortName?.asString()?.contains( "Math") == true
                    }

                    val symbolArg = annotation?.valueArguments?.firstOrNull()?.getArgumentExpression()?.text
                    val symbol = symbolArg?.removeSurrounding("\"") ?: return

                    val arg = element.valueArguments.firstOrNull()?.getArgumentExpression()?.text ?: "?"
                    val placeholder = "$symbol$arg"

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

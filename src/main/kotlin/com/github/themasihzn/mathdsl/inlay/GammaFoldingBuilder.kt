package com.github.themasihzn.mathdsl.inlay

import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilderEx
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiRecursiveElementWalkingVisitor
import org.jetbrains.kotlin.psi.KtCallExpression

class GammaFoldingBuilder : FoldingBuilderEx() {
    override fun getPlaceholderText(node: ASTNode): String? {
        val psi = node.psi
        if (psi is KtCallExpression && psi.calleeExpression?.text == "gamma") {
            val arg = psi.valueArguments.firstOrNull()?.text ?: "?"
            return "Γ$arg"
        }
        return null
    }

    override fun buildFoldRegions(root: PsiElement, document: Document, quick: Boolean): Array<FoldingDescriptor> {
        val descriptors = mutableListOf<FoldingDescriptor>()
        root.accept(object : PsiRecursiveElementWalkingVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element is KtCallExpression && element.calleeExpression?.text == "gamma") {
                    descriptors.add(FoldingDescriptor(element.node, element.textRange))
                }
                super.visitElement(element)
            }
        })
        return descriptors.toTypedArray()
    }

    override fun isCollapsedByDefault(node: ASTNode): Boolean = true
}


package com.github.themasihzn.mathdsl.folding

import com.github.themasihzn.mathdsl.scripter.MathPlaceholderBuilder
import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilderEx
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiRecursiveElementWalkingVisitor
import org.jetbrains.kotlin.psi.KtCallExpression

class MathFoldingBuilder : FoldingBuilderEx() {
    override fun buildFoldRegions(root: PsiElement, document: Document, quick: Boolean): Array<FoldingDescriptor> {
        val descriptors = mutableListOf<FoldingDescriptor>()

        root.accept(object : PsiRecursiveElementWalkingVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element is KtCallExpression) {
                    val placeholder = MathPlaceholderBuilder.build(element, root)
                    if (placeholder != null) {
                        descriptors.add(FoldingDescriptor(element.node, element.textRange, null, placeholder))
                    }
                }
                super.visitElement(element)
            }
        })

        return descriptors.toTypedArray()
    }

    override fun getPlaceholderText(node: ASTNode): String = "..."
    override fun isCollapsedByDefault(node: ASTNode): Boolean = true
}

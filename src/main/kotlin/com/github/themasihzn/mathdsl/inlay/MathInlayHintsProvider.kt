package com.github.themasihzn.mathdsl.inlay

import com.intellij.codeInsight.hints.*
import com.intellij.codeInsight.hints.presentation.InlayPresentation
import com.intellij.codeInsight.hints.presentation.PresentationFactory
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtNamedFunction
import javax.swing.JComponent

@Suppress("UnstableApiUsage")
class MathInlayHintsProvider : InlayHintsProvider<NoSettings> {
    override val name: String get() = "MathHints"
    override val key: SettingsKey<NoSettings> = SettingsKey("math.dsl.inlay")
    override val previewText: String get() = "pow(2)"
    override fun createConfigurable(settings: NoSettings) = object : ImmediateConfigurable {
        override fun createComponent(listener: ChangeListener): JComponent {
            // Return a minimal empty panel to satisfy the API
            return javax.swing.JPanel()
        }

        override val mainCheckboxText: String
            get() = "Enable Math DSL Inlay Hints"
    }

    override fun createSettings() = NoSettings()

    override fun getCollectorFor(
        file: PsiFile,
        editor: Editor,
        settings: NoSettings,
        sink: InlayHintsSink
    ): InlayHintsCollector {
        return object : FactoryInlayHintsCollector(editor) {
            override fun collect(element: PsiElement, editor: Editor, sink: InlayHintsSink): Boolean {
                if (element is KtCallExpression) {
                    val styled = buildPresentation(element, factory)
                    if (styled != null) {
                        val padding = " ".repeat(element.textLength)
                        val blank = factory.text(padding)

                        // Render the styled expression
                        sink.addInlineElement(element.textOffset, false, styled, false)

                        // Visually suppress the actual code
                        sink.addInlineElement(0, false, blank, false)
                    }
                }
                return true
            }
        }
    }

    private fun buildPresentation(expr: KtCallExpression, factory: PresentationFactory): InlayPresentation? {
        val calleeName = expr.calleeExpression?.text ?: return null
        val root = expr.containingKtFile
        val function = PsiTreeUtil.findChildrenOfType(root, KtNamedFunction::class.java)
            .find { it.name == calleeName } ?: return null

        val mathAnnotation = function.annotationEntries.find {
            it.shortName?.asString() == "Math"
        } ?: return null

        val symbolArg = mathAnnotation.valueArguments.firstOrNull()?.getArgumentExpression()?.text
        val symbol = symbolArg?.removeSurrounding("\"") ?: return null

        val args = expr.valueArguments
        val params = function.valueParameters

        val parts = mutableListOf<InlayPresentation>()
        parts.add(factory.smallText(symbol))

        for ((i, param) in params.withIndex()) {
            val annots = param.annotationEntries.mapNotNull { it.shortName?.asString() }
            val innerArg = args.getOrNull(i)?.getArgumentExpression()?.text ?: continue

            val styled = when {
                "Super" in annots -> factory.withSuperscript(innerArg)
                "Sub" in annots -> factory.withSubscript(innerArg)
                else -> factory.smallText(innerArg)
            }
            parts.add(styled)
        }

        return factory.seq(*parts.toTypedArray())
    }

    override val isVisibleInSettings: Boolean get() = true

    @Suppress("UnstableApiUsage")
    private fun PresentationFactory.withSuperscript(text: String): InlayPresentation {
        val base = smallText(text) // already a bit smaller
        return inset(base, top = 0, left = 1, down = 4, right = 1)
    }

    @Suppress("UnstableApiUsage")
    private fun PresentationFactory.withSubscript(text: String): InlayPresentation {
        val base = smallText(text)
        return inset(base, top = 4, left = 1, down = 0, right = 1)
    }

}

@file:Suppress("UnstableApiUsage")

package com.github.themasihzn.mathdsl.inlay

import com.intellij.codeInsight.hints.*
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.ui.dsl.builder.bindSelected
import com.intellij.ui.dsl.builder.panel
import org.jetbrains.kotlin.psi.KtCallExpression

data class GammaSettings(var enabled: Boolean = true)

class GammaInlayHintsProvider : InlayHintsProvider<GammaSettings> {
    override val key = SettingsKey<GammaSettings>("GammaHints")
    override val name = "Gamma Function Hint"
    override val previewText = "val x = gamma(2)"

    override fun createSettings(): GammaSettings = GammaSettings()

    override fun getCollectorFor(
        file: PsiFile,
        editor: Editor,
        settings: GammaSettings,
        sink: InlayHintsSink
    ): InlayHintsCollector {
        if (!settings.enabled) return object : InlayHintsCollector {
            override fun collect(element: PsiElement, editor: Editor, sink: InlayHintsSink): Boolean {
                return false
            }
        }
        return object : FactoryInlayHintsCollector(editor) {
            override fun collect(element: PsiElement, editor: Editor, sink: InlayHintsSink): Boolean {
                if (element is KtCallExpression &&
                    element.calleeExpression?.text == "gamma" &&
                    element.valueArguments.size == 1
                ) {
                    val arg = element.valueArguments.first().getArgumentExpression()?.text ?: "?"
                    val presentation = factory.text("Γ$arg")

                    // Key change: replace the function call text with the symbol
                    sink.addInlineElement(
                        element.textRange.startOffset, // place it at the start of the function call
                        false, // don't associate it with surrounding text
                        presentation,
                        false
                    )
                }
                return true
            }
        }

    }

    override fun createConfigurable(settings: GammaSettings): ImmediateConfigurable =
        object : ImmediateConfigurable {
            override fun createComponent(listener: ChangeListener) =
                panel {
                row {
                    checkBox("Show Gamma inlay hints")
                        .bindSelected(settings::enabled)
                        .onChanged { listener.settingsChanged() }
                }
            }
        }
}

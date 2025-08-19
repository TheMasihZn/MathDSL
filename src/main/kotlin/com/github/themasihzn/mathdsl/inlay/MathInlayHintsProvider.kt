package com.github.themasihzn.mathdsl.inlay

import com.github.themasihzn.mathdsl.scripter.MathPlaceholderBuilder
import com.intellij.codeInsight.hints.*
import com.intellij.codeInsight.hints.presentation.ScaleAwarePresentationFactory
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import org.jetbrains.kotlin.psi.KtCallExpression
import javax.swing.JComponent

@Suppress("UnstableApiUsage")
class MathInlayHintsProvider : InlayHintsProvider<NoSettings> {
    override val name: String get() = "LiveRun"
    override val key: SettingsKey<NoSettings> = SettingsKey("math.dsl.inlay")
    override val previewText: String get() = ":result"
    override fun createConfigurable(settings: NoSettings): ImmediateConfigurable = object : ImmediateConfigurable {
        override fun createComponent(listener: ChangeListener): JComponent = javax.swing.JPanel()

        override val mainCheckboxText: String
            get() = "Enable Math DSL Inlay Hints"
    }

    override fun createSettings(): NoSettings = NoSettings()

    override fun getCollectorFor(
        file: PsiFile,
        editor: Editor,
        settings: NoSettings,
        sink: InlayHintsSink
    ): InlayHintsCollector {
        return object : FactoryInlayHintsCollector(editor) {
            override fun collect(element: PsiElement, editor: Editor, sink: InlayHintsSink): Boolean {
                if (element is KtCallExpression) {
                    val placeholder = MathPlaceholderBuilder.build(element, element.containingKtFile)
                    if (placeholder != null) {
                        val factory = ScaleAwarePresentationFactory(editor, this.factory)
                        val output = factory.text(placeholder)


                        sink.addInlineElement(
                            element.textOffset,
                            false,
                            output,
                            true
                        )
                    }
                }
                return true
            }
        }
    }

    override val isVisibleInSettings: Boolean get() = true
}

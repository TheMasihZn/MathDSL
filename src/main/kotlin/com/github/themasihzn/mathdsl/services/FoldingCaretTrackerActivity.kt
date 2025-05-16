package com.github.themasihzn.mathdsl.services

import com.intellij.openapi.application.readAction
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.event.CaretEvent
import com.intellij.openapi.editor.event.CaretListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity

class FoldingCaretTrackerActivity : ProjectActivity {

    private val logger = Logger.getInstance(FoldingCaretTrackerActivity::class.java)

    override suspend fun execute(project: Project) {
        readAction {
            logger.info("🚨 FoldingCaretTrackerService started for project: ${project.name}")

            EditorFactory.getInstance().allEditors.forEach { editor ->
                logger.info("Editor created, adding caret listener.")

                editor.caretModel.addCaretListener(object : CaretListener {
                    override fun caretPositionChanged(event: CaretEvent) {
                        val caretOffset = event.editor.caretModel.offset
                        val foldingModel = event.editor.foldingModel

                        foldingModel.runBatchFoldingOperation {
                            for (region in foldingModel.allFoldRegions) {
                                val start = region.startOffset
                                val end = region.endOffset
                                val isNear = caretOffset in (start)..(end)

                                if (region.placeholderText.firstOrNull()?.isLetter() == true) {
                                    region.isExpanded = isNear
                                    logger.info("Updated region [${start}-${end}]: expanded=$isNear")
                                }
                            }
                        }
                    }
                })
            }
        }
    }
}

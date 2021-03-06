@file:Suppress("ClassName")

package com.idanatz.oneadapter.tests.module_configs.selection

import android.graphics.Color
import android.util.SparseIntArray
import android.view.View
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.idanatz.oneadapter.external.modules.ItemModule
import com.idanatz.oneadapter.external.modules.ItemSelectionModule
import com.idanatz.oneadapter.external.modules.ItemSelectionModuleConfig
import com.idanatz.oneadapter.external.states.SelectionState
import com.idanatz.oneadapter.helpers.getViewLocationOnScreen
import com.idanatz.oneadapter.helpers.BaseTest
import com.idanatz.oneadapter.internal.utils.extensions.let2
import com.idanatz.oneadapter.models.TestModel
import com.idanatz.oneadapter.test.R
import org.amshove.kluent.shouldEqualTo
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WhenSelectionTypeMultiple_ThenSelectingMultipleItems_ShouldBeSupported : BaseTest() {

	private val modelSelectedEvents = SparseIntArray(2)
	private var isNotSelectedCount = 0

	@Test
	fun test() {
		configure {
			val models = modelGenerator.generateModels(3)
			modelSelectedEvents.put(models[0].id, 0)
			modelSelectedEvents.put(models[1].id, 0)

			prepareOnActivity {
				oneAdapter.apply {
					attachItemModule(TestItemModule())
					attachItemSelectionModule(TestItemSelectionModule())
					oneAdapter.internalAdapter.data = models.toMutableList()
				}
			}
			actOnActivity {
				runWithDelay {
					val firstHolderRootView = recyclerView.findViewHolderForAdapterPosition(0)?.itemView
					val secondsHolderRootView = recyclerView.findViewHolderForAdapterPosition(1)?.itemView

					let2(firstHolderRootView, secondsHolderRootView) { rootView1: View, rootView2: View ->
						rootView1.post {
							val (x1, y1) = rootView1.getViewLocationOnScreen()
							touchSimulator.simulateLongTouch(recyclerView, x1, y1)

							runWithDelay(100) {
								val (x2, y2) = rootView2.getViewLocationOnScreen()
								touchSimulator.simulateTouch(recyclerView, x2, y2)
							}
						}
					}
				}
			}
			untilAsserted {
				modelSelectedEvents.get(models[0].id) shouldEqualTo 1
				modelSelectedEvents.get(models[1].id) shouldEqualTo 1
				isNotSelectedCount shouldEqualTo 0
			}
		}
	}

	inner class TestItemModule : ItemModule<TestModel>() {
		init {
			config = modulesGenerator.generateValidItemModuleConfig(R.layout.test_model_large)
			onBind { _, viewBinder, metadata ->
				if (metadata.isSelected) {
					viewBinder.rootView.setBackgroundColor(Color.parseColor("#C7226E"))
				} else {
					viewBinder.rootView.setBackgroundColor(Color.parseColor("#ffffff"))
				}
			}
			states += SelectionState<TestModel>().apply {
				onSelected { model, selected ->
					if (selected) {
						modelSelectedEvents.put(model.id, modelSelectedEvents.get(model.id).inc())
					} else {
						isNotSelectedCount++
					}
				}
			}
		}
	}

	private class TestItemSelectionModule : ItemSelectionModule() {
		init {
			config {
				selectionType = ItemSelectionModuleConfig.SelectionType.Multiple
			}
		}
	}
}
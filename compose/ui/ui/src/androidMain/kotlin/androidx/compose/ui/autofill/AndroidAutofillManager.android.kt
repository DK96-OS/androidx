/*
 * Copyright 2024 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.compose.ui.autofill

import android.graphics.Rect
import android.os.Build
import android.util.Log
import android.util.SparseArray
import android.view.View
import android.view.ViewStructure
import android.view.autofill.AutofillId
import android.view.autofill.AutofillValue
import androidx.annotation.RequiresApi
import androidx.collection.MutableIntSet
import androidx.collection.mutableObjectListOf
import androidx.compose.ui.internal.checkPreconditionNotNull
import androidx.compose.ui.platform.coreshims.ViewCompatShims
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsConfiguration
import androidx.compose.ui.semantics.SemanticsInfo
import androidx.compose.ui.semantics.SemanticsListener
import androidx.compose.ui.semantics.SemanticsOwner
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.spatial.RectManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.util.fastForEach

private const val logTag = "ComposeAutofillManager"

/**
 * Semantic autofill implementation for Android.
 *
 * @param view The parent compose view.
 */
@RequiresApi(Build.VERSION_CODES.O)
internal class AndroidAutofillManager(
    var platformAutofillManager: PlatformAutofillManager,
    private val semanticsOwner: SemanticsOwner,
    private val view: View,
    private val rectManager: RectManager,
    private val packageName: String,
) : AutofillManager(), SemanticsListener {
    private var reusableRect = Rect()
    private var rootAutofillId: AutofillId

    init {
        view.importantForAutofill = View.IMPORTANT_FOR_AUTOFILL_YES
        rootAutofillId =
            checkPreconditionNotNull(ViewCompatShims.getAutofillId(view)?.toAutofillId())
    }

    override fun commit() {
        platformAutofillManager.commit()
    }

    override fun cancel() {
        platformAutofillManager.cancel()
    }

    // This will be used to request autofill when
    // `AutofillManager.requestAutofillForActiveElement()` is called (e.g. from the text toolbar).
    private var previouslyFocusedId = -1

    override fun requestAutofillForActiveElement() {
        if (previouslyFocusedId <= 0) return

        rectManager.rects.withRect(previouslyFocusedId) { left, top, right, bottom ->
            reusableRect.set(left, top, right, bottom)
            platformAutofillManager.requestAutofill(view, previouslyFocusedId, reusableRect)
        }
    }

    /** Send events to the autofill service in response to semantics changes. */
    override fun onSemanticsChanged(
        semanticsInfo: SemanticsInfo,
        previousSemanticsConfiguration: SemanticsConfiguration?
    ) {
        val config = semanticsInfo.semanticsConfiguration
        val prevConfig = previousSemanticsConfiguration
        val semanticsId = semanticsInfo.semanticsId

        // Check Editable Text.
        val previousText = prevConfig?.getOrNull(SemanticsProperties.EditableText)?.text
        val newText = config?.getOrNull(SemanticsProperties.EditableText)?.text
        if (!previousText.isNullOrEmpty() && previousText != newText && !newText.isNullOrEmpty()) {
            val contentDataType = config.getOrNull(SemanticsProperties.ContentDataType)
            if (contentDataType == ContentDataType.Text) {
                platformAutofillManager.notifyValueChanged(
                    view,
                    semanticsId,
                    AutofillApi26Helper.getAutofillTextValue(newText.toString())
                )
            }
        }

        // Check Focus.
        // TODO: Instead of saving the focused item here, add some internal API to focusManager
        //  so that this could be more efficient.
        val previousFocus = prevConfig?.getOrNull(SemanticsProperties.Focused)
        val currFocus = config?.getOrNull(SemanticsProperties.Focused)
        if (previousFocus != true && currFocus == true && config.isAutofillable()) {
            previouslyFocusedId = semanticsId
            rectManager.rects.withRect(semanticsId) { left, top, right, bottom ->
                platformAutofillManager.notifyViewEntered(
                    view,
                    semanticsId,
                    Rect(left, top, right, bottom)
                )
            }
        }
        if (previousFocus == true && currFocus != true && prevConfig.isAutofillable()) {
            platformAutofillManager.notifyViewExited(view, semanticsId)
        }

        // Update currentlyDisplayedIDs if relevance to Autocommit has changed.
        val prevRelatedToAutoCommit = prevConfig?.isRelatedToAutoCommit() == true
        val currRelatedToAutoCommit = config?.isRelatedToAutoCommit() == true
        if (prevRelatedToAutoCommit != currRelatedToAutoCommit) {
            if (currRelatedToAutoCommit) {
                currentlyDisplayedIDs.add(semanticsId)
            } else {
                currentlyDisplayedIDs.remove(semanticsId)
            }
            pendingChangesToDisplayedIds = true
        }
    }

    /** Populate the structure of the entire view hierarchy when the framework requests it. */
    fun populateViewStructure(rootViewStructure: ViewStructure) {
        val autofillApi = AutofillApi26Helper
        val rootSemanticInfo = semanticsOwner.rootInfo

        // Populate view structure for the root.
        rootViewStructure.populate(rootSemanticInfo, rootAutofillId, packageName, rectManager)

        // We save the semanticInfo and viewStructure of the item in a list. These are always stored
        // as pairs, and we need to cast them back to the required types when we extract them.
        val populateChildren = mutableObjectListOf<Any>(rootSemanticInfo, rootViewStructure)

        @Suppress("Range") // isNotEmpty ensures removeAt is not called with -1.
        while (populateChildren.isNotEmpty()) {

            val parentStructure =
                populateChildren.removeAt(populateChildren.lastIndex) as ViewStructure
            val parentInfo = populateChildren.removeAt(populateChildren.lastIndex) as SemanticsInfo

            parentInfo.childrenInfo.fastForEach { childInfo ->
                if (childInfo.isDeactivated || !childInfo.isAttached || !childInfo.isPlaced) {
                    return@fastForEach
                }

                // TODO(b/378160001): For now we only populate nodes that are relevant for autofill.
                //  Populate the structure for all nodes in the future.
                val semanticsConfigurationChild = childInfo.semanticsConfiguration
                if (semanticsConfigurationChild?.isRelatedToAutofill() != true) {
                    populateChildren.add(childInfo)
                    populateChildren.add(parentStructure)
                    return@fastForEach
                }

                val childIndex = autofillApi.addChildCount(parentStructure, 1)
                val childStructure = autofillApi.newChild(parentStructure, childIndex)
                childStructure.populate(childInfo, rootAutofillId, packageName, rectManager)
                populateChildren.add(childInfo)
                populateChildren.add(childStructure)
            }
        }
    }

    /** When the autofill service provides data, perform autofill using semantic actions. */
    fun performAutofill(values: SparseArray<AutofillValue>) {
        for (index in 0 until values.size()) {
            val itemId = values.keyAt(index)
            val value = values[itemId]
            when {
                AutofillApi26Helper.isText(value) ->
                    semanticsOwner[itemId]
                        ?.semanticsConfiguration
                        ?.getOrNull(SemanticsActions.OnAutofillText)
                        ?.action
                        ?.invoke(AnnotatedString(AutofillApi26Helper.textValue(value).toString()))

                // TODO(b/138604541): Add Autofill support for date fields.
                AutofillApi26Helper.isDate(value) ->
                    Log.w(logTag, "Auto filling Date fields is not yet supported.")

                // TODO(b/138604541): Add Autofill support for dropdown lists.
                AutofillApi26Helper.isList(value) ->
                    Log.w(logTag, "Auto filling dropdown lists is not yet supported.")

                // TODO(b/138604541): Add Autofill support for toggle fields.
                AutofillApi26Helper.isToggle(value) ->
                    Log.w(logTag, "Auto filling toggle fields are not yet supported.")
            }
        }
    }

    // Consider moving the currently displayed IDs to a separate VisibilityManager class. This might
    // be needed by ContentCapture and Accessibility.
    private var currentlyDisplayedIDs = MutableIntSet()
    private var pendingChangesToDisplayedIds = false

    internal fun onPostAttach(semanticsInfo: SemanticsInfo) {
        if (semanticsInfo.semanticsConfiguration?.isRelatedToAutoCommit() == true) {
            currentlyDisplayedIDs.add(semanticsInfo.semanticsId)
            pendingChangesToDisplayedIds = true
            // TODO(MNUZEN): Notify autofill manager that a node has been added.
            // platformAutofillManager
            //     .notifyViewVisibilityChanged(view, semanticsInfo.semanticsId, true)
        }
    }

    internal fun onPostLayoutNodeReused(semanticsInfo: SemanticsInfo, previousSemanticsId: Int) {
        if (currentlyDisplayedIDs.remove(previousSemanticsId)) {
            pendingChangesToDisplayedIds = true
        }
        if (semanticsInfo.semanticsConfiguration?.isRelatedToAutoCommit() == true) {
            currentlyDisplayedIDs.add(semanticsInfo.semanticsId)
            pendingChangesToDisplayedIds = true
        }
    }

    internal fun onLayoutNodeDeactivated(semanticsInfo: SemanticsInfo) {
        if (currentlyDisplayedIDs.remove(semanticsInfo.semanticsId)) {
            pendingChangesToDisplayedIds = true
            // TODO(MNUZEN): Notify autofill manager that a node has been removed.
            // platformAutofillManager
            //     .notifyViewVisibilityChanged(view, semanticsInfo.semanticsId, false)
        }
    }

    internal fun onDetach(semanticsInfo: SemanticsInfo) {
        if (currentlyDisplayedIDs.remove(semanticsInfo.semanticsId)) {
            pendingChangesToDisplayedIds = true
            // TODO(MNUZEN): Notify autofill manager that a node has been removed.
            // platformAutofillManager
            //     .notifyViewVisibilityChanged(view, semanticsInfo.semanticsId, false)
        }
    }

    internal fun onEndApplyChanges() {
        if (pendingChangesToDisplayedIds) {
            executeAutoCommit()
            pendingChangesToDisplayedIds = false
        }
    }

    // We maintain a copy of the previously displayed IDs, and call AutofillManager.commit() when
    // all the previously displayed IDs were removed.
    private var previouslyDisplayedIDs = MutableIntSet()

    private fun executeAutoCommit() {
        // Check for screen changes or complete removal.
        if (!currentlyDisplayedIDs.containsAll(previouslyDisplayedIDs)) {
            platformAutofillManager.commit()
        }
        previouslyDisplayedIDs.copyFrom(currentlyDisplayedIDs)
    }
}

private fun SemanticsConfiguration.isAutofillable(): Boolean {
    // TODO add more actions once we add support for Toggle, List, Date etc.
    return props.contains(SemanticsActions.OnAutofillText)
}

private fun SemanticsConfiguration.isRelatedToAutoCommit(): Boolean {
    return props.contains(SemanticsProperties.ContentType)
}

private fun SemanticsConfiguration.isRelatedToAutofill(): Boolean {
    return props.contains(SemanticsActions.OnAutofillText) ||
        props.contains(SemanticsProperties.ContentType) ||
        props.contains(SemanticsProperties.ContentDataType)
}

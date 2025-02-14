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

package androidx.xr.compose.subspace

import androidx.annotation.RestrictTo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCompositionContext
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.findViewTreeViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.findViewTreeSavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import java.util.UUID

/**
 * Create a [ComposeView] that is applicable to the local context.
 *
 * This handles propagating the composition context and ensures that savable state is remembered.
 */
@Composable
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
public fun rememberComposeView(content: @Composable () -> Unit): ComposeView {
    val localId = rememberSaveable { UUID.randomUUID() }
    val context = LocalContext.current
    val parentView = LocalView.current
    val compositionContext = rememberCompositionContext()

    return remember {
            ComposeView(context).apply {
                id = android.R.id.content
                setViewTreeLifecycleOwner(parentView.findViewTreeLifecycleOwner())
                setViewTreeViewModelStoreOwner(parentView.findViewTreeViewModelStoreOwner())
                setViewTreeSavedStateRegistryOwner(parentView.findViewTreeSavedStateRegistryOwner())
                // Dispose of the Composition when the view's LifecycleOwner is destroyed
                setParentCompositionContext(compositionContext)
                // Set unique id for AbstractComposeView. This allows state restoration for the
                // state
                // defined inside the ElevatedSurface via rememberSaveable()
                setTag(
                    androidx.compose.ui.R.id.compose_view_saveable_id_tag,
                    "ComposeView:$localId"
                )

                // Enable children to draw their shadow by not clipping them
                clipChildren = false
            }
        }
        .apply {
            setContent(content)
            DisposableEffect(this) { onDispose { disposeComposition() } }
        }
}

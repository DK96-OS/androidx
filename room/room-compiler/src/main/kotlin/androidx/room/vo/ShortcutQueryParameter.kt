/*
 * Copyright (C) 2016 The Android Open Source Project
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

package androidx.room.vo

import androidx.room.compiler.processing.XNullability
import androidx.room.compiler.processing.XType
import androidx.room.compiler.processing.XVariableElement

/** Parameters used in DAO functions that are annotated with Insert, Delete, Update, and Upsert. */
data class ShortcutQueryParameter(
    val element: XVariableElement,
    val name: String,
    val type: XType, // actual param type (List<Foo>, Set<Foo>, Foo, etc...)
    val pojoType: XType?, // extracted type, never a Collection
    val isMultiple: Boolean
) {
    /** Function name in entity insertion or update adapter. */
    val handleFunctionName =
        if (isMultiple) {
            "handleMultiple"
        } else {
            "handle"
        }

    val isNonNull = type.nullability == XNullability.NONNULL
}

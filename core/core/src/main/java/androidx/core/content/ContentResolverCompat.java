/*
 * Copyright (C) 2013 The Android Open Source Project
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

package androidx.core.content;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.CancellationSignal;

import androidx.core.os.OperationCanceledException;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Helper for accessing features in {@link ContentResolver} in a backwards
 * compatible fashion.
 */
public final class ContentResolverCompat {
    private ContentResolverCompat() {
        /* Hide constructor */
    }

    /**
     * Query the given URI, returning a {@link Cursor} over the result set
     * with optional support for cancellation.
     * <p>
     * For best performance, the caller should follow these guidelines:
     * <ul>
     * <li>Provide an explicit projection, to prevent
     * reading data from storage that aren't going to be used.</li>
     * <li>Use question mark parameter markers such as 'phone=?' instead of
     * explicit values in the {@code selection} parameter, so that queries
     * that differ only by those values will be recognized as the same
     * for caching purposes.</li>
     * </ul>
     * </p>
     *
     * @param resolver resolver to use for the query.
     * @param uri The URI, using the content:// scheme, for the content to
     *         retrieve.
     * @param projection A list of which columns to return. Passing null will
     *         return all columns, which is inefficient.
     * @param selection A filter declaring which rows to return, formatted as an
     *         SQL WHERE clause (excluding the WHERE itself). Passing null will
     *         return all rows for the given URI.
     * @param selectionArgs You may include ?s in selection, which will be
     *         replaced by the values from selectionArgs, in the order that they
     *         appear in the selection. The values will be bound as Strings.
     * @param sortOrder How to order the rows, formatted as an SQL ORDER BY
     *         clause (excluding the ORDER BY itself). Passing null will use the
     *         default sort order, which may be unordered.
     * @param cancellationSignal A signal to cancel the operation in progress, or null if none.
     * If the operation is canceled, then {@link OperationCanceledException} will be thrown
     * when the query is executed.
     * @return A Cursor object, which is positioned before the first entry, or null
     * @see Cursor
     * @deprecated Use
     * {@link #query(ContentResolver, Uri, String[], String, String[], String, CancellationSignal)}
     */
    @Deprecated
    public static @Nullable Cursor query(@NonNull ContentResolver resolver,
            @NonNull Uri uri, String @Nullable [] projection, @Nullable String selection,
            String @Nullable [] selectionArgs, @Nullable String sortOrder,
            androidx.core.os.@Nullable CancellationSignal cancellationSignal) {
        return query(resolver, uri, projection, selection, selectionArgs, sortOrder,
                cancellationSignal != null
                        ? (CancellationSignal) cancellationSignal.getCancellationSignalObject() :
                        null);
    }

    /**
     * Query the given URI, returning a {@link Cursor} over the result set
     * with optional support for cancellation.
     * <p>
     * For best performance, the caller should follow these guidelines:
     * <ul>
     * <li>Provide an explicit projection, to prevent
     * reading data from storage that aren't going to be used.</li>
     * <li>Use question mark parameter markers such as 'phone=?' instead of
     * explicit values in the {@code selection} parameter, so that queries
     * that differ only by those values will be recognized as the same
     * for caching purposes.</li>
     * </ul>
     * </p>
     *
     * @param resolver resolver to use for the query.
     * @param uri The URI, using the content:// scheme, for the content to
     *         retrieve.
     * @param projection A list of which columns to return. Passing null will
     *         return all columns, which is inefficient.
     * @param selection A filter declaring which rows to return, formatted as an
     *         SQL WHERE clause (excluding the WHERE itself). Passing null will
     *         return all rows for the given URI.
     * @param selectionArgs You may include ?s in selection, which will be
     *         replaced by the values from selectionArgs, in the order that they
     *         appear in the selection. The values will be bound as Strings.
     * @param sortOrder How to order the rows, formatted as an SQL ORDER BY
     *         clause (excluding the ORDER BY itself). Passing null will use the
     *         default sort order, which may be unordered.
     * @param cancellationSignal A signal to cancel the operation in progress, or null if none.
     * If the operation is canceled, then {@link OperationCanceledException} will be thrown
     * when the query is executed.
     * @return A Cursor object, which is positioned before the first entry, or null
     * @see Cursor
     */
    public static @Nullable Cursor query(@NonNull ContentResolver resolver,
            @NonNull Uri uri, String @Nullable [] projection, @Nullable String selection,
            String @Nullable [] selectionArgs, @Nullable String sortOrder,
            @Nullable CancellationSignal cancellationSignal) {
        try {
            return resolver.query(uri, projection, selection, selectionArgs, sortOrder,
                    cancellationSignal);
        } catch (Exception e) {
            if (e instanceof android.os.OperationCanceledException) {
                // query() can throw a framework OperationCanceledException if it has been
                // canceled. We catch that and throw the support version instead.
                throw new OperationCanceledException();
            } else {
                // If it's not a framework OperationCanceledException, re-throw the exception
                throw e;
            }
        }
    }
}

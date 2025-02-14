/*
 * Copyright (C) 2015 The Android Open Source Project
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

package androidx.preference;

import static androidx.annotation.RestrictTo.Scope.LIBRARY;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.RestrictTo;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * @deprecated Use {@link EditTextPreferenceDialogFragmentCompat} instead
 */
@SuppressWarnings("deprecation")
@Deprecated
public class EditTextPreferenceDialogFragment extends PreferenceDialogFragment {

    private static final String SAVE_STATE_TEXT = "EditTextPreferenceDialogFragment.text";

    private EditText mEditText;

    private CharSequence mText;

    /**
     * @deprecated Use {@link EditTextPreferenceDialogFragmentCompat} instead
     */
    @Deprecated
    public EditTextPreferenceDialogFragment() {}

    /**
     * @deprecated Use {@link EditTextPreferenceDialogFragmentCompat} instead
     */
    @Deprecated
    public static @NonNull EditTextPreferenceDialogFragment newInstance(String key) {
        final EditTextPreferenceDialogFragment
                fragment = new EditTextPreferenceDialogFragment();
        final Bundle b = new Bundle(1);
        b.putString(ARG_KEY, key);
        fragment.setArguments(b);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            mText = getEditTextPreference().getText();
        } else {
            mText = savedInstanceState.getCharSequence(SAVE_STATE_TEXT);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putCharSequence(SAVE_STATE_TEXT, mText);
    }

    @Override
    protected void onBindDialogView(@NonNull View view) {
        super.onBindDialogView(view);

        mEditText = view.findViewById(android.R.id.edit);
        mEditText.requestFocus();

        if (mEditText == null) {
            throw new IllegalStateException("Dialog view must contain an EditText with id"
                    + " @android:id/edit");
        }

        mEditText.setText(mText);
        // Place cursor at the end
        mEditText.setSelection(mEditText.getText().length());
    }

    private EditTextPreference getEditTextPreference() {
        return (EditTextPreference) getPreference();
    }

    @RestrictTo(LIBRARY)
    @Override
    protected boolean needInputMethod() {
        // We want the input method to show, if possible, when dialog is displayed
        return true;
    }

    /**
     * @deprecated Use {@link EditTextPreferenceDialogFragmentCompat} instead
     */
    @Deprecated
    @Override
    public void onDialogClosed(boolean positiveResult) {

        if (positiveResult) {
            String value = mEditText.getText().toString();
            if (getEditTextPreference().callChangeListener(value)) {
                getEditTextPreference().setText(value);
            }
        }
    }

}

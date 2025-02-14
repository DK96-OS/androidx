/*
 * Copyright (C) 2017 The Android Open Source Project
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

package androidx.appcompat.app.inflater;

import android.content.Context;
import android.util.AttributeSet;

import androidx.appcompat.app.AppCompatViewInflater;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.AppCompatToggleButton;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Custom view inflater that takes over the inflation of a few widget types.
 */
public class CustomViewInflater extends AppCompatViewInflater {
    public static class CustomTextView extends AppCompatTextView {
        public CustomTextView(Context context) {
            super(context);
        }

        public CustomTextView(Context context,
                @Nullable AttributeSet attrs) {
            super(context, attrs);
        }

        public CustomTextView(Context context,
                @Nullable AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
        }
    }

    public static class CustomButton extends AppCompatButton {
        public CustomButton(Context context) {
            super(context);
        }

        public CustomButton(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        public CustomButton(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
        }
    }

    public static class CustomImageButton extends AppCompatImageButton {
        public CustomImageButton(Context context) {
            super(context);
        }

        public CustomImageButton(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        public CustomImageButton(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
        }
    }

    public static class CustomToggleButton extends AppCompatToggleButton {
        public CustomToggleButton(Context context) {
            super(context);
        }

        public CustomToggleButton(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        public CustomToggleButton(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
        }
    }

    @Override
    protected @NonNull AppCompatButton createButton(Context context, AttributeSet attrs) {
        return new CustomButton(context, attrs);
    }

    @Override
    protected @NonNull AppCompatTextView createTextView(Context context, AttributeSet attrs) {
        return new CustomTextView(context, attrs);
    }

    @Override
    protected @NonNull AppCompatImageButton createImageButton(Context context, AttributeSet attrs) {
        return new CustomImageButton(context, attrs);
    }

    @Override
    protected @NonNull AppCompatToggleButton createToggleButton(
            Context context, AttributeSet attrs) {
        return new CustomToggleButton(context, attrs);
    }
}

/*
 * Copyright (C) 2011 The Android Open Source Project
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

package com.example.android.supportv4.app;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;

import com.example.android.supportv4.R;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Demonstrates a fragment that can be configured through both Bundle arguments
 * and layout attributes.
 */
public class FragmentArgumentsSupport extends FragmentActivity {
//BEGIN_INCLUDE(create)
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_arguments_support);

        if (savedInstanceState == null) {
            // First-time init; create fragment to embed in activity.
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            Fragment newFragment = MyFragment.newInstance("From Arguments");
            ft.add(R.id.created, newFragment);
            ft.commit();
        }
    }
//END_INCLUDE(create)

//BEGIN_INCLUDE(fragment)
    public static class MyFragment extends Fragment {
        CharSequence mLabel;

        /**
         * Create a new instance of MyFragment that will be initialized
         * with the given arguments.
         */
        static MyFragment newInstance(CharSequence label) {
            MyFragment f = new MyFragment();
            Bundle b = new Bundle();
            b.putCharSequence("label", label);
            f.setArguments(b);
            return f;
        }

        /**
         * Parse attributes during inflation from a view hierarchy into the
         * arguments we handle.
         */
        @Override
        public void onInflate(@NonNull Context context, @NonNull AttributeSet attrs,
                @Nullable Bundle savedInstanceState) {
            super.onInflate(context, attrs, savedInstanceState);

            TypedArray a = context.obtainStyledAttributes(attrs,
                    R.styleable.FragmentArguments);
            mLabel = a.getText(R.styleable.FragmentArguments_android_label);
            a.recycle();
        }

        /**
         * During creation, if arguments have been supplied to the fragment
         * then parse those out.
         */
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            Bundle args = getArguments();
            if (args != null) {
                CharSequence label = args.getCharSequence("label");
                if (label != null) {
                    mLabel = label;
                }
            }
        }

        /**
         * Create the view for this fragment, using the arguments given to it.
         */
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.hello_world, container, false);
            View tv = v.findViewById(R.id.text);
            ((TextView)tv).setText(mLabel != null ? mLabel : "(no label)");
            ViewCompat.setBackground(
                    tv, ContextCompat.getDrawable(getContext(), android.R.drawable.gallery_thumb));
            return v;
        }
    }
//END_INCLUDE(fragment)
}

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

package androidx.transition;

import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.LayoutRes;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * A scene represents the collection of values that various properties in the
 * View hierarchy will have when the scene is applied. A Scene can be
 * configured to automatically run a Transition when it is applied, which will
 * animate the various property changes that take place during the
 * scene change.
 */
public class Scene {

    private Context mContext;
    private int mLayoutId = -1;
    private ViewGroup mSceneRoot;
    private View mLayout; // alternative to layoutId
    private Runnable mEnterAction, mExitAction;

    /**
     * Returns a Scene described by the resource file associated with the given
     * <code>layoutId</code> parameter. If such a Scene has already been created for
     * the given <code>sceneRoot</code>, that same Scene will be returned.
     * This caching of layoutId-based scenes enables sharing of common scenes
     * between those created in code and those referenced by {@link TransitionManager}
     * XML resource files.
     *
     * @param sceneRoot The root of the hierarchy in which scene changes
     *                  and transitions will take place.
     * @param layoutId  The id of a standard layout resource file.
     * @param context   The context used in the process of inflating
     *                  the layout resource.
     * @return The scene for the given root and layout id
     */
    public static @NonNull Scene getSceneForLayout(@NonNull ViewGroup sceneRoot,
            @LayoutRes int layoutId, @NonNull Context context) {
        @SuppressWarnings("unchecked")
        SparseArray<Scene> scenes =
                (SparseArray<Scene>) sceneRoot.getTag(R.id.transition_scene_layoutid_cache);
        if (scenes == null) {
            scenes = new SparseArray<>();
            sceneRoot.setTag(R.id.transition_scene_layoutid_cache, scenes);
        }
        Scene scene = scenes.get(layoutId);
        if (scene != null) {
            return scene;
        } else {
            scene = new Scene(sceneRoot, layoutId, context);
            scenes.put(layoutId, scene);
            return scene;
        }
    }

    /**
     * Constructs a Scene with no information about how values will change
     * when this scene is applied. This constructor might be used when
     * a Scene is created with the intention of being dynamically configured,
     * through setting {@link #setEnterAction(Runnable)} and possibly
     * {@link #setExitAction(Runnable)}.
     *
     * @param sceneRoot The root of the hierarchy in which scene changes
     *                  and transitions will take place.
     */
    public Scene(@NonNull ViewGroup sceneRoot) {
        mSceneRoot = sceneRoot;
    }

    /**
     * Constructs a Scene which, when entered, will remove any
     * children from the sceneRoot container and will inflate and add
     * the hierarchy specified by the layoutId resource file.
     *
     * <p>This method is hidden because layoutId-based scenes should be
     * created by the caching factory method {@link Scene#getCurrentScene(View)}.</p>
     *
     * @param sceneRoot The root of the hierarchy in which scene changes
     *                  and transitions will take place.
     * @param layoutId  The id of a resource file that defines the view
     *                  hierarchy of this scene.
     * @param context   The context used in the process of inflating
     *                  the layout resource.
     */
    private Scene(ViewGroup sceneRoot, int layoutId, Context context) {
        mContext = context;
        mSceneRoot = sceneRoot;
        mLayoutId = layoutId;
    }

    /**
     * Constructs a Scene which, when entered, will remove any
     * children from the sceneRoot container and add the layout
     * object as a new child of that container.
     *
     * @param sceneRoot The root of the hierarchy in which scene changes
     *                  and transitions will take place.
     * @param layout    The view hierarchy of this scene, added as a child
     *                  of sceneRoot when this scene is entered.
     */
    public Scene(@NonNull ViewGroup sceneRoot, @NonNull View layout) {
        mSceneRoot = sceneRoot;
        mLayout = layout;
    }

    /**
     * Gets the root of the scene, which is the root of the view hierarchy
     * affected by changes due to this scene, and which will be animated
     * when this scene is entered.
     *
     * @return The root of the view hierarchy affected by this scene.
     */
    public @NonNull ViewGroup getSceneRoot() {
        return mSceneRoot;
    }

    /**
     * Exits this scene, if it is the current scene
     * on the scene's {@link #getSceneRoot() scene root}. The current scene is
     * set when {@link #enter() entering} a scene.
     * Exiting a scene runs the {@link #setExitAction(Runnable) exit action}
     * if there is one.
     */
    public void exit() {
        if (getCurrentScene(mSceneRoot) == this) {
            if (mExitAction != null) {
                mExitAction.run();
            }
        }
    }

    /**
     * Enters this scene, which entails changing all values that
     * are specified by this scene. These may be values associated
     * with a layout view group or layout resource file which will
     * now be added to the scene root, or it may be values changed by
     * an {@link #setEnterAction(Runnable)} enter action}, or a
     * combination of the these. No transition will be run when the
     * scene is entered. To get transition behavior in scene changes,
     * use one of the methods in {@link androidx.transition.TransitionManager} instead.
     */
    public void enter() {
        // Apply layout change, if any
        if (mLayoutId > 0 || mLayout != null) {
            // empty out parent container before adding to it
            getSceneRoot().removeAllViews();

            if (mLayoutId > 0) {
                LayoutInflater.from(mContext).inflate(mLayoutId, mSceneRoot);
            } else {
                mSceneRoot.addView(mLayout);
            }
        }

        // Notify next scene that it is entering. Subclasses may override to configure scene.
        if (mEnterAction != null) {
            mEnterAction.run();
        }

        setCurrentScene(mSceneRoot, this);
    }

    /**
     * Set the scene that the given ViewGroup is in. The current scene is set only
     * on the root ViewGroup of a scene, not for every view in that hierarchy. This
     * information is used by Scene to determine whether there is a previous
     * scene which should be exited before the new scene is entered.
     *
     * @param sceneRoot The ViewGroup on which the current scene is being set
     */
    static void setCurrentScene(@NonNull ViewGroup sceneRoot, @Nullable Scene scene) {
        sceneRoot.setTag(R.id.transition_current_scene, scene);
    }

    /**
     * Gets the current {@link Scene} set on the given ViewGroup. A scene is set on a ViewGroup
     * only if that ViewGroup is the scene root.
     *
     * @param sceneRoot The ViewGroup on which the current scene will be returned
     * @return The current Scene set on this ViewGroup. A value of null indicates that
     * no Scene is currently set.
     */
    public static @Nullable Scene getCurrentScene(@NonNull ViewGroup sceneRoot) {
        return (Scene) sceneRoot.getTag(R.id.transition_current_scene);
    }

    /**
     * Scenes that are not defined with layout resources or
     * hierarchies, or which need to perform additional steps
     * after those hierarchies are changed to, should set an enter
     * action, and possibly an exit action as well. An enter action
     * will cause Scene to call back into application code to do
     * anything else the application needs after transitions have
     * captured pre-change values and after any other scene changes
     * have been applied, such as the layout (if any) being added to
     * the view hierarchy. After this method is called, Transitions will
     * be played.
     *
     * @param action The runnable whose {@link Runnable#run() run()} method will
     *               be called when this scene is entered
     * @see #setExitAction(Runnable)
     * @see Scene#Scene(ViewGroup)
     */
    public void setEnterAction(@Nullable Runnable action) {
        mEnterAction = action;
    }

    /**
     * Scenes that are not defined with layout resources or
     * hierarchies, or which need to perform additional steps
     * after those hierarchies are changed to, should set an enter
     * action, and possibly an exit action as well. An exit action
     * will cause Scene to call back into application code to do
     * anything the application needs to do after applicable transitions have
     * captured pre-change values, but before any other scene changes
     * have been applied, such as the new layout (if any) being added to
     * the view hierarchy. After this method is called, the next scene
     * will be entered, including a call to {@link #setEnterAction(Runnable)}
     * if an enter action is set.
     *
     * @see #setEnterAction(Runnable)
     * @see Scene#Scene(ViewGroup)
     */
    public void setExitAction(@Nullable Runnable action) {
        mExitAction = action;
    }

    /**
     * Returns whether this Scene was created by a layout resource file, determined
     * by the layoutId passed into
     * {@link #getSceneForLayout(ViewGroup, int, Context)}.
     */
    boolean isCreatedFromLayoutResource() {
        return (mLayoutId > 0);
    }

}

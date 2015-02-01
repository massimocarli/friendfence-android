package uk.co.massimocarli.friendfence.util;

import android.app.Activity;
import android.view.View;

/**
 * Utility class for UI reference without cast
 * Created by Massimo Carli on 06/06/14.
 */
public final class UI {

    /**
     * Private constructor
     */
    private UI() {
        throw new AssertionError("Never instantiate me! I'm an Utility class!");
    }

    /**
     * This method returns the reference of the View with the given Id in the
     * layout of the Activity passed as parameter.
     *
     * @param act    The Activity that is using the layout with the given View
     * @param viewId The id of the View we want to get a reference
     * @param <T>    The type of the inferred View
     * @return The View with the given id and type
     */

    public static <T extends View> T findViewById(final Activity act, final int viewId) {
        // We get the ViewGroup of the Activity
        View containerView = act.getWindow().getDecorView();
        return findViewById(containerView, viewId);
    }

    /**
     * This method returns the reference of the View with the given Id in the
     * view passed as parameter.
     *
     * @param containerView The container View
     * @param viewId        The id of the View we want to get a reference
     * @param <T>           The type of the inferred View
     * @return The View with the given id and type
     */

    @SuppressWarnings("unchecked")
    public static <T extends View> T findViewById(final View containerView, final int viewId) {
        // We find the view with the given Id
        View foundView = containerView.findViewById(viewId);
        // We return the View with the given cast
        return (T) foundView;
    }
}

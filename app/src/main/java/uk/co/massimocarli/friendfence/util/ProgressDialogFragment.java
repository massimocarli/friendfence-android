package uk.co.massimocarli.friendfence.util;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

/**
 * Created by Massimo Carli on 10/06/14.
 */
public class ProgressDialogFragment extends DialogFragment {

    /**
     * The Id of the message to show
     */
    private static final String MESSAGE_RESOURCE_ID = ".args.MESSAGE_RESOURCE_ID";

    /**
     * Creates a ProgressDialogFragment with the given message to show
     *
     * @param messageResourceId The resourceId of the message to show
     * @return The ProgressDialogFragment for the given message
     */
    public static ProgressDialogFragment get(final int messageResourceId) {
        final ProgressDialogFragment progressDialogFragment = new ProgressDialogFragment();
        final Bundle args = new Bundle();
        args.putInt(MESSAGE_RESOURCE_ID, messageResourceId);
        progressDialogFragment.setArguments(args);
        return progressDialogFragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final ProgressDialog dialog = new ProgressDialog(getActivity());
        final int resourceId = getArguments().getInt(MESSAGE_RESOURCE_ID);
        final String message = getActivity().getResources().getString(resourceId);
        dialog.setMessage(message);
        dialog.setIndeterminate(true);
        dialog.setCancelable(false);
        return dialog;
    }
}

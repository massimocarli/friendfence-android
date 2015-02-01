package uk.co.massimocarli.friendfence.location.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import uk.co.massimocarli.friendfence.Conf;
import uk.co.massimocarli.friendfence.R;
import uk.co.massimocarli.friendfence.util.UI;

/**
 * Created by Massimo Carli on 11/08/14.
 */
public class EditSessionDialog extends DialogFragment {

    /**
     * The key we use to save the sessionId
     */
    private static final String SESSION_ID_ARG_KEY = Conf.PKG + ".arg.SESSION_ID_ARG_KEY";

    /**
     * Edit Text for owner
     */
    private EditText mOwnerEditText;

    /**
     * Create the Dialog to change the owner of the session
     *
     * @param sessionId The id for the session to edit
     * @return The Dialog for editing
     */
    public static EditSessionDialog create(final long sessionId) {
        final EditSessionDialog fragment = new EditSessionDialog();
        final Bundle args = new Bundle();
        args.putLong(SESSION_ID_ARG_KEY, sessionId);
        fragment.setArguments(args);
        return fragment;
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // The EditText for the edit
        final LayoutInflater inflater = LayoutInflater.from(getActivity());
        final View dialogLayout = inflater.inflate(R.layout.dialog_session_edit, null);
        mOwnerEditText = UI.findViewById(dialogLayout, R.id.session_owner_edittext);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setCancelable(true).setView(mOwnerEditText)
                .setTitle(R.string.session_edit_title)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Here we have to update the session
                    }
                })
                .setNegativeButton(android.R.string.no, null);
        return builder.create();
    }
}

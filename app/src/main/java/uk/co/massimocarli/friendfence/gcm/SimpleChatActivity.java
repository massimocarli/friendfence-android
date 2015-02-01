package uk.co.massimocarli.friendfence.gcm;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.RemoteInput;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.IOException;

import uk.co.massimocarli.friendfence.Conf;
import uk.co.massimocarli.friendfence.R;
import uk.co.massimocarli.friendfence.model.UserModel;
import uk.co.massimocarli.friendfence.notification.FenceNotificationHelper;
import uk.co.massimocarli.friendfence.util.ProgressDialogFragment;
import uk.co.massimocarli.friendfence.util.UI;

/**
 * Created by Massimo Carli on 12/10/14.
 */
public class SimpleChatActivity extends ActionBarActivity {

    /**
     * The Tag for the name
     */
    private final static String TAG_LOG = SimpleChatActivity.class.getName();

    /**
     * The MediaType fro JSON data
     */
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    /**
     * The Tag for the ProgressDialog
     */
    private final static String PROGRESS_DIALOG_TAG = "PROGRESS_DIALOG_TAG";

    /**
     * The extra for the username of the sender
     */
    public final static String SENDER_EXTRA = Conf.PKG + ".extra.SENDER_EXTRA";

    /**
     * The extra for the message
     */
    public final static String MESSAGE_EXTRA = Conf.PKG + ".extra.MESSAGE_EXTRA";

    /**
     * The username of the sender
     */
    private String mSender;

    /**
     * The message
     */
    private String mMessage;

    /**
     * The ProgressDialog
     */
    private ProgressDialogFragment mProgressDialog;

    /**
     * The current task
     */
    private SendMessageAsyncTask mCurrentTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_chat);
        mSender = getIntent().getStringExtra(SENDER_EXTRA);
        mMessage = getIntent().getStringExtra(MESSAGE_EXTRA);
        // We show the information in the layout
        final TextView senderTextView = UI.findViewById(this, R.id.chat_message_sender_label);
        final TextView messageTextView = UI.findViewById(this, R.id.chat_message_text);
        // If message and sender are not present we hide the related field
        if (TextUtils.isEmpty(mSender)) {
            senderTextView.setVisibility(View.GONE);
        }
        if (TextUtils.isEmpty(mMessage)) {
            messageTextView.setVisibility(View.GONE);
        }
        final EditText replyEditText = UI.findViewById(this, R.id.chat_message_replay_exittext);
        // We check if exists some text from the Wear device
        Bundle remoteInput = RemoteInput.getResultsFromIntent(getIntent());
        if (remoteInput != null) {
            final CharSequence fromWear = remoteInput
                    .getCharSequence(FenceNotificationHelper.REPLY_TEXT_EXTRA_NAME);
            if (!TextUtils.isEmpty(fromWear)) {
                replyEditText.setText(fromWear);
            }
        }
        // Show info
        senderTextView.setText(getString(R.string.chat_sender_message_label, mSender));
        messageTextView.setText(mMessage);
        final UserModel userModel = UserModel.get(this);
        // Manage send button. If the user is not logged in we hide the button
        if (!userModel.isLogged()) {
            UI.findViewById(this, R.id.chat_message_button).setEnabled(false);
            Toast.makeText(SimpleChatActivity.this, R.string.chat_not_logged_message,
                    Toast.LENGTH_SHORT).show();
        } else {
            UI.findViewById(this, R.id.chat_message_button).setEnabled(true);
            UI.findViewById(this, R.id.chat_message_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // We use the AsyncTask to send the message
                    final Editable reply = replyEditText.getText();
                    if (!TextUtils.isEmpty(reply)) {
                        if (mCurrentTask != null) {
                            mCurrentTask.cancel(true);
                        }
                        final String currentUsername = UserModel.get(SimpleChatActivity.this).getUsername();
                        mCurrentTask = new SendMessageAsyncTask(currentUsername, mSender);
                        mCurrentTask.execute(reply.toString());
                    } else {
                        // We show a message
                        Log.w(TAG_LOG, "Empty message!");
                        Toast.makeText(SimpleChatActivity.this, R.string.chat_empty_reply,
                                Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    /**
     * This is the AsyncTask we use to send a Message using GCM HTTP
     */
    private class SendMessageAsyncTask extends AsyncTask<String, Void, Boolean> {

        /**
         * The destination of the message
         */
        private final String mDestination;

        /**
         * The sender of the message
         */
        private final String mSender;

        /**
         * Create a SendMessageAsyncTask o send a message to a specific destination
         *
         * @param sender      The sender of this message
         * @param destination The username of the user that should receive the message
         */
        public SendMessageAsyncTask(final String sender, final String destination) {
            this.mDestination = destination;
            this.mSender = sender;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // We show the Dialog
            mProgressDialog = ProgressDialogFragment.get(R.string.chat_sending___);
            mProgressDialog.show(getSupportFragmentManager(), PROGRESS_DIALOG_TAG);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPreExecute();
            // We hide the Dialog
            mProgressDialog.dismiss();
            mProgressDialog = null;
            if (result) {
                Toast.makeText(SimpleChatActivity.this, R.string.chat_message_sent_ok,
                        Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(SimpleChatActivity.this, R.string.chat_message_sent_ko,
                        Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            // We hide the Dialog
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }

        @Override
        protected Boolean doInBackground(String... params) {
            final String message = params[0];
            final OkHttpClient httpClient = new OkHttpClient();
            // The url for the service
            final String registerUrl = getString(R.string.send_message_url);
            // We create the Json object to send
            final String jsonInput = GcmRequest.SendBuilder.create()
                    .withSender(mSender)
                    .withTo(mDestination)
                    .withMessageBody(message)
                    .getJsonAsString();
            // We create the output
            RequestBody body = RequestBody.create(JSON, jsonInput);
            Request request = new Request.Builder()
                    .url(registerUrl)
                    .post(body)
                    .build();
            try {
                Response response = httpClient.newCall(request).execute();
                return response.isSuccessful();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
    }
}

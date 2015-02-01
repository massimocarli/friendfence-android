package uk.co.massimocarli.friendfence.datalayer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;

import uk.co.massimocarli.friendfence.R;

public class ShowMessageActivity extends Activity {

    /**
     * The name of the extra for the message to show
     */
    public static final String MESSAGE_EXTRA = "MESSAGE_EXTRA";

    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_message_layout);
        mTextView = (TextView) findViewById(R.id.show_message_text);
        final String message = getIntent().getStringExtra(MESSAGE_EXTRA);
        if (!TextUtils.isEmpty(message)) {
            mTextView.setText(message);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        final String message = intent.getStringExtra(MESSAGE_EXTRA);
        if (!TextUtils.isEmpty(message)) {
            mTextView.setText(message);
        }
    }
}

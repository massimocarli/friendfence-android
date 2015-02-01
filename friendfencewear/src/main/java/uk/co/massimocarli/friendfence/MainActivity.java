package uk.co.massimocarli.friendfence;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.wearable.view.WatchViewStub;
import android.view.View;
import android.widget.TextView;

import java.util.List;

public class MainActivity extends Activity {

    /**
     * The request id for the talk operation
     */
    private static final int TALK_REQUEST_ID = 37;

    /**
     * The TextView for the text
     */
    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mTextView = (TextView) stub.findViewById(R.id.text);
                stub.findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Here we want to launch the custom notification
                        launchNotification();
                    }
                });
                stub.findViewById(R.id.talk).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Here we want to launch the custom notification
                        startTalk();
                    }
                });
            }
        });
    }

    /**
     * Utility method to launch a custom notification
     */
    private void launchNotification() {
        Intent displayIntent = new Intent(this, NotificationActivity.class);
        PendingIntent displayPendingIntent = PendingIntent.getActivity(this,
                0, displayIntent, 0);
        // We create the Notification
        final Notification.Builder builder = new Notification.Builder(this)
                .setContentTitle(this.getString(R.string.custom_notification_title))
                .setContentText(this.getString(R.string.custom_notification_text))
                .setSmallIcon(R.mipmap.ic_launcher)
                .extend(new Notification.WearableExtender()
                        .setDisplayIntent(displayPendingIntent));
        final NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, builder.build());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (TALK_REQUEST_ID == requestCode) {
            if (RESULT_OK == resultCode) {
                // We get a list of possible result
                List<String> results = data.getStringArrayListExtra(
                        RecognizerIntent.EXTRA_RESULTS);
                // We get the first if available
                if (results != null && results.size() > 0) {
                    String recognizedText = results.get(0);
                    mTextView.setText(recognizedText);
                } else {
                    mTextView.setText(R.string.talk_not_understood);
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * We launch the speech recognizer
     */
    private void startTalk() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        startActivityForResult(intent, TALK_REQUEST_ID);
    }
}

package uk.co.massimocarli.friendfence.plus;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;

import uk.co.massimocarli.friendfence.Conf;
import uk.co.massimocarli.friendfence.R;
import uk.co.massimocarli.friendfence.gcm.GcmService;
import uk.co.massimocarli.friendfence.model.UserModel;
import uk.co.massimocarli.friendfence.util.UI;

public class GooglePlusLoginActivity extends ActionBarActivity implements ShowPersonFragment.PersonProvider, GooglePlayClientProvider {

    /**
     * The Tag for the Log
     */
    private static final String TAG_LOG = "GooglePlusLoginActivity";

    /**
     * The key to persist the resolution state of this Activity
     */
    private static final String KEY_IN_RESOLUTION = "is_in_resolution";

    /**
     * Request code for auto Google Play Services error resolution.
     */
    protected static final int REQUEST_CODE_RESOLUTION = 1;

    /**
     * The Tag for the Friends Fragment
     */
    protected static final String FRIENDS_FRAGMENT_TAG = Conf.PKG + ".tag.FRIENDS_FRAGMENT_TAG";

    /**
     * The Tag for the Person Fragment
     */
    protected static final String PERSON_FRAGMENT_TAG = Conf.PKG + ".tag.PERSON_FRAGMENT_TAG";

    /**
     * Google API client.
     */
    private GoogleApiClient mGoogleApiClient;

    /**
     * Determines if the client is in a resolution state, and
     * waiting for resolution intent to return.
     */
    private boolean mIsInResolution;

    /**
     * The Current Fragment
     */
    private Fragment mCurrentFragment;

    /**
     * This is the implementation of the ConnectionCallbacks interface we use to manage
     * connection callback methods
     */
    private final GoogleApiClient.ConnectionCallbacks mConnectionCallbacks =
            new GoogleApiClient.ConnectionCallbacks() {

                @Override
                public void onConnected(Bundle bundle) {
                    Log.i(TAG_LOG, "GoogleApiClient connected");
                    // Here we set the User as logged and we get the username
                    final String username = Plus.AccountApi.getAccountName(mGoogleApiClient);
                    UserModel.get(getApplicationContext()).login(username);
                    // We switch the buttons
                    UI.findViewById(GooglePlusLoginActivity.this, R.id.plus_login_button)
                            .setVisibility(View.GONE);
                    UI.findViewById(GooglePlusLoginActivity.this, R.id.plus_logout_button)
                            .setVisibility(View.VISIBLE);
                    invalidateOptionsMenu();
                    // We launch the registration for GCM
                    GcmService.manageRegistrationId(GooglePlusLoginActivity.this, username);
                    // We add the information about the Person to the
                    showPersonData();
                }

                @Override
                public void onConnectionSuspended(int i) {
                    Log.i(TAG_LOG, "GoogleApiClient connection suspended");
                    retryConnecting();
                }
            };


    /**
     * Implementation of the interface that manages connection errors
     */
    private final GoogleApiClient.OnConnectionFailedListener mOnConnectionFailedLister =
            new GoogleApiClient.OnConnectionFailedListener() {
                @Override
                public void onConnectionFailed(ConnectionResult connectionResult) {
                    Log.i(TAG_LOG, "GoogleApiClient connection failed: " + connectionResult.toString());
                    if (!connectionResult.hasResolution()) {
                        // Show a localized error dialog.
                        GooglePlayServicesUtil.getErrorDialog(
                                connectionResult.getErrorCode(), GooglePlusLoginActivity.this, 0, new OnCancelListener() {
                                    @Override
                                    public void onCancel(DialogInterface dialog) {
                                        retryConnecting();
                                    }
                                }).show();
                        return;
                    }
                    // If there is an existing resolution error being displayed or a resolution
                    // activity has started before, do nothing and wait for resolution
                    // progress to be completed.
                    if (mIsInResolution) {
                        return;
                    }
                    mIsInResolution = true;
                    try {
                        connectionResult.startResolutionForResult(GooglePlusLoginActivity.this, REQUEST_CODE_RESOLUTION);
                    } catch (SendIntentException e) {
                        Log.e(TAG_LOG, "Exception while starting resolution activity", e);
                        retryConnecting();
                    }
                }
            };

    /**
     * Called when the activity is starting. Restores the activity state.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plus_login);
        // We get the reference to the Login Button
        final SignInButton signInButton = UI.findViewById(this, R.id.plus_login_button);
        signInButton.setSize(SignInButton.SIZE_WIDE);
        final Button signOutButton = UI.findViewById(this, R.id.plus_logout_button);
        if (!UserModel.get(this).isLogged()) {
            signOutButton.setVisibility(View.GONE);
        } else {
            // We have to hide the LoginButton and show the Logout
            signInButton.setVisibility(View.GONE);
            signOutButton.setVisibility(View.VISIBLE);
        }
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGoogleApiClient.connect();
            }
        });
        signOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // We do logout
                Plus.AccountApi.revokeAccessAndDisconnect(mGoogleApiClient)
                        .setResultCallback(new ResultCallback<Status>() {
                            @Override
                            public void onResult(Status status) {
                                if (status.isSuccess()) {
                                    // We switch the buttons
                                    signInButton.setVisibility(View.VISIBLE);
                                    signOutButton.setVisibility(View.GONE);
                                    UserModel.get(GooglePlusLoginActivity.this).logout();
                                    mGoogleApiClient.disconnect();
                                    clearFragment();
                                    invalidateOptionsMenu();
                                } else {
                                    Toast.makeText(GooglePlusLoginActivity.this,
                                            R.string.plus_logout_error,
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });
        if (savedInstanceState != null) {
            mIsInResolution = savedInstanceState.getBoolean(KEY_IN_RESOLUTION, false);
        }
    }

    /**
     * Called when the Activity is made visible.
     * A connection to Play Services need to be initiated as
     * soon as the activity is visible. Registers {@code ConnectionCallbacks}
     * and {@code OnConnectionFailedListener} on the
     * activities itself.
     */
    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(Plus.API)
                    .addScope(Plus.SCOPE_PLUS_LOGIN)
                    .addConnectionCallbacks(mConnectionCallbacks)
                    .addOnConnectionFailedListener(mOnConnectionFailedLister)
                    .build();
        }
        if (UserModel.get(this).isLogged()) {
            mGoogleApiClient.connect();
        }
    }

    /**
     * Called when activity gets invisible. Connection to Play Services needs to
     * be disconnected as soon as an activity is invisible.
     */
    @Override
    protected void onStop() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    /**
     * Saves the resolution state.
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_IN_RESOLUTION, mIsInResolution);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        final MenuInflater menuInflater = new MenuInflater(this);
        menuInflater.inflate(R.menu.social, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        final boolean isUserLogged = UserModel.get(this).isLogged();
        // This is visible only if the user is logged
        menu.findItem(R.id.action_social_friends).setVisible(isUserLogged);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int itemId = item.getItemId();
        if (itemId == R.id.action_social_friends) {
            showFriendList();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    /**
     * Handles Google Play Services resolution callbacks.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_RESOLUTION:
                if (resultCode == Activity.RESULT_OK) {
                    retryConnecting();
                } else {
                    mGoogleApiClient.disconnect();
                    mIsInResolution = false;
                }
                break;
        }
        // IMPORTANT!!!!!
        // We need this because the lifecycle of the Google Play Service
        // initialization is bounded to the Activity and not to the Fragment.
        if (mCurrentFragment != null) {
            mCurrentFragment.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void retryConnecting() {
        mIsInResolution = false;
        if (!mGoogleApiClient.isConnecting()) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    public Person getPerson() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            final Person currentPerson = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);
            return currentPerson;
        } else {
            return null;
        }
    }


    /**
     * Utility method to show the data about a person
     */
    private void showPersonData() {
        final ShowPersonFragment personFragment = new ShowPersonFragment();
        mCurrentFragment = personFragment;
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.plus_anchor_point, personFragment, PERSON_FRAGMENT_TAG)
                .commit();
    }

    /**
     * Utility method to show the list of friends
     */
    private void showFriendList() {
        final ShowFriendsFragment friendFragment = new ShowFriendsFragment();
        mCurrentFragment = friendFragment;
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.plus_anchor_point, friendFragment, FRIENDS_FRAGMENT_TAG)
                .addToBackStack(FRIENDS_FRAGMENT_TAG)
                .commit();
    }

    private void clearFragment() {
        getSupportFragmentManager().beginTransaction().remove(mCurrentFragment).commit();
    }

    @Override
    public GoogleApiClient getGoogleApiClient() {
        return mGoogleApiClient;
    }

}

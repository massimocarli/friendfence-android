package uk.co.massimocarli.friendfence.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import uk.co.massimocarli.friendfence.Conf;

/**
 * This is the class that encapsulate the information related to a logged User
 * Created by Massimo Carli on 02/10/14.
 */
public final class UserModel {

    /**
     * The Tag for the Log
     */
    private static final String TAG_LOG = UserModel.class.getName();

    /**
     * This is the name for the SharedPreferences
     */
    private static final String USER_PREFS = Conf.PKG + ".prefs.USER_PREFS";

    /**
     * The Reference to the SharedPreferences
     */
    private final SharedPreferences mPrefs;

    /**
     * The Editor to manage data
     */
    private SharedPreferences.Editor mEditor;

    /**
     * The Jeys we use for the items into the SharedPreferences
     */
    private static interface Keys {

        /**
         * The Key we use to manage the username
         */
        String USERNAME = Conf.PKG + ".key.USERNAME";

        /**
         * The Key we use to manage the nickname
         */
        String NICKNAME = Conf.PKG + ".key.NICKNAME";

        /**
         * The Key we use to manage the token
         */
        String TOKEN = Conf.PKG + ".key.TOKEN";

        /**
         * The Key to use for the registrationId
         */
        String REGISTRATION_ID = Conf.PKG + ".key.REGISTRATION_ID";

        /**
         * The Key to use for the application version
         */
        String APP_VERSION = Conf.PKG + ".key.APP_VERSION";
    }

    /**
     * Private constructor
     *
     * @param context The Context
     */
    private UserModel(final Context context) {
        // We create the SharedPreferences
        mPrefs = context.getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE);
    }


    /**
     * Creates and return a UserMode
     *
     * @param context The Context
     * @return The UserModel read from the SharedPreferences
     */
    public synchronized static UserModel get(final Context context) {
        final UserModel userModel = new UserModel(context);
        return userModel;
    }

    /**
     * @return True if the user is logged and false otherwise
     */
    public boolean isLogged() {
        // The user is logged if a username is present
        return mPrefs.contains(Keys.USERNAME);
    }

    /**
     * Login the user with the given Username
     *
     * @param username The username for this user
     * @return The instance for chaining
     */
    public UserModel login(final String username) {
        mPrefs.edit().putString(Keys.USERNAME, username).commit();
        return this;
    }

    /**
     * We delete all the information so the user is logged out
     */
    public void logout() {
        // Clear all the data so the user is not logged
        mPrefs.edit().clear().commit();
    }


    /**
     * This method get the Registration Id for Google Cloud Messaging. If the
     * registrationId is not present or the version of the app is different we
     * return the empty String
     *
     * @param context        The Context
     * @param currentVersion The current version of the app
     * @return The RegistrationId if any or empty ("") if not present
     */
    public String getRegistrationId(final Context context, final int currentVersion) {
        // We check if the registrationId is present
        String registrationId = mPrefs.getString(Keys.REGISTRATION_ID, "");
        if (registrationId.isEmpty()) {
            Log.i(TAG_LOG, "No registration Id found");
            return "";
        }
        // If we're here we have a registrationId. We have to check if the version
        // of the app has been changed or not
        int registeredVersion = mPrefs.getInt(Keys.APP_VERSION, Integer.MIN_VALUE);
        if (registeredVersion != currentVersion) {
            Log.i(TAG_LOG, "App version changed.");
            return "";
        }
        return registrationId;
    }


    /**
     * @return The username if the user is logged or null if not
     */
    public String getUsername() {
        return mPrefs.getString(Keys.USERNAME, null);
    }

    /**
     * This method saves the information for the RegistrationId for the given application
     * version
     *
     * @param regId      The registrationId
     * @param appVersion The version of the app
     */
    public void setRegistrationId(final String regId, final int appVersion) {
        Log.i(TAG_LOG, "Saving regId on app version " + appVersion);
        mPrefs.edit().putString(Keys.REGISTRATION_ID, regId)
                .putInt(Keys.APP_VERSION, appVersion)
                .commit();
    }


}

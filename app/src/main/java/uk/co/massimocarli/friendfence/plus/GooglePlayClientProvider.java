package uk.co.massimocarli.friendfence.plus;

import com.google.android.gms.common.api.GoogleApiClient;

/**
 * This is the interface that should be implemented by Activities that provide
 * GooglePlayClient implementations usually to their Fragments
 * Created by Massimo Carli on 05/10/14.
 */
public interface GooglePlayClientProvider {

    /**
     * @return The GoogleApiClient Object reference
     */
    GoogleApiClient getGoogleApiClient();

}

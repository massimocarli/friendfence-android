package uk.co.massimocarli.friendfence.fragment;

import android.content.Context;
import android.support.v4.app.Fragment;

import uk.co.massimocarli.friendfence.geofence.GeoFenceFragment;
import uk.co.massimocarli.friendfence.location.MyLocationFragment;
import uk.co.massimocarli.friendfence.location.MyMoveFragment;

/**
 * This is the Factory for all the Fragment of the application
 * <p/>
 */
public class FragmentFactory {

    /**
     * The Singleton Instance
     */
    private static FragmentFactory sFragmentFactory;

    /**
     * Private constructor
     */
    private FragmentFactory() {
    }

    /**
     * @return The FragmentFactory Singleton
     */
    public synchronized static FragmentFactory get() {
        if (null == sFragmentFactory) {
            sFragmentFactory = new FragmentFactory();
        }
        return sFragmentFactory;
    }

    public final Fragment getFragment(final Context context, final int selection) {
        switch (selection) {
            case 0:
                return new MyLocationFragment();
            case 1:
                return new MyMoveFragment();
            case 2:
                return new GeoFenceFragment();
        }
        return new Fragment();
    }
}

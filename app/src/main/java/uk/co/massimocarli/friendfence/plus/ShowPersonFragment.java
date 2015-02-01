package uk.co.massimocarli.friendfence.plus;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.plus.model.people.Person;
import com.squareup.picasso.Picasso;

import java.util.List;

import uk.co.massimocarli.friendfence.R;
import uk.co.massimocarli.friendfence.util.UI;

/**
 * This is the Fragment that shows the information related to the current logged Person
 * <p/>
 * Created by Massimo Carli on 04/10/14.
 */
public class ShowPersonFragment extends Fragment {

    /**
     * The Tag for the Log
     */
    private static final String TAG_LOG = ShowPersonFragment.class.getName();

    /**
     * The ImageView for the Picture
     */
    private ImageView mPictureImageView;

    /**
     * The TextView for the NickName
     */
    private TextView mDisplayNameTextView;

    /**
     * The TextView for the Location
     */
    private TextView mLocationTextView;

    /**
     * The TextView for the about
     */
    private TextView mAboutTextView;

    /**
     * The Reference to the current person
     */
    private Person mPerson;

    /**
     * The Reference to the Activity as PersonProvider
     */
    private PersonProvider mPersonProvider;

    /**
     * The interface the hosting activity should implement to provide Person data
     */
    public static interface PersonProvider {

        /**
         * @return The Person to show
         */
        Person getPerson();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        final View fragmentLayout = inflater.inflate(R.layout.fragment_person_layout, null);
        // We get UI references
        mPictureImageView = UI.findViewById(fragmentLayout, R.id.plus_person_imageview);
        mDisplayNameTextView = UI.findViewById(fragmentLayout, R.id.plus_person_display_name);
        mLocationTextView = UI.findViewById(fragmentLayout, R.id.plus_person_location);
        mAboutTextView = UI.findViewById(fragmentLayout, R.id.plus_person_about);
        // We return the layout
        return fragmentLayout;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof PersonProvider) {
            mPersonProvider = (PersonProvider) activity;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        // Here we show the data for the Person
        updatePersonData();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mPersonProvider = null;
    }


    /**
     * Here we update person data if any
     */
    private void updatePersonData() {
        if (mPersonProvider != null) {
            final Person person = mPersonProvider.getPerson();
            if (person == null) {
                // We exit if the person is not available
                return;
            }
            // We manage the Image for the Person
            if (person.hasImage() && person.getImage().hasUrl()) {
                final String imageUrl = person.getImage().getUrl();
                // We show the image with Picasso
                Picasso.with(getActivity()).load(imageUrl).into(mPictureImageView);
            }
            // We manage the Nickname
            if (person.hasDisplayName()) {
                mDisplayNameTextView.setText(person.getDisplayName());
            }
            // We manage the location
            if (person.hasPlacesLived()) {
                List<Person.PlacesLived> placesLived = person.getPlacesLived();
                for (Person.PlacesLived place : placesLived) {
                    if (place.isPrimary()) {
                        mLocationTextView.setText(place.getValue());
                        break;
                    }
                }
            }
            // We manage the about field
            if (person.hasAboutMe()) {
                mAboutTextView.setText(Html.fromHtml(person.getAboutMe()));
            }

            Log.d(TAG_LOG, "PERSON " + person);
        }
    }
}

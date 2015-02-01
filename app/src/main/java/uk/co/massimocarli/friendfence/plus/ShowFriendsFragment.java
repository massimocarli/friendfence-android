package uk.co.massimocarli.friendfence.plus;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Checkable;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.plus.People;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.PlusShare;
import com.google.android.gms.plus.model.people.Person;
import com.google.android.gms.plus.model.people.PersonBuffer;
import com.squareup.picasso.Picasso;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import uk.co.massimocarli.friendfence.R;
import uk.co.massimocarli.friendfence.util.UI;

/**
 * This is the Fragment that shows the list of all the Friends so we can invite them to use
 * this application
 * Created by Massimo Carli on 05/10/14.
 */
public class ShowFriendsFragment extends ListFragment {

    /**
     * The Tag for the Log
     */
    private static final String TAG_LOG = ShowFriendsFragment.class.getName();

    /**
     * The MimeType for plain text
     */
    private static final String PLAIN_TEXT_MIME_TYPE = "text/plain";

    /**
     * The MimeType for HTML
     */
    private static final String HTML_MIME_TYPE = "text/html";

    /**
     * The RequestCode for the Share option
     */
    private static final int SHARE_REQUEST_CODE = 37;

    /**
     * The RequestCode for the Pick Picture action
     */
    private static final int PICK_PICTURE_REQUEST_CODE = 38;

    /**
     * The Reference to the object that provides the GooglePlayClient object
     */
    private GooglePlayClientProvider mGooglePlayClientProvider;

    /**
     * The List of Persons
     */
    private List<Person> mPersons = new LinkedList<Person>();

    /**
     * Se the of selected persons
     */
    private Set<Person> mSelectedPersons = new HashSet<Person>();

    /**
     * The Adapter for the Person
     */
    private ArrayAdapter<Person> mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof GooglePlayClientProvider) {
            mGooglePlayClientProvider = (GooglePlayClientProvider) activity;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mGooglePlayClientProvider != null) {
            // We get the list of friends
            final GoogleApiClient googleApiClient = mGooglePlayClientProvider.getGoogleApiClient();
            if (googleApiClient != null) {
                mPersons.clear();
                Plus.PeopleApi.loadVisible(googleApiClient, null).setResultCallback(new ResultCallback<People.LoadPeopleResult>() {
                    @Override
                    public void onResult(People.LoadPeopleResult loadPeopleResult) {
                        final PersonBuffer personBuffer = loadPeopleResult.getPersonBuffer();
                        for (Person person : personBuffer) {
                            mPersons.add(person);
                        }
                        // We update the Adapter with the data
                        mAdapter.notifyDataSetChanged();
                        // We notify the Fragment
                    }
                });
            }
        }
        mAdapter = new ArrayAdapter<Person>(getActivity(), R.layout.fragment_person_item, mPersons) {

            class Holder {
                ImageView imageView;
                CheckedTextView displayNameView;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                Holder holder = null;
                if (convertView == null) {
                    // Inflate layout
                    convertView = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_person_item, null);
                    // Create Holder
                    holder = new Holder();
                    convertView.setTag(holder);
                    // Get UI items
                    holder.imageView = UI.findViewById(convertView, R.id.person_item_imageview);
                    holder.displayNameView = UI.findViewById(convertView, R.id.person_item_display_name);
                } else {
                    holder = (Holder) convertView.getTag();
                }
                // We fill Holder data
                final Person currentPerson = getItem(position);
                holder.displayNameView.setText(currentPerson.getDisplayName());
                if (currentPerson.hasImage() && currentPerson.getImage().hasUrl()) {
                    Picasso.with(getActivity()).load(currentPerson.getImage().getUrl())
                            .into(holder.imageView);
                } else {
                    holder.imageView.setImageResource(R.drawable.question_mark);
                }
                // We manage the state of the selection
                holder.displayNameView.setChecked(mSelectedPersons.contains(currentPerson));
                return convertView;
            }
        };
        // We want to select multiple items
        getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        // We set the adapter of the ListView
        getListView().setAdapter(mAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        setListShown(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.share, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        //menu.getItem(R.id.action_social_friends).setVisible(false);
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_share) {
            shareToFriends();
            return true;
        } else if (item.getItemId() == R.id.action_share_image) {
            launchShareImageToFriends();
            return true;
        } else if (item.getItemId() == R.id.action_share_homepage) {
            shareAuthorPage();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (SHARE_REQUEST_CODE == requestCode) {
            if (Activity.RESULT_OK == resultCode) {
                Toast.makeText(getActivity(), R.string.action_social_share_success,
                        Toast.LENGTH_SHORT).show();
            } else {
                Log.d(TAG_LOG, "Share cancelled!");
            }
        } else if (PICK_PICTURE_REQUEST_CODE == requestCode) {
            if (Activity.RESULT_OK == resultCode) {
                // We share the selected image
                shareImage(data.getData());
            }
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mGooglePlayClientProvider = null;
    }

    @Override
    public void onListItemClick(ListView l, View view, int position, long id) {
        // Here we have to manage the selection of a Friend
        final Person selectedPerson = mPersons.get(position);
        // We get the reference to the selected item
        Checkable checkable = (Checkable) UI.findViewById(view, R.id.person_item_display_name);
        // We update the related View state and add/remove the selected person
        if (mSelectedPersons.contains(selectedPerson)) {
            // We remove it
            mSelectedPersons.remove(selectedPerson);
            // Uncheck the item
            checkable.setChecked(false);
        } else {
            // We add the selected person
            mSelectedPersons.add(selectedPerson);
            // Check the item
            checkable.setChecked(true);
        }
    }

    /**
     * This is the utility method we have created to send information to the
     * selected friends
     */
    private void shareToFriends() {
        if (mSelectedPersons.isEmpty()) {
            Toast.makeText(getActivity(), R.string.plus_share_no_friends_selected_message,
                    Toast.LENGTH_SHORT).show();
        } else {
            // We put the selected users into a List
            final List<Person> selectedList = new LinkedList<Person>(mSelectedPersons);
            final Intent shareIntent = new PlusShare.Builder(getActivity())
                    .setRecipients(selectedList)
                    .setType(PLAIN_TEXT_MIME_TYPE)
                    .setText(getString(R.string.plus_share_text))
                    .getIntent();
            getActivity().startActivityForResult(shareIntent, SHARE_REQUEST_CODE);
        }
    }

    /**
     * This method share a sample image to friends
     */
    private void launchShareImageToFriends() {
        if (mSelectedPersons.isEmpty()) {
            Toast.makeText(getActivity(), R.string.plus_share_no_friends_selected_message,
                    Toast.LENGTH_SHORT).show();
        } else {
            // We choose an existing picture
            Intent photoPicker = new Intent(Intent.ACTION_PICK);
            photoPicker.setType("video/*, image/*");
            startActivityForResult(photoPicker, PICK_PICTURE_REQUEST_CODE);
        }
    }

    /**
     * Share the image for the given uri
     *
     * @param imageUri The uri of the image to share
     */
    private void shareImage(final Uri imageUri) {
        final List<Person> selectedList = new LinkedList<Person>(mSelectedPersons);
        ContentResolver cr = getActivity().getContentResolver();
        String mime = cr.getType(imageUri);
        Intent shareImageIntent = new PlusShare.Builder(getActivity())
                .setText(getString(R.string.action_social_share_image_text))
                .addStream(imageUri)
                .setRecipients(selectedList)
                .setType(mime)
                .getIntent();
        getActivity().startActivityForResult(shareImageIntent, SHARE_REQUEST_CODE);
    }

    /**
     * This is an utility method to share the homepage of the author of this code
     */
    private void shareAuthorPage() {
        if (mSelectedPersons.isEmpty()) {
            Toast.makeText(getActivity(), R.string.plus_share_no_friends_selected_message,
                    Toast.LENGTH_SHORT).show();
        } else {
            // We put the selected users into a List
            final List<Person> selectedList = new LinkedList<Person>(mSelectedPersons);
            final Intent shareIntent = new PlusShare.Builder(getActivity())
                    .setRecipients(selectedList)
                    .setText(getString(R.string.plus_share_text))
                    .setType(PLAIN_TEXT_MIME_TYPE)
                    .setContentUrl(Uri.parse("http://www.massimocarli.eu"))
                    .getIntent();
            getActivity().startActivityForResult(shareIntent, SHARE_REQUEST_CODE);
        }
    }

}

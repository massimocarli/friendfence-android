package uk.co.massimocarli.friendfence.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import uk.co.massimocarli.friendfence.Conf;
import uk.co.massimocarli.friendfence.R;
import uk.co.massimocarli.friendfence.datalayer.SendMessageActivity;
import uk.co.massimocarli.friendfence.datalayer.SyncDataActivity;
import uk.co.massimocarli.friendfence.datalayer.SyncImageActivity;
import uk.co.massimocarli.friendfence.fragment.FragmentFactory;
import uk.co.massimocarli.friendfence.plus.GooglePlusLoginActivity;

public class MainActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    /**
     * This is the extra we use to set the first Fragment
     */
    public static final String FIRST_FRAGMENT_INDEX = Conf.PKG + ".extra.FIRST_FRAGMENT_INDEX";

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    /**
     * The current fragment
     */
    private Fragment mCurrentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        // We use this to se the first fragment from outside
        int firstFragmentIndex = getIntent().getIntExtra(FIRST_FRAGMENT_INDEX, 0);
        onNavigationDrawerItemSelected(firstFragmentIndex);
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();
        final Fragment optionFragment = FragmentFactory.get()
                .getFragment(this, position);
        mCurrentFragment = optionFragment;
        fragmentManager.beginTransaction()
                .replace(R.id.container, optionFragment)
                .commit();
    }

    public void onSectionAttached(int number) {
        // We return the label for the given option
        mTitle = getResources().getStringArray(R.array.menu_options)[number];
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // IMPORTANT!!!!!
        // We need this because the lifecycle of the Google Play Service
        // initialization is bounded to the Activity and not to the Fragment.
        if (mCurrentFragment != null) {
            mCurrentFragment.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onBackPressed() {
        // if there is a fragment and the back stack of this fragment is not empty,
        // then emulate 'onBackPressed' behaviour, because in default, it is not working
        FragmentManager fm = getSupportFragmentManager();
        for (Fragment frag : fm.getFragments()) {
            if (frag.isVisible()) {
                FragmentManager childFm = frag.getChildFragmentManager();
                if (childFm.getBackStackEntryCount() > 0) {
                    childFm.popBackStack();
                    return;
                }
            }
        }
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        final MenuInflater menuInflater = new MenuInflater(this);
        menuInflater.inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int itemId = item.getItemId();
        if (itemId == R.id.action_social) {
            Intent socialIntent = new Intent(this, GooglePlusLoginActivity.class);
            startActivity(socialIntent);
            return true;
        } else if (itemId == R.id.action_data_layer_ping_pong) {
            Intent dataLayerIntent = new Intent(this, SyncDataActivity.class);
            startActivity(dataLayerIntent);
            return true;
        } else if (itemId == R.id.action_data_layer_sync_image) {
            Intent syncImageIntent = new Intent(this, SyncImageActivity.class);
            startActivity(syncImageIntent);
            return true;
        } else if (itemId == R.id.action_data_layer_send_message) {
            Intent sendMessageIntent = new Intent(this, SendMessageActivity.class);
            startActivity(sendMessageIntent);
            return true;

        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * We use this to set the current Fragment
     *
     * @param currentFragment The current Fragment
     */
    public void setCurrentFragment(Fragment currentFragment) {
        this.mCurrentFragment = currentFragment;
    }

}

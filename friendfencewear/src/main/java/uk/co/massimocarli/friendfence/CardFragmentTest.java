package uk.co.massimocarli.friendfence;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.wearable.view.CardFragment;

/**
 * The Activity that shows a simple CardFragment
 */
public class CardFragmentTest extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.card_fragment_container);
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        CardFragment cardFragment = CardFragment.create(getString(R.string.card_fragment_title),
                getString(R.string.card_fragment_description),
                R.mipmap.ic_launcher);
        fragmentTransaction.add(R.id.frame_card_anchor, cardFragment);
        fragmentTransaction.commit();
    }
}

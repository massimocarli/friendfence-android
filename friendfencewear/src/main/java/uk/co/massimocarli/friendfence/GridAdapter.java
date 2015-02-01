package uk.co.massimocarli.friendfence;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.wearable.view.CardFragment;
import android.support.wearable.view.CardFrame;
import android.support.wearable.view.FragmentGridPagerAdapter;
import android.view.Gravity;

/**
 * This is the class that define a simple Adapter to manage the 2D Picker pattern
 * Created by Massimo Carli on 23/10/14.
 */
public class GridAdapter extends FragmentGridPagerAdapter {

    /**
     * The number of rows
     */
    private static final int ROW_COUNT = 5;

    /**
     * Create a GridAdapter using a given FragmentManager
     *
     * @param fm The FragmentManager
     */
    public GridAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getFragment(int row, int col) {
        final String text = "(" + row + "," + col + ")";
        CardFragment fragment = CardFragment.create("Grid", text, R.mipmap.ic_launcher);
        // Advanced settings (card gravity, card expansion/scrolling)
        fragment.setCardGravity(Gravity.CENTER);
        fragment.setExpansionEnabled(true);
        fragment.setExpansionDirection(CardFrame.EXPAND_DOWN);
        fragment.setExpansionFactor(2.0f);
        return fragment;
    }

    @Override
    public int getRowCount() {
        return ROW_COUNT;
    }

    @Override
    public int getColumnCount(int rowNum) {
        return rowNum + 1;
    }

}

package uk.co.massimocarli.friendfence;

import android.app.Activity;
import android.os.Bundle;
import android.support.wearable.view.GridViewPager;

/**
 * Activity we use to describe the 2D Picker patterns
 * Created by Massimo Carli on 23/10/14.
 */
public class GridActivity extends Activity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.grid_layout);
        // We get the reference to the Pager
        final GridViewPager pager = (GridViewPager) findViewById(R.id.grid_pager);
        // We create the Adapter
        final GridAdapter gridAdapter = new GridAdapter(getFragmentManager());
        // We set the Adapter to the Pager
        pager.setAdapter(gridAdapter);
    }
}

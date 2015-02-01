package uk.co.massimocarli.friendfence;

import android.app.Activity;
import android.os.Bundle;
import android.support.wearable.view.CardFrame;
import android.support.wearable.view.CardScrollView;
import android.view.Gravity;

/**
 * Created by Massimo Carli on 23/10/14.
 */
public class CardScrollActivity extends Activity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.card_scroll);
        CardScrollView cardScrollView =
                (CardScrollView) findViewById(R.id.card_scroll_view);
        cardScrollView.setCardGravity(Gravity.BOTTOM);
        cardScrollView.setExpansionEnabled(true);

        cardScrollView.setExpansionDirection(CardFrame.EXPAND_DOWN);
        cardScrollView.setExpansionFactor(2.0f);
    }
}

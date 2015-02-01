package uk.co.massimocarli.friendfence;

import android.content.Context;
import android.graphics.Color;
import android.support.wearable.view.WearableListView;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * This is the class that defines the items of the WearableListView
 * Created by Massimo Carli on 22/10/14.
 */
public class WearListItem extends LinearLayout implements WearableListView.Item {

    /**
     * The Tag for the Log
     */
    private static final String TAG_LOG = WearListItem.class.getName();

    /**
     * The ImageView for the icon
     */
    private ImageView mIcon;

    /**
     * The TextView for the label
     */
    private TextView mName;

    /**
     * The Current scale value
     */
    private float mScale;

    public WearListItem(Context context) {
        super(context);
    }

    public WearListItem(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public WearListItem(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        // We get the reference to the items into the layout
        mIcon = (ImageView) findViewById(R.id.icon_item);
        mName = (TextView) findViewById(R.id.text_item);
    }

    @Override
    public float getProximityMinValue() {
        return 1.0f;
    }

    @Override
    public float getProximityMaxValue() {
        return 1.4f;
    }

    @Override
    public float getCurrentProximityValue() {
        return mScale;
    }

    @Override
    public void setScalingAnimatorValue(float scale) {
        mScale = scale;
        mName.setScaleX(mScale);
        mName.setScaleY(mScale);
        mIcon.setScaleX(mScale);
        mIcon.setScaleY(mScale);
    }

    @Override
    public void onScaleUpStart() {
        mName.setTextColor(Color.RED);
    }

    @Override
    public void onScaleDownStart() {
        mName.setTextColor(Color.GREEN);
    }
}

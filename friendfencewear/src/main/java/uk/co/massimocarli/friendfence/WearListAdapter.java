package uk.co.massimocarli.friendfence;

import android.content.Context;
import android.support.wearable.view.WearableListView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by Massimo Carli on 22/10/14.
 */
public class WearListAdapter extends WearableListView.Adapter {

    /**
     * The Reference to the Context
     */
    private final Context mContext;

    /**
     * The reference to the Inflater given the Context
     */
    private LayoutInflater mLayoutInflater;

    /**
     * The Model for this Adapter
     */
    private final String[] mModel;

    /**
     * This is the implementation  of the ViewHolder as an implementation of the Holder Pattern
     * that now is explicit
     */
    public static class ItemViewHolder extends WearableListView.ViewHolder {

        /**
         * The TextView for the Text
         */
        private TextView mTextView;

        /**
         * The ImageView reference
         */
        private ImageView mImageView;

        /**
         * We create all the item from the View
         *
         * @param itemView The View to recycle
         */
        public ItemViewHolder(View itemView) {
            super(itemView);
            // We recycle the reference to the TextView and ImageView
            mTextView = (TextView) itemView.findViewById(R.id.text_item);
            mImageView = (ImageView) itemView.findViewById(R.id.icon_item);
        }
    }

    /**
     * Creates a WearListAdapter with the given Model
     *
     * @param context The Context
     * @param model   The Model
     */
    public WearListAdapter(Context context, String[] model) {
        mContext = context;
        mModel = model;
        mLayoutInflater = LayoutInflater.from(context);
    }

    @Override
    public WearableListView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Here we create the Holder starting from the View. In out case we don't have different
        // types so we ignore the viewType parameter
        final View inflatedViewItem = mLayoutInflater.inflate(R.layout.wearable_list_item, null);
        return new ItemViewHolder(inflatedViewItem);
    }

    @Override
    public void onBindViewHolder(WearableListView.ViewHolder viewHolder, int position) {
        // Here we have to bind the data to the holder
        ItemViewHolder itemHolder = (ItemViewHolder) viewHolder;
        // We show data into the items
        itemHolder.mTextView.setText(mModel[position]);
        itemHolder.mImageView.setImageResource(R.drawable.ic_launcher);
        // Every holder has the reference to the complete View. We use this to store
        // the current position into it so we can get later when we select the item itself
        itemHolder.itemView.setTag(position);
    }

    @Override
    public int getItemCount() {
        // We return the length of the model
        if (mModel != null) {
            return mModel.length;
        } else {
            return 0;
        }
    }

}

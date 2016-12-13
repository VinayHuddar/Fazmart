package com.fazmart.androidapp.View.Common;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;

import com.fazmart.androidapp.R;

/**
 * Created by poliveira on 24/10/2014.
 */
public class FilterDrawerAdapter extends RecyclerView.Adapter<FilterDrawerAdapter.ViewHolder> {

    private String[] mData;
    private FilterDrawerCallbacks mFilterDrawerCallbacks;
    private int mSelectedPosition;
    private int mTouchedPosition = -1;

    public FilterDrawerAdapter(String[] data) {
        mData = data;
    }

    public FilterDrawerCallbacks getFilterDrawerCallbacks() {
        return mFilterDrawerCallbacks;
    }

    public void setFilterDrawerCallbacks(FilterDrawerCallbacks filterDrawerCallbacks) {
        mFilterDrawerCallbacks = filterDrawerCallbacks;
    }

    @Override
    public FilterDrawerAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.filter_drawer_row, viewGroup, false);
        final ViewHolder viewholder = new ViewHolder(v);
        viewholder.itemView.setOnTouchListener(new View.OnTouchListener() {
                                                   @Override
                                                   public boolean onTouch(View v, MotionEvent event) {

                                                       switch (event.getAction()) {
                                                           case MotionEvent.ACTION_DOWN:
                                                               touchPosition(viewholder.getAdapterPosition());
                                                               return false;
                                                           case MotionEvent.ACTION_CANCEL:
                                                               touchPosition(-1);
                                                               return false;
                                                           case MotionEvent.ACTION_MOVE:
                                                               return false;
                                                           case MotionEvent.ACTION_UP:
                                                               touchPosition(-1);
                                                               return false;
                                                       }
                                                       return true;
                                                   }
                                               }
        );
        viewholder.itemView.setOnClickListener(new View.OnClickListener() {
                                                   @Override
                                                   public void onClick(View v) {
                                                       if (mFilterDrawerCallbacks != null)
                                                           mFilterDrawerCallbacks.onFilterDrawerItemSelected(viewholder.getAdapterPosition());
                                                   }
                                               }
        );
        return viewholder;
    }

    @Override
    public void onBindViewHolder(FilterDrawerAdapter.ViewHolder viewHolder, final int i) {
        viewHolder.checkedTextView.setText(mData[i]);

        //TODO: selected menu position, change layout accordingly
        if (mSelectedPosition == i || mTouchedPosition == i) {
            viewHolder.itemView.setBackgroundColor(viewHolder.itemView.getContext().getResources().getColor(R.color.lighter_grey));
        } else {
            viewHolder.itemView.setBackgroundColor(Color.TRANSPARENT);
        }
    }

    private void touchPosition(int position) {
        int lastPosition = mTouchedPosition;
        mTouchedPosition = position;
        if (lastPosition >= 0)
            notifyItemChanged(lastPosition);
        if (position >= 0)
            notifyItemChanged(position);
    }

    public void selectPosition(int position) {
        int lastPosition = mSelectedPosition;
        mSelectedPosition = position;
        notifyItemChanged(lastPosition);
        notifyItemChanged(position);
    }

    @Override
    public int getItemCount() {
        return mData != null ? mData.length : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public CheckedTextView checkedTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            checkedTextView = (CheckedTextView) itemView.findViewById(R.id.item_name);
        }
    }
}

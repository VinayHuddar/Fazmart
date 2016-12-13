package com.fazmart.androidapp.View.Common;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.fazmart.androidapp.R;

import java.util.List;

/**
 * Created by poliveira on 24/10/2014.
 */
public class BreadCrumbAdapter extends RecyclerView.Adapter<BreadCrumbAdapter.ViewHolder> {

    private List<String> mData;
    private BreadcrumbAdapterCallbacks mBreadcrumbAdapterCallbacks;
    private int mSelectedPosition;
    private int mTouchedPosition = -1;

    Activity mParentActivity;
    boolean mIsSearch;

    public BreadCrumbAdapter(List<String> data, Activity parentActivity, boolean isSearch) {
        mData = data;
        mParentActivity = parentActivity;

        mIsSearch = isSearch;
    }

    public BreadcrumbAdapterCallbacks getBreadcrumbAdapterCallbacks() {
        return mBreadcrumbAdapterCallbacks;
    }

    public void setBreadcrumbAdapterCallbacks(BreadcrumbAdapterCallbacks BreadcrumbAdapterCallbacks) {
        mBreadcrumbAdapterCallbacks = BreadcrumbAdapterCallbacks;
    }

    @Override
    public BreadCrumbAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.breadcrumbitem, viewGroup, false);
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
                                                       if (mBreadcrumbAdapterCallbacks != null)
                                                           mBreadcrumbAdapterCallbacks.onBreadcrumbItemSelected(viewholder.getAdapterPosition());
                                                   }
                                               }
        );

        if (mIsSearch)
            viewholder.textView.setTextSize(14);

        return viewholder;
    }

    @Override
    public void onBindViewHolder(BreadCrumbAdapter.ViewHolder viewHolder, final int i) {
        viewHolder.textView.setText(mData.get(i));
        if (i == (mData.size() - 1)) {
            viewHolder.textView.setTextColor(mParentActivity.getResources().getColor(R.color.white));
            viewHolder.textView.setTextSize(18);
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
        return mData != null ? mData.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView textView;

        public ViewHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(R.id.category_name);
        }
    }
}

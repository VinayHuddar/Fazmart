package com.fazmart.androidapp.View.Common;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.fazmart.androidapp.R;

/**
 * Created by vinayhuddar on 05/07/15.
 */

public class BreadCrumbAdapter_old extends BaseAdapter {
    Activity mActivity = null;
    String[] mCategoryNames = null;

    public BreadCrumbAdapter_old(Activity activity, String[] catNames) {
        mActivity = activity;
        mCategoryNames = catNames;
    }

    @Override
    public int getCount() {
        return mCategoryNames.length;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View retval = LayoutInflater.from(parent.getContext()).inflate(R.layout.breadcrumbitem, null);
        TextView catName = (TextView) retval.findViewById(R.id.title);
        catName.setText(mCategoryNames[position]);
        if (position == mCategoryNames.length-1)
            catName.setTextColor(mActivity.getResources().getColor(R.color.burgundy_dark));

        return retval;
    }
}
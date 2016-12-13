package com.fazmart.androidapp.Controller;

import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseExpandableListAdapter;
import android.widget.GridView;
import android.widget.TextView;

import com.fazmart.androidapp.Model.OrderSummaryData;
import com.fazmart.androidapp.R;
import com.fazmart.androidapp.View.Checkout.OrderSummaryActivity;

/**
 * Created by Vinay on 19-06-2015.
 */
public class ItemsSummaryAdapter extends BaseExpandableListAdapter{
    Activity mOrderSummaryActivity;
    OrderSummaryData mOrderSummary;

    public ItemsSummaryAdapter(Activity activity) {
        mOrderSummaryActivity = activity;
        //mOrderSummary = ReceivedOrderSummaryData.GetInstance().GetOrderSummaryData();
        mOrderSummary = ((OrderSummaryActivity)mOrderSummaryActivity).GetOrderSummaryData();
    }

    public long getChildId(int groupPosition, int childPosition) {
        return mOrderSummary.GetItemId(groupPosition, childPosition);
    }

    public int getChildrenCount(int groupPosition) {
        return mOrderSummary.GetItemCount(groupPosition);
    }

    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    public Object getGroup(int groupPosition) {
        return mOrderSummary.GetCategoryName(groupPosition);
    }

    public Object getChild(int groupPosition, int childPosition) {
        return mOrderSummary.GetItem(groupPosition, childPosition);
    }

    public View getGroupView(int groupPosition, boolean isExpanded, View convertView,
                             ViewGroup parent) {
        // Layout parameters for the ExpandableListView
        AbsListView.LayoutParams lp = new AbsListView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 64);

        TextView textView = new TextView(mOrderSummaryActivity);
        textView.setLayoutParams(lp);
        // Center the text vertically
        textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
        // Set the text starting position
        textView.setPadding(60, 0, 0, 0);
        textView.setText(getGroup(groupPosition).toString());
        //textView.setTypeface(Typeface.DEFAULT_BOLD);
        textView.setTextColor(mOrderSummaryActivity.getResources().getColor(android.R.color.black));
        textView.setBackgroundColor(mOrderSummaryActivity.getResources().getColor(R.color.light_blue));
        return textView;
    }

    public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
                                View convertView, ViewGroup parent) {
        final OrderSummaryData.Product currItem = mOrderSummary.GetItem(groupPosition, childPosition);
        LayoutInflater inflater = (LayoutInflater) mOrderSummaryActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View gridItem;
        if (convertView == null) {
            gridItem = inflater.inflate(R.layout.item_summary_data_layout, null);
        } else {
            gridItem = (View) convertView;
        }

        TextView productTitle = (TextView) gridItem.findViewById(R.id.item_summary_name);
        String label;
        if (currItem.GetManufacturer().compareTo("") != 0)
            label = String.format("%s: %s", currItem.GetManufacturer(), currItem.GetTitle());
        else
            label = currItem.GetTitle();
        productTitle.setText(label);

        // Setup options menu
        final TextView option = (TextView) gridItem.findViewById(R.id.item_summary_option);
        option.setText(currItem.GetOptionValue());

        final TextView quantityTV = (TextView) gridItem.findViewById(R.id.item_summary_quantity);
        quantityTV.setText("Qty: ".concat(String.valueOf(currItem.GetQuantity())));

        TextView priceTV = (TextView) gridItem.findViewById(R.id.item_summary_price);
        if (currItem.GetSpecialPrice() != null) {
            String specialPrice = currItem.GetSpecialPrice().substring(1).replace(",", "");
            priceTV.setText(String.format("\u20B9 %.2f", Double.parseDouble(specialPrice) * currItem.GetQuantity()));
        } else {
            // Set current price
            String price = currItem.GetPrice().substring(1).replace(",", "");
            priceTV.setText(String.format("\u20B9 %.2f", Double.parseDouble(price) * currItem.GetQuantity()));
        }

        gridItem.setLayoutParams(new GridView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        return gridItem;
    }

    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
    public int getGroupCount() {
        return mOrderSummary.GetCategoryCount();
    }
    public boolean hasStableIds() {
        return true;
    }
}

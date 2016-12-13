package com.fazmart.androidapp.View.Checkout;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import com.fazmart.androidapp.Controller.Cart.CartAdapter;
import com.fazmart.androidapp.Controller.ItemsSummaryAdapter;
import com.fazmart.androidapp.R;

/**
 * A placeholder fragment containing a simple view.
 */
public class OrderSummaryActivity_ItemsFragment extends Fragment {

    public OrderSummaryActivity_ItemsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_ordersummary_items_summary, container, false);

        // Populate Cart List View
        ExpandableListView mItemsSummaryELV = (ExpandableListView) rootView.findViewById(R.id.items_summary);
        final ItemsSummaryAdapter itemsSummaryAdapter = new ItemsSummaryAdapter(getActivity());
        //mItemsSummaryAdapter = itemsSummaryAdapter;
        mItemsSummaryELV.setAdapter(itemsSummaryAdapter);

        for (int i = 0; i < itemsSummaryAdapter.getGroupCount(); i++)
            mItemsSummaryELV.expandGroup(i);

        return rootView;

    }
}

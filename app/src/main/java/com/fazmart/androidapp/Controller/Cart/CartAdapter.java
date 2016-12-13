package com.fazmart.androidapp.Controller.Cart;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.fazmart.androidapp.Common.Events.EventItemAddedToCart;
import com.fazmart.androidapp.Controller.Common.CartUpdatesHandler;
import com.fazmart.androidapp.Controller.Common.ImageDownloader;
import com.fazmart.androidapp.Model.UserAccountData;
import com.fazmart.androidapp.Model.CartData;
import com.fazmart.androidapp.R;
import com.fazmart.androidapp.View.Checkout.DeliveryDataActivity;
import com.fazmart.androidapp.View.SignInActivity;

import de.greenrobot.event.EventBus;

/**
 * Created by Vinay on 19-06-2015.
 */
public class CartAdapter extends BaseExpandableListAdapter{
    CartController mCartController;
    Activity mCartActivity;
    private final ImageDownloader imageDownloader = new ImageDownloader();
    CartUpdatesHandler mCartUpdHndlr;

    public CartAdapter (Activity activity) {
        mCartController = CartController.GetInstance();
        mCartActivity = activity;
        mCartUpdHndlr = new CartUpdatesHandler();
    }

    public long getChildId(int groupPosition, int childPosition) {
        return mCartController.GetCartItem(groupPosition, childPosition).GetProductId();
    }

    public int getChildrenCount(int groupPosition) {
        return mCartController.GetProductCount(groupPosition);
    }

    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    public Object getGroup(int groupPosition) {
        return mCartController.GetCategoryName(groupPosition);
    }

    public Object getChild(int groupPosition, int childPosition) {
        return mCartController.GetCartItem(groupPosition, childPosition);
    }

    public View getGroupView(int groupPosition, boolean isExpanded, View convertView,
                             ViewGroup parent) {
        // Layout parameters for the ExpandableListView
        AbsListView.LayoutParams lp = new AbsListView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 64);

        TextView textView = new TextView(mCartActivity);
        textView.setLayoutParams(lp);
        // Center the text vertically
        textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
        // Set the text starting position
        textView.setPadding(60, 0, 0, 0);
        textView.setText(getGroup(groupPosition).toString());
        textView.setTextColor(mCartActivity.getResources().getColor(R.color.black));
        textView.setBackgroundColor(mCartActivity.getResources().getColor(R.color.burgundy_lighter_than_lightest));
        return textView;
    }

    public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
                                View convertView, ViewGroup parent) {
        final CartData.Product currProduct = mCartController.GetCartItem(groupPosition, childPosition);
        LayoutInflater inflater = (LayoutInflater) mCartActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View gridItem;
        if (convertView == null) {
            gridItem = inflater.inflate(R.layout.cart_product_data_layout, null);
        } else {
            gridItem = (View) convertView;
        }
        TextView productTitle = (TextView) gridItem.findViewById(R.id.ProductTitle_Cart);
        String label;
        if (currProduct.GetManufacturer().compareTo("") != 0)
            label = String.format("%s: %s", currProduct.GetManufacturer(), currProduct.GetTitle());
        else
            label = currProduct.GetTitle();
        productTitle.setText(label);
        productTitle.setLines(2);

        final ImageView imageView = (ImageView) gridItem.findViewById(R.id.ProductImage_Cart);
        String imgURL = currProduct.GetThumbImageURL();
        if (imgURL == null)
            imageView.setImageResource(R.drawable.fazmart_new_logo);
        else
            imageDownloader.download(imgURL, imageView);

        // Set Item Prices
        UpdateItemPriceViews(gridItem, currProduct);

        // Setup options menu
        final TextView selectedOption = (TextView) gridItem.findViewById(R.id.ProductOptionsMenu_Cart);
        selectedOption.setText(currProduct.GetOptionValue());

        final TextView quantityTV = (TextView) gridItem.findViewById(R.id.ProductQuantity_Cart);
        quantityTV.setText(String.valueOf(currProduct.GetQuantity()));

        // Handle Quantity Increment/Decrement
        TextView qntyDecr = (TextView) gridItem.findViewById(R.id.QtyDecr_Cart);
        qntyDecr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Integer.parseInt(quantityTV.getText().toString()) > 1) {
                    mCartUpdHndlr.PutProduct(currProduct.GetProductId(), currProduct.GetProductOptionValueId(), -1);
                }
            }
        });

        TextView qntyIncr = (TextView) gridItem.findViewById(R.id.QtyIncr_Cart);
        qntyIncr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCartUpdHndlr.PutProduct(currProduct.GetProductId(), currProduct.GetProductOptionValueId(), 1);
            }
        });

        // Handle remove from cart
        ImageView removeFromCartIV = (ImageView)gridItem.findViewById(R.id.remove_item_from_cart);
        removeFromCartIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCartUpdHndlr.PutProduct(currProduct.GetProductId(), currProduct.GetProductOptionValueId(), -currProduct.GetQuantity());
            }
        });


        gridItem.setLayoutParams(new GridView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 180));
        return gridItem;
    }

    public void EventBusRegister () {
        EventBus.getDefault().register(this);
    }

    public void EventBusUnregister () {
        EventBus.getDefault().unregister(this);
    }

    public void onEvent(EventItemAddedToCart evt) {
        if (mCartController.IsCartEmpty()) {
            mCartActivity.findViewById(R.id.empty_cart_view).setVisibility(View.VISIBLE);
            TextView delChargeTV = (TextView)mCartActivity.findViewById((R.id.delivery_charge_note_empty_cart));
            delChargeTV.setText(mCartController.GetDeliveryChargeNote());
            mCartActivity.findViewById(R.id.ProductList_Cart).setVisibility(View.INVISIBLE);
        } else {
            notifyDataSetChanged();
            UpdateFooter();
        }
    }


    public void UpdateFooter () {
        // Handle Checkout button
        Button checkoutButton = (Button)mCartActivity.findViewById(R.id.checkout_button);
        checkoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // A non-null account data means that the user has already logged-in
                if (UserAccountData.GetInstance().GetAccountData(mCartActivity) != null) {
                    Intent intent = new Intent(mCartActivity, DeliveryDataActivity.class);
                    mCartActivity.startActivity(intent);
                } else {
                    Intent intent = new Intent(mCartActivity, SignInActivity.class);
                    mCartActivity.startActivity(intent);
                }
            }
        });

        // Set labels
        ((TextView) mCartActivity.findViewById(R.id.subtotal_label)).setText(mCartController.GetSubTotalLabel().concat(" :"));
        ((TextView) mCartActivity.findViewById(R.id.delivery_charges_label)).setText("Delivery Fee:"); //mCartController.GetDeliveryCharges()); // intentionally hard coded since the received label can't be accommodated in the space available
        ((TextView) mCartActivity.findViewById(R.id.total_label)).setText(mCartController.GetTotalLabel().concat(" :"));
        ((TextView) mCartActivity.findViewById(R.id.savings_label)).setText("Savings :"); // Label not available in API

        // Set values
        ((TextView) mCartActivity.findViewById(R.id.subtotal)).setText(mCartController.GetSubTotal());
        ((TextView) mCartActivity.findViewById(R.id.delivery_charges)).setText(mCartController.GetDeliveryCharges());
        ((TextView) mCartActivity.findViewById(R.id.total)).setText(mCartController.GetTotal());
        if (mCartController.GetSavings() != null)
            ((TextView) mCartActivity.findViewById(R.id.savings)).setText(mCartController.GetSavings());
        else {
            ((TextView) mCartActivity.findViewById(R.id.savings_label)).setVisibility(View.INVISIBLE);
            ((TextView) mCartActivity.findViewById(R.id.savings)).setVisibility(View.INVISIBLE);
        }

        ((TextView) mCartActivity.findViewById(R.id.delivery_charge_note)).setText(mCartController.GetDeliveryChargeNote());
    }

    void UpdateItemPriceViews (View gridItem, CartData.Product currProduct) {
        TextView oldPriceTV = (TextView) gridItem.findViewById(R.id.ProductOldPrice_Cart);
        TextView currentPriceTV = (TextView) gridItem.findViewById(R.id.ProductCurrentPrice_Cart);

        if (currProduct.GetSpecialPrice() != null) {
            String price = currProduct.GetPrice().substring(1).replace(",", "");
            String specialPrice = currProduct.GetSpecialPrice().substring(1).replace(",", "");

            // Set Old Price
            oldPriceTV.setPaintFlags(oldPriceTV.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            oldPriceTV.setText(String.format("\u20B9 %.2f", Double.parseDouble(price) * currProduct.GetQuantity()));

            // Set Special Price
            currentPriceTV.setText(String.format("\u20B9 %.2f", Double.parseDouble(specialPrice) * currProduct.GetQuantity()));
        } else {
            // Set current price
            String price = currProduct.GetPrice().substring(1).replace(",", "");
            currentPriceTV.setText(String.format("\u20B9 %.2f", Double.parseDouble(price) * currProduct.GetQuantity()));
        }
    }

    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
    public int getGroupCount() {
        return mCartController.GetCategoryCount();
    }
    public boolean hasStableIds() {
        return true;
    }
}

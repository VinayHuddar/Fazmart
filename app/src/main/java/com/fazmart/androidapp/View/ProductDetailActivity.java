package com.fazmart.androidapp.View;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.fazmart.androidapp.Common.Events.EventItemAddedToCart;
import com.fazmart.androidapp.Common.Events.EventProductDetailsFetched;
import com.fazmart.androidapp.Controller.Cart.CartController;
import com.fazmart.androidapp.Controller.Common.ImageDownloader;
import com.fazmart.androidapp.Model.ProductData.ProductDetailData;
import com.fazmart.androidapp.Controller.ProductDetailDataController;
import com.fazmart.androidapp.R;

import de.greenrobot.event.EventBus;

public class ProductDetailActivity extends Activity {

    ProductDetailDataController mProductDetailDataController;
    ProductDetailDataController.ProductDetailData mCurrentProductData;
    Context mContext;
    Activity mActivity;
    ImageDownloader imageDownloader;
    int mQuantity;

    CartController mCartController;

    public ProductDetailActivity() {
        mProductDetailDataController = ProductDetailDataController.GetInstance();
        mCartController = CartController.GetInstance();

        imageDownloader = new ImageDownloader();
        mContext = mActivity = this;

        mCurrentProductData = null;
        mQuantity = 0;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail_screen);
        this.setTitle("");

        Intent intent = getIntent();
        int productId = intent.getIntExtra("PRODUCT_ID", 0);
        int categoryId = intent.getIntExtra("CATEGORY_ID", 0);
        int optionIdx = intent.getIntExtra("CURRENT_OPTION_INDEX", 0);

        findViewById(R.id.loading_message).setVisibility(View.VISIBLE);
        findViewById(R.id.product_details).setFadingEdgeLength(View.INVISIBLE);

        mProductDetailDataController.FetchProductDetailData(categoryId, productId, optionIdx);
    }

    public void onEvent(EventProductDetailsFetched evt) {
        mCurrentProductData = mProductDetailDataController.GetCurrentProductData();

        findViewById(R.id.loading_message).setVisibility(View.GONE);
        findViewById(R.id.product_details).setFadingEdgeLength(View.VISIBLE);

        FillupProductDetailViews();
    }

    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    private void FillupProductDetailViews() {
        final int currOption = mCurrentProductData.GetOptionSelection();
        final ProductDetailData.Product productStaticData = mCurrentProductData.GetProductDetailData();

        // Product Title
        String title;
        if (productStaticData.GetManufacturer() != null)
            title = String.format("%s: %s", productStaticData.GetManufacturer(), productStaticData.GetTitle());
        else
            title = productStaticData.GetTitle();
        TextView titleView = (TextView) findViewById(R.id.ProductName_DetailView);
        titleView.setText(title);

        // Hide the quantity-in-cart and remove-from-cart views until at-least 1 of this item is
        // added to the cart
        ((TextView) findViewById(R.id.ItemsInCart_DetailView)).setVisibility(View.INVISIBLE);
        ((ImageView) findViewById(R.id.RemoveFromCart_DetailView)).setVisibility(View.INVISIBLE);

        // Product Image
        final ImageView imageView = (ImageView) findViewById(R.id.ProductImage_DetailView);
        String imgURL = productStaticData.GetImage();
        if (imgURL.compareTo("null") != 0)
            imageDownloader.download(imgURL, imageView);
        else
            imageView.setImageResource(R.drawable.fazmart_new_logo);

        // Product Options
        final TextView optionsMenu = (TextView) findViewById(R.id.ProductOptions_DetailView);
        optionsMenu.setText(productStaticData.GetPrimaryOptionName(currOption)); //ProductDetailDataController.DEFAULT_OPTION));

        optionsMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(mContext, v);
                int optionCnt = productStaticData.GetPrimaryOptionsCount();
                for (int i = 0; i < optionCnt; i++)
                    popup.getMenu().add(1, i, i, productStaticData.GetPrimaryOptionName(i));
                popup.show();

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        int optionIdx = item.getItemId();

                        // Show selected option
                        optionsMenu.setText(productStaticData.GetPrimaryOptionName(optionIdx));
                        mCurrentProductData.SetOptionSelection(optionIdx);

                        UpdateOptionBasedViews();

                        // Replace existing image with option image, if one exists
                        String imgURL = productStaticData.GetPrimaryOptionImage(optionIdx);
                        if (imgURL != null) {
                            imageDownloader.download(imgURL, imageView);
                        }

                        return true;
                    }
                });
            }
        });

        // Set default quantity to 1
        final TextView quantity = (TextView) findViewById(R.id.ProductQuantity_DetailView);
        mQuantity = 1;
        quantity.setText("1");

        // Handle Quantity Increment/Decrement
        ImageView qntyDecr = (ImageView) findViewById(R.id.QtyDecr_DetailView);
        qntyDecr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mQuantity > 0)
                    --mQuantity;
                quantity.setText(String.valueOf(mQuantity));
            }
        });
        ImageView qntyIncr = (ImageView) findViewById(R.id.QtyIncr_DetailView);
        qntyIncr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mQuantity++;
                quantity.setText(String.valueOf(mQuantity));
            }
        });

        // Handle Add To Wish List
        final LinearLayout addToWishList = (LinearLayout) findViewById(R.id.AddToWishList_DetailView);
        final ImageView addToWishListImage = (ImageView) findViewById(R.id.AddToWishListImage_DetailView);
        if (!mCurrentProductData.IsAddedToWishList()) {
            addToWishListImage.setImageResource(R.drawable.wishlist_icon);
        } else {
            addToWishListImage.setImageResource(R.drawable.wishlist_icon_red);
        }
        addToWishList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg;
                if (mCurrentProductData.IsAddedToWishList()) {
                    mCurrentProductData.RemoveFromWishList();
                    addToWishListImage.setImageResource(R.drawable.wishlist_icon);
                    msg = String.format("\"%s\" - removed from Wish List", productStaticData.GetTitle());
                } else {
                    mCurrentProductData.AddToWishList();
                    addToWishListImage.setImageResource(R.drawable.wishlist_icon_red);
                    msg = String.format("\"%s\" - added to Wish List", productStaticData.GetTitle());
                }

                // Show added-to/removed-from wish list message
                Toast toast = Toast.makeText(mContext, msg, Toast.LENGTH_SHORT);
                LinearLayout toastLayout = (LinearLayout) toast.getView();
                TextView toastTV = (TextView) toastLayout.getChildAt(0);
                toastTV.setTextSize(18);
                toast.show();
            }
        });

        UpdateOptionBasedViews();
    }

    void UpdateOptionBasedViews() {
        final int currOption = mCurrentProductData.GetOptionSelection();
        final ProductDetailData.Product productStaticData = mCurrentProductData.GetProductDetailData();

        // Product Prices
        final TextView oldPriceTV = (TextView) findViewById(R.id.ProductOldPrice_DetailView);
        final TextView currentPriceTV = (TextView) findViewById(R.id.ProductCurrentPrice_DetailView);
        final TextView savingsTV = (TextView) findViewById(R.id.ProductSavings_DetailView);
        if (productStaticData.GetPrimaryOptionSpecialPrice(currOption) != null) {
            // Set Old Price
            oldPriceTV.setPaintFlags(oldPriceTV.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            oldPriceTV.setText(productStaticData.GetPrimaryOptionPrice(currOption));

            // Set Special Price
            currentPriceTV.setText(productStaticData.GetPrimaryOptionSpecialPrice(currOption));

            // Set Savings
            Double savedAmt = Double.parseDouble(productStaticData.GetPrimaryOptionPrice(currOption).substring(1).replace(",", "")) -
                    Double.parseDouble(productStaticData.GetPrimaryOptionSpecialPrice(currOption).substring(1).replace(",", ""));
            savingsTV.setText(String.format("(save \u20B9 %.2f)", savedAmt));
        } else {
            // Set current price
            currentPriceTV.setText(productStaticData.GetPrimaryOptionPrice(currOption));
        }

        // Defining in advance since it is used in Add-to-cart processing
        final ImageView removeFromCart = (ImageView) findViewById(R.id.RemoveFromCart_DetailView);

        // Print quantity in cart
        final TextView itemsInCartView = (TextView) findViewById(R.id.ItemsInCart_DetailView);
        if (mCurrentProductData.IsPresentInCart(currOption)) {
            // Show ItemsInCart View, and in it, the number of units of the current product in the cart
            itemsInCartView.setVisibility(View.VISIBLE);
            itemsInCartView.setText(String.format("%d in Cart", mCurrentProductData.GetQuantityInCart(currOption)));
        } else {
            // There are no items in the Cart - don't show the ItemsInCart views
            itemsInCartView.setVisibility(View.INVISIBLE);
        }

        final ImageView removeFromCartView = (ImageView) findViewById(R.id.RemoveFromCart_DetailView);
        final TextView addToCart = (TextView) findViewById(R.id.AddToCart_DetailView);
        addToCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mQuantity != 0) {
                    mProductDetailDataController.AddToCart(mQuantity, currOption);
                }
            }
        });

        // Handle remove-from-cart
        if (mCurrentProductData.IsPresentInCart(currOption)) {
            // Show the RemoveFromCart View
            removeFromCartView.setVisibility(View.VISIBLE);
        } else {
            removeFromCartView.setVisibility(View.INVISIBLE);
        }

        removeFromCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProductDetailDataController.RemoveFromCart(currOption);
                if (mCurrentProductData.IsPresentInCart(currOption) == false) {
                    removeFromCart.setVisibility(View.INVISIBLE);
                    itemsInCartView.setVisibility(View.INVISIBLE);
                } else {
                    itemsInCartView.setText(String.format("%d in Cart", mCurrentProductData.GetQuantityInCart(currOption)));
                }

                // Show added-to/removed-from cart message
                String msg = String.format("One \"%s\" item removed from the cart", productStaticData.GetTitle());
                Toast toast = Toast.makeText(mContext, msg, Toast.LENGTH_SHORT);
                LinearLayout toastLayout = (LinearLayout) toast.getView();
                TextView toastTV = (TextView) toastLayout.getChildAt(0);
                toastTV.setTextSize(18);
                toast.show();
            }
        });
    }

    public void onEvent(EventItemAddedToCart evt) {
        //mCurrentProductData.UpdateQuantityInCart(optIdx, qty);
        mProductDetailDataController.GetCurrentProductData().UpdateQuantityInCart(evt.GetProdOptValId(), evt.GetQuantity());

        TextView itemsInCartView = (TextView) findViewById(R.id.ItemsInCart_DetailView);
        ImageView removeFromCartView = (ImageView) findViewById(R.id.RemoveFromCart_DetailView);

        int qty = mCurrentProductData.GetQuantityInCart(mCurrentProductData.GetOptionSelection());
        if (mCurrentProductData.GetQuantityInCart(mCurrentProductData.GetOptionSelection())  != 0) {
            itemsInCartView.setVisibility(View.VISIBLE);
            itemsInCartView.setText(String.format("%d in Cart", qty));
            removeFromCartView.setVisibility(View.VISIBLE);
        } else {
            itemsInCartView.setVisibility(View.INVISIBLE);
            removeFromCartView.setVisibility(View.INVISIBLE);
        }

        // Show added-to-cart message
        /*String msg = String.format("\"%s\" - added to Cart", mCurrentProductData.GetTitle());
        Toast toast = Toast.makeText(mContext, msg, Toast.LENGTH_SHORT);
        LinearLayout toastLayout = (LinearLayout) toast.getView();
        TextView toastTV = (TextView) toastLayout.getChildAt(0);
        toastTV.setTextSize(18);
        toast.show();*/
    }
}

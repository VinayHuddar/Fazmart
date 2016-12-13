package com.fazmart.androidapp.Controller.ProductList;

import android.content.Context;
import android.graphics.Paint;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.fazmart.androidapp.Common.Events.EventItemAddedToCart;
import com.fazmart.androidapp.Common.Events.EventOpenDetailView;
import com.fazmart.androidapp.Controller.Cart.CartController;
import com.fazmart.androidapp.Controller.Common.CartUpdatesHandler;
import com.fazmart.androidapp.Controller.Common.ImageDownloader;
import com.fazmart.androidapp.Model.ProductData.ProductData;
import com.fazmart.androidapp.R;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by Vinay on 22-05-2015.
 */
public class ProductListAdapter extends ArrayAdapter implements QuantityUpdateCallbacks {
    Context mContext;

    ArrayList<ProductListManager.ProductDataForListView> mProductList;
    int mCategoryId;
    ProductListManager mProductListManager;
    CartController mCartController;
    int mTotalProdCount;
    int mNumProdsToDisplay;
    boolean mShowAllItems;
    int[] mProdPosList;

    ProductListAdapter mThisAdapter;
    CartUpdatesHandler mAPIHandler;

    public ProductListAdapter(Context context, int resource,
                              ArrayList<ProductListManager.ProductDataForListView> productList,
                              int categoryId) {
        super(context, resource);

        mThisAdapter = this;
        mProductList = productList;
        mContext = context;
        mCategoryId = categoryId;
        mProductListManager = ProductListManager.GetInstance();
        mCartController = CartController.GetInstance();
        mAPIHandler = new CartUpdatesHandler();
        mAPIHandler.setQuantityUpdateCallbacks(this);

        mTotalProdCount = mNumProdsToDisplay = mProductList.size();

        mShowAllItems = true;
        mProdPosList = null;

        EventBus.getDefault().register(this);
    }

    public void ShowAllItems() {
        mShowAllItems = true;
        mNumProdsToDisplay = mTotalProdCount;
        mProdPosList = null;
    }

    public void FilterProducts(int pNumProdsToDisplay, int[] pProdPosList) {
        mShowAllItems = false;
        mNumProdsToDisplay = pNumProdsToDisplay;
        mProdPosList = pProdPosList;
    }

    public int getCount() {
        //return mProductList.length;
        return mNumProdsToDisplay;
    }

    public void UpdateProductCount(int newCnt) {
        mNumProdsToDisplay = newCnt;
        mTotalProdCount = newCnt;
    }

    public ProductListManager.ProductDataForListView getItem(int position) {
        int currProdIdx = (mProdPosList != null) ? mProdPosList[position] : position;
        return (mProductList.get(currProdIdx));
    }

    public long getItemId(int position) {
        int posInList = (mProdPosList != null) ? mProdPosList[position] : position;
        return mProductList.get(posInList).GetProductId();
    }

    private final ImageDownloader imageDownloader = new ImageDownloader();

    class ProductState {
        int prodId;
        int optionValId;
        View listItem;
        ProductListManager.ProductDataForListView prodData;

        public ProductState (int _prodId, int _optionIdx, View _listItem, ProductListManager.ProductDataForListView _prodData) {
            prodId = _prodId;
            optionValId = _optionIdx;
            listItem = _listItem;
            prodData = _prodData;
        }

        int GetProdId () { return prodId; }
        int GetOptionValId () { return optionValId; }
        View GetListItem () { return listItem; }
        ProductListManager.ProductDataForListView GetProdData () { return prodData; }
    }
    List<ProductState> itemsPendingCartUpdate = new ArrayList<ProductState>();

    int currOption;

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        final int pos = position;
        final int currProdPos = mShowAllItems ? position : mProdPosList[position];
        final ProductListManager.ProductDataForListView currProduct = mProductList.get(currProdPos);
        currOption = currProduct.GetOptionSelection();

        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View listItem;
        if (convertView == null) {
            listItem = inflater.inflate(R.layout.list_view_product_data_layout, null);
        } else {
            listItem = (View) convertView;
        }

        TextView productTitle = (TextView) listItem.findViewById(R.id.ProductTitle);
        String label;
        if (currProduct.GetManufacturer() != null)
            label = String.format("%s: %s", currProduct.GetManufacturer(), currProduct.GetName());
        else
            label = currProduct.GetName();
        productTitle.setText(label);

        final ImageView imageView = (ImageView) listItem.findViewById(R.id.ProductImage);
        //imageView.setBackgroundResource(R.drawable.fazmart_new_logo);
        String imgURL = currProduct.GetImageURL();
        if (imgURL == null) // Needs investigation; dig into imageDownloader to understand how it handles null URLs
            imgURL = "null";
        imageDownloader.download(imgURL, imageView);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.getDefault().post(new EventOpenDetailView(((int) getItemId(pos)), getItem(pos).GetOptionSelection()));
            }
        });

        // Setup options menu
        final TextView optionsMenu = (TextView) listItem.findViewById(R.id.ProductOptionsMenu);

        final ProductData prodData = currProduct.GetProductData();
        if (prodData.GetPrimaryOptionsCount() > 0)
            optionsMenu.setText(prodData.GetPrimaryOptionName(currProduct.GetOptionSelection()));

        final TextView oldPriceTV = (TextView) listItem.findViewById(R.id.ProductOldPrice);
        final TextView currentPriceTV = (TextView) listItem.findViewById(R.id.ProductCurrentPrice);
        optionsMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(mContext, v);
                int optionCnt = prodData.GetPrimaryOptionsCount();
                for (int i = 0; i < optionCnt; i++)
                    popup.getMenu().add(1, i, i, prodData.GetPrimaryOptionName(i));
                popup.show();

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        int optionIdx = item.getItemId();

                        if (optionIdx != currProduct.GetOptionSelection()) {
                            // Show selected option
                            optionsMenu.setText(prodData.GetPrimaryOptionName(optionIdx));
                            currProduct.SetOptionSelection(optionIdx);
                            currOption = optionIdx;

                            UpdateOptionBasedViews(currProduct, listItem);

                            // Replace existing image with option image, if one exists
                            String imgURL = prodData.GetPrimaryOptionImage(optionIdx);
                            if (imgURL != null) {
                                imageDownloader.download(imgURL, imageView);
                            }
                        }

                        return true;
                    }
                });
            }
        });

        // Handle Quantity Increment/Decrement
        TextView qntyDecr = (TextView) listItem.findViewById(R.id.QtyDecr);
        qntyDecr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int optIdx = currProduct.GetOptionSelection();
                RemoveProductFromCart(currProduct.GetProductId(), optIdx);
            }
        });

        TextView qntyIncr = (TextView) listItem.findViewById(R.id.QtyIncr);
        qntyIncr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int optIdx = currProduct.GetOptionSelection();
                AddToCart(currProduct.GetProductId(), optIdx);
            }
        });

        // Handle Add To Wish List
        final ImageView addToWishList = (ImageView) listItem.findViewById(R.id.AddToWishlist);
        if (!currProduct.IsAddedToWishList()) {
            addToWishList.setImageResource(R.drawable.add_to_wish_list_icon_grey);
        } else {
            addToWishList.setImageResource(R.drawable.add_to_wish_list_icon_red);
        }
        addToWishList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg;
                if (currProduct.IsAddedToWishList()) {
                    mProductListManager.RemoveProductFromWishList(mCategoryId, currProduct.GetProductId());
                    addToWishList.setImageResource(R.drawable.add_to_wish_list_icon_grey);
                    msg = String.format("\"%s\" - removed from Wish List", currProduct.GetName());
                } else {
                    mProductListManager.AddProductToWishList(mCategoryId, currProduct.GetProductId());
                    addToWishList.setImageResource(R.drawable.add_to_wish_list_icon_red);
                    msg = String.format("\"%s\" - added to Wish List", currProduct.GetName());
                }

                // Show added-to/removed-from wish list message
                Toast toast = Toast.makeText(mContext, msg, Toast.LENGTH_SHORT);
                LinearLayout toastLayout = (LinearLayout) toast.getView();
                TextView toastTV = (TextView) toastLayout.getChildAt(0);
                toastTV.setTextSize(18);
                toast.show();
            }
        });

        UpdateOptionBasedViews(currProduct, listItem);

        //listItem.setLayoutParams(new ListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return listItem;
    }

    void UpdateOptionBasedViews (ProductListManager.ProductDataForListView prod, View listItem) {
        final ProductListManager.ProductDataForListView currProduct = prod;

        // Handle quantity-in-cart
        TextView quantityInCartTV = (TextView) listItem.findViewById(R.id.QuantityInCart);
        int qty = currProduct.GetQuantityInCart(currOption);
        if (qty > 0) {
            quantityInCartTV.setText(String.format(" %d in cart ", qty));
            //quantityInCartTV.setBackgroundColor(mContext.getResources().getColor(R.color.primary_color_dark));
            //quantityInCartTV.setTextColor(mContext.getResources().getColor(R.color.white));

            quantityInCartTV.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
        }
        else {
            quantityInCartTV.setText("0");
            int width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30, mContext.getResources().getDisplayMetrics());
            quantityInCartTV.setLayoutParams(new LinearLayout.LayoutParams(width, ViewGroup.LayoutParams.MATCH_PARENT));
        }

        // Set prices
        final TextView oldPriceTV = (TextView) listItem.findViewById(R.id.ProductOldPrice);
        final TextView currentPriceTV = (TextView) listItem.findViewById(R.id.ProductCurrentPrice);
        if (currProduct.GetSpecial(currOption) != null) {
            // Set Old Price
            oldPriceTV.setPaintFlags(oldPriceTV.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            oldPriceTV.setText(currProduct.GetPrice(currOption));

            // Set Special Price
            currentPriceTV.setText(currProduct.GetSpecial(currOption));
        } else {
            // Set current price
            currentPriceTV.setText(currProduct.GetPrice(currOption));
        }
    }

    public void AddToCart(int prodId, int optIdx) {
        ProductListManager.ProductDataForListView product = mProductListManager.GetProduct(mCategoryId, prodId);

        //if ((product.GetTotalQuantityInCart() == 0) && (quantity > 0))
        //    mProductListManager.AddToLocalCartList (mCategoryId, prodId);

        if (mCartController.IsProductPresentInCart(prodId, product.GetProductOptionValueId(optIdx))) {
            mAPIHandler.PutProduct(prodId, product.GetProductOptionValueId(optIdx), 1);
        } else {
            mAPIHandler.PostProduct(prodId, product.GetProductOptionId(), product.GetProductOptionValueId(optIdx), 1);
        }
    }

    public void RemoveProductFromCart(int prodId, int optionIdx) {
        ProductListManager.ProductDataForListView product = mProductListManager.GetProduct(mCategoryId, prodId);

        //int newQty = product.GetQuantityInCart(optionIdx) - 1;
        //product.UpdateQuantity(newQty < 0 ? 0 : newQty, optionIdx);
        if (product.GetTotalQuantityInCart() == 0)
            mProductListManager.RemoveFromLocalCartList (mCategoryId, prodId);

        mAPIHandler.PutProduct(prodId, product.GetProductOptionValueId(optionIdx), -1);
    }

    public void onEvent(EventItemAddedToCart evt) {
        ProductListManager.ProductDataForListView product = mProductListManager.GetProduct(mCategoryId, evt.GetProdId());
        product.UpdateQuantity(evt.GetQuantity(), evt.GetProdOptValId(), mCategoryId);

        notifyDataSetChanged();
    }

    public void EventBusRegister () {
        EventBus.getDefault().register(this);
    }

    public void EventBusUnregister () {
        EventBus.getDefault().unregister(this);
    }

    public void onQuantityUpdated(int prodId, int prodOptValId) {
        int numItemsPendingCartUpdate = itemsPendingCartUpdate.size();
        for (int i = 0; i < numItemsPendingCartUpdate; i++) {
            ProductState productState = itemsPendingCartUpdate.get(i);
            if ((productState.GetProdId() == prodId) && (productState.GetOptionValId() == prodOptValId)) {
            }
        }
    }
}

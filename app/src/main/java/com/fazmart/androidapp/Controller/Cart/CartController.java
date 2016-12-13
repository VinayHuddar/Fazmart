package com.fazmart.androidapp.Controller.Cart;

import com.fazmart.androidapp.Common.CommonDefinitions;
import com.fazmart.androidapp.Common.Events.EventCartDataInitialized;
import com.fazmart.androidapp.FazmartApplication;
import com.fazmart.androidapp.Model.CartData;
import com.fazmart.androidapp.Model.ProductData.ProductOptionsData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import de.greenrobot.event.EventBus;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by Vinay on 15-05-2015.
 */
public class CartController {
    private static CartController instance = null;
    public static CartController GetInstance () {
        if (instance == null)
            instance = new CartController();
        return instance;
    }

    CartData mCartData = null;
    int mFetchCartRetryCnt = 0;
    private CartController() {
        mDataSetChanged = false;
        InitializeCartData();
    }
    void InitializeCartData () {
        FazmartApplication.GetAPIService().GetCart(new Callback<CartData>() {
            @Override
            public void success(CartData cartData, Response response) {
                mCartData = cartData;
                EventBus.getDefault().post(new EventCartDataInitialized());
            }

            @Override
            public void failure(RetrofitError error) {
                mFetchCartRetryCnt++;
                if (mFetchCartRetryCnt < CommonDefinitions.RETRY_COUNT)
                    InitializeCartData();
                else
                    mFetchCartRetryCnt = 0;
            }
        });

    }

    public void SetCartReference (CartData cartData) {
        mCartData = cartData;
        mDataSetChanged = true;

        RecreateHash();
    }

    HashMap<Integer, HashMap<Integer, CartData.Product>> mCartItemsHash =
            new HashMap<Integer, HashMap<Integer, CartData.Product>>();

    private void RecreateHash () {
        int catCnt = mCartData.GetCategoryCount();
        mCartItemsHash.clear();
        for (int catIdx = 0; catIdx < catCnt; catIdx++) {
            int prodCnt = mCartData.GetProductCount(catIdx);
            for (int itemIdx = 0; itemIdx < prodCnt; itemIdx++) {
                int prodId = mCartData.GetProductId(catIdx, itemIdx);
                int productOptionValueId = mCartData.GetProductOptionValueId(catIdx, itemIdx);
                if (mCartItemsHash.get(prodId) == null) {
                    HashMap<Integer, CartData.Product> items = new HashMap<Integer, CartData.Product>();
                    items.put(productOptionValueId, mCartData.GetCartItem(catIdx, itemIdx));
                    mCartItemsHash.put(prodId, items);
                } else {
                    mCartItemsHash.get(prodId).put(productOptionValueId, mCartData.GetCartItem(catIdx, itemIdx));
                }
            }
        }
    }

    public String GetItemKey(int productId, int productOptionValueId) {
        if (mCartItemsHash.containsKey(productId) && mCartItemsHash.get(productId).containsKey(productOptionValueId))
            return mCartItemsHash.get(productId).get(productOptionValueId).GetKey();
        else
            return null;
    }

    public int GetItemQuantity(int productId, int productOptionValueId) {
        int qty = 0;
        if (mCartItemsHash.containsKey(productId) && mCartItemsHash.get(productId).containsKey(productOptionValueId))
            qty = mCartItemsHash.get(productId).get(productOptionValueId).GetQuantity();

        return qty;
    }

    public CartData.Product GetCartItem(int catIdx, int itemIdx) {
        return mCartData.GetCartItem(catIdx, itemIdx);
    }

    public int GetCategoryCount () {
        return mCartData.GetCategoryCount();
    }
    public String GetCategoryName (int catIdx) {
        return mCartData.GetCategoryName(catIdx);
    }
    public int GetProductCount (int catIdx) {
        return mCartData.GetProductCount(catIdx);
    }
    public String GetSubTotalLabel () { return mCartData.GetSubTotalLabel(); }
    public String GetDeliveryChargesLabel () {
        return mCartData.GetDeliveryChargesLabel();
    }
    public String GetTotalLabel () {
        return mCartData.GetTotalLabel();
    }

    public String GetSubTotal () { return mCartData.GetSubTotal(); }
    public String GetDeliveryCharges () {
        return mCartData.GetDeliveryCharges();
    }
    public String GetTotal () {
        return mCartData.GetTotal();
    }
    public String GetSavings () {
        return mCartData.GetSavings();
    }
    public String GetDeliveryChargeNote () {
        return mCartData.GetDeliveryChargeNote();
    }

    boolean mDataSetChanged;

    public boolean WasDataSetChanged () {
        return mDataSetChanged;
    }

    public void SetDataSetChanged (boolean status) {
        mDataSetChanged = status;
    }

    public boolean IsCartEmpty () {
        return (mCartData != null) ? mCartData.GetCategoryCount() == 0 : true;
    }

    public void EmptyCart () {
        mCartData = null;
        mCartItemsHash.clear();
    }

    public boolean IsCartDataInitialized () {
        return (mCartData != null);
    }

    public Integer[] GetProductsInCart () {
        Set<Integer> prodIdSet = mCartItemsHash.keySet();
        Integer[] prodIds = new Integer[prodIdSet.size()];
        prodIdSet.toArray(prodIds);
        return (prodIds);
    }

    public boolean IsProductPresentInCart (int prodId) {
        return mCartItemsHash.containsKey(prodId);
    }

    public boolean IsProductPresentInCart (int prodId, int option) {
        boolean prodPresent = mCartItemsHash.containsKey(prodId);
        if (prodPresent) {
            prodPresent = (GetItemQuantity(prodId, option) != 0);
        }
        return prodPresent;
    }
}

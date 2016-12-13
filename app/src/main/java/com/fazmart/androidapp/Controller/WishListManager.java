package com.fazmart.androidapp.Controller;

import java.util.ArrayList;

/**
 * Created by Vinay on 15-05-2015.
 */
public class WishListManager {
    private static WishListManager instance = null;
    public static WishListManager GetInstance () {
        if (instance == null)
            instance = new WishListManager();
        return instance;
    }

    ArrayList<Integer> mWishListedItems;

    public WishListManager () {
        mWishListedItems = new ArrayList<Integer>();
    }

    public void AddToWishList (int productId) {
        if (mWishListedItems.contains(productId) == false) {
            mWishListedItems.add(productId);
        }
    }

    public void RemoveFromWishList (int productId) {
        if (mWishListedItems.contains(productId)) {
            mWishListedItems.remove(((Integer) productId));
        }
    }

    public boolean IsWishListEmpty () {
        return mWishListedItems.isEmpty();
    }

    public boolean IsProductPresentInWishList (int prodId) {
        return mWishListedItems.contains(prodId);
    }
    
    public Integer[] GetWishListedProducts () {
        Integer[] prodIds = new Integer[mWishListedItems.size()];
        mWishListedItems.toArray(prodIds);
        return (prodIds);
    }
}

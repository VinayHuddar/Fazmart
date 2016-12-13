package com.fazmart.androidapp.Common.Events;

/**
 * Created by Vinay on 19-06-2015.
 */
public class EventItemAddedToCart {
    int mProdId, mProdOptValId, mQuantity;

    public EventItemAddedToCart (int prodId, int prodOptValId, int quantity) {
        mProdId = prodId;
        mProdOptValId = prodOptValId;
        mQuantity = quantity;
    }
    public int GetProdId () { return mProdId; }
    public int GetProdOptValId () { return mProdOptValId; }
    public int GetQuantity () { return mQuantity; }
}

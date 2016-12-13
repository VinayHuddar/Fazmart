package com.fazmart.androidapp.Common.Events;

/**
 * Created by Vinay on 23-06-2015.
 */
public class EventOpenDetailView {
    int mProdId, mOptIdx;

    public EventOpenDetailView (int prodId, int optIdx) {
        mProdId = prodId;
        mOptIdx = optIdx;
    }

    public int GetProdId () { return mProdId; }
    public int GetOptIdx () { return mOptIdx; }
}

package com.fazmart.androidapp.Common.Events;

import com.fazmart.androidapp.Common.APIService;

/**
 * Created by Vinay on 08-06-2015.
 */
public class EventGetSearchResults {
    String mQueryString;
    int mPageNum;
    String mOrder;

    public EventGetSearchResults(String query, int pageNum, String order) {
        mQueryString = query;
        mPageNum = pageNum;
        mOrder = order;
    }

    public String GetQueryString () { return mQueryString; }
    public int GetPageNum () { return mPageNum; }
    public String GetOrder () { return mOrder; }
}

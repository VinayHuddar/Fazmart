package com.fazmart.androidapp.Controller;

import com.fazmart.androidapp.Model.OrderSummaryData;

/**
 * Created by vinayhuddar on 29/06/15.
 */
public class ReceivedOrderSummaryData {
    private static ReceivedOrderSummaryData instance = null;
    public static ReceivedOrderSummaryData GetInstance () {
        if (instance == null)
            instance = new ReceivedOrderSummaryData();
        return instance;
    }

    OrderSummaryData mOrderSummaryData = null;
    public void SetOrderSummaryData (OrderSummaryData osd) {
        mOrderSummaryData = osd;
    }

    public OrderSummaryData GetOrderSummaryData () {
        return mOrderSummaryData;
    }
}

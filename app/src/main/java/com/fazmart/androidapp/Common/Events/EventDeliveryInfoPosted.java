package com.fazmart.androidapp.Common.Events;

import android.app.usage.UsageEvents;

import com.fazmart.androidapp.Model.OrderSummaryData;

/**
 * Created by vinayhuddar on 27/06/15.
 */
public class EventDeliveryInfoPosted {
    OrderSummaryData mOrderSummaryData;
    public EventDeliveryInfoPosted (OrderSummaryData osd) {
        mOrderSummaryData = osd;
    }

    public OrderSummaryData GetOrderSummaryData () {
        return mOrderSummaryData;
    }
}

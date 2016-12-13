package com.fazmart.androidapp.Common.Events;

/**
 * Created by Vinay on 15-06-2015.
 */
public class EventDeliveryDatePicked {
    String mDatePicked = null;
    public EventDeliveryDatePicked (String date) {
        mDatePicked = date;
    }
    public String GetDatePicked () {
        return mDatePicked;
    }
}

package com.fazmart.androidapp.Common.Events;

import com.fazmart.androidapp.Common.APIService;

/**
 * Created by Vinay on 08-06-2015.
 */
public class EventGetAreas {
    int mCityId;

    public EventGetAreas(int cityId) {
        mCityId = cityId;
    }

    public int GetCityId() { return mCityId; }
}

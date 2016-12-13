package com.fazmart.androidapp.Controller.ProductList;

import android.widget.AbsListView;

import com.fazmart.androidapp.Common.Events.EventGetProducts;
import com.fazmart.androidapp.Common.Events.EventGetSearchResults;
import com.fazmart.androidapp.Common.APIService;

import de.greenrobot.event.EventBus;

/**
 * Created by Vinay on 10-06-2015.
 */
public class ProductListScrollListener implements AbsListView.OnScrollListener {
    final int SCROLL_LISTENER_THRESHOLD = 8;

    // how many entries earlier to start loading next page
    private int currentPage = 1;
    private int previousTotal = 0;
    private boolean loading = true;
    private int mTotalProductCount = 0;

    private String mSearchQuery = null;
    ProductListViewHelper mProductListViewHelper = null;
    APIService mAPIService = null;

    public ProductListScrollListener(ProductListViewHelper listViewHelper, APIService apiSevice) {
        mProductListViewHelper = listViewHelper;
        mAPIService = apiSevice;
    }

    public ProductListScrollListener(ProductListViewHelper listViewHelper, APIService apiSevice, String searchQuery) {
        mProductListViewHelper = listViewHelper;
        mSearchQuery = searchQuery;
        mAPIService = apiSevice;

        mProductListViewHelper.RegisterScrollListener(this);
    }

    void initializeState () {
        currentPage = 1;
        previousTotal = 0;
        loading = true;
        mTotalProductCount = 0;
    }

    // User by search activity
    public void NewSearch (String query) {
        mSearchQuery = query;
        initializeState();
    }

    public void SetTotalProductCount (int cnt) {
        mTotalProductCount = cnt;
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem,
                         int visibleItemCount, int totalItemCount) {
        if (mProductListViewHelper.IsAttribSelectionApplied() == false) {
            if (loading) {
                if (totalItemCount > previousTotal) {
                    loading = false;
                    previousTotal = totalItemCount;
                    currentPage++;
                }
            }
            //if (!loading && (totalItemCount - visibleItemCount) <= (firstVisibleItem + SCROLL_LISTENER_THRESHOLD)) {
            if (!loading && (firstVisibleItem  >= (totalItemCount - SCROLL_LISTENER_THRESHOLD)) && (totalItemCount < mTotalProductCount)) {
                loading = true;

                if (mSearchQuery != null)
                    EventBus.getDefault().post(new EventGetSearchResults(mSearchQuery, currentPage, "ASC"));
                else
                    EventBus.getDefault().post(new EventGetProducts());
            }
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {}
}

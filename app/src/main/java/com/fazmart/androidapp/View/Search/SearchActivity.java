package com.fazmart.androidapp.View.Search;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;

import com.fazmart.androidapp.Common.Events.EventGetSearchResults;
import com.fazmart.androidapp.Controller.Category.CategoryController;
import com.fazmart.androidapp.Controller.ProductList.ProductListManager;
import com.fazmart.androidapp.Controller.ProductList.ProductListScrollListener;
import com.fazmart.androidapp.Controller.ProductList.ProductListViewHelper;
import com.fazmart.androidapp.FazmartApplication;
import com.fazmart.androidapp.R;
import com.fazmart.androidapp.View.CartActivity;
import com.fazmart.androidapp.View.Common.BaseActivity;
import com.fazmart.androidapp.View.Common.BreadCrumbAdapter;
import com.fazmart.androidapp.View.Common.BreadcrumbAdapterCallbacks;
import com.fazmart.androidapp.View.Common.FilterDrawerCallbacks;
import com.fazmart.androidapp.View.Common.FilterDrawerFragment;
import com.fazmart.androidapp.View.Common.NavDrawerItemList;
import com.fazmart.androidapp.View.Common.NavigationDrawerCallbacks;
import com.getbase.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;


public class SearchActivity extends BaseActivity implements BreadcrumbAdapterCallbacks, FilterDrawerCallbacks, NavigationDrawerCallbacks {
    // Used to store the last screen title. For use in {@link #restoreActionBar()}.
    private CharSequence mTitle;

    CategoryController mCategoryController;
    ProductListManager mProductListManager;
    ProductListViewHelper mProductListViewHelper;

    static final int SEARCH_CATEGORY_ID = 0;

    // The following flag is used in onResume() to decide whether or not to sync with the cart.
    // List view creation includes syncing with cart, hence, syncing in resume is unnecessary.
    boolean mComingFromOnCreate = false;

    ProductListScrollListener scrollListener = null;
    boolean mNewSearch = true;

    FilterDrawerFragment mFilterDrawerFragment = null;
    List<String> mSearchMessage;
    BreadCrumbAdapter mSearchMessageAdapter;

    Activity mActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        super.onCreateDrawer();

        mActivity = this;

        getSupportActionBar().setTitle("Search");

        mFilterDrawerFragment = (FilterDrawerFragment) getFragmentManager().findFragmentById(R.id.fragment_filter_drawer);

        // Setup Filter
        ImageView productFilter = (ImageView)findViewById(R.id.product_filter);
        productFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFilterDrawerFragment.openDrawer();
            }
        });

        RecyclerView breadcrumdRV = (RecyclerView)findViewById(R.id.breadcrumb);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        breadcrumdRV.setLayoutManager(layoutManager);

        mSearchMessage = new ArrayList<String>();
        mSearchMessage.add("Search in progress...");
        mSearchMessageAdapter = new BreadCrumbAdapter(mSearchMessage, this, true);
        mSearchMessageAdapter.setBreadcrumbAdapterCallbacks(this);
        breadcrumdRV.setAdapter(mSearchMessageAdapter);

        // Setup the ScrollListener
        mProductListViewHelper = new ProductListViewHelper(this, SEARCH_CATEGORY_ID, mAPIService); // Zero is used as the Category ID for search
        mProductListViewHelper.ResetDetailViewInitiated();

        mAPIService = ((FazmartApplication)getApplication()).GetAPIService();

        if (Intent.ACTION_SEARCH.equals(getIntent().getAction())) { // Defensive programming - in case this activity receives other broadcast actions in the future
            String query = getIntent().getStringExtra(SearchManager.QUERY);

            scrollListener = new ProductListScrollListener(mProductListViewHelper, mAPIService, query);

            ListView lv = (ListView) findViewById(R.id.ListViewOfProducts);
            lv.setOnScrollListener(scrollListener);

            EventBus.getDefault().post(new EventGetSearchResults(query, 1, "ASC"));
        }

        mProductListManager = ProductListManager.GetInstance();
        mCategoryController = CategoryController.GetInstance();

        FloatingActionButton fab = (FloatingActionButton)findViewById(R.id.pink_icon);
        fab.setSize(FloatingActionButton.SIZE_MINI);
        findViewById(R.id.pink_icon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mActivity, CartActivity.class);
                startActivity(intent);
            }
        });

        mComingFromOnCreate = true;
    }

    public void UpdateSearchMessage (String msg) {
        RecyclerView breadcrumdRV = (RecyclerView)findViewById(R.id.breadcrumb);
        mSearchMessage.set(0, msg);
        mSearchMessageAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {// Defensive programming - in case this activity receives other broadcast actions in the future
            mNewSearch = true;
            String query = intent.getStringExtra(SearchManager.QUERY);

            scrollListener.NewSearch(query);
            mProductListViewHelper.EventBusRegister();

            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            EventBus.getDefault().post(new EventGetSearchResults(query, 1, "ASC"));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mNewSearch)
            mNewSearch = false;
        else {
            mProductListViewHelper.EventBusRegister();
            if (mProductListManager.SyncWithCartAndWishList(SEARCH_CATEGORY_ID))
                mProductListViewHelper.GetProductListAdapter().notifyDataSetChanged();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        mProductListViewHelper.ResetDetailViewInitiated();
        mProductListViewHelper.EventBusUnRegister();
    }

    @Override
    public void onBreadcrumbItemSelected(int position) {
        // Nothing to be done for search
    }

    @Override
    public void onFilterDrawerItemSelected(int position) {
        mProductListViewHelper.onFilterDrawerItemSelected(position, SEARCH_CATEGORY_ID);
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
    }

    @Override
    public int getDefaultItemSelectId() {
        return NavDrawerItemList.SEARCH_ITEM_ID;
    }
}

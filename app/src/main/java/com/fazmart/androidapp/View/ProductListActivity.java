package com.fazmart.androidapp.View;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.fazmart.androidapp.Common.Events.EventGetProducts;
import com.fazmart.androidapp.Controller.Category.CategoryController;
import com.fazmart.androidapp.Controller.ProductList.ProductListManager;
import com.fazmart.androidapp.Controller.ProductList.ProductListViewHelper;
import com.fazmart.androidapp.R;
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

/**
 * Created by Vinay on 13-06-2015.
 */
public class ProductListActivity extends BaseActivity implements BreadcrumbAdapterCallbacks, FilterDrawerCallbacks, NavigationDrawerCallbacks  {

    CategoryController mCategoryController;
    ProductListManager mProductListManager;
    ProductListViewHelper mProductListViewHelper;
    int mCategoryId;

    // The following flag is used in onResume() to decide whether or not to sync with the cart.
    // List view creation includes syncing with cart, hence, syncing in resume is unnecessary.
    boolean mComingFrom_onCreate = true;

    FilterDrawerFragment mFilterDrawerFragment = null;
    int[] mLineage = null;

    Activity mActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_list_view);
        super.onCreateDrawer();

        mActivity = this;

        mFilterDrawerFragment = (FilterDrawerFragment) getFragmentManager().findFragmentById(R.id.fragment_filter_drawer);

        RelativeLayout contentView = (RelativeLayout) findViewById(R.id.ProductList);
        contentView.setVisibility(View.INVISIBLE);

        mLineage = getIntent().getIntArrayExtra(CategoryActivity.LINEAGE);
        mCategoryId = mLineage[mLineage.length - 1];

        mProductListViewHelper = new ProductListViewHelper(this, mCategoryId, mAPIService);
        mProductListViewHelper.ResetDetailViewInitiated();

        mProductListManager = ProductListManager.GetInstance();
        mCategoryController = CategoryController.GetInstance();

        getSupportActionBar().setTitle(mCategoryController.GetCategoryName(mLineage[mLineage.length - 1]));

        // Show breadcrumb
        final int hrchyLevel = mLineage.length;
        RecyclerView breadcrumdRV = (RecyclerView)findViewById(R.id.breadcrumb);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        breadcrumdRV.setLayoutManager(layoutManager);

        final List<String> catNameList = new ArrayList<String>();
        final String rootCatName = "Main Sections";
        catNameList.add(rootCatName);

        String catName;
        for (int i = 1; i < hrchyLevel; i++) {
            catName = String.format(" > %s", mCategoryController.GetCategoryName(mLineage[i]));
            catNameList.add(catName);
        }
        BreadCrumbAdapter adapter = new BreadCrumbAdapter(catNameList, this, false);
        adapter.setBreadcrumbAdapterCallbacks(this);
        breadcrumdRV.setAdapter(adapter);

        layoutManager.scrollToPositionWithOffset(hrchyLevel - 1, 20);

        // Setup Filter
        ImageView productFilter = (ImageView)findViewById(R.id.product_filter);
        productFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFilterDrawerFragment.openDrawer();
            }
        });

        findViewById(R.id.loading_message).setVisibility(View.VISIBLE);

        if (mCategoryController.WasCategoryViewed(mCategoryId) == false) {
            EventBus.getDefault().post(new EventGetProducts());
        } else {
            mProductListViewHelper.PopulateViews(mCategoryId, false);
            contentView.setVisibility(View.VISIBLE);
        }

        FloatingActionButton fab = (FloatingActionButton)findViewById(R.id.pink_icon);
        fab.setSize(FloatingActionButton.SIZE_MINI);
        findViewById(R.id.pink_icon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mActivity, CartActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onBreadcrumbItemSelected(int position) {
        if (position < mLineage.length-1) {
            Intent intent = new Intent();
            intent.putExtra(CategoryActivity.HIERARCHY_BACK_TRACK_CNT, (mLineage.length - 1) - position);
            setResult(RESULT_OK, intent);
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mComingFrom_onCreate)
            mComingFrom_onCreate = false;
        else {
            mProductListViewHelper.EventBusRegister();
            if (mProductListManager.SyncWithCartAndWishList(mCategoryId))
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
    public void onFilterDrawerItemSelected(int position) {
        mProductListViewHelper.onFilterDrawerItemSelected(position, mCategoryId);
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
    }

    @Override
    public int getDefaultItemSelectId() {
        return NavDrawerItemList.BROWSE_ITEM_ID;
    }
}

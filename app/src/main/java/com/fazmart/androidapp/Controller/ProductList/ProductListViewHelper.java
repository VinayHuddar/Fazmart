package com.fazmart.androidapp.Controller.ProductList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.fazmart.androidapp.Common.APIService;
import com.fazmart.androidapp.Common.Events.EventGetProducts;
import com.fazmart.androidapp.Common.Events.EventGetSearchResults;
import com.fazmart.androidapp.Common.Events.EventOpenDetailView;
import com.fazmart.androidapp.FazmartApplication;
import com.fazmart.androidapp.Model.CategoryData;
import com.fazmart.androidapp.Model.ProductData.ProductListData;
import com.fazmart.androidapp.View.Common.FilterDrawerFragment;
import com.fazmart.androidapp.View.ProductDetailActivity;
import com.fazmart.androidapp.R;
import com.fazmart.androidapp.Model.SearchData;
import com.fazmart.androidapp.View.Search.SearchActivity;

import java.util.ArrayList;

import de.greenrobot.event.EventBus;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by Vinay on 19-05-2015.
 */
public class ProductListViewHelper {
    Context mContext;
    APIService mAPIService;

    Activity mParentActivity;
    ProductListManager mProductListManager;
    CategoryData mCategorData;
    ArrayList<ProductListManager.ProductDataForListView> mProductList;
    ProductListAdapter mProductListAdapter;

    AttributeMap mAttributeMap;
    int mCategoryId;
    View footerView = null;
    boolean mDetailViewInitiated = false;

    public ProductListViewHelper (Activity parentActivity, int catId, APIService apiService) {
        mParentActivity = parentActivity;
        mContext = parentActivity;
        mCategoryId = catId;
        mAPIService = apiService;

        mAttributeMap = null;

        mProductListManager = ProductListManager.GetInstance();
        mCategorData = CategoryData.GetInstance();

        footerView = ((LayoutInflater)mParentActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.list_view_footer_layout, null, false);
        EventBusRegister();
    }

    public void EventBusRegister () {
        EventBus.getDefault().register(this);
        if (mProductListAdapter != null)
            mProductListAdapter.EventBusRegister();
    }

    public void EventBusUnRegister () {
        EventBus.getDefault().unregister(this);
        if (mProductListAdapter != null)
            mProductListAdapter.EventBusUnregister();
    }

    public void ResetDetailViewInitiated() {
        mDetailViewInitiated = false;
    }

    public ProductListAdapter GetProductListAdapter () {
        return mProductListAdapter;
    }

    boolean mAttribSelectionApplied = false;
    public boolean IsAttribSelectionApplied () { return mAttribSelectionApplied; }

    int retryCntGetSearchProds = 0;
    public void onEvent (EventGetSearchResults evt) {
        final EventGetSearchResults evtLocal = evt;
        // Fetch Product Data from the server
        FazmartApplication.GetAPIService().GetProductsSearched(evt.GetQueryString(), evt.GetPageNum(), evt.GetOrder(),
                new Callback<SearchData>() {
                    @Override
                    public void success(SearchData searchData, Response response) {
                        mParentActivity.findViewById(R.id.loading_message).setVisibility(View.GONE);

                        RelativeLayout contentView = (RelativeLayout) mParentActivity.findViewById(R.id.ProductList);
                        contentView.setVisibility(View.VISIBLE);

                        // Search count is saved for later access in PopulateViews to hide the ProgressBar in
                        // the product ListView when the end of the list is reached
                        boolean appendData = evtLocal.GetPageNum() > 1;
                        if (!appendData) {
                            mProductListManager.SetSearchProductCount(searchData.GetCount());
                            mProductListScrollListener.SetTotalProductCount(searchData.GetCount());
                        }

                        mProductListManager.ProcessRetrofitData(searchData.GetProductsData(), 0, appendData);
                        PopulateViews(0, appendData);
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        retryCntGetSearchProds++;
                        /*if (retryCntGetSearchProds < 5)
                            EventBus.getDefault().post(new EventGetSearchResults(evtLocal.GetAPIService(),
                                    evtLocal.GetQueryString(), evtLocal.GetPageNum(), evtLocal.GetOrder()));
                        else
                            retryCntGetSearchProds = 0;*/
                    }
                }
        );
    }

    int retryCntGetProds = 0;
    public void onEvent (EventGetProducts evt) {
        final EventGetProducts evtLocal = evt;
        // Fetch Product Data from the server
        FazmartApplication.GetAPIService().GetCategoryProducts(mCategoryId, mCategorData.GetProductCount(mCategoryId), 1,
                new Callback<ProductListData>() {
                    @Override
                    public void success(ProductListData productListData, Response response) {
                        mParentActivity.findViewById(R.id.loading_message).setVisibility(View.GONE);

                        RelativeLayout contentView = (RelativeLayout) mParentActivity.findViewById(R.id.ProductList);
                        contentView.setVisibility(View.VISIBLE);

                        mProductListManager.ProcessRetrofitData(productListData.GetProductsData(), mCategoryId, false);
                        PopulateViews(mCategoryId, false);

                        retryCntGetProds = 0;
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        retryCntGetProds++;
                        /*if (retryCntGetProds < 5)
                            EventBus.getDefault().post(new EventGetProducts(evtLocal.GetAPIService()));
                        else
                            retryCntGetProds = 0;*/
                    }
                }
        );
    }

    ProductListScrollListener mProductListScrollListener = null;
    public void RegisterScrollListener (ProductListScrollListener scrollListener) {
        mProductListScrollListener = scrollListener;
    }

    public void PopulateViews (final int parentCategoryId, boolean append)
    {
        mProductListManager.SyncWithCartAndWishList(parentCategoryId);

        int itemCount = mProductListManager.GetProductsListForCategory(parentCategoryId).size();
        mProductList = mProductListManager.GetProductsListForCategory(parentCategoryId);

        // Populate product list view
        ListView mProductListLV = (ListView) mParentActivity.findViewById(R.id.ListViewOfProducts);
        if (!append) {
            mProductListLV.addFooterView(footerView);

            final ProductListAdapter productListAdapter =
                    new ProductListAdapter(mParentActivity, R.id.ListViewOfProducts, mProductList, mCategoryId);

            mProductListAdapter = productListAdapter;   // Save a reference for use in refreshing the view when required
            mProductListLV.setAdapter(mProductListAdapter);
        } else {
            mProductListAdapter.UpdateProductCount(itemCount);
            mProductListAdapter.notifyDataSetChanged();
        }
        if (itemCount == ((parentCategoryId == 0) ? mProductListManager.GetSearchProductCount() :
                                                    mCategorData.GetProductCount(parentCategoryId)))
            mProductListLV.removeFooterView(footerView);

        // Setup Attribute Spinner
        mAttributeMap = mProductListManager.GetAttributeMapForCategory(parentCategoryId);
        String[] attribNames = mAttributeMap.GetAttributeList();
        int numAttribs = mAttributeMap.GetAttributeList().length;
        String[] attributeList = new String[numAttribs + 1];

        if (parentCategoryId == 0) { // i.e. if it is a search
            int hitCount = mProductListManager.GetSearchProductCount();
            attributeList[0] = (itemCount == 0) ? "Search returned 0 results" :
                    String.format("All %d of %d hits", itemCount, hitCount);

            ((SearchActivity)mParentActivity).UpdateSearchMessage(String.format("Showing %d of %d hits",
                    hitCount, itemCount, hitCount));
        }
        else
            attributeList[0] = String.format("All Items - (%d of %d)", //mCategorData.GetCategory(parentCategoryId).GetName(),
                    itemCount, mCategorData.GetProductCount(parentCategoryId));

        for (int i = 1; i <= numAttribs; i++)
            attributeList[i] = attribNames[i - 1];
        attribNames = null; // This is to let the Garbage Collector consider attribNames for Garbage collection

        FilterDrawerFragment filterDrawerFragment = (FilterDrawerFragment) mParentActivity.getFragmentManager().findFragmentById(R.id.fragment_filter_drawer);
        filterDrawerFragment.setup(R.id.fragment_filter_drawer, (DrawerLayout) mParentActivity.findViewById(R.id.drawer_layout), attributeList);

        /*Spinner subCatSpinner = (Spinner) mParentActivity.findViewById(R.id.SubCategorySpinner);
        ArrayAdapter subCatListadapter = new ArrayAdapter<String>(mContext, R.layout.option_selector, attributeList);
        subCatListadapter.setDropDownViewResource(R.layout.option_selector_list_item);
        subCatSpinner.setAdapter(subCatListadapter);

        subCatSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int prodCnt = 0;
                int[] prodIds = null;

                ListView mProductListLV = (ListView) mParentActivity.findViewById(R.id.ListViewOfProducts);
                if (position == 0) {
                    mProductListAdapter.ShowAllItems();

                    int totalProdCnt = (parentCategoryId == 0) ? mProductListManager.GetSearchProductCount()
                            : mCategorData.GetProductCount(parentCategoryId);
                    if (mProductListLV.getFooterViewsCount() > 0) {
                        if (mProductListAdapter.getCount() == totalProdCnt)
                            mProductListLV.removeFooterView(footerView);
                    } else if (mProductListAdapter.getCount() < totalProdCnt) {
                        mProductListLV.addFooterView(footerView);
                    }

                    mAttribSelectionApplied = false;
                } else {
                    prodCnt = mAttributeMap.GetProductsOfAttribute(position - 1).length;
                    prodIds = mAttributeMap.GetProductsOfAttribute(position - 1).clone();
                    mProductListAdapter.FilterProducts(prodCnt, prodIds);

                    if (mProductListLV.getFooterViewsCount() > 0) {
                        mProductListLV.removeFooterView(footerView);
                    }
                    mAttribSelectionApplied = true;
                }

                mProductListAdapter.notifyDataSetChanged();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        // Setup the Filter
        ImageView filter = (ImageView) mParentActivity.findViewById(R.id.Filter);
        filter.setImageResource(R.drawable.filter_icon_dark);
        filter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(mContext, v);
                String[] filterOptions = mParentActivity.getResources().getStringArray(R.array.filters);
                for (int i = 0; i < filterOptions.length; i++)
                    popup.getMenu().add(1, i, i, filterOptions[i]);
                popup.show();

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        Toast.makeText(mContext, "Filter - ".concat(item.toString()), Toast.LENGTH_SHORT).show();
                        return true;
                    }
                });
            }
        });*/
    }

    public void onEvent (EventOpenDetailView evt) {
        if (mDetailViewInitiated == false) {
            Intent intent = new Intent(mParentActivity, ProductDetailActivity.class);
            intent.putExtra("PRODUCT_ID", evt.GetProdId());
            intent.putExtra("CATEGORY_ID", mCategoryId);
            intent.putExtra("CURRENT_OPTION_INDEX", mProductListAdapter.getItem(evt.GetOptIdx()).GetOptionSelection());
            mParentActivity.startActivity(intent);

            mDetailViewInitiated = true;
        }
    }

    public void onFilterDrawerItemSelected (int position, int parentCategoryId) {
        int prodCnt = 0;
        int[] prodIds = null;

        ListView mProductListLV = (ListView) mParentActivity.findViewById(R.id.ListViewOfProducts);
        if (position == 0) {
            mProductListAdapter.ShowAllItems();

            int totalProdCnt = (parentCategoryId == 0) ? mProductListManager.GetSearchProductCount()
                    : mCategorData.GetProductCount(parentCategoryId);
            if (mProductListLV.getFooterViewsCount() > 0) {
                if (mProductListAdapter.getCount() == totalProdCnt)
                    mProductListLV.removeFooterView(footerView);
            } else if (mProductListAdapter.getCount() < totalProdCnt) {
                mProductListLV.addFooterView(footerView);
            }

            mAttribSelectionApplied = false;
        } else {
            prodCnt = mAttributeMap.GetProductsOfAttribute(position - 1).length;
            prodIds = mAttributeMap.GetProductsOfAttribute(position - 1).clone();
            mProductListAdapter.FilterProducts(prodCnt, prodIds);

            if (mProductListLV.getFooterViewsCount() > 0) {
                mProductListLV.removeFooterView(footerView);
            }
            mAttribSelectionApplied = true;
        }

        mProductListAdapter.notifyDataSetChanged();

    }
}

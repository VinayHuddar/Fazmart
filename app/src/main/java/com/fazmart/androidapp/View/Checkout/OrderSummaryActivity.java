package com.fazmart.androidapp.View.Checkout;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TabHost;

import com.fazmart.androidapp.Common.AuthenticationData;
import com.fazmart.androidapp.Common.CommonDefinitions;
import com.fazmart.androidapp.Common.NetworkProblemDialog;
import com.fazmart.androidapp.Controller.Cart.CartController;
import com.fazmart.androidapp.Controller.ReceivedOrderSummaryData;
import com.fazmart.androidapp.FazmartApplication;
import com.fazmart.androidapp.Model.OrderSummaryData;
import com.fazmart.androidapp.R;
import com.fazmart.androidapp.View.CategoryActivity;

import java.util.HashMap;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class OrderSummaryActivity extends FragmentActivity implements TabHost.OnTabChangeListener {
    Bundle mSavedInstanceState = null;
    OrderSummaryData mOrderSummaryData = null;
    String mMobileNumber = null;
    String mEmailId = null;
    NetworkProblemDialog mNetworkProblemDialog = null;
    Activity mActivity = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSavedInstanceState = savedInstanceState;
        mActivity = this;

        mNetworkProblemDialog = NetworkProblemDialog.newInstance();

        mMobileNumber = getIntent().getStringExtra("MOBILE_NUMBER");
        mEmailId = getIntent().getStringExtra("EMAIL_ID");

        setContentView(R.layout.activity_ordersummary);

        mOrderSummaryData = ReceivedOrderSummaryData.GetInstance().GetOrderSummaryData();

        InitializeTabHosts(mSavedInstanceState);

        if (mSavedInstanceState != null) {
            mTabHost.setCurrentTabByTag(mSavedInstanceState.getString("tab")); //set the tab as per the saved state
        }

        Button confirmAndPayButton = (Button)findViewById(R.id.confirm_and_pay);
        confirmAndPayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProceedToPayment ();
            }
        });
    }

    int mGetPayRetryCount = 0;
    void ProceedToPayment () {
        FazmartApplication.GetAPIService().GetPay(AuthenticationData.GetInstance().GetAuthenticationToken(),
                new Callback<String>() {
                    @Override
                    public void success(String s, Response response) {
                        OrderSuccess();
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        mGetPayRetryCount++;
                        if (mGetPayRetryCount < CommonDefinitions.RETRY_COUNT)
                            ProceedToPayment();
                        else
                            if (mNetworkProblemDialog.isAdded() == false)
                                mNetworkProblemDialog.show(getSupportFragmentManager(), "dialog");
                    }
                });
    }

    int mGetSuccessRetryCount = 0;
    void OrderSuccess () {
        FazmartApplication.GetAPIService().GetSuccess(new Callback<String>() {
            @Override
            public void success(String s, Response response) {
                AlertDialog successDialog = new AlertDialog.Builder(mActivity)
                        .setIcon(R.drawable.success_icon_256_256)
                        .setTitle(R.string.order_success_message)
                        .setPositiveButton(R.string.alert_dialog_continue, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                finish();

                                Intent intent = new Intent(mActivity, CategoryActivity.class);
                                int[] lineageRoot = new int[1];
                                lineageRoot[0] = -1;
                                intent.putExtra(CategoryActivity.LINEAGE, lineageRoot);
                                startActivity(intent);
                            }
                        })
                        /*.setNegativeButton(R.string.alert_dialog_cancel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {

                            }
                        })*/
                        .create();

                successDialog.setCancelable(false);
                successDialog.show();

                // Clear cart data
                CartController.GetInstance().EmptyCart();
            }

            @Override
            public void failure(RetrofitError error) {
                mGetSuccessRetryCount++;
                if (mGetSuccessRetryCount < CommonDefinitions.RETRY_COUNT)
                    OrderSuccess();
                else if (mNetworkProblemDialog.isAdded() == false)
                    mNetworkProblemDialog.show(getSupportFragmentManager(), "dialog");
            }
        });
    }



    public OrderSummaryData GetOrderSummaryData () { return mOrderSummaryData; }
    public String GetMobileNumber () { return mMobileNumber; }
    public String GetEmailId () { return mEmailId; }

    protected void onSaveInstanceState(Bundle outState) {
        outState.putString("tab", mTabHost.getCurrentTabTag()); //save the tab selected
        super.onSaveInstanceState(outState);
    }

    private TabHost mTabHost;
    private HashMap<String, TabInfo> mapTabInfo = new HashMap<String, TabInfo>();
    private TabInfo mLastTab = null;

    private class TabInfo {
        private String tag;
        private Class clss;
        private Bundle args;
        private Fragment fragment;
        private int contentId;
        TabInfo(String tag, Class clazz, Bundle args, int layoutId) {
            this.tag = tag;
            this.clss = clazz;
            this.args = args;
            this.contentId = layoutId;
        }
    }

    class TabFactory implements TabHost.TabContentFactory {
        private final Context mContext;

        public TabFactory(Context context) {
            mContext = context;
        }

        public View createTabContent(String tag) {
            View v = new View(mContext);
            v.setMinimumWidth(0);
            v.setMinimumHeight(0);
            return v;
        }

    }

    private void InitializeTabHosts (Bundle args) {
        mTabHost = (TabHost) findViewById(android.R.id.tabhost);
        mTabHost.setup();
        TabInfo tabInfo = null;
        OrderSummaryActivity.addTab(this, mTabHost, mTabHost.newTabSpec("OrderSummary").setIndicator("Order Summary"),
                (tabInfo = new TabInfo("OrderSummary", OrderSummaryActivity_SummaryFragment.class,
                        args, R.layout.fragment_ordersummary_order_summary)));
        this.mapTabInfo.put(tabInfo.tag, tabInfo);
        OrderSummaryActivity.addTab(this, mTabHost, mTabHost.newTabSpec("ItemsSummary").setIndicator("Items Summary"),
                (tabInfo = new TabInfo("ItemsSummary", OrderSummaryActivity_ItemsFragment.class,
                        args, R.layout.fragment_ordersummary_items_summary)));
        this.mapTabInfo.put(tabInfo.tag, tabInfo);

        // Default to first tab
        this.onTabChanged("OrderSummary");
        mTabHost.setOnTabChangedListener(this);
    }

    private static void addTab(OrderSummaryActivity activity, TabHost tabHost, TabHost.TabSpec tabSpec, TabInfo tabInfo) {
        // Attach a Tab view factory to the spec
        tabSpec.setContent(activity.new TabFactory(activity));
        String tag = tabSpec.getTag();

        // Check to see if we already have a fragment for this tab, probably
        // from a previously saved state.  If so, deactivate it, because our
        // initial state is that a tab isn't shown.
        tabInfo.fragment = activity.getSupportFragmentManager().findFragmentByTag(tag);
        if (tabInfo.fragment != null && !tabInfo.fragment.isDetached()) {
            FragmentTransaction ft = activity.getSupportFragmentManager().beginTransaction();
            ft.detach(tabInfo.fragment);
            ft.commit();
            activity.getSupportFragmentManager().executePendingTransactions();
        }

        tabHost.addTab(tabSpec);
    }

    public void onTabChanged(String tag) {
        TabInfo newTab = this.mapTabInfo.get(tag);
        if (mLastTab != newTab) {
            FragmentTransaction ft = this.getSupportFragmentManager().beginTransaction();
            if (mLastTab != null) {
                if (mLastTab.fragment != null) {
                    ft.detach(mLastTab.fragment);
                }
            }
            if (newTab != null) {
                if (newTab.fragment == null) {
                    newTab.fragment = Fragment.instantiate(this,
                            newTab.clss.getName(), newTab.args);
                    ft.add(R.id.realtabcontent, newTab.fragment, newTab.tag);
                } else {
                    ft.attach(newTab.fragment);
                }
            }

            mLastTab = newTab;
            ft.commit();
            this.getSupportFragmentManager().executePendingTransactions();
        }
    }

    @Override
    protected void onResume () {
        super.onResume();
        //EventBus.getDefault().register(this);
    }

    @Override
    protected void onPause () {
        super.onPause();
        //EventBus.getDefault().unregister(this);
    }

    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_ordersummary, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }*/
}

package com.fazmart.androidapp.View.UserAccount;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SearchViewCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.fazmart.androidapp.Common.CommonDefinitions;
import com.fazmart.androidapp.Common.NetworkProblemDialog;
import com.fazmart.androidapp.FazmartApplication;
import com.fazmart.androidapp.Model.AddressData.Address;
import com.fazmart.androidapp.Model.AddressData.AddressList;
import com.fazmart.androidapp.R;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by vinayhuddar on 14/07/15.
 */
public class MyWishlistFragment extends ListFragment {

    // This is the Adapter being used to display the list's data.
    AddressListAdapter mAdapter;

    // If non-null, this is the current filter the user has provided.
    String mCurFilter;

    SearchViewCompat.OnQueryTextListenerCompat mOnQueryTextListenerCompat;

    NetworkProblemDialog mNetworkProblemDialog;

    @Override public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mNetworkProblemDialog = NetworkProblemDialog.newInstance();

        // We have a menu item to show in action bar.
        setHasOptionsMenu(true);

        //FetchAllAddresses();

        // Start out with a progress indicator.
        setListShown(false);
    }

    @Override public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Place an action bar item for searching.
        MenuItem item = menu.add("Search");
        item.setIcon(android.R.drawable.ic_menu_search);
        MenuItemCompat.setShowAsAction(item, MenuItemCompat.SHOW_AS_ACTION_IF_ROOM
                | MenuItemCompat.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
        final View searchView = SearchViewCompat.newSearchView(getActivity());
        if (searchView != null) {
            SearchViewCompat.setOnQueryTextListener(searchView,
                    new SearchViewCompat.OnQueryTextListenerCompat() {
                        @Override
                        public boolean onQueryTextChange(String newText) {
                            // Called when the action bar search text has changed.  Since this
                            // is a simple array adapter, we can just have it do the filtering.
                            mCurFilter = !TextUtils.isEmpty(newText) ? newText : null;
                            mAdapter.getFilter().filter(mCurFilter);
                            return true;
                        }
                    });
            SearchViewCompat.setOnCloseListener(searchView,
                    new SearchViewCompat.OnCloseListenerCompat() {
                        @Override
                        public boolean onClose() {
                            if (!TextUtils.isEmpty(SearchViewCompat.getQuery(searchView))) {
                                SearchViewCompat.setQuery(searchView, null, true);
                            }
                            return true;
                        }

                    });
            MenuItemCompat.setActionView(item, searchView);
        }
    }

    @Override public void onListItemClick(ListView l, View v, int position, long id) {
        // Insert desired behavior here.
        Log.i("LoaderCustom", "Item clicked: " + id);
    }

    int mFetchOtherAddressesRetryCnt = 0;
    Address[] mAddressList = null;
    boolean mDefaultAddressChanged = false;
    Address mNewAddress = null;
    void FetchAllAddresses() {
        FazmartApplication.GetAPIService().GetAllAddresses(new Callback<AddressList>() {
            @Override
            public void success(AddressList addressList, Response response) {
                mAddressList = addressList.GetAddresses();

                // Create an empty adapter we will use to display the loaded data.
                mAdapter = new AddressListAdapter(getActivity(),
                        android.R.layout.simple_dropdown_item_1line, mAddressList);
                setListAdapter(mAdapter);
            }

            @Override
            public void failure(RetrofitError error) {
                if (mFetchOtherAddressesRetryCnt < CommonDefinitions.RETRY_COUNT) {
                    mFetchOtherAddressesRetryCnt++;
                    FetchAllAddresses();
                } else {
                    if (mNetworkProblemDialog.isAdded() == false)
                        mNetworkProblemDialog.show(getActivity().getSupportFragmentManager(), "dialog");
                }

            }
        });
    }


    public class AddressListAdapter extends ArrayAdapter<Address> {

        public AddressListAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);
        }

        public AddressListAdapter(Context context, int resource, Address[] items) {
            super(context, resource, items);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View v = convertView;

            if (v == null) {
                LayoutInflater vi;
                vi = LayoutInflater.from(getContext());
                v = vi.inflate(R.layout.layout_address, null);
            }

            Address currAddr = getItem(position);

            if (currAddr != null) {
                TextView firstNameTV = (TextView) v.findViewById(R.id.firstname);
                TextView lastNameTV  = (TextView) v.findViewById(R.id.lastname);
                TextView aptNumTV    = (TextView) v.findViewById(R.id.apartment_number);
                TextView aptNameTV   = (TextView) v.findViewById(R.id.apartment_name);
                TextView areaNameTV  = (TextView) v.findViewById(R.id.area_name);
                TextView cityNameTV  = (TextView) v.findViewById(R.id.city_name);
                TextView postcodeTV  = (TextView) v.findViewById(R.id.postcode);

                firstNameTV.setText(currAddr.GetFirstName());
                lastNameTV.setText(currAddr.GetLastName());
                aptNumTV.setText(currAddr.GetAptNum());
                aptNameTV.setText(currAddr.GetAptName());
                areaNameTV.setText(currAddr.GetAreaName());
                cityNameTV.setText(currAddr.GetCity().concat(" -"));
                postcodeTV.setText(currAddr.GetPostcode());
            }

            return v;
        }

        @Override
        public Address getItem(int position) {
            return mAddressList[position];
        }
    }
}

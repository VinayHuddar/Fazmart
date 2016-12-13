package com.fazmart.androidapp.View.UserAccount;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.view.MenuItemCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.fazmart.androidapp.Common.CommonDefinitions;
import com.fazmart.androidapp.Common.NetworkProblemDialog;
import com.fazmart.androidapp.Model.AddressData.Address;
import com.fazmart.androidapp.Model.AddressData.AddressAccessorCallbacks;
import com.fazmart.androidapp.Model.AddressData.AddressHelper;
import com.fazmart.androidapp.Model.AddressData.AddressList;
import com.fazmart.androidapp.Model.AddressData.AddressAPIHandler;
import com.fazmart.androidapp.Model.AddressData.AddressModel;
import com.fazmart.androidapp.Model.DeliveryData.ApartmentsAreaData;
import com.fazmart.androidapp.R;

/**
 * Created by vinayhuddar on 14/07/15.
 */
public class MyAddressesFragment extends ListFragment implements AddressAccessorCallbacks, AddressHelper.HelperCallback {

    // This is the Adapter being used to display the list's data.
    AddressListAdapter mAdapter;

    NetworkProblemDialog mNetworkProblemDialog;

    AddressAPIHandler mAddressAPIHandler;
    AddressHelper mAddressHelper;

    @Override public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mNetworkProblemDialog = NetworkProblemDialog.newInstance();
        mAddressAPIHandler = new AddressAPIHandler(getActivity(), this);
        mAddressHelper = new AddressHelper(getActivity(), this);

        // We have a menu item to show in action bar.
        setHasOptionsMenu(true);

        mAddressAPIHandler.FetchAllAddresses();

        // Start out with a progress indicator.
        setListShown(false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Place an action bar item for searching.
        menu.removeItem(R.id.action_search);
        MenuItem item = menu.add("Add New Address");
        item.setIcon(R.drawable.add_plus_button);
        MenuItemCompat.setShowAsAction(item, MenuItemCompat.SHOW_AS_ACTION_ALWAYS);

    }

    Address mNewAddress = null;
    @Override
    public boolean onOptionsItemSelected (MenuItem item) {
        mAddressHelper.GetNewAddress();

        return false;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        // Insert desired behavior here.
        Log.i("LoaderCustom", "Item clicked: " + id);
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
                v = vi.inflate(R.layout.layout_address_my_account, null);
            }

            final Address currAddr = getItem(position);

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

            ImageView editButton = (ImageView) v.findViewById(R.id.edit_address);
            editButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                }
            });

            ImageView deleteButton = (ImageView) v.findViewById(R.id.delete_address);
            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mAddressAPIHandler.DeleteAddress (currAddr.GetAddressId());
                }
            });
            return v;
        }

        @Override
        public Address getItem(int position) {
            return mAddressList[position];
        }
    }

    public void onFetchAddress (AddressModel address) {

    }

    Address[] mAddressList = null;
    public void onFetchAllAddresses (AddressList addressList) {
        mAddressList = addressList.GetAddresses();

        // Create an empty adapter we will use to display the loaded data.
        mAdapter = new AddressListAdapter(getActivity(),
                android.R.layout.simple_dropdown_item_1line, mAddressList);

        ListView lv = getListView();
        ColorDrawable sepColor = new ColorDrawable(getActivity().getResources().getColor(R.color.primary_color));
        lv.setDivider(sepColor);
        lv.setDividerHeight(1);
        setListAdapter(mAdapter);
    }

    public void onFetchApartmentData (ApartmentsAreaData aptmtsInACity) {

    }

    public void onApartmentSelected (int selectedAreaIdx, int selectedAreaId, int selectedAptmtIdx, int selectedAptmtId) {

    }

    public void onGetNewAddress (Address address) {
        mAddressAPIHandler.PostNewShippingAddress(address);
    }

    public void onPostGuestAddress () {

    }

    public void onPostExistingAddress () {

    }

    public void onPostNewShippingAddress () {
        mAddressAPIHandler.FetchAllAddresses();
    }

    public void onDeleteAddress () {
        mAddressAPIHandler.FetchAllAddresses();
    }

    public void onPutAddress () {
        mAddressAPIHandler.FetchAllAddresses();
    }
}

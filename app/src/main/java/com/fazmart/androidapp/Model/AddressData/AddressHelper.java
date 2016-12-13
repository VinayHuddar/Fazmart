package com.fazmart.androidapp.Model.AddressData;

import android.app.Dialog;
import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.fazmart.androidapp.Common.CommonDefinitions;
import com.fazmart.androidapp.Common.NetworkProblemDialog;
import com.fazmart.androidapp.Model.DeliveryData.ApartmentsAreaData;
import com.fazmart.androidapp.R;

import java.util.ArrayList;

/**
 * Created by vinayhuddar on 16/07/15.
 */
public class AddressHelper implements AddressAccessorCallbacks {

    public interface HelperCallback {
        void onGetNewAddress (Address addr);
        void onApartmentSelected (int selectedAreaIdx, int selectedAreaId, int selectedAptmtIdx, int selectedAptmtId);
    }

    HelperCallback mCallBackObject;
    NetworkProblemDialog mNetworkProblemDialog;
    FragmentActivity mActivity;
    AddressAPIHandler mAddressAPIHandler;

    public AddressHelper (FragmentActivity activity, HelperCallback callBack) {
        mActivity = activity;
        mCallBackObject = callBack;

        mAddressAPIHandler = new AddressAPIHandler(activity, this);
        mNetworkProblemDialog = NetworkProblemDialog.newInstance();
    }

    private class AptmtIdAreaIdxMap {
        int aptmtId;
        String aptmtName;
        int areaIdx;

        public AptmtIdAreaIdxMap(int aptmt_id, String aptmt_name, int area_idx) {
            aptmtId = aptmt_id;
            aptmtName = aptmt_name;
            areaIdx = area_idx;
        }

        public int  GetApartmentId () { return aptmtId; }
        public String GetApartmentName () { return aptmtName; }
        public int GetAreaIdx () { return areaIdx; }
    }

    private ArrayList<AptmtIdAreaIdxMap> CreateAptmtToAreaMap (ApartmentsAreaData aptmtsInACity) {
        ArrayList<AptmtIdAreaIdxMap> mAptmtIdAreaIdxMap = new ArrayList<AptmtIdAreaIdxMap>();
        int areaCnt = aptmtsInACity.GetAreaCount();
        for (int areaIdx = 0; areaIdx < areaCnt; areaIdx++) {
            int aptmtCnt = aptmtsInACity.GetApartmentCount(areaIdx);
            for (int aptmtIdx = 0; aptmtIdx < aptmtCnt; aptmtIdx++) {
                AptmtIdAreaIdxMap mapObj = new AptmtIdAreaIdxMap(
                        aptmtsInACity.GetApartmentId(areaIdx, aptmtIdx),
                        aptmtsInACity.GetApartmentName(areaIdx, aptmtIdx),
                        areaIdx);
                mAptmtIdAreaIdxMap.add(mapObj);
            }
        }
        return mAptmtIdAreaIdxMap;
    }


    public void LoadApartmentSelector (final ApartmentsAreaData aptmtsInACity, final View view, final Dialog newAddressDialog) {

        final ArrayList<AptmtIdAreaIdxMap> aptmtIdAreaIdxMaps = CreateAptmtToAreaMap(aptmtsInACity);

        final Spinner aptmtSelector;
        if (newAddressDialog != null)
            aptmtSelector = (Spinner)newAddressDialog.findViewById(R.id.address_apartment_selector);
        else
            aptmtSelector = (Spinner)view.findViewById(R.id.address_apartment_selector);

        final int aptmtCount = aptmtIdAreaIdxMaps.size();
        final String[] aptmtNames = new String[aptmtCount + 1]; // +1 for "Select Apartment" label
        aptmtNames[0] = "Select Apartment";
        for (int i = 0; i < aptmtCount; i++)
            aptmtNames[i+1] = aptmtIdAreaIdxMaps.get(i).GetApartmentName();

        ArrayAdapter<String> aptmtListAdapter;
        if (newAddressDialog != null)
            aptmtListAdapter = new ArrayAdapter<String>(newAddressDialog.getContext(), R.layout.apt_selector_spinner, aptmtNames);
        else
            aptmtListAdapter = new ArrayAdapter<String>(mActivity, R.layout.apt_selector_spinner, aptmtNames);

        aptmtListAdapter.setDropDownViewResource(R.layout.apt_selector_spinner_dropdown);
        aptmtSelector.setAdapter(aptmtListAdapter);

        aptmtSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position != 0) {
                    int selectedAptmtIdx = position - 1;
                    AptmtIdAreaIdxMap mapObj = aptmtIdAreaIdxMaps.get(selectedAptmtIdx);

                    int selectedAptmtId = mapObj.GetApartmentId();

                    // Update the area field
                    TextView areaTV;
                    if (newAddressDialog != null)
                        areaTV = (TextView) newAddressDialog.findViewById(R.id.address_area);
                    else
                        areaTV = (TextView) view.findViewById(R.id.address_area);
                    areaTV.setText(aptmtsInACity.GetAreaName(mapObj.GetAreaIdx()));

                    int selectedAreaIdx = mapObj.GetAreaIdx();
                    int selectedAreaId = aptmtsInACity.GetAreaId(selectedAreaIdx);

                    if (newAddressDialog == null)
                        mCallBackObject.onApartmentSelected (selectedAreaIdx, selectedAreaId, selectedAptmtIdx, selectedAptmtId);
                    else
                        onApartmentSelected (selectedAreaIdx, selectedAreaId, selectedAptmtIdx, selectedAptmtId);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    boolean AllFieldsFilledAndValid_AddressOnly(Context context) {
        boolean allFieldsCmplAndValid = true;

        String errorMsg = "All fields are required. Please ";
        if ((mSelectedCityId == -1) || (mSelectedAreaIdx == -1) || (mSelectedAptmtId == -1)) {
            allFieldsCmplAndValid = false;

            String selections = null;
            if (mSelectedCityId == -1)
                selections = (selections == null) ? "City" : selections.concat(", City");
            if (mSelectedAreaIdx == -1)
                selections = (selections == null) ? "Area" : selections.concat(", Area");
            if (mSelectedAptmtId == -1)
                selections = (selections == null) ? "Apartment" : selections.concat(", Apartment");

            errorMsg = errorMsg.concat("select ").concat(selections);
        }

        if ((mFirstName.compareTo("") == 0) || (mAptmtNumber.compareTo("") == 0)) {
            if (allFieldsCmplAndValid == false)
                errorMsg = errorMsg.concat(" and enter ");
            else {
                errorMsg = errorMsg.concat("enter ");
                allFieldsCmplAndValid = false;
            }

            String entries = null;
            if (mFirstName.compareTo("") == 0)
                entries = (entries == null) ? "Customer Name" : entries.concat(", Customer Name");
            if (mAptmtNumber.compareTo("") == 0)
                entries = (entries == null) ? "Flat Number" : entries.concat(", Flat Number");

            errorMsg = errorMsg.concat(entries);
        }

        if (allFieldsCmplAndValid == false)
            Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show();

        return allFieldsCmplAndValid;
    }

    Dialog mNewAddressDialog = null;
    int mSelectedCityId;
    String mFirstName;
    String mAptmtNumber;

    public void GetNewAddress () {
        mNewAddressDialog = new Dialog(mActivity, R.style.cust_dialog);
        mNewAddressDialog.setContentView(R.layout.layout_get_new_address_dialog);
        final float scale = mActivity.getResources().getDisplayMetrics().density;
        int pixels = (int) (360 * scale + 0.5f);
        mNewAddressDialog.getWindow().setLayout(pixels, LinearLayout.LayoutParams.WRAP_CONTENT);
        //mNewAddressDialog.getWindow().setLayout(parentDialog.getWindow().getDecorView().getWidth(), LinearLayout.LayoutParams.WRAP_CONTENT);

        mSelectedCityId = CommonDefinitions.DEFAULT_CITY_INDEX;
        mAddressAPIHandler.FetchApartmentData(mSelectedCityId);

        Button doneButton = (Button) mNewAddressDialog.findViewById(R.id.new_address_done_button);
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView nameTV = (TextView) mNewAddressDialog.findViewById(R.id.address_name);
                mFirstName = nameTV.getText().toString();

                TextView flatTV = (TextView) mNewAddressDialog.findViewById(R.id.address_apt_num);
                mAptmtNumber = flatTV.getText().toString();

                Spinner aptmtSelector = (Spinner) mNewAddressDialog.findViewById(R.id.address_apartment_selector);
                String aptName = (String)aptmtSelector.getSelectedItem();

                boolean completeAndValid = AllFieldsFilledAndValid_AddressOnly(mNewAddressDialog.getContext());
                Address newAddress;
                if (completeAndValid) {
                    newAddress = new Address(0, mFirstName, "NA", mAptmtNumber, mSelectedAptmtId,
                            aptName, mSelectedAreaId, mAptmtAreaDataFromAPI.GetAreaName(mSelectedAreaIdx),
                            mAptmtAreaDataFromAPI.GetPostCode(mSelectedAreaIdx), mSelectedCityId,
                            mAptmtAreaDataFromAPI.GetCityName(), mAptmtAreaDataFromAPI.GetZoneId(),
                            mAptmtAreaDataFromAPI.GetCountryId());

                    mNewAddressDialog.dismiss();
                    mNewAddressDialog = null;

                    mCallBackObject.onGetNewAddress(newAddress);
                }
            }
        });

        Button cancelButton = (Button) mNewAddressDialog.findViewById(R.id.new_address_cancel_button);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mNewAddressDialog.dismiss();
            }
        });

        mNewAddressDialog.show();
    }

    public void onFetchAddress (AddressModel address) {

    }

    public void onFetchAllAddresses (AddressList addressList) {

    }

    ApartmentsAreaData mAptmtAreaDataFromAPI;
    public void onFetchApartmentData (ApartmentsAreaData aptmtsInACity) {
        mAptmtAreaDataFromAPI = aptmtsInACity;

        TextView cityTV = (TextView) mNewAddressDialog.findViewById(R.id.address_city);
        cityTV.setText(aptmtsInACity.GetCityName());

        LoadApartmentSelector(aptmtsInACity, null, mNewAddressDialog);
    }

    int mSelectedAreaIdx = -1;
    int mSelectedAreaId = -1;
    int mSelectedAptmtIdx = -1;
    int mSelectedAptmtId = -1;
    public void onApartmentSelected (int selectedAreaIdx, int selectedAreaId, int selectedAptmtIdx, int selectedAptmtId) {
        mSelectedAreaIdx = selectedAreaIdx;
        mSelectedAreaId = selectedAreaId;
        mSelectedAptmtIdx = selectedAptmtIdx;
        mSelectedAptmtId = selectedAptmtId;
    }

    public void onPostGuestAddress () {
    }

    public void onPostExistingAddress () {
    }

    public void onPostNewShippingAddress () {
    }

    public void onDeleteAddress () {
    }

    public void onPutAddress () {
    }
}

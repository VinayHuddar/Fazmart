package com.fazmart.androidapp.Controller;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.support.v4.app.FragmentActivity;
import android.text.InputType;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.fazmart.androidapp.Common.AuthenticationData;
import com.fazmart.androidapp.Common.CommonDefinitions;
import com.fazmart.androidapp.Common.Events.EventDeliveryDatePicked;
import com.fazmart.androidapp.Common.NetworkProblemDialog;
import com.fazmart.androidapp.FazmartApplication;
import com.fazmart.androidapp.Model.AddressData.AddressAccessorCallbacks;
import com.fazmart.androidapp.Model.AddressData.AddressAPIHandler;
import com.fazmart.androidapp.Model.AddressData.AddressHelper;
import com.fazmart.androidapp.Model.UserAccountData;
import com.fazmart.androidapp.Model.AddressData.Address;
import com.fazmart.androidapp.Model.AddressData.AddressModel;
import com.fazmart.androidapp.Model.AddressData.AddressList;
import com.fazmart.androidapp.Model.DeliveryData.AccountData;
import com.fazmart.androidapp.Model.DeliveryData.ApartmentsAreaData;
import com.fazmart.androidapp.Model.DeliveryData.DeliverySlots;
import com.fazmart.androidapp.Model.DeliveryData.PaymentMethods;
import com.fazmart.androidapp.Model.OrderSummaryData;
import com.fazmart.androidapp.R;
import com.fazmart.androidapp.View.Checkout.ListViewHeightSetUtility;
import com.fazmart.androidapp.View.Checkout.OrderSummaryActivity;

import de.greenrobot.event.EventBus;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by Vinay on 13-06-2015.
 */
public class DeliveryDataController implements AddressAccessorCallbacks, AddressHelper.HelperCallback {
    static final String MINIMUM_DATE = "minimum_date";
    static final String MAXIMUM_DATE = "maximum_date";

    FragmentActivity mActivity = null;
    boolean mSignedIn = false;

    DeliverySlots mDeliverySlots = null;
    String[] mDays = null;
    String[] mTimeSlots = null;
    String mSelectedDay = null;
    int mSelectedDayIdx = -1;
    String mSelectedTimeSlot = null;
    int mSelectedTimeSlotIdx = -1;
    String mFirstName = null;
    String mAptmtNumber = null;
    String mMobileNumber = null;
    String mEmailId = null;
    String mCity = null;

    int mSelectedCityId = -1;
    ApartmentsAreaData mAptmtAreaDataFromAPI = null;

    int mFetchDeliverySlotsRetryCnt = 0;
    int mFetchPaymethodRetryCnt = 0;
    NetworkProblemDialog mNetworkProblemDialog;

    AddressAPIHandler mAddressAPIHandler;
    AddressHelper mAddressHelper;

    int mSelectedAddressId = 0;

    AccountData mAccountData;
    Dialog mNewAddressDialog;

    public DeliveryDataController(FragmentActivity activity, final boolean signedIn) {
        mActivity = activity;
        mSignedIn = signedIn;

        mNetworkProblemDialog = NetworkProblemDialog.newInstance();
        mAddressAPIHandler = new AddressAPIHandler(mActivity, this);
        mAddressHelper = new AddressHelper(mActivity, this);

        EventBusRegister();

        if (mSignedIn) {
            TextView delAddrLbl = (TextView)mActivity.findViewById(R.id.delivery_address_label);
            delAddrLbl.setText("Delivery Address");
            delAddrLbl.setGravity(Gravity.LEFT);

            mAccountData = UserAccountData.GetInstance().GetAccountData(mActivity);
            mSelectedAddressId = mAccountData.GetDefaultDeliveryAddressId();

            mAddressAPIHandler.FetchAddress(mSelectedAddressId);
        }
        else {
            mActivity.findViewById(R.id.change_address).setVisibility(View.GONE);

            mNewAddressDialog = null;
            mSelectedCityId = CommonDefinitions.DEFAULT_CITY_INDEX;
            mAddressAPIHandler.FetchApartmentData(mSelectedCityId);
        }

        FetchDeliverySlots();

        FetchPaymentMethods();

        ListView delTimeList = (ListView) mActivity.findViewById(R.id.delivery_time);
        delTimeList.setOnTouchListener(new View.OnTouchListener() {
            // Setting on Touch Listener for handling the touch inside ScrollView
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // Disallow the touch request for parent scroll on touch of child view
                v.getParent().requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });

        Button nextButton = (Button)mActivity.findViewById(R.id.delivery_info_complete);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Intent intent = new Intent(mActivity, OrderSummaryActivity.class);
                //mActivity.startActivity(intent);

                if (AllFieldsFilledAndValid() == true) {
                    mActivity.findViewById(R.id.loading_message).setVisibility(View.VISIBLE);
                    mActivity.findViewById(R.id.delivery_info).setVisibility(View.INVISIBLE);

                    if (signedIn) {
                        if (mNewAddress != null)
                            mAddressAPIHandler.PostNewShippingAddress(mNewAddress);
                        else
                            mAddressAPIHandler.PostExistingAddress(mSelectedAddressId);
                    } else {
                        mAddressAPIHandler.PostGuestAddress(mFirstName, "NA", mEmailId, mMobileNumber,
                                mAptmtNumber, mSelectedAptmtId, mSelectedAreaId,
                                mSelectedCityId, mAptmtAreaDataFromAPI.GetPostCode(mSelectedAreaIdx),
                                mAptmtAreaDataFromAPI.GetCountryId(), mAptmtAreaDataFromAPI.GetZoneId(), true);
                    }
                }
            }
        });
    }


    public void onPostExistingAddress() {
        PostDeliverySlot(mSelectedDay, mSelectedTimeSlot);
    }

    void ShowAddress (Address address) {
        TextView nameTV = (TextView) mActivity.findViewById(R.id.address_name);
        nameTV.setText(String.format("%s %s", address.GetFirstName(), address.GetLastName()));
        nameTV.setInputType(InputType.TYPE_NULL);
        TextView flatTV = (TextView) mActivity.findViewById(R.id.address_apt_num);
        flatTV.setText(address.GetAptNum());
        flatTV.setInputType(InputType.TYPE_NULL);

        Spinner aptSelector = (Spinner) mActivity.findViewById(R.id.address_apartment_selector);
        aptSelector.setClickable(false);
        String[] aptmtNames = new String[1];
        aptmtNames[0] = address.GetAptName();
        ArrayAdapter<String> aptmtListAdapter = new ArrayAdapter<String>(mActivity, android.R.layout.simple_spinner_item, aptmtNames);
        aptSelector.setAdapter(aptmtListAdapter);

        TextView areaTV = (TextView) mActivity.findViewById(R.id.address_area);
        areaTV.setText(address.GetAreaName());
        areaTV.setInputType(InputType.TYPE_NULL);
        TextView cityTV = (TextView) mActivity.findViewById(R.id.address_city);
        cityTV.setText(address.GetCity());
        cityTV.setInputType(InputType.TYPE_NULL);
    }

    int mDaySelected;
    static final int TODAY_INDEX = 0;
    static final int NEXTDAY_INDEX = 0;

    void FetchDeliverySlots () {
        FazmartApplication.GetAPIService().GetDeliverySlots(new Callback<DeliverySlots>() {
            @Override
            public void success(DeliverySlots deliverySlots, Response response) {
                mDeliverySlots = deliverySlots;

                mDays = mDeliverySlots.GetDeliveryDayNames();

                mDaySelected = mDeliverySlots.AreSlotsAvailable(TODAY_INDEX) ? TODAY_INDEX : NEXTDAY_INDEX;

                mTimeSlots = mDeliverySlots.GetSlotNames(mDaySelected);

                LoadTimeSlotFields();

                FetchPaymentMethods();
            }

            @Override
            public void failure(RetrofitError error) {
                if (mFetchDeliverySlotsRetryCnt < CommonDefinitions.RETRY_COUNT) {
                    mFetchDeliverySlotsRetryCnt++;
                    FetchDeliverySlots();
                } else {
                    if (mNetworkProblemDialog.isAdded() == false)
                        mNetworkProblemDialog.show(mActivity.getSupportFragmentManager(), "dialog");
                }
            }
        });
    }

    private void LoadTimeSlotFields () {
        // Load Time Slots
        final ListView deliverySlotList = (ListView) mActivity.findViewById(R.id.delivery_time);
        deliverySlotList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        //deliverySlotList.setFastScrollAlwaysVisible(true);

        final TimeSlotAdapter timeSlotAdapter = new TimeSlotAdapter(mTimeSlots);
        deliverySlotList.setAdapter(timeSlotAdapter);

        deliverySlotList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mSelectedTimeSlot = String.format("%d:%s", mDeliverySlots.GetSlotId(mSelectedDayIdx, position), mTimeSlots[position]);
                mSelectedTimeSlotIdx = position;
                UpdateSlotSelectedField();
            }
        });

        // Load Days
        ListView delDayList = (ListView) mActivity.findViewById(R.id.delivery_day);
        ArrayAdapter delDayAapter = new ArrayAdapter<String>(mActivity, R.layout.single_choice_selector_list_item, mDays);
        delDayList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        //delDayList.setFastScrollAlwaysVisible(true);
        delDayList.setAdapter(delDayAapter);

        delDayList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mSelectedDay = mDays[position];
                mSelectedDayIdx = position;
                if (position != mDaySelected) {
                    mDaySelected = position;
                    mTimeSlots = mDeliverySlots.GetSlotNames(mDaySelected);
                    timeSlotAdapter.setTimeSlots(mTimeSlots);
                    timeSlotAdapter.notifyDataSetChanged();

                    if (mSelectedTimeSlot != null) {
                        if (mSelectedTimeSlotIdx < mDeliverySlots.GetSlotCount(position)) {
                            if (mDeliverySlots.IsSlotAvailable(mDaySelected, position) == false) {
                                mSelectedTimeSlot = null;
                                deliverySlotList.clearChoices();
                                deliverySlotList.requestLayout();
                            }
                        }
                        else {
                            mSelectedTimeSlot = null;
                            deliverySlotList.clearChoices();
                            deliverySlotList.requestLayout();
                        }
                    }
                    UpdateSlotSelectedField();
                }
            }
        });

    }

    class TimeSlotAdapter extends BaseAdapter {
        String[] mTimeSlotNames;
        public TimeSlotAdapter (String[] timeSlotNames) {
            mTimeSlotNames = timeSlotNames;
        }

        public void setTimeSlots (String[] newTimeSlots) {
            mTimeSlotNames = newTimeSlots;
        }

        public int getCount () {
            return mTimeSlotNames.length;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater)mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View listItem;
            if (convertView == null) {
                listItem = inflater.inflate(R.layout.single_choice_selector_list_item, null);
            } else {
                listItem = (View) convertView;
            }

            CheckedTextView textView = (CheckedTextView) listItem.findViewById(android.R.id.text1);
            textView.setText(mDeliverySlots.GetSlotName(mDaySelected, position));
            if (mDeliverySlots.IsSlotAvailable(mDaySelected, position) == false) {
                textView.setTextColor(mActivity.getResources().getColor(R.color.light_grey));
            }
            else
                textView.setTextColor(mActivity.getResources().getColor(R.color.dark_grey));

            return listItem;
        }

        public boolean isEnabled (int position) {
            return mDeliverySlots.IsSlotAvailable(mDaySelected, position);
        }

        public Object getItem(int position) {
            return mDeliverySlots.GetSlotName(mDaySelected, position);
        }

        public long getItemId(int position) {
            return position;
        }
    }

    public void UpdateSlotSelectedField () {
        String slotSelectedText;
        if ((mSelectedDay != null) && (mSelectedTimeSlot != null))
            slotSelectedText = String.format("%s, %s", mSelectedDay, mTimeSlots[mSelectedTimeSlotIdx]);
        else
            slotSelectedText = mActivity.getResources().getString(R.string.selected_slot);

        TextView tv = (TextView) mActivity.findViewById(R.id.selected_slot);
        tv.setText(slotSelectedText);
    }

    PaymentMethods mPaymentMethods;
    String mSelectedPaymentMethodCode = null;
    void FetchPaymentMethods() {
        FazmartApplication.GetAPIService().GetPaymentMethods(new Callback<PaymentMethods>() {
            @Override
            public void success(PaymentMethods paymentMethods, Response response) {
                mPaymentMethods = paymentMethods;

                int payMethodsCount = mPaymentMethods.GetPaymentMethodsCount();
                String[] payMethodNames = new String[payMethodsCount];
                for (int i = 0; i < payMethodsCount; i++)
                    payMethodNames[i] = mPaymentMethods.GetPaymentMethodName(i);

                ListView paymentModeList = (ListView) mActivity.findViewById(R.id.payment_mode);
                ArrayAdapter<String> payModeAdapter = new ArrayAdapter<String>(mActivity, R.layout.single_choice_selector_list_item,
                        payMethodNames);
                paymentModeList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
                paymentModeList.setAdapter(payModeAdapter);
                ListViewHeightSetUtility.setListViewHeightBasedOnChildren(paymentModeList);
                paymentModeList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        mSelectedPaymentMethodCode = mPaymentMethods.GetPaymentMethodCode(position);
                    }
                });
            }

            @Override
            public void failure(RetrofitError error) {
                if (mFetchPaymethodRetryCnt < CommonDefinitions.RETRY_COUNT) {
                    mFetchPaymethodRetryCnt++;
                    FetchPaymentMethods();
                } else {
                    if (mNetworkProblemDialog.isAdded() == false)
                        mNetworkProblemDialog.show(mActivity.getSupportFragmentManager(), "dialog");
                }
            }
        });
    }

    boolean mDeliverySlotPosted = false;
    int mPostDeliverySlotRetryCnt = 0;
    public void PostDeliverySlot(final String day, final String time) {
        FazmartApplication.GetAPIService().PostDeliverySlot(mSelectedDay, mSelectedTimeSlot, new Callback<String>() {
            @Override
            public void success(String s, Response response) {
                mDeliverySlotPosted = true;

                PostPaymentMethod(mSelectedPaymentMethodCode);
            }

            @Override
            public void failure(RetrofitError error) {
                if (mPostDeliverySlotRetryCnt < CommonDefinitions.RETRY_COUNT) {
                    mPostDeliverySlotRetryCnt++;
                    PostDeliverySlot(day, time);
                } else {
                    if (mNetworkProblemDialog.isAdded() == false)
                        mNetworkProblemDialog.show(mActivity.getSupportFragmentManager(), "dialog");
                }
            }
        });
    }

    boolean mPaymentMethodPosted = false;
    int mPostPaymentMethodRetryCnt = 0;
    public void PostPaymentMethod(final String code) {
        FazmartApplication.GetAPIService().PostPaymentMethod(mSelectedPaymentMethodCode, new Callback<String>() {
            @Override
            public void success(String s, Response response) {
                mPaymentMethodPosted = true;

                PostConfirm();
            }

            @Override
            public void failure(RetrofitError error) {
                if (mPostPaymentMethodRetryCnt < CommonDefinitions.RETRY_COUNT) {
                    mPostDeliverySlotRetryCnt++;
                    PostPaymentMethod(mSelectedPaymentMethodCode);
                } else {
                    if (mNetworkProblemDialog.isAdded() == false)
                        mNetworkProblemDialog.show(mActivity.getSupportFragmentManager(), "dialog");
                }
            }
        });
    }

    int mGetConfirmRetryCnt = 0;
    public void PostConfirm() {
        FazmartApplication.GetAPIService().GetConfirm(AuthenticationData.GetInstance().GetAuthenticationToken(),
                new Callback<OrderSummaryData>() {
                @Override
                public void success(OrderSummaryData osd, Response response) {

                    // We reach here only when all the fields are filled by the user and all the posts
                    // were successfull. Now we move to Order Summary screen.
                    ReceivedOrderSummaryData.GetInstance().SetOrderSummaryData(osd);

                    mActivity.findViewById(R.id.loading_message).setVisibility(View.INVISIBLE);
                    mActivity.findViewById(R.id.delivery_info).setVisibility(View.VISIBLE);
                    //EventBus.getDefault().post(new EventDeliveryInfoPosted(osd));

                    Intent intent = new Intent(mActivity, OrderSummaryActivity.class);
                    intent.putExtra("MOBILE_NUMBER", mMobileNumber);
                    intent.putExtra("EMAIL_ID", mEmailId);
                    mActivity.startActivity(intent);
                }

                @Override
                public void failure(RetrofitError error) {
                    if (mGetConfirmRetryCnt < CommonDefinitions.RETRY_COUNT) {
                        mGetConfirmRetryCnt++;
                        PostConfirm();
                    } else {
                        if (mNetworkProblemDialog.isAdded() == false)
                            mNetworkProblemDialog.show(mActivity.getSupportFragmentManager(), "dialog");
                    }
                }
            });
    }

    boolean AllFieldsFilledAndValid() {
        boolean allFieldsCmplAndValid = true;

        String errorMsg = "All fields are required. Please ";
        if (mSignedIn) {
            if ((mSelectedDay == null) || (mSelectedTimeSlot == null) || (mSelectedPaymentMethodCode == null)) {
                allFieldsCmplAndValid = false;

                String selections = null;
                if (mSelectedDay == null)
                    selections = (selections == null) ? "Day" : selections.concat(", Day");
                if (mSelectedTimeSlot == null)
                    selections = (selections == null) ? "Time Slot" : selections.concat(", Time Slot");
                if (mSelectedPaymentMethodCode == null)
                    selections = (selections == null) ? "Payment Method" : selections.concat(", Payment Method");

                errorMsg = errorMsg.concat("select ").concat(selections);
            }
        } else {
            if ((mSelectedCityId == -1) || (mSelectedAreaId == -1) || (mSelectedAptmtId == -1) ||
                    (mSelectedDay == null) || (mSelectedTimeSlot == null)) {
                allFieldsCmplAndValid = false;

                String selections = null;
                if (mSelectedCityId == -1)
                    selections = (selections == null) ? "City" : selections.concat(", City");
                if (mSelectedAreaId == -1)
                    selections = (selections == null) ? "Area" : selections.concat(", Area");
                if (mSelectedAptmtId == -1)
                    selections = (selections == null) ? "Apartment" : selections.concat(", Apartment");
                if (mSelectedDay == null)
                    selections = (selections == null) ? "Day" : selections.concat(", Day");
                if (mSelectedTimeSlot == null)
                    selections = (selections == null) ? "Time Slot" : selections.concat(", Time Slot");

                errorMsg = errorMsg.concat("select ").concat(selections);
            }

            TextView nameTV = (TextView) mActivity.findViewById(R.id.address_name);
            mFirstName = nameTV.getText().toString();
            TextView flatTV = (TextView) mActivity.findViewById(R.id.address_apt_num);
            mAptmtNumber = flatTV.getText().toString();
            TextView mobileTV = (TextView) mActivity.findViewById(R.id.address_mobile);
            mMobileNumber = mobileTV.getText().toString();
            TextView emailTV = (TextView) mActivity.findViewById(R.id.address_email_id);
            mEmailId = emailTV.getText().toString();

            if ((mFirstName.compareTo("") == 0) || (mAptmtNumber.compareTo("") == 0) ||
                    (mMobileNumber.compareTo("") == 0) || (mEmailId.compareTo("") == 0)) {
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
                if (mMobileNumber.compareTo("") == 0)
                    entries = (entries == null) ? "Mobile Number" : entries.concat(", Mobile Number");
                if (mEmailId.compareTo("") == 0)
                    entries = (entries == null) ? "Email Id" : entries.concat(", Email Id");

                errorMsg = errorMsg.concat(entries);
            }
        }

        if (allFieldsCmplAndValid == false)
            Toast.makeText(mActivity, errorMsg, Toast.LENGTH_SHORT).show();

        return allFieldsCmplAndValid;
    }

    public void onEvent (EventDeliveryDatePicked evt) {
        mSelectedDay = evt.GetDatePicked();
        UpdateSlotSelectedField();
    }

    public void EventBusRegister () {
        EventBus.getDefault().register(this);
    }
    public void EventBusUnRegister () {
        EventBus.getDefault().unregister(this);
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

    // Callback implementations
    boolean mGuestAddressPosted = false;
    public void onPostGuestAddress () {
        mGuestAddressPosted = true;
        PostDeliverySlot(mSelectedDay, mSelectedTimeSlot);
    }

    // AddressAPIHandler interface implementation
    AddressModel mDefaultAddress = null;
    public void onFetchAddress (AddressModel address) {
        mDefaultAddress = address;
        ShowAddress(address.GetAddress());

        AccountData accountData = UserAccountData.GetInstance().GetAccountData(mActivity);

        TextView mobileTV = (TextView) mActivity.findViewById(R.id.address_mobile);
        mobileTV.setText(accountData.GetMobileNumber());
        mobileTV.setInputType(InputType.TYPE_NULL);
        TextView emailTV = (TextView) mActivity.findViewById(R.id.address_email_id);
        emailTV.setText(accountData.GetEmail());
        emailTV.setInputType(InputType.TYPE_NULL);

        mAddressAPIHandler.FetchAllAddresses();
    }

    Address[] mAddressList = null;
    Dialog mAddrSelectDialog;
    public void onFetchAllAddresses (AddressList addressList) {
        mAddressList = addressList.GetAddresses();

        TextView changeAddrTV = (TextView) mActivity.findViewById(R.id.change_address);
        changeAddrTV.setPaintFlags(changeAddrTV.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        changeAddrTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAddrSelectDialog = new Dialog(mActivity, R.style.cust_dialog);
                mAddrSelectDialog.setContentView(R.layout.address_selector_dialog);

                ArrayAdapter<Address> addrListAdapter = new AddressListAdapter(mAddrSelectDialog.getContext(),
                        android.R.layout.simple_dropdown_item_1line, mAddressList);
                ListView addressLV = (ListView) mAddrSelectDialog.findViewById(R.id.address_list);
                addressLV.setAdapter(addrListAdapter);

                addressLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                        if (mAddressList[position].GetAddressId() != mDefaultAddress.GetAddress().GetAddressId()) {
                            mSelectedAddressId = mAddressList[position].GetAddressId();
                            ShowAddress(mAddressList[position]);
                        }
                        mAddrSelectDialog.dismiss();
                        mAddrSelectDialog = null;
                    }
                });

                Button addNewAddrButton = (Button) mAddrSelectDialog.findViewById(R.id.add_new_address_button);
                addNewAddrButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mAddressHelper.GetNewAddress();
                    }
                });

                mAddrSelectDialog.show();

            }
        });
    }

    public void onFetchApartmentData(ApartmentsAreaData aptmtsInACity) {
        mAptmtAreaDataFromAPI = aptmtsInACity;

        TextView cityTV;
        if (mNewAddressDialog != null)
            cityTV = (TextView) mNewAddressDialog.findViewById(R.id.address_city);
        else
            cityTV = (TextView) mActivity.findViewById(R.id.address_city);
        cityTV.setText(aptmtsInACity.GetCityName());

        mAddressHelper.LoadApartmentSelector(mAptmtAreaDataFromAPI, null, mNewAddressDialog);

    }

    // AddressHelper interface implementation
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

    Address mNewAddress = null;
    public void onGetNewAddress (Address address) {
        mNewAddress = address;

        ShowAddress(address);

        if (mAddrSelectDialog.isShowing()) {
            mAddrSelectDialog.dismiss();
            mAddrSelectDialog = null;
        }
    }

    public void onPostNewShippingAddress () {
        PostDeliverySlot(mSelectedDay, mSelectedTimeSlot);
    }

    public void onDeleteAddress () {}

    public void onPutAddress () {}
}

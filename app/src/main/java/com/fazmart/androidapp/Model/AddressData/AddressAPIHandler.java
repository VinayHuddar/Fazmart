package com.fazmart.androidapp.Model.AddressData;

import android.support.v4.app.FragmentActivity;

import com.fazmart.androidapp.Common.CommonDefinitions;
import com.fazmart.androidapp.Common.NetworkProblemDialog;
import com.fazmart.androidapp.FazmartApplication;
import com.fazmart.androidapp.Model.DeliveryData.ApartmentsAreaData;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by vinayhuddar on 16/07/15.
 */
public class AddressAPIHandler {
    NetworkProblemDialog mNetworkProblemDialog;
    AddressAccessorCallbacks mCallBackObject;
    FragmentActivity mActivity;


    public AddressAPIHandler(FragmentActivity activity, AddressAccessorCallbacks callBack) {
        mActivity = activity;
        mCallBackObject = callBack;

        mNetworkProblemDialog = NetworkProblemDialog.newInstance();
    }

    int mFetchDefaultAddressRetryCnt = 0;
    public void FetchAddress(final int addrId) {
        FazmartApplication.GetAPIService().GetAddressId(addrId, new Callback<AddressModel>() {
            @Override
            public void success(AddressModel address, Response response) {
                mCallBackObject.onFetchAddress(address);
            }

            @Override
            public void failure(RetrofitError error) {
                if (mFetchDefaultAddressRetryCnt < CommonDefinitions.RETRY_COUNT) {
                    mFetchDefaultAddressRetryCnt++;
                    FetchAddress(addrId);
                } else {
                    if (mNetworkProblemDialog.isAdded() == false)
                        mNetworkProblemDialog.show(mActivity.getSupportFragmentManager(), "dialog");
                }

            }
        });
    }

    int mFetchAllAddressesRetryCnt = 0;
    public void FetchAllAddresses() {
        FazmartApplication.GetAPIService().GetAllAddresses(new Callback<AddressList>() {
            @Override
            public void success(AddressList addressList, Response response) {
                mCallBackObject.onFetchAllAddresses(addressList);
            }

            @Override
            public void failure(RetrofitError error) {
                if (mFetchAllAddressesRetryCnt < CommonDefinitions.RETRY_COUNT) {
                    mFetchAllAddressesRetryCnt++;
                    FetchAllAddresses();
                } else {
                    if (mNetworkProblemDialog.isAdded() == false)
                        mNetworkProblemDialog.show(mActivity.getSupportFragmentManager(), "dialog");
                }

            }
        });
    }

    int mFetchApartmentDataRetryCnt = 0;
    public void FetchApartmentData (final int cityId) {
        FazmartApplication.GetAPIService().GetAreaApartmentData(cityId, new Callback<ApartmentsAreaData>() {
            @Override
            public void success(ApartmentsAreaData aptmtsInACity, Response response) {
                mCallBackObject.onFetchApartmentData(aptmtsInACity);
            }

            @Override
            public void failure(RetrofitError error) {
                if (mFetchApartmentDataRetryCnt < CommonDefinitions.RETRY_COUNT) {
                    mFetchApartmentDataRetryCnt++;
                    FetchApartmentData(cityId);
                } else {
                    if (mNetworkProblemDialog.isAdded() == false)
                        mNetworkProblemDialog.show(mActivity.getSupportFragmentManager(), "dialog");
                }
            }
        });
    }

    int mPostNewAddressRetryCnt = 0;
    public void PostNewShippingAddress(final Address newAddress) {
        FazmartApplication.GetAPIService().PostAddress(
                "new",
                newAddress.GetFirstName(),
                newAddress.GetLastName(),
                newAddress.GetAptNum(),
                newAddress.GetAptId(),
                newAddress.GetAreaId(),
                newAddress.GetCityId(),
                newAddress.GetPostcode(),
                newAddress.GetZoneId(),
                new Callback<String>() {
                    @Override
                    public void success(String s, Response response) {
                        mCallBackObject.onPostNewShippingAddress();
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        if (mPostNewAddressRetryCnt < CommonDefinitions.RETRY_COUNT) {
                            mPostNewAddressRetryCnt++;
                            PostNewShippingAddress(newAddress);
                        } else {
                            if (mNetworkProblemDialog.isAdded() == false)
                                mNetworkProblemDialog.show(mActivity.getSupportFragmentManager(), "dialog");
                        }

                    }
                }
        );
    }

    int mPostGuestAddressRetryCnt = 0;
    public void PostGuestAddress(final String pFirstName,
                          final String pLastName,
                          final String pEmailId,
                          final String pMobileNumber,
                          final String pFlatNumber,
                          final int pSelectedAptmtId,
                          final int pSelectedAreaId,
                          final int pSelectedCityId,
                          final String pPostCode,
                          final int pCountryId,
                          final int pZoneId,
                          final boolean pPaymentAddress) {
        FazmartApplication.GetAPIService().PostGuestAddress(
                pFirstName,
                pLastName,
                pEmailId,
                pMobileNumber,
                pFlatNumber,
                pSelectedAptmtId,
                pSelectedAreaId,
                pSelectedCityId,
                pPostCode,
                pCountryId,
                pZoneId,
                pPaymentAddress,
                new Callback<String>() {
                    @Override
                    public void success(String s, Response response) {
                        mCallBackObject.onPostGuestAddress();
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        if (mPostGuestAddressRetryCnt < CommonDefinitions.RETRY_COUNT) {
                            mPostGuestAddressRetryCnt++;
                            PostGuestAddress(pFirstName,
                                    pLastName,
                                    pEmailId,
                                    pMobileNumber,
                                    pFlatNumber,
                                    pSelectedAptmtId,
                                    pSelectedAreaId,
                                    pSelectedCityId,
                                    pPostCode,
                                    pCountryId,
                                    pZoneId,
                                    pPaymentAddress);
                        } else {
                            if (mNetworkProblemDialog.isAdded() == false)
                                mNetworkProblemDialog.show(mActivity.getSupportFragmentManager(), "dialog");
                        }

                    }
                }
        );
    }

    int mPostExistingAddressRetryCnt = 0;
    public void PostExistingAddress (final int addrId) {
        FazmartApplication.GetAPIService().PostExistingShippingAddress("existing", addrId, new Callback<String>() {
            @Override
            public void success(String s, Response response) {
                mCallBackObject.onPostExistingAddress();
            }

            @Override
            public void failure(RetrofitError error) {
                if (mPostExistingAddressRetryCnt < CommonDefinitions.RETRY_COUNT) {
                    mPostExistingAddressRetryCnt++;
                    PostExistingAddress(addrId);
                } else {
                    if (mNetworkProblemDialog.isAdded() == false)
                        mNetworkProblemDialog.show(mActivity.getSupportFragmentManager(), "dialog");
                }
            }
        });
    }

    int mDeleteAddressRetryCnt = 0;
    public void DeleteAddress (final int addrId) {
        FazmartApplication.GetAPIService().DeleteAddress(addrId, new Callback<String>() {
            @Override
            public void success(String s, Response response) {
                mCallBackObject.onDeleteAddress();
            }

            @Override
            public void failure(RetrofitError error) {
                if (mDeleteAddressRetryCnt < CommonDefinitions.RETRY_COUNT) {
                    mDeleteAddressRetryCnt++;
                    DeleteAddress(addrId);
                } else {
                    if (mNetworkProblemDialog.isAdded() == false)
                        mNetworkProblemDialog.show(mActivity.getSupportFragmentManager(), "dialog");
                }
            }
        });
    }

    int mPutAddressRetryCnt = 0;
    public void PutAddress (final String pFirstName,
                            final String pLastName,
                            final String pAptNum,
                            final int pSelectedAptmtId,
                            final int pSelectedAreaId,
                            final int pSelectedCityId,
                            final String pPostCode,
                            final int pCountryId,
                            final int pZoneId,
                            final boolean pDefaultAddr) {
        FazmartApplication.GetAPIService().PutAddress(pFirstName,
                pLastName,
                pAptNum,
                pSelectedAptmtId,
                pSelectedAreaId,
                pSelectedCityId,
                pPostCode,
                pCountryId,
                pZoneId,
                pDefaultAddr,
                new Callback<String>() {
                    @Override
                    public void success(String s, Response response) {
                        mCallBackObject.onPutAddress();
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        if (mDeleteAddressRetryCnt < CommonDefinitions.RETRY_COUNT) {
                            mDeleteAddressRetryCnt++;
                            PutAddress(pFirstName,
                                    pLastName,
                                    pAptNum,
                                    pSelectedAptmtId,
                                    pSelectedAreaId,
                                    pSelectedCityId,
                                    pPostCode,
                                    pCountryId,
                                    pZoneId,
                                    pDefaultAddr);
                        } else {
                            if (mNetworkProblemDialog.isAdded() == false)
                                mNetworkProblemDialog.show(mActivity.getSupportFragmentManager(), "dialog");
                        }
                    }
        });
    }
}
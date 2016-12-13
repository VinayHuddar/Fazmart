package com.fazmart.androidapp.Model.AddressData;

import com.fazmart.androidapp.Model.DeliveryData.ApartmentsAreaData;

/**
 * Created by poliveira on 27/10/2014.
 */
public interface AddressAccessorCallbacks {
    void onFetchAddress (AddressModel address);

    void onFetchAllAddresses (AddressList addressList);

    void onFetchApartmentData (ApartmentsAreaData aptmtsInACity);

    void onPostNewShippingAddress ();

    void onPostGuestAddress ();

    void onPostExistingAddress ();

    void onDeleteAddress ();

    void onPutAddress ();
}

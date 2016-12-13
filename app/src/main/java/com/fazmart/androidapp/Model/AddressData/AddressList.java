package com.fazmart.androidapp.Model.AddressData;

/**
 * Created by vinayhuddar on 27/06/15.
 */
public class AddressList {
    Address[] addresses;

    public Address[] GetAddresses () { return addresses; }
    public int GetAddressCount () { return addresses.length; }
    public Address GetAddress (int idx) {
        return addresses[idx];
        /*String formattedAddress = String.format("%s %s\r\n%s\r\n%s\r\n%s\r\n%s - %s",
                addresses[idx].GetFirstName(), addresses[idx].GetLastName(),
                addresses[idx].GetAptNum(), addresses[idx].GetAptName(),
                addresses[idx].GetAreaName(), addresses[idx].GetCity(), addresses[idx].GetPostcode());
        return formattedAddress;*/
    }
}

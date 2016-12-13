package com.fazmart.androidapp.Model.DeliveryData;

/**
 * Created by Vinay on 16-06-2015.
 */
public class PostedAddress {
    String firstname;
    String lastname;
    String email;
    String mobile;
    String apt_num;
    int apt_id;
    int area_id;
    int city_id;
    String postcode;
    int country_id;
    int zone_id;
    boolean payment_address;

    public PostedAddress (String pfirstname, String plastname, String pemail, String pmobile,
            String paptmtnum, int papt_id, int parea_id, int pcity_id, String ppostcode,
            int pcountry_id, int pzone_id, boolean ppayment_addr) {
        firstname = pfirstname;
        lastname = plastname;
        email = pemail;
        mobile = pmobile;
        apt_num = paptmtnum;
        apt_id = papt_id;
        area_id = parea_id;
        city_id = pcity_id;
        postcode = ppostcode;
        country_id = pcountry_id;
        zone_id = pzone_id;
        payment_address = ppayment_addr;
    }

    public String GetFirstName () { return firstname; }
    public String GetLastName () { return lastname; }
    public String GetEmail () { return email; }
    public String GetMobile () { return mobile; }
    public String GetApartmentNumber () { return apt_num; }
    public int GetApartmentId () { return apt_id; }
    public int GetAreaId () { return area_id; }
    public int GetCityId () { return city_id; }
    public String GetPostCode () { return postcode; }
    public int GetCountryId () { return country_id; }
    public int GetZoneId () { return zone_id; }
    public boolean GetPaymentAddress () { return payment_address; }

    public void SetFirstName (String pfirstname) { firstname = pfirstname; }
    public void SetLastName (String plastname) { lastname = plastname; }
    public void SetEmail (String pemail) { email = pemail; }
    public void SetMobile (String pmobile) { mobile = pmobile; }
    public void SetApartmentNumber (String paptmtnum) { apt_num = paptmtnum; }
    public void SetApartmentName (int papt_id) { apt_id = papt_id; }
    public void SetArea (int parea_id) { area_id = parea_id; }
    public void SetCity (int pcity_id) { city_id = pcity_id; }
    public void SetPostCode (String ppostcode) { postcode = ppostcode; }
    public void SetCountryId (int pcountry_id) { country_id = pcountry_id; }
    public void SetZoneId (int pzone_id) { zone_id = pzone_id; }
    public void SetShippingAddress (boolean ppayment_addr) { payment_address = ppayment_addr; }
}

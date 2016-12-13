package com.fazmart.androidapp.Model.AddressData;

/**
 * Created by vinayhuddar on 27/06/15.
 */
public class Address {
    int address_id;
    String firstname;
    String lastname;
    String apt_num;
    int apt_id;
    String apt_name;
    int area_id;
    String area_name;
    String postcode;
    int city_id;
    String city;
    int zone_id;
    int country_id;

    public int GetAddressId () { return address_id; }
    public String GetFirstName () { return firstname; }
    public String GetLastName () { return lastname; }
    public String GetAptNum () { return apt_num; }
    public String GetAptName () { return apt_name; }
    public String GetAreaName () { return area_name; }
    public String GetPostcode () { return postcode; }
    public String GetCity () { return city; }

    public int GetAptId () { return apt_id; }
    public int GetAreaId () { return area_id; }
    public int GetCityId () { return city_id; }
    public int GetZoneId () { return zone_id; }
    public int GetCountryId () { return country_id; }

    public Address (int p_address_id,
            String p_firstname,
            String p_lastname,
            String p_apt_num,
            int p_apt_id,
            String p_apt_name,
            int p_area_id,
            String p_area_name,
            String p_postcode,
            int p_city_id,
            String p_city,
            int p_zone_id,
            int p_country_id) {
        address_id = p_address_id;
        firstname = p_firstname;
        lastname = p_lastname;
        apt_num = p_apt_num;
        apt_id = p_apt_id;
        apt_name = p_apt_name;
        area_id = p_area_id;
        area_name = p_area_name;
        postcode = p_postcode;
        city_id = p_city_id;
        city = p_city;
        zone_id = p_zone_id;
        country_id = p_country_id;
    }
}
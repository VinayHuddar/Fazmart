package com.fazmart.androidapp.Model.DeliveryData;

/**
 * Created by Vinay on 16-06-2015.
 */
public class ApartmentsAreaData {
    CityWithAreasAndApartments city;

    class CityWithAreasAndApartments {
        int city_id;
        int zone_id;
        String name;
        int country_id;
        Area[] areas;

        class Area {
            int area_id;
            String name;
            String postcode;
            Apartments[] apts;

            class Apartments {
                int apt_id;
                String name;
                String address;
            }
        }
    }

    public String GetCityName () { return city.name; }
    public int GetZoneId () { return city.zone_id; }
    public int GetCountryId () { return city.country_id; }

    public int GetAreaCount () { return city.areas.length; }
    public String GetAreaName (int areaIdx) { return city.areas[areaIdx].name; }
    public int GetAreaId (int areaIdx) { return city.areas[areaIdx].area_id; }
    public String GetPostCode (int areaIdx) { return city.areas[areaIdx].postcode; }

    public int GetApartmentCount (int areaIdx) { return city.areas[areaIdx].apts.length; }
    public String GetApartmentName (int areaIdx, int aptIdx) { return city.areas[areaIdx].apts[aptIdx].name; }
    public int GetApartmentId (int areaIdx, int aptIdx) { return city.areas[areaIdx].apts[aptIdx].apt_id; }
}

package com.fazmart.androidapp.Model;

import com.fazmart.androidapp.Common.CommonDefinitions;

/**
 * Created by Vinay on 17-06-2015.
 */
public class OrderSummaryData {
    OrderSummary order;
    public class OrderSummary {
        Category[] categories;
        Voucher[] vouchers;
        Total[] totals;
        boolean savings;
        DeliveryAddress delivery_address;
        DeliverySlot delivery_slot;
        DeliveryMethod delivery_method;
        PaymentMethod payment_method;
        String payment_information;
        boolean needs_payment_now;
    }

    public class Category {
        String name;
        Product[] products;
    }

    public class Product {
        String key;
        int product_id;
        String name;
        String model;
        Option[] option;
        String recurring;
        int quantity;
        String reward;
        String price;
        int points;
        String special;
        String savings;
        String total;
        String manufacturer;
        String thumb_image;
        boolean in_stock;

        class Option {
            String name;
            String value;
            int product_option_value_id;
        }

        public String GetKey () { return key; }
        public int GetProductId () { return product_id; }
        public int GetQuantity () { return quantity; }
        public String GetManufacturer () { return manufacturer; }
        public String GetTitle () { return name; }
        public String GetThumbImageURL () { return thumb_image; }
        public String GetSpecialPrice () { return special; }
        public String GetPrice () { return price; }
        public String GetOptionValue () { return (option.length > 0 ? option[CommonDefinitions.PRIMARY_OPTION_ID].value : null); }
        public int GetProductOptionValueId () { return option[CommonDefinitions.PRIMARY_OPTION_ID].product_option_value_id; }
    }


    class Voucher {
    }

    class Total {
        String title;
        String text;
    }
    class Savings {
    }

    class DeliveryAddress {
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
    }
    class DeliverySlot {
        String date;
        String time;
        String text;
    }
    class DeliveryMethod {
        String code;
        String title;
        String cost;
        String tax_class_id;
        String text;
    }
    class PaymentMethod {
        String code;
        String title;
        String terms;
        String sort_order;
    }

    public String GetFirstName () {
        return order.delivery_address.firstname;
    }

    public String GetLastName () {
        return order.delivery_address.lastname;
    }

    public String GetApartmentNumber () {
        return order.delivery_address.apt_num;
    }

    public String GetApartmentName () {
        return order.delivery_address.apt_name;
    }

    public String GetAreaName () {
        return order.delivery_address.area_name;
    }

    public String GetCityName () {
        return order.delivery_address.city;
    }

    public String GetPostcode () {
        return order.delivery_address.postcode;
    }

    public String GetSubTotal () {
        for (Total totalItem: order.totals) {
            if (totalItem.title.compareTo("Sub-Total") == 0) {
                return totalItem.text;
            }
        }
        return null;
    }

    public String GetDeliveryCharges () {
        for (Total totalItem: order.totals) {
            if (totalItem.title.compareTo("Delivery Charges") == 0) {
                return totalItem.text;
            }
        }
        return null;
    }
    public String GetTotal () {
        for (Total totalItem: order.totals) {
            if (totalItem.title.compareTo("Total") == 0) {
                return totalItem.text;
            }
        }
        return null;
    }

    public String GetSubTotalLabel () {
        for (Total totalItem: order.totals) {
            if (totalItem.title.compareTo("Sub-Total") == 0) {
                return totalItem.title;
            }
        }
        return null;
    }

    public String GetDeliveryChargesLabel () {
        for (Total totalItem: order.totals) {
            if (totalItem.title.compareTo("Delivery Charges") == 0) {
                return totalItem.title;
            }
        }
        return null;
    }
    public String GetTotalLabel () {
        for (Total totalItem: order.totals) {
            if (totalItem.title.compareTo("Total") == 0) {
                return totalItem.title;
            }
        }
        return null;
    }

    public int GetCategoryCount () {
        return (order.categories != null) ? order.categories.length : 0;
    }

    public int GetItemCount (int catIdx) {
        return order.categories[catIdx].products.length;
    }

    public int GetItemId (int catIdx, int itemIdx) {
        return order.categories[catIdx].products[itemIdx].product_id;
    }

    public Product GetItem(int catIdx, int itemIdx) {
        return order.categories[catIdx].products[itemIdx];
    }

    public int GetProductOptionValueId(int catIdx, int itemIdx) {
        return order.categories[catIdx].products[itemIdx].option[CommonDefinitions.PRIMARY_OPTION_ID].product_option_value_id;
    }

    public String GetCategoryName (int catIdx) {
        return order.categories[catIdx].name;
    }

    public String GetDeliverySlot () {
        return String.format("%s, %s", order.delivery_slot.date, order.delivery_slot.time);
    }

    public String GetPaymentMethod () {
        return order.payment_method.title;
    }

}


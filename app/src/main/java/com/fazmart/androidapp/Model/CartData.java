package com.fazmart.androidapp.Model;

import com.fazmart.androidapp.Common.CommonDefinitions;

/**
 * Created by Vinay on 17-06-2015.
 */
public class CartData {
    public int GetCategoryCount () {
        return (cart.categories != null) ? cart.categories.length : 0;
    }

    public int GetProductCount (int catIdx) {
        return cart.categories[catIdx].products.length;
    }

    public int GetProductId (int catIdx, int itemIdx) {
        return cart.categories[catIdx].products[itemIdx].product_id;
    }

    public Product GetCartItem(int catIdx, int itemIdx) {
        return cart.categories[catIdx].products[itemIdx];
    }

    public int GetProductOptionValueId(int catIdx, int itemIdx) {
        return cart.categories[catIdx].products[itemIdx].option[CommonDefinitions.PRIMARY_OPTION_ID].product_option_value_id;
    }

    public String GetCategoryName (int catIdx) {
        return cart.categories[catIdx].name;
    }

    Cart cart;
    public class Cart {
        Category[] categories;
        Voucher[] vouchers;
        Total[] totals;
        String weight;
        String savings;
        String delivery_info;
        boolean coupon_status;
        String coupon;
        boolean voucher_status;
        String voucher;
        boolean reward_status;
        int reward;
        int max_reward_points_to_use;
        boolean shipping_status;
        String error_warning;
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

    public String GetSubTotal () {
        for (Total totalItem: cart.totals) {
            if (totalItem.title.compareTo("Sub-Total") == 0) {
                return totalItem.text;
            }
        }
        return null;
    }

    public String GetDeliveryCharges () {
        for (Total totalItem: cart.totals) {
            if (totalItem.title.compareTo("Delivery Charges") == 0) {
                return totalItem.text;
            }
        }
        return null;
    }
    public String GetTotal () {
        for (Total totalItem: cart.totals) {
            if (totalItem.title.compareTo("Total") == 0) {
                return totalItem.text;
            }
        }
        return null;
    }

    public String GetSubTotalLabel () {
        for (Total totalItem: cart.totals) {
            if (totalItem.title.compareTo("Sub-Total") == 0) {
                return totalItem.title;
            }
        }
        return null;
    }

    public String GetDeliveryChargesLabel () {
        for (Total totalItem: cart.totals) {
            if (totalItem.title.compareTo("Delivery Charges") == 0) {
                return totalItem.title;
            }
        }
        return null;
    }
    public String GetTotalLabel () {
        for (Total totalItem: cart.totals) {
            if (totalItem.title.compareTo("Total") == 0) {
                return totalItem.title;
            }
        }
        return null;
    }

    public String GetSavings () {
        return cart.savings;
    }
    public String GetDeliveryChargeNote () {
        return cart.delivery_info;
    }

}


package com.fazmart.androidapp.Model.ProductData;

import com.fazmart.androidapp.Common.CommonDefinitions;

/**
 * Created by Vinay on 09-06-2015.
 */
public class ProductDetailData {
    Product product;
    public Product GetProduct () { return product; }

    public class Product {
        int product_id;
        String title;
        String model;
        String description;
        String thumb_image;
        String image;
        String[] images;
        String price;
        String tax;
        String special;
        String[] discounts;
        ProductOptionsData[] options;
        String manufacturer;
        int reward_points;
        int reward_points_needed_to_buy;
        AttributeGroup[] attribute_groups;
        int minimum_quantity;
        String stock_status;
        RelatedProduct[] related_products;
        int rating;
        String reviews;
        boolean review_enabled;
        String[] recurrings;

        public int GetProductId() { return product_id; }
        public ProductOptionsData[] GetProductOptionsData () { return options; }
        public String GetTitle() { return title; }
        public String GetDescription() { return description; }
        public String GetThumbImage() { return thumb_image; }
        public String GetImage() { return image; }
        public String[] GetImages() { return images; }
        public String GetManufacturer() { return manufacturer; }
        public String GetStockStatus() { return stock_status; }
        public int GetRating() { return rating; }
        public String GetReviews() { return reviews; }
        public RelatedProduct[] GetRelatedProducts() {
            return related_products;
        }

        public class RelatedProduct {
            int product_id;
            boolean thumb2; // To be changed to string when "false" is changed to "null"
            String[] labels;
            String name;
            String description;
            String price;
            String special;
            boolean date_end; // To be changed to string when "false" is changed to "null"
            boolean tax; // To be changed to string when "false" is changed to "null"
            int rating;
            String thumb_image;

            String GetName() { return name; }
            String GetPrice() {
                return price;
            }
            String GetSpecial() {
                return special;
            }
            String GetThumbImage() {
                return thumb_image;
            }
        }

        public int GetPrimaryOptionsCount () {
            return options[CommonDefinitions.PRIMARY_OPTION_ID].GetOptionsCount();
        }

        public String GetPrimaryOptionName(int optionIdx) {
            return options[CommonDefinitions.PRIMARY_OPTION_ID].GetOptionName(optionIdx);
        }
        public String GetPrimaryOptionImage(int optionIdx) {
            return options[CommonDefinitions.PRIMARY_OPTION_ID].GetOptionImageURL(optionIdx);
        }
        public String GetPrimaryOptionPrice(int optionIdx) {
            return options[CommonDefinitions.PRIMARY_OPTION_ID].GetOptionPrice(optionIdx);
        }
        public String GetPrimaryOptionSpecialPrice (int optionIdx) {
            return options[CommonDefinitions.PRIMARY_OPTION_ID].GetOptionSpecialPrice(optionIdx);
        }
        public int GetPrimaryOptionId() {
            return options[CommonDefinitions.PRIMARY_OPTION_ID].GetProductOptionId();
        }
        public int GetPrimaryOptionValueId(int optionIdx) {
            return options[CommonDefinitions.PRIMARY_OPTION_ID].GetProductOptionValueId(optionIdx);
        }

        public ProductOptionsData CloneProductOptionsData(int optionIdx) {
            return (new ProductOptionsData(options[optionIdx]));
        }

    }
}

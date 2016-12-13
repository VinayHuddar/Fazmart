package com.fazmart.androidapp;

/**
 * Created by Vinay on 07-06-2015.
 */
public class PerfCategoryData {
    /*private CategoryArray mCategoryTree;

    public CategoryArray GetCategoryTree() {
        return mRootCategory;
    }

    public class CategoryArray {
        Category[] categories;
    }*/

    Category[] categories;
    public class Category {
        // Category Data
        int category_id;
        String name;
        String description;
        String thumb_image;
        int total_products;
        Category[] categories;

        // Accessors
        /*public int GetId() {
            return mId;
        }

        public String GetName() {
            return mName;
        }

        public String GetImageURL() {
            return mImageURL;
        }

        public int GetTotalProducts() {
            return mTotalProducts;
        }

        public Category[] GetSubCategories() {
            return mSubCats;
        }
*/
    }

    /*public class CategoryArray {
        @SerializedName("countries")
        Category[] categoryData;
    }

    public class Category {
        // Category Data
        @SerializedName("country_id")
        int country_id;
        @SerializedName("name")
        String mName;
        @SerializedName("iso_code_2")
        String iso_code_2;
        @SerializedName("iso_code_3")
        String iso_code_3;
        @SerializedName("address_format")
        String address_format;
        @SerializedName("postcode_required")
        boolean postcode_required;
    }*/
}
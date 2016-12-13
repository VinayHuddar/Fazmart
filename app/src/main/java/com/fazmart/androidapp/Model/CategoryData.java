package com.fazmart.androidapp.Model;

import java.util.HashMap;

/**
 * Created by Vinay on 11-06-2015.
 */

public class CategoryData {
    private static String ROOT_CATEGORY_NAME = "Grocery Store";

    // Make this class a Singleton
    private static CategoryData instance = null;
    public static CategoryData GetInstance() {
        if (instance == null)
            instance = new CategoryData();
        return instance;
    }

    // Constructor
    public CategoryData () {
        mCategoryTreeHash = new HashMap<Integer, CategoryData.Category>();
    }

    // Root Category - a dummy category
    Category mRootCategory = null;
    public void BuildCategoryTree (MainCategories mainCategories) {
        mRootCategory = new CategoryData.Category(0, ROOT_CATEGORY_NAME, null, null, 0, mainCategories.GetMainCategories());
        BuildCategoryHash (mainCategories);
    }
    public Category GetRootCategory() {
        return mRootCategory;
    }

    // A hashmap of all the categories to enable quick access of categories
    HashMap<Integer, Category> mCategoryTreeHash;
    void BuildCategoryHash (CategoryData.MainCategories mainCategories) {
        CategoryData.Category[] topLevelCats = mainCategories.GetMainCategories();
        int numTopLevelCats = topLevelCats.length;
        for (int topCatIdx = 0; topCatIdx < numTopLevelCats; topCatIdx++) {
            CategoryData.Category topCatObj = topLevelCats[topCatIdx];
            mCategoryTreeHash.put(topCatObj.GetId(), topCatObj);

            // Get first level sub categories
            CategoryData.Category[] firstLevelSubCats = topCatObj.GetSubCategories();
            if (firstLevelSubCats.length == 0)
                continue;

            int numFirstLevelSubCats = firstLevelSubCats.length;
            for (int firstLevelCatIdx = 0; firstLevelCatIdx < numFirstLevelSubCats; firstLevelCatIdx++) {
                CategoryData.Category firstLevelCatObj = firstLevelSubCats[firstLevelCatIdx];
                mCategoryTreeHash.put(firstLevelCatObj.GetId(), firstLevelCatObj);

                // Get second level sub categories
                CategoryData.Category[] secondLevelSubCats = firstLevelCatObj.GetSubCategories();
                if (secondLevelSubCats.length == 0)
                    continue;

                int numSecondLevelSubCats = secondLevelSubCats.length;
                for (int secondLevelCatIdx = 0; secondLevelCatIdx < numSecondLevelSubCats; secondLevelCatIdx++) {
                    CategoryData.Category secondLevelCatObj = secondLevelSubCats[secondLevelCatIdx];
                    mCategoryTreeHash.put(secondLevelCatObj.GetId(), secondLevelCatObj);
                }
            }
        }
    }

    // The main first-level categories obtained from the Retrofit API
    public class MainCategories {
        Category[] categories = null;
        public Category[] GetMainCategories () {
            return categories;
        }
    }

    // The Category class
    public class Category {
        // Category Data
        int category_id;
        String name;
        String description;
        String thumb_image;
        int total_products;
        Category[] categories;

        // Accessors
        public int GetId() { return category_id; }
        public String GetName() { return name; }
        public String GetImageURL() { return thumb_image; }
        public int GetTotalProducts() { return total_products; }
        public Category[] GetSubCategories() { return categories; }

        public Category(int pId, String pName, String pDescription, String pImageURL,
                        int pTotalProducts, Category[] pSubCats) {
            category_id = pId;
            name = pName;
            description = pDescription;
            thumb_image = pImageURL;
            total_products = pTotalProducts;
            categories = pSubCats;
        }
    }

    public Category GetCategory (int catId) {
        return mCategoryTreeHash.get(catId);
    }

    public Category[] GetMainCategories () { return mRootCategory.GetSubCategories(); }
    public Category GetFirstLevelCategory (int mainCatPos, int firstLevelSubCatPos) {
        return mRootCategory.GetSubCategories()[mainCatPos]
                .GetSubCategories()[firstLevelSubCatPos];
    }

    public Category GetSecondLevelCategory (int mainCatPos, int firstLevelSubCatPos, int secondLevelSubCatPos) {
        return mRootCategory.GetSubCategories()[mainCatPos]
                .GetSubCategories()[firstLevelSubCatPos]
                .GetSubCategories()[secondLevelSubCatPos];
    }

    public int GetProductCount (int catId) {
        return mCategoryTreeHash.get(catId).total_products;
    }

    // Used only for testing purposes
    public void ResetCategoryTree () {
        mRootCategory = null;
        mCategoryTreeHash.clear();
    }

}


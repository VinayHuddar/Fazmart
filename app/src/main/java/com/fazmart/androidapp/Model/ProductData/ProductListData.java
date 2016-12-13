package com.fazmart.androidapp.Model.ProductData;

/**
 * Created by Vinay on 08-06-2015.
 */
public class ProductListData {
    CategoryData category;
    private class CategoryData {
        int category_id;
        String name;
        String description;
        String thumb_image;
        ProductData[] products;
        Path[] path;
        FilterGroup[] filtergroups;
    }

    public ProductData[] GetProductsData () {
        return category.products;
    }

    public class Path {
        int id;
        String name;
        int level;
        String path;
    }

    private class FilterGroup {
    }
}

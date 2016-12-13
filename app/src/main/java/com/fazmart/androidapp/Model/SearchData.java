package com.fazmart.androidapp.Model;

import com.fazmart.androidapp.Model.ProductData.ProductData;

/**
 * Created by Vinay on 08-06-2015.
 */
public class SearchData {
    int count;   // Should be converted to int when the API is updated
    ProductData[] products;

    public int GetCount () { return count; }
    public ProductData[] GetProductsData () {
        return products;
    }
}

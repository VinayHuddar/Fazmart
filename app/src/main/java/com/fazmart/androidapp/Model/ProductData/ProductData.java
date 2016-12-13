package com.fazmart.androidapp.Model.ProductData;

import com.fazmart.androidapp.Common.CommonDefinitions;

/**
 * Created by Vinay on 12-06-2015.
 */
public class ProductData {
    int product_id;
    Label[] labels;
    String name;
    ProductOptionsData[] options;
    AttributeGroup[] attribute_groups;
    int minimum;
    String manufacturer;
    int manufacturer_id;
    String mhref;
    String description;
    String price;
    String special;
    String date_end;
    String tax;
    int rating;
    String thumb_image;
    String thumb_image2;

    private class Label {
        String sale;
    }

    public String GetName() { return name; }
    public int GetProductId() { return product_id; }
    public String GetPrice() { return price; }
    public String GetImageURL() { return thumb_image; }
    public ProductOptionsData[] GetProductOptions () { return options; }
    public String GetSpecialPrice () { return special; }
    public String GetManufacturer () { return manufacturer; }

    public int GetPrimaryOptionsCount () { return options[CommonDefinitions.PRIMARY_OPTION_ID].GetOptionsCount(); }
    public String GetPrimaryOptionName(int optionIdx) {
        return options[CommonDefinitions.PRIMARY_OPTION_ID].GetOptionName(optionIdx);
    }
    public String GetPrimaryOptionImage(int optionIdx) {
        return options[CommonDefinitions.PRIMARY_OPTION_ID].GetOptionImageURL(optionIdx);
    }
    public String GetPrimaryOptionPrice(int optionIdx) {
        return options[CommonDefinitions.PRIMARY_OPTION_ID].GetOptionPrice(optionIdx);
    }
    public int GetPrimaryOptionId() {
        return options[CommonDefinitions.PRIMARY_OPTION_ID].GetProductOptionId();
    }
    public int GetPrimaryOptionValueId(int optionIdx) {
        return options[CommonDefinitions.PRIMARY_OPTION_ID].GetProductOptionValueId(optionIdx);
    }
    public String GetPrimaryOptionSpecialPrice (int optionIdx) {
        return options[CommonDefinitions.PRIMARY_OPTION_ID].GetOptionSpecialPrice(optionIdx);
    }

    public String GetPrimaryAttributeText () {
        return attribute_groups[CommonDefinitions.PRIMARY_ATTRIBUTE_GROUP_ID].GetAttributeText(CommonDefinitions.PRIMARY_ATTRIBUTE_ID);
    }

    // Copy constructor
    public ProductData(ProductData instanceToCopy) {
        this.product_id = instanceToCopy.product_id;
        this.labels = instanceToCopy.labels;
        this.name = instanceToCopy.name;
        this.options = instanceToCopy.options;
        this.attribute_groups = instanceToCopy.attribute_groups;
        this.minimum = instanceToCopy.minimum;
        this.manufacturer = instanceToCopy.manufacturer;
        this.manufacturer_id = instanceToCopy.manufacturer_id;
        this.mhref = instanceToCopy.mhref;
        this.description = instanceToCopy.description;
        this.price = instanceToCopy.price;
        this.special = instanceToCopy.special;
        this.date_end = instanceToCopy.date_end;
        this.tax = instanceToCopy.tax;
        this.rating = instanceToCopy.rating;
        this.thumb_image = instanceToCopy.thumb_image;
        this.thumb_image2 = instanceToCopy.thumb_image2;
    }

    public ProductOptionsData CloneProductOptionsData(int optionIdx) {
        return (new ProductOptionsData(options[optionIdx]));
    }
}

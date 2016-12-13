package com.fazmart.androidapp.Model.ProductData;

/**
 * Created by Vinay on 12-06-2015.
 */
public class ProductOptionsData {
    int product_option_id;
    ProductOptionValue[] product_option_value;
    int option_id;
    String name;
    String type;
    String value;
    boolean required;

    public int GetProductOptionId() { return product_option_id; }
    public ProductOptionValue[] GetOptionValues() { return product_option_value; }
    public int GetOptionId() { return option_id; }
    public String GetName () { return name; }
    public String GetType () { return type; }
    public String GetValue () { return value; }
    public boolean GetRequired () { return required; }

    public int GetOptionsCount() {
        return product_option_value.length;
    }
    public int GetProductOptionValueId(int optionsIdx) { return product_option_value[optionsIdx].GetProductOptionValueId(); }
    public String GetOptionName(int optionsIdx) {
        return product_option_value[optionsIdx].GetName();
    }

    public String GetOptionPrice(int optionsIdx) {
        return product_option_value[optionsIdx].GetPrice();
    }

    public String GetOptionSpecialPrice(int optionsIdx) {
        return product_option_value[optionsIdx].GetSpecial();
    }

    public String GetOptionImageURL(int optionsIdx) {
        return product_option_value[optionsIdx].GetImage();
    }

    private class ProductOptionValue {
        int product_option_value_id;
        int option_value_id;
        String name;
        String image;
        String price;
        String special;

        public int GetProductOptionValueId () { return product_option_value_id; }
        public int GetOptionValueId () { return option_value_id; }
        public String GetName() {
            return name;
        }
        public String GetImage() {
            return image;
        }
        public String GetPrice() {
            return price;
        }
        public String GetSpecial() {
            return special;
        }

        // Copy constructor
        public ProductOptionValue (ProductOptionValue pov) {
            this.product_option_value_id = pov.GetProductOptionValueId();
            this.option_value_id = pov.GetOptionValueId();
            this.name = pov.GetName();
            this.image = pov.GetImage();
            this.price = pov.GetPrice();
            this.special = pov.GetSpecial();
        }
    }

    // Copy constructor
    public ProductOptionsData (ProductOptionsData srcData) {
        this.product_option_id = srcData.GetProductOptionId();
        this.option_id = srcData.GetOptionId();
        this.name = srcData.GetName();
        this.type = srcData.GetType();
        this.value = srcData.GetValue();
        this.required = srcData.GetRequired();

        int optValCnt = srcData.GetOptionValues().length;
        this.product_option_value = new ProductOptionValue[optValCnt];
        for (int i = 0; i < optValCnt; i++) {
            this.product_option_value[i] = new ProductOptionValue(srcData.GetOptionValues()[i]);
        }
    }

}

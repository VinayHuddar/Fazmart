package com.fazmart.androidapp.Model.ProductData;

/**
 * Created by Vinay on 12-06-2015.
 */
public class AttributeGroup {
    int attribute_group_id;
    String name;
    Attribute[] attribute;

    public String GetAttributeText(int attribIdx) {
        return attribute[attribIdx].GetText();
    }

    private class Attribute {
        int attribute_id;
        String name;
        String text;

        public int GetAttributeId() {
            return attribute_id;
        }

        public String GetName() {
            return name;
        }

        public String GetText() {
            return text;
        }
    }
}

package com.fazmart.androidapp.Controller.ProductList;

import com.fazmart.androidapp.Model.ProductData.ProductData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by Vinay on 12-06-2015.
 */ // This class manages Product Id <--> Attribute mapping
public class AttributeMap {
    public AttributeMap() {
        mListIdAttributePair = new ArrayList<IdAttributePair>();
        mAttribIdxList = new ArrayList<AttributeIndexElem>();
    }

    public String[] GetAttributeList() {
        int numAttribs = mAttribIdxList.size();
        String[] attribList = new String[numAttribs];
        for (int i = 0; i < numAttribs; i++) {
            attribList[i] = String.format("%s (%d)", mAttribIdxList.get(i).GetAttribText(), mAttribIdxList.get(i).GetElemCnt());
        }
        return attribList;
    }

    public int[] GetProductsOfAttribute(int attribIdx) {
        AttributeIndexElem attribIdxElem = mAttribIdxList.get(attribIdx);
        int[] prodIdsList = new int[attribIdxElem.GetElemCnt()];
        int endIdx = attribIdxElem.GetStartIdx() + prodIdsList.length;
        for (int i = attribIdxElem.GetStartIdx(), j = 0; i < endIdx; i++, j++) {
            prodIdsList[j] = mListIdAttributePair.get(i).GetProdId();
        }
        return prodIdsList;
    }

    // The Attribute <--> Product-ID mapping
    private ArrayList<IdAttributePair> mListIdAttributePair;

    private class IdAttributePair {
        int mProdId;
        String mAttribText;

        public IdAttributePair(int pProdId, String pAttribText) {
            mProdId = pProdId;
            mAttribText = (pAttribText == null ? "MissingAttribute" : pAttribText);
        }

        public String GetAttribText() {
            return mAttribText;
        }

        public int GetProdId() {
            return mProdId;
        }
    }

    protected void AddToMap(int prodId, ProductData prodData) {
        String attribText = prodData.GetPrimaryAttributeText();
        mListIdAttributePair.add(new IdAttributePair(prodId, attribText));
    }

    private class AttributeComparator implements Comparator<IdAttributePair> {
        @Override
        public int compare(IdAttributePair pAttrib1, IdAttributePair pAttrib2) {
            return pAttrib1.GetAttribText().compareTo(pAttrib2.GetAttribText());
        }
    }

    // Index of attributes
    private ArrayList<AttributeIndexElem> mAttribIdxList;

    private class AttributeIndexElem {
        String mAttribText;
        int mStartIdx;
        int mNumElems;

        public AttributeIndexElem(String pAttribText, int pStartIdx, int pNumElems) {
            mAttribText = pAttribText;
            mStartIdx = pStartIdx;
            mNumElems = pNumElems;
        }

        public void UpdateElemCount(int elemCnt) {
            mNumElems = elemCnt;
        }

        public String GetAttribText() {
            return mAttribText;
        }

        public int GetStartIdx() {
            return mStartIdx;
        }

        public int GetElemCnt() {
            return mNumElems;
        }
    }

    private void BuildAttribeMapIndex() {
        int idxAttribIdxList = 0, elemCnt = 1;

        if (mListIdAttributePair.size() > 0) {
            mAttribIdxList.add(new AttributeIndexElem(mListIdAttributePair.get(0).GetAttribText(), 0, 1));
            int szAttribMap = mListIdAttributePair.size();
            for (int i = 0; i < szAttribMap - 1; i++) {
                if (mListIdAttributePair.get(i).GetAttribText().equals(mListIdAttributePair.get(i + 1).GetAttribText())) {
                    elemCnt++;
                } else {
                    mAttribIdxList.get(idxAttribIdxList).UpdateElemCount(elemCnt);
                    elemCnt = 1;
                    ++idxAttribIdxList;
                    mAttribIdxList.add(new AttributeIndexElem(mListIdAttributePair.get(i + 1).GetAttribText(), i + 1, 1));
                }
            }
            mAttribIdxList.get(idxAttribIdxList).UpdateElemCount(elemCnt);
        }
    }

    protected void AllProductsAdded() {
        Collections.sort(mListIdAttributePair, new AttributeComparator());
        BuildAttribeMapIndex();
    }

    public ArrayList<IdAttributePair> Get_ProdIdAttrib_Map() {
        return mListIdAttributePair;
    }

    void Update() { //AttributeMap attribMap) {
        //mListIdAttributePair.addAll(attribMap.Get_ProdIdAttrib_Map());
        mAttribIdxList.clear();
        AllProductsAdded();
    }
}

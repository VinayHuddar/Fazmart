package com.fazmart.androidapp.Controller.ProductList;

import com.fazmart.androidapp.Controller.Cart.CartController;
import com.fazmart.androidapp.Controller.Common.CartUpdatesHandler;
import com.fazmart.androidapp.Controller.WishListManager;
import com.fazmart.androidapp.Model.ProductData.ProductData;
import com.fazmart.androidapp.Model.ProductData.ProductOptionsData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Vinay on 20-05-2015.
 */
public class ProductListManager {
    // Implement Singleton
    private static ProductListManager instance = null;
    public static ProductListManager GetInstance ()
    {
        if(instance == null) {
            instance = new ProductListManager();
        }
        return instance;
    }

    WishListManager mWishListManager;
    CartController mCartController;
    CartUpdatesHandler mAPIHandler;
    HashMap<Integer, ProductList> mProductListHash;

    public ArrayList<ProductDataForListView> GetProductsListForCategory(int catId) {
        return (mProductListHash.get(catId) != null) ? mProductListHash.get(catId).GetProductsArray() : null;
    }

    public AttributeMap GetAttributeMapForCategory (int catId) {
        return (mProductListHash.get(catId) != null) ? mProductListHash.get(catId).GetAttributeMap() : null;
    }

    public ProductListManager () {
        mWishListManager = WishListManager.GetInstance();
        mCartController = CartController.GetInstance();
        mAPIHandler = new CartUpdatesHandler ();

        mProductListHash = new HashMap<Integer, ProductList>();
    }

    private class ProductList {
        ArrayList<ProductDataForListView> mProductsArray;
        HashMap<Integer, ProductDataForListView> mProductsHash;
        AttributeMap mAttributeMap;

        ArrayList<Integer> mProductsInWishList;
        ArrayList<Integer> mProductsInCart;

        public ProductList (ProductDataForListView[] products, AttributeMap attribMap) {
            int productCount = products.length;

            // Create and initialize array
            mProductsArray = new ArrayList<ProductDataForListView>(productCount);
            mProductsArray.addAll(Arrays.asList(products));

            // Create and initialize HashMap
            mProductsHash = new HashMap<Integer, ProductDataForListView>(productCount);
            for (int prodIdx = 0; prodIdx < productCount; prodIdx++)
                mProductsHash.put(products[prodIdx].GetProductId(), products[prodIdx]);

            // Save a reference to the attribute map
            mAttributeMap = attribMap;
            mAttributeMap.AllProductsAdded();

            // Create arrays for products in Wishlist and Cart
            mProductsInWishList = new ArrayList<Integer>();
            mProductsInCart = new ArrayList<Integer>();

            // Other initializations
            mWishListManager = WishListManager.GetInstance();
            mCartController = CartController.GetInstance();
        }

        void AddItems (ProductDataForListView[] newProds) { //}, AttributeMap attribMap) {
            mProductsArray.addAll(Arrays.asList(newProds));
            int itemCount = newProds.length;
            for (int prodIdx = 0; prodIdx < itemCount; prodIdx++)
                mProductsHash.put(newProds[prodIdx].GetProductId(), newProds[prodIdx]);

            mAttributeMap.Update(); //attribMap);    // Not Implemented yet
        }

        // Accessors
        public ArrayList<ProductDataForListView> GetProductsArray () { return mProductsArray; }
        public HashMap<Integer, ProductDataForListView> GetProductsHash () { return mProductsHash; }
        public AttributeMap GetAttributeMap () { return mAttributeMap; }

        public ArrayList<Integer> GetProductsInWishList () { return mProductsInWishList; }
        public ArrayList<Integer> GetProductsInCart () { return mProductsInCart; }

        void RemoveProductFromLocalCartList (int prodId) {
            for (Integer prod: mProductsInCart)
                if (prod == prodId) {
                    mProductsInCart.remove(prod);
                    break;
                }
        }
    }

    int mSearchCount = 0;
    public void SetSearchProductCount (int count) {
        mSearchCount = count;
    }
    public int GetSearchProductCount () {
        return mSearchCount;
    }

    public void ProcessRetrofitData (ProductData[] products, int catId, boolean append) {
        int numProducts = products.length;
        ProductDataForListView[] prodList = new ProductDataForListView[numProducts];
        AttributeMap attribMap;
        int attribStartIdx;

        if (append) {
            attribMap = mProductListHash.get(catId).GetAttributeMap();
            attribStartIdx = mProductListHash.get(catId).GetProductsArray().size();
        } else {
            attribMap = new AttributeMap();
            attribStartIdx = 0;
        }

        for (int prodIdx = 0; prodIdx < numProducts; prodIdx++) {
            prodList[prodIdx] = new ProductDataForListView (products[prodIdx]);
            attribMap.AddToMap (attribStartIdx + prodIdx, products[prodIdx]);
        }

        if (append)
            mProductListHash.get(catId).AddItems (prodList); //, attribMap);
        else
            mProductListHash.put(catId, new ProductList(prodList, attribMap));

        SyncWithCartAndWishList(catId);
    }

    public boolean SyncWithCartAndWishList (int catId) {
        HashMap<Integer, ProductDataForListView> productsList = mProductListHash.get(catId).GetProductsHash();
        boolean statusChanged = false;

        // Sync with cart
        Integer[] prodsInCartIDs = mCartController.GetProductsInCart();
        for (int prodId : prodsInCartIDs) {
            if (productsList.containsKey(prodId)) {
                ProductDataForListView product =  productsList.get(prodId);
                //ProductOptionsData optionsData = product.GetProductOptionsData();
                int optionCount = product.GetOptionsCount();
                for (int optIdx = 0; optIdx < optionCount; optIdx++) {
                    int qtyOfProdInCart = mCartController.GetItemQuantity(prodId, product.GetProductOptionValueId(optIdx));
                    if (product.GetQuantityInCart(optIdx) != qtyOfProdInCart) {
                        UpdateQuantityInCart(catId, prodId, optIdx, qtyOfProdInCart);
                        statusChanged = true;
                    }
                }
            }
        }

        // Remove items from the categories internal cart account if it was removed from the cart
        ArrayList<Integer> prodsInCart = mProductListHash.get(catId).GetProductsInCart();
        List<Integer> prodsToRemove = new ArrayList<Integer>();
        if (prodsInCart.size() > 0) {
            for (Integer prod : prodsInCart) {
                if (mCartController.IsProductPresentInCart(prod) == false) {
                    mProductListHash.get(catId).GetProductsHash().get(prod).ZeroOutQuantity();
                    prodsToRemove.add(prod);        // A separate removal-list is maintained to avoid ConcurrentModificationException
                    statusChanged = true;
                }
            }
        }
        if (prodsToRemove.size() > 0) {
            prodsInCart.removeAll(prodsToRemove);
            prodsToRemove.clear();
        }

        // Sync with WishList
        ArrayList<Integer> prodsInWishList = mProductListHash.get(catId).GetProductsInWishList();
        Integer[] wishListedProdIds = mWishListManager.GetWishListedProducts();
        // Update items that are not in the category's wish list
        for (int wishListedProd : wishListedProdIds) {
            if (productsList.containsKey(wishListedProd)) {
                InitWishListStatus(catId, wishListedProd);
                statusChanged = true;
            }
        }

        // Update items that are in the category wish list
        if (prodsInWishList.size() > 0) {
            for (Integer prod : prodsInWishList) {
                if (mWishListManager.IsProductPresentInWishList(prod) == false) {
                    mProductListHash.get(catId).GetProductsHash().get(prod).SetWishListStatus(false);
                    prodsToRemove.add(prod);
                    statusChanged = true;
                }
            }
        }
        if (prodsToRemove.size() > 0) {
            prodsInCart.removeAll(prodsToRemove);
            prodsToRemove.clear();
        }

        return statusChanged;
    }

    public ProductListManager.ProductDataForListView GetProduct(int catId, int prodId) {
        return mProductListHash.get(catId).GetProductsHash().get(prodId);
    }

    public boolean WasCategoryViewed (int catId) {
        return mProductListHash.containsKey(catId);
    }

    public void UpdateQuantityInCart(int catId, int prodId, int optIdx, int newQty) {
        ProductDataForListView product = mProductListHash.get(catId).GetProductsHash().get(prodId);
        int prodOptValId = product.GetProductOptionValueId(optIdx);

        product.UpdateQuantity(newQty, prodOptValId, catId);

        /*// If the product is newly added to cart, add a new entry to the products lists internal cart account
        if ((product.GetTotalQuantityInCart() == 0) && (newQty > 0))
            mProductListHash.get(catId).GetProductsInCart().add(((Integer) prodId));

        product.UpdateQuantity(newQty, optIdx);

        // If the product is removed from the cart, remove it entry to the products lists internal cart account
        if (product.GetTotalQuantityInCart() == 0)
            mProductListHash.get(catId).RemoveProductFromLocalCartList(prodId);
            */
    }

    public void AddProductToWishList (int catId, int prodId) {
        ProductDataForListView product = mProductListHash.get(catId).GetProductsHash().get(prodId);
        product.SetWishListStatus(true);
        mProductListHash.get(catId).GetProductsInWishList().add((Integer) prodId);
        mWishListManager.AddToWishList(prodId);
    };

    public void RemoveProductFromWishList (int catId, int prodId) {
        ProductDataForListView product = mProductListHash.get(catId).GetProductsHash().get(prodId);
        product.SetWishListStatus(false);
        mProductListHash.get(catId).GetProductsInWishList().remove((Integer) prodId);
        mWishListManager.RemoveFromWishList(prodId);
    };

    // This function is invoked from WishListManager when the list is initially created and
    // synced with the wishlist. Note the absence of a call back to mWishListManager.AddToWishList(mProductId);
    public void InitWishListStatus (int catId, int prodId) {
        ProductDataForListView product = mProductListHash.get(catId).GetProductsHash().get(prodId);
        product.SetWishListStatus(true);
        if (mProductListHash.get(catId).GetProductsInWishList().contains(((Integer) prodId)) == false)
            mProductListHash.get(catId).GetProductsInWishList().add(prodId);
    }

    public class ProductDataForListView {
        ProductData mProductData;
        public ProductData GetProductData () { return mProductData; }
        public int GetOptionsCount() { return mProductData.GetPrimaryOptionsCount(); }

        public ProductDataForListView(ProductData prodData) {
            //mProductData = new ProductData(prodData);
            mProductData = prodData;

            int optionCnt = mProductData.GetPrimaryOptionsCount();
            mQuantityInCart = new int[optionCnt];
            for (int i = 0; i < optionCnt; i++)
                mQuantityInCart[i] = 0;

            mOptionSpinnerPosition = 0;
        }

        // Accessors
        public String GetName() { return mProductData.GetName(); }
        public int GetProductId() {
            return mProductData.GetProductId();
        }
        public String GetPrice(int optionIdx) {
            return mProductData.GetPrimaryOptionPrice(optionIdx);
        }
        public int GetProductOptionValueId(int optionIdx) {
            return mProductData.GetPrimaryOptionValueId(optionIdx);
        }
        public int GetProductOptionId() {
            return mProductData.GetPrimaryOptionId();
        }
        public String GetImageURL() {
            return mProductData.GetImageURL();
        }
        public String GetImageURL(int optionIdx) {
            return mProductData.GetPrimaryOptionImage(optionIdx);
        }
        public String GetSpecial(int optionIdx) {
            return mProductData.GetPrimaryOptionSpecialPrice(optionIdx);
        }
        public String GetManufacturer() { return mProductData.GetManufacturer(); }

        public ProductOptionsData[] GetProductOptionsData() {
            return mProductData.GetProductOptions();    // Revisit this while refactoring CartController and ProdictDetailDataManager
        }

        // Quantity-in-cart doesn't come from the server. It is maintained locally
        int[] mQuantityInCart;
        public int GetQuantityInCart(int option) {
            return mQuantityInCart[option];
        }
        public boolean IsPresentInCart(int option) {
            return (mQuantityInCart[option] != 0);
        }
        public void UpdateQuantity(int newQty, int prodOptValId, int catId) {
            int prevTotalQty = GetTotalQuantityInCart();
            for (int i = 0; i < mQuantityInCart.length; i++) {
                if (GetProductOptionValueId(i) == prodOptValId) {
                    mQuantityInCart[i] = newQty;
                    break;
                }
            }

            if ((GetTotalQuantityInCart() == 0) && (prevTotalQty > 0))
                RemoveFromLocalCartList (catId, mProductData.GetProductId());
            if ((newQty > 0) && (prevTotalQty == 0))
                AddToLocalCartList(catId, mProductData.GetProductId());
        }

        public int GetTotalQuantityInCart () {
            int optCnt = mProductData.GetPrimaryOptionsCount();
            int totalQty = 0;
            for( int i = 0; i < optCnt; i++)
                totalQty += mQuantityInCart[i];
            return totalQty;
        }
        public void ZeroOutQuantity () {
            int optCnt = mProductData.GetPrimaryOptionsCount();
            for( int i = 0; i < optCnt; i++)
                mQuantityInCart[i] = 0;
        }

        int mOptionSpinnerPosition;
        public void SetOptionSelection (int option) { mOptionSpinnerPosition = option; }
        public int GetOptionSelection () { return mOptionSpinnerPosition; }

        // Status-in-wish-list doesn't come from the server. It is maintained locally
        boolean mAddedToWishList;
        public boolean IsAddedToWishList () { return mAddedToWishList; };
        void SetWishListStatus (boolean status) {
            mAddedToWishList = status;
        }

        public void Update_AddedToWishList_Status (boolean wishListStatus) {
            mAddedToWishList = wishListStatus;
        }
    }

    public void AddToLocalCartList (int catId, int prodId) {
        mProductListHash.get(catId).GetProductsInCart().add(((Integer) prodId));
    }


    public void RemoveFromLocalCartList (int catId, int prodId) {
        mProductListHash.get(catId).RemoveProductFromLocalCartList(prodId);
    }

    /*class APIHandler {
        Handler handler = new Handler();
        Timer timer = new Timer();
        long startTime;

        int postTapCount = 0;
        int putTapCount = 0;

        int putRetryCnt = 0;
        int postRetryCnt = 0;

        void PutProduct(int cat_id, int prod_id, int opt_idx, int quantity, ProductListAdapter listAdapter) {
            final int catId = cat_id;
            final int prodId = prod_id;
            final int optIdx = opt_idx;
            final int qty = quantity;
            final ProductListAdapter prodListAdapter = listAdapter;
            final ProductDataForListView product = GetProduct(catId, prodId);
            final int optValId = product.GetProductOptionValueId(optIdx);

            if (putTapCount == 0) {
                startTime = System.currentTimeMillis();
                putTapCount++;
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        handler.post(new Runnable() {
                            public void run() {
                                int newQty = mCartController.GetItemQuantity(prodId, optValId) + putTapCount * qty;
                                final int quantity = newQty < 0 ? 0 : newQty;
                                final int putTapCountSnapShot = putTapCount;
                                FazmartApplication.GetAPIService().PutProduct(mCartController.GetItemKey(prodId, optValId),
                                        quantity, new Callback<CartData>() {
                                            @Override
                                            public void success(CartData cartData, Response response) {
                                                product.UpdateQuantity(quantity, optIdx);

                                                // Update Cart Reference
                                                mCartController.SetCartReference(cartData);
                                                prodListAdapter.notifyDataSetChanged();

                                                if (putTapCount != putTapCountSnapShot) {
                                                    int tapDiff = putTapCount - putTapCountSnapShot;
                                                    putTapCount = 0;
                                                    AddToCart(catId, prodId, optIdx, tapDiff * qty, prodListAdapter);
                                                } else {
                                                    putTapCount = 0;
                                                }
                                            }

                                            @Override
                                            public void failure(RetrofitError error) {
                                                int tapCnt = putTapCount;
                                                putTapCount = 0;
                                                RetryPut(product, mCartController.GetItemKey(prodId, optValId), optIdx, quantity, prodListAdapter, tapCnt);
                                            }
                                        });
                            }
                        });
                    }
                }, CommonDefinitions.QUICK_TAP_AGGRE_THRESHOLD_TIME_MS);
            } else if (((System.currentTimeMillis() - startTime) < CommonDefinitions.QUICK_TAP_AGGRE_THRESHOLD_TIME_MS)
                    || (putTapCount > 0)) {
                putTapCount++;
            }
        }

        void RetryPut(final ProductDataForListView product, final String prodKey, final int optIdx, final int quantity,
                      final ProductListAdapter prodListAdapter, final int tapCnt) {
            FazmartApplication.GetAPIService().PutProduct(prodKey, quantity, new Callback<CartData>() {
                @Override
                public void success(CartData cartData, Response response) {
                    product.UpdateQuantity(quantity, optIdx);

                    // Update Cart Reference
                    mCartController.SetCartReference(cartData);
                    prodListAdapter.notifyDataSetChanged();
                }
                @Override
                public void failure(RetrofitError error) {
                    putRetryCnt++;
                    if (putRetryCnt < 5)
                        RetryPut(product, prodKey, optIdx, quantity, prodListAdapter, tapCnt);
                    else
                        putRetryCnt = 0;
                }
            });
        }

        void PostProduct(int cat_id, int prod_id, int opt_idx, int quantity, ProductListAdapter listAdapter) {
            final int catId = cat_id;
            final int prodId = prod_id;
            final int optIdx = opt_idx;
            final int qty = quantity;
            final ProductListAdapter prodListAdapter = listAdapter;
            final ProductDataForListView product = GetProduct(catId, prodId);

            if (postTapCount == 0) {
                startTime = System.currentTimeMillis();
                postTapCount++;
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        handler.post(new Runnable() {
                            public void run() {
                                final int quantity = postTapCount * qty;
                                final int postTapCountSnapShot = postTapCount;
                                final int prodOptId = product.GetProductOptionsData()[CommonDefinitions.PRIMARY_OPTION_ID].GetProductOptionId();
                                final int prodOptValId = product.GetProductOptionsData()[CommonDefinitions.PRIMARY_OPTION_ID].GetProductOptionValueId(product.GetOptionSelection());
                                FazmartApplication.GetAPIService().PostProduct(product.GetProductId(), quantity,
                                        prodOptId, prodOptValId, new Callback<CartData>() {
                                            @Override
                                            public void success(CartData cartData, Response response) {
                                                product.UpdateQuantity(quantity, optIdx);

                                                // Update Cart Reference
                                                mCartController.SetCartReference(cartData);
                                                prodListAdapter.notifyDataSetChanged();

                                                if (postTapCount != postTapCountSnapShot) {
                                                    int tapDiff = postTapCount - postTapCountSnapShot;
                                                    postTapCount = 0;
                                                    AddToCart(catId, prodId, optIdx, tapDiff * qty, prodListAdapter);
                                                } else {
                                                    postTapCount = 0;
                                                }
                                            }

                                            @Override
                                            public void failure(RetrofitError error) {
                                                int tapCnt = postTapCount;
                                                postTapCount = 0;
                                                RetryPost(product, prodId, quantity, prodOptId, prodOptValId, optIdx, prodListAdapter, tapCnt);
                                            }
                                        });
                            }
                        });
                    }
                }, CommonDefinitions.QUICK_TAP_AGGRE_THRESHOLD_TIME_MS);
            } else if (((System.currentTimeMillis() - startTime) < CommonDefinitions.QUICK_TAP_AGGRE_THRESHOLD_TIME_MS)
                    || (postTapCount > 0)) {
                postTapCount++;
            }
        }

        void RetryPost(final ProductDataForListView product, final int prodId, final int quantity, final int prodOptId, final int prodOptValId,
                       final int optIdx, final ProductListAdapter prodListAdapter, final int tapCnt) {
            FazmartApplication.GetAPIService().PostProduct(prodId, quantity, prodOptId, prodOptValId,
                    new Callback<CartData>() {
                        @Override
                        public void success(CartData cartData, Response response) {
                            product.UpdateQuantity(quantity, optIdx);

                            // Update Cart Reference
                            mCartController.SetCartReference(cartData);
                            prodListAdapter.notifyDataSetChanged();
                        }

                        @Override
                        public void failure(RetrofitError error) {
                            postRetryCnt++;
                            if (postRetryCnt < 5)
                                RetryPost(product, prodId, quantity, prodOptId, prodOptValId, optIdx, prodListAdapter, tapCnt);
                            else
                                postRetryCnt = 0;
                        }
                    });
        }
    }*/
}

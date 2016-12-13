package com.fazmart.androidapp.Controller;

import android.content.Context;
import android.os.Handler;

import com.fazmart.androidapp.Common.CommonDefinitions;
import com.fazmart.androidapp.Common.Events.EventProductDetailsFetched;
import com.fazmart.androidapp.Controller.Cart.CartController;
import com.fazmart.androidapp.Controller.Category.CategoryController;
import com.fazmart.androidapp.Controller.Common.CartUpdatesHandler;
import com.fazmart.androidapp.Controller.ProductList.ProductListManager;
import com.fazmart.androidapp.FazmartApplication;

import de.greenrobot.event.EventBus;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by Admin on 08-05-2015.
 */
public class ProductDetailDataController {
    // Implement Singleton
    private static ProductDetailDataController instance = null;
    public static ProductDetailDataController GetInstance ()
    {
        if(instance == null) {
            instance = new ProductDetailDataController();
        }
        return instance;
    }

    int mCategoryId;
    Context mParentContext;
    ProductListManager.ProductDataForListView mRefToProdInListView;

    ProductListManager mProductListManager;
    CategoryController mCategoryController;
    CartController mCartController;
    WishListManager mWishListManager;

    CartUpdatesHandler mAPIHandler;

    public ProductDetailDataController() {
        mCategoryId = 0;
        mRefToProdInListView = null;
        mParentContext = null;

        mProductListManager = ProductListManager.GetInstance();
        mCategoryController = CategoryController.GetInstance();
        mCartController = CartController.GetInstance();
        mWishListManager = WishListManager.GetInstance();

        mAPIHandler = new CartUpdatesHandler();
    }

    int retryCnt = 0;
    public void FetchProductDetailData(int cat_Id, int prod_id, final int opt_idx) {
        final int catId = cat_Id;
        final int prodId = prod_id;
        final int optIdx = opt_idx;
        FazmartApplication.GetAPIService().GetProductDetails(prodId, new Callback<com.fazmart.androidapp.Model.ProductData.ProductDetailData>() {
            @Override
            public void success(com.fazmart.androidapp.Model.ProductData.ProductDetailData productDetailData, Response response) {
                mCurrentProductData = new ProductDetailData(catId, productDetailData);
                mCurrentProductData.SetOptionSelection(optIdx);
                EventBus.getDefault().post(new EventProductDetailsFetched());
                retryCnt = 0;
            }

            @Override
            public void failure(RetrofitError error) {
                retryCnt++;
                if (retryCnt < 5)
                    FetchProductDetailData(catId, prodId, optIdx);
                else
                    retryCnt = 0;
            }
        });
    }

    public void AddToCart(int quantity, int optionIdx) {
        com.fazmart.androidapp.Model.ProductData.ProductDetailData.Product prodDetailData =
                mCurrentProductData.GetProductDetailData();

        if (mCartController.IsProductPresentInCart (prodDetailData.GetProductId(),
                prodDetailData.GetPrimaryOptionValueId(optionIdx))) {
            mAPIHandler.PutProduct(prodDetailData.GetProductId(), prodDetailData.GetPrimaryOptionValueId(optionIdx),
                    quantity);
        } else {
            mAPIHandler.PostProduct(prodDetailData.GetProductId(), prodDetailData.GetPrimaryOptionId(),
                    prodDetailData.GetPrimaryOptionValueId(optionIdx), quantity);
        }
    }

    public void RemoveFromCart(int optionIdx) {
        com.fazmart.androidapp.Model.ProductData.ProductDetailData.Product prodDetailData =
                mCurrentProductData.GetProductDetailData();
        mAPIHandler.PutProduct(prodDetailData.GetProductId(), prodDetailData.GetPrimaryOptionValueId(optionIdx), -1);
    };

    ProductDetailData mCurrentProductData;
    public ProductDetailData GetCurrentProductData () {
        return mCurrentProductData;
    }

    // An object of this class is created for each product that is viewed in product detail screen.
    // The objects are then buffered for later use
    public class ProductDetailData {
        com.fazmart.androidapp.Model.ProductData.ProductDetailData.Product mProductDetailData;
        public com.fazmart.androidapp.Model.ProductData.ProductDetailData.Product GetProductDetailData () { return mProductDetailData; }

        // This one doesn't come from the server; it is maintained locally
        int[] mQuantityInCart;
        public void UpdateQuantityInCart(int prodOptValId, int newQty) {
            for (int i = 0; i < mQuantityInCart.length; i++) {
                if (mProductDetailData.GetPrimaryOptionValueId(i) == prodOptValId) {
                    mQuantityInCart[i] = newQty;
                    break;
                }
            }
        };
        public int GetQuantityInCart(int option) { return mQuantityInCart[option]; };
        public String GetTitle () { return mProductDetailData.GetTitle(); }
        public boolean IsPresentInCart(int option) { return (mQuantityInCart[option] != 0); };

        // Status-in-wish-list doesn't come from the server. It is maintained locally
        boolean mAddedToWishList;
        public boolean IsAddedToWishList () { return mAddedToWishList; };
        public void AddToWishList () {
            mAddedToWishList = true;
            mWishListManager.AddToWishList(mProductDetailData.GetProductId());
        };

        public void RemoveFromWishList () {
            mAddedToWishList = false;
            mWishListManager.RemoveFromWishList(mProductDetailData.GetProductId());
        };

        // This function is invoked from WishListManager when the list is initially created and
        // synced with the wishlist. Note the absence of a call back to mWishListManager.AddToWishList(mProductId);
        public void InitWishListStatus () {
            mAddedToWishList = true;
        }

        int mOptionSpinnerPosition;
        public void SetOptionSelection (int option) { mOptionSpinnerPosition = option; }
        public int GetOptionSelection () { return mOptionSpinnerPosition; }

        public ProductDetailData (int catId, com.fazmart.androidapp.Model.ProductData.ProductDetailData prodData) {
            mProductDetailData = prodData.GetProduct();

            int prodId = prodData.GetProduct().GetProductId();
            if (catId == CommonDefinitions.DUMMY_CATEGORY_ID_FOR_CART) {
                // Get quantity-in-cart
                int numOptions = mProductDetailData.GetPrimaryOptionsCount();
                mQuantityInCart = new int[numOptions];
                for (int i = 0; i < numOptions; i++) {
                    mQuantityInCart[i] = mCartController.GetItemQuantity(mProductDetailData.GetProductId(),
                            mProductDetailData.GetPrimaryOptionValueId(i));
                }

                mAddedToWishList = mWishListManager.IsProductPresentInWishList(prodId);
            }
            else {
                ProductListManager.ProductDataForListView dataFromProductList = mProductListManager.GetProduct(catId, prodId);

                // Read options from pre-fetched product data
                int numOptions = mProductDetailData.GetPrimaryOptionsCount();
                mQuantityInCart = new int[numOptions];
                for (int i = 0; i < numOptions; i++) {
                    mQuantityInCart[i] = dataFromProductList.GetQuantityInCart(i);
                }

                mAddedToWishList = dataFromProductList.IsAddedToWishList();
            }
        }
    }

/*    class APIHandler {
        Handler handler = new Handler();
        Timer timer = new Timer();
        long startTime;

        int postTapCount = 0;
        int putTapCount = 0;

        int putRetryCnt = 0;
        int postRetryCnt = 0;

        void PutProductToCart(int quantity, int optionIdx) {
            final com.fazmart.androidapp.Model.ProductData.ProductDetailData.Product prodDetailData =
                    mCurrentProductData.GetProductDetailData();
            final int qty = quantity;
            final int optIdx = optionIdx;
            final int prodId = prodDetailData.GetProductId();
            final int optValId = prodDetailData.GetPrimaryOptionValueId(optionIdx);

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
                                                mCurrentProductData.UpdateQuantityInCart(optIdx, quantity);

                                                // Update Cart Reference
                                                mCartController.SetCartReference(cartData);
                                                EventBus.getDefault().post(new EventItemAddedToCart());

                                                if (putTapCount != putTapCountSnapShot) {
                                                    int tapDiff = putTapCount - putTapCountSnapShot;
                                                    putTapCount = 0;
                                                    AddToCart(tapDiff * qty, optIdx);
                                                } else {
                                                    putTapCount = 0;
                                                }
                                            }

                                            @Override
                                            public void failure(RetrofitError error) {
                                                int tapCnt = putTapCount;
                                                putTapCount = 0;
                                                RetryPut (mCartController.GetItemKey(prodId, optValId), optIdx, quantity, tapCnt);
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

        void RetryPut(final String prodKey, final int optIdx, final int quantity, final int tapCnt) {
            FazmartApplication.GetAPIService().PutProduct(prodKey, quantity, new Callback<CartData>() {
                        @Override
                        public void success(CartData cartData, Response response) {
                            mCurrentProductData.UpdateQuantityInCart(optIdx, quantity);

                            // Update Cart Reference
                            mCartController.SetCartReference(cartData);
                            EventBus.getDefault().post(new EventItemAddedToCart());
                        }
                        @Override
                        public void failure(RetrofitError error) {
                            putRetryCnt++;
                            if (putRetryCnt < 5)
                                RetryPut(prodKey, optIdx, quantity, tapCnt);
                            else
                                putRetryCnt = 0;
                        }
                    });
        }

        void PostProduct(int quantity, int optionIdx) {
            final com.fazmart.androidapp.Model.ProductData.ProductDetailData.Product prodDetailData =
                    mCurrentProductData.GetProductDetailData();
            final int qty = quantity;
            final int optIdx = optionIdx;
            final int prodId = prodDetailData.GetProductId();
            final int prodOptValId = prodDetailData.GetPrimaryOptionValueId(optionIdx);
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
                                final int prodOptId = prodDetailData.GetProductOptionsData()[CommonDefinitions.PRIMARY_OPTION_ID].GetProductOptionId();
                                FazmartApplication.GetAPIService().PostProduct(prodId, quantity,
                                        prodOptId, prodOptValId, new Callback<CartData>() {
                                            @Override
                                            public void success(CartData cartData, Response response) {
                                                mCurrentProductData.UpdateQuantityInCart(optIdx, quantity);

                                                // Update Cart Reference
                                                mCartController.SetCartReference(cartData);
                                                EventBus.getDefault().post(new EventItemAddedToCart());

                                                if (postTapCount != postTapCountSnapShot) {
                                                    int tapDiff = postTapCount - postTapCountSnapShot;
                                                    postTapCount = 0;
                                                    AddToCart(tapDiff * qty, optIdx);
                                                } else {
                                                    postTapCount = 0;
                                                }
                                            }

                                            @Override
                                            public void failure(RetrofitError error) {
                                                int tapCnt = postTapCount;
                                                postTapCount = 0;
                                                RetryPost(prodId, quantity, prodOptId, prodOptValId, optIdx, tapCnt);
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

        void RetryPost(final int prodId, final int quantity, final int prodOptId, final int prodOptValId,
                       final int optIdx, final int tapCnt) {
            FazmartApplication.GetAPIService().PostProduct(prodId, quantity, prodOptId, prodOptValId,
                    new Callback<CartData>() {
                        @Override
                        public void success(CartData cartData, Response response) {
                            mCurrentProductData.UpdateQuantityInCart(optIdx, quantity);

                            // Update Cart Reference
                            mCartController.SetCartReference(cartData);
                            EventBus.getDefault().post(new EventItemAddedToCart());
                        }

                        @Override
                        public void failure(RetrofitError error) {
                            postRetryCnt++;
                            if (postRetryCnt < 5)
                                RetryPost(prodId, quantity, prodOptId, prodOptValId, optIdx, tapCnt);
                            else
                                postRetryCnt = 0;
                        }
                    });
        }

    }*/
}

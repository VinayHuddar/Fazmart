package com.fazmart.androidapp.Controller.Common;

import android.os.Handler;
import android.view.View;

import com.fazmart.androidapp.Common.CommonDefinitions;
import com.fazmart.androidapp.Common.Events.EventItemAddedToCart;
import com.fazmart.androidapp.Controller.Cart.CartController;
import com.fazmart.androidapp.Controller.ProductList.ProductListManager;
import com.fazmart.androidapp.Controller.ProductList.QuantityUpdateCallbacks;
import com.fazmart.androidapp.FazmartApplication;
import com.fazmart.androidapp.Model.CartData;

import java.util.Timer;
import java.util.TimerTask;

import de.greenrobot.event.EventBus;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by Vinay on 23-06-2015.
 */
public class CartUpdatesHandler {
    Handler handler = new Handler();
    Timer timer = new Timer();
    long startTime;
    boolean mRetryInProgress = false;

    int postTapCount = 0;
    int putTapCount = 0;

    int putRetryCnt = 0;
    int postRetryCnt = 0;

    QuantityUpdateCallbacks mQuantityUpdateCallbacks;
    public void setQuantityUpdateCallbacks(QuantityUpdateCallbacks quantityUpdateCallbacks) {
        mQuantityUpdateCallbacks = quantityUpdateCallbacks;
    }

    CartController mCartController = CartController.GetInstance();

    public void PutProduct(final int prodId, final int prodOptValId, final int quantity) {
        if ((putTapCount == 0) && (mRetryInProgress == false)){
            startTime = System.currentTimeMillis();
            putTapCount++;
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    handler.post(new Runnable() {
                        public void run() {
                            int newQty = mCartController.GetItemQuantity(prodId, prodOptValId) + putTapCount * quantity;
                            final int qty = newQty < 0 ? 0 : newQty;
                            final int putTapCountSnapShot = putTapCount;
                            FazmartApplication.GetAPIService().PutProduct(mCartController.GetItemKey(prodId, prodOptValId),
                                    qty, new Callback<CartData>() {
                                        @Override
                                        public void success(CartData cartData, Response response) {
                                            // Update Cart Reference
                                            mCartController.SetCartReference(cartData);
                                            EventBus.getDefault().post(new EventItemAddedToCart(prodId, prodOptValId, qty));

                                            //mQuantityUpdateCallbacks.onQuantityUpdated(prodId, prodOptValId);

                                            if (putTapCount != putTapCountSnapShot) {
                                                int tapDiff = putTapCount - putTapCountSnapShot;
                                                putTapCount = 0;
                                                // Note this is actually not a retry. Just wanted to reuse the funtion.
                                                PutMore(prodId, prodOptValId, mCartController.GetItemQuantity(prodId, prodOptValId) + quantity * tapDiff);
                                            } else {
                                                putTapCount = 0;
                                            }
                                        }

                                        @Override
                                        public void failure(RetrofitError error) {
                                            int tapCnt = putTapCount;
                                            putTapCount = 0;
                                            // We are actually not "putting more" here. Just wanted to reuse the function.
                                            PutMore(prodId, prodOptValId, mCartController.GetItemQuantity(prodId, prodOptValId) + quantity * tapCnt);
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

    void PutMore(final int prodId, final int prodOptValId, final int quantity) {
        mRetryInProgress = true;
        final String prodKey = mCartController.GetItemKey(prodId, prodOptValId);
        FazmartApplication.GetAPIService().PutProduct(prodKey, quantity, new Callback<CartData>() {
            @Override
            public void success(CartData cartData, Response response) {
                mRetryInProgress = false;

                // Update Cart Reference
                mCartController.SetCartReference(cartData);
                EventBus.getDefault().post(new EventItemAddedToCart(prodId, prodOptValId, quantity));
            }
            @Override
            public void failure(RetrofitError error) {
                putRetryCnt++;
                if (putRetryCnt < CommonDefinitions.RETRY_COUNT)
                    PutMore(prodId, prodOptValId, quantity);
                else
                    putRetryCnt = 0;
            }
        });
    }

    public void PostProduct(final int prodId, final int prodOptId, final int prodOptValId, final int quantity) {
        if (postTapCount == 0) {
            startTime = System.currentTimeMillis();
            postTapCount++;
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    handler.post(new Runnable() {
                        public void run() {
                            final int qty = postTapCount * quantity;
                            final int postTapCountSnapShot = postTapCount;
                            FazmartApplication.GetAPIService().PostProduct(prodId, qty,
                                    prodOptId, prodOptValId, new Callback<CartData>() {
                                        @Override
                                        public void success(CartData cartData, Response response) {
                                            // Update Cart Reference
                                            mCartController.SetCartReference(cartData);
                                            EventBus.getDefault().post(new EventItemAddedToCart(prodId, prodOptValId, qty));

                                            if (postTapCount != postTapCountSnapShot) {
                                                int tapDiff = postTapCount - postTapCountSnapShot;
                                                postTapCount = 0;
                                                PutMore(prodId, prodOptValId, mCartController.GetItemQuantity(prodId, prodOptValId) + quantity * tapDiff);
                                            } else {
                                                postTapCount = 0;
                                            }
                                        }

                                        @Override
                                        public void failure(RetrofitError error) {
                                            int tapCnt = postTapCount;
                                            postTapCount = 0;
                                            RetryPost(prodId, prodOptId, prodOptValId, mCartController.GetItemQuantity(prodId, prodOptValId) + quantity * tapCnt);
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

    void RetryPost(final int prodId, final int prodOptId, final int prodOptValId, final int quantity) {
        FazmartApplication.GetAPIService().PostProduct(prodId, quantity, prodOptId, prodOptValId,
                new Callback<CartData>() {
                    @Override
                    public void success(CartData cartData, Response response) {
                        // Update Cart Reference
                        mCartController.SetCartReference(cartData);
                        EventBus.getDefault().post(new EventItemAddedToCart(prodId, prodOptValId, quantity));
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        postRetryCnt++;
                        if (postRetryCnt < CommonDefinitions.RETRY_COUNT)
                            RetryPost(prodId, prodOptId, prodOptValId, quantity);
                        else
                            postRetryCnt = 0;
                    }
                });
    }

}

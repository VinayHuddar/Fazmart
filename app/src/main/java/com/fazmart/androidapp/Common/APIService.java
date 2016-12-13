package com.fazmart.androidapp.Common;

import com.fazmart.androidapp.Model.AddressData.AddressModel;
import com.fazmart.androidapp.Model.AddressData.AddressList;
import com.fazmart.androidapp.Model.CartData;
import com.fazmart.androidapp.Model.CategoryData;
import com.fazmart.androidapp.Model.DeliveryData.AccountData;
import com.fazmart.androidapp.Model.DeliveryData.ApartmentsAreaData;
import com.fazmart.androidapp.Model.DeliveryData.DeliverySlots;
import com.fazmart.androidapp.Model.DeliveryData.PaymentMethods;
import com.fazmart.androidapp.Model.OrderSummaryData;
import com.fazmart.androidapp.Model.ProductData.ProductDetailData;
import com.fazmart.androidapp.Model.ProductData.ProductListData;
import com.fazmart.androidapp.Model.SearchData;
import com.fazmart.androidapp.Model.TokenResponse;

import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Path;
import retrofit.http.Query;

/**
 * Created by Vinay on 08-06-2015.
 */
public interface APIService {
    @POST("/oauth2/token")
    void GetAuthenticationToken (@Header("Accept") String accept, @Header("Authorization") String authHdr, @Query("grant_type") String grantType, @Body String emptyString,
                                 Callback<TokenResponse> callback);

    @GET("/common/city/{cityId}")
    void GetAreaApartmentData (@Path("cityId") int cityId, Callback<ApartmentsAreaData> callBack);

    /****************** Product APIs ******************/

    @GET("/product/category")
    void GetCategoryTree (Callback<CategoryData.MainCategories> callBack);

    @GET("/product/category/{catId}")
    void GetCategoryProducts (@Path("catId") int catId, @Query("limit") int lmt, @Query("page") int pageNum,
                              Callback<ProductListData> callBack);

    @GET("/product/product/{prodId}")
    void GetProductDetails (@Path("prodId") int prodId, Callback<ProductDetailData> callBack);

    @GET("/product/search")
    void GetProductsSearched (@Query("search") String search, @Query("page") int page,
                              @Query("order") String order, Callback<SearchData> callBack);

    /****************** Cart APIs ******************/

    @GET("/cart/cart")
    void GetCart (Callback<CartData> callBack);

    @FormUrlEncoded
    @POST("/cart/product")
    void PostProduct(@Field("product_id") int prodId, @Field("quantity") int qty,
                     @Field("option_id") int option_id, @Field("option_value_id") int option_value_id,
                     Callback<CartData> callback);

    @FormUrlEncoded
    @PUT("/cart/product")
    void PutProduct (@Field("prod_key") String prodKey, @Field("prod_qty") int prodQty, Callback<CartData> callback);

    @DELETE("/cart/product/{prod_key}")
    void RemoveItemFromCart (@Path("prod_key") String prod_key, Callback<String> callback);

    /****************** Checkout APIs ******************/

    @GET("/checkout/delivery_slot")
    void GetDeliverySlots (Callback<DeliverySlots> callBack);

    @FormUrlEncoded
    @POST("/checkout/guest")
    void PostGuestAddress(
            @Field("firstname") String firstname,
            @Field("lastname") String lastname,
            @Field("email") String email,
            @Field("mobile") String mobile,
            @Field("apt_num") String apt_num,
            @Field("apt_id") int apt_id,
            @Field("area_id") int area_id,
            @Field("city_id") int city_id,
            @Field("postcode") String postcode,
            @Field("country_id") int country_id,
            @Field("zone_id") int zone_id,
            @Field("payment_address") boolean payment_address,
            Callback<String> callback);

    @GET("/checkout/payment_method")
    void GetPaymentMethods(Callback<PaymentMethods> callback);

    @FormUrlEncoded
    @POST("/checkout/delivery_slot")
    void PostDeliverySlot (
            @Field("delivery_date") String delivery_date,
            @Field("delivery_time") String delivery_time,
            Callback<String> callback);

    @FormUrlEncoded
    @POST("/checkout/payment_method")
    void PostPaymentMethod (@Field("payment_method") String payment_method, Callback<String> callback);

    @GET("/checkout/confirm")
    void GetConfirm(@Query("access_token") String access_token, Callback<OrderSummaryData> callback);

    @FormUrlEncoded
    @POST("/checkout/shipping_address")
    void PostAddress(
            @Field("shipping_address") String shipping_address,
            @Field("firstname") String firstname,
            @Field("lastname") String lastname,
            @Field("apt_num") String apt_num,
            @Field("apt_id") int apt_id,
            @Field("area_id") int area_id,
            @Field("city_id") int city_id,
            @Field("postcode") String postcode,
            @Field("zone_id") int zone_id,
            Callback<String> callback);

    @GET("/checkout/pay")
    void GetPay(@Query("access_token") String access_token, Callback<String> callback);

    @GET("/checkout/success")
    void GetSuccess(Callback<String> callback);

    /****************** Account APIs ******************/

    @FormUrlEncoded
    @POST("/account/login")
    void PostLogin (
            @Field("email") String email,
            @Field("password") String password,
            Callback<AccountData> callback);

    @GET("/account/logout")
    void GetLogout (Callback<String> callback);

    @GET("/account/address/{id}")
    void GetAddressId (@Path("id") int id, Callback<AddressModel> callback);

    @GET("/account/account")
    void GetAccount(Callback<AccountData> callback);

    @FormUrlEncoded
    @POST("/checkout/shipping_address")
    void PostExistingShippingAddress(@Field("shipping_address") String shipping_address,
                                     @Field("address_id") int addrId, Callback<String> callback);

    @GET("/account/address")
    void GetAllAddresses (Callback<AddressList> callback);

    @DELETE("/account/address/{addrId}")
    void DeleteAddress(@Path("addrId") int addrId, Callback<String> callback);

    @FormUrlEncoded
    @PUT("/account/address/{addrId}")
    void PutAddress(@Field("firstname") String firstname,
                    @Field("lastname") String lastname,
                    @Field("apt_num") String apt_num,
                    @Field("apt_id") int apt_id,
                    @Field("area_id") int area_id,
                    @Field("city_id") int city_id,
                    @Field("postcode") String postcode,
                    @Field("country_id") int country_id,
                    @Field("zone_id") int zone_id,
                    @Field("default") boolean default_addr,
                   Callback<String> callback);
}

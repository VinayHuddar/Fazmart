package com.fazmart.androidapp;

import android.app.Application;
import android.content.Intent;

import retrofit.Callback;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.OkClient;
import retrofit.client.Response;

import com.fazmart.androidapp.Common.AuthenticationData;
import com.fazmart.androidapp.Common.APIService;
import com.fazmart.androidapp.Common.CommonDefinitions;
import com.fazmart.androidapp.Model.TokenResponse;
import com.fazmart.androidapp.Model.UserAccountData;
import com.fazmart.androidapp.View.CategoryActivity;
import com.fazmart.androidapp.View.Common.NavDrawerItemList;
import com.fazmart.androidapp.View.IntegratedServicesScreen;
import com.squareup.okhttp.OkHttpClient;

/**
 * Created by Vinay on 07-06-2015.
 */
public class FazmartApplication extends Application {
    private static AuthenticationData mAuthenticationData = null;
    private RestAdapter mRestAdapter;
    private static APIService mAPIService;
    private String Url = "http://ec2-54-173-60-3.compute-1.amazonaws.com/api/v1";
    private int fetchAuthKeyRetryCnt = 0;

    public static APIService GetAPIService () {
        return mAPIService;
    }

    public void onCreate() {
        mRestAdapter = new RestAdapter.Builder()
                .setEndpoint(Url)
                .build();
        mAPIService = mRestAdapter.create(APIService.class);

        mAuthenticationData = AuthenticationData.GetInstance();

        /*if (UserAccountData.GetInstance().GetAccountData(this) != null) {
            mAuthenticationData.SetAuthenticationToken(UserAccountData.GetInstance().GetAuthenticationToken(this));
            InitAppSettings();
        }
        else*/
            FetchAuthKeyAndLaunch();
    }

    void FetchAuthKeyAndLaunch () {
        //AuthenticationData authData = AuthenticationData.GetInstance();
        String strAuthHdr = "Basic ZGV2aWNlY2xpZW50MzAxMjpyYWplc2g5OTg2MDE4NjQzYW5pdGhhOTk4NjAwNjA2Mw=="; // Base64 encoded string for "deviceclient3012:rajesh9986018643anitha9986006063"
        mAPIService.GetAuthenticationToken("application/json", strAuthHdr, "client_credentials", "", new Callback<TokenResponse>() {
            @Override
            public void success(TokenResponse tokenResponse, Response response) {
                mAuthenticationData.SetAuthenticationToken(tokenResponse.GetAccessToken());

                Initialize ();
            }

            @Override
            public void failure(RetrofitError error) {
                fetchAuthKeyRetryCnt++;
                if (fetchAuthKeyRetryCnt < CommonDefinitions.RETRY_COUNT)
                    FetchAuthKeyAndLaunch();
            }
        });
    }

    void Initialize () {
        RequestInterceptor requestInterceptor = new RequestInterceptor() {
            @Override
            public void intercept(RequestInterceptor.RequestFacade request) {
                request.addHeader("Accept", "application/json");
                request.addHeader("Authorization", "Bearer ".concat(mAuthenticationData.GetAuthenticationToken())); //mTokenResponse.GetTokenType()
            }
        };

        mRestAdapter = new RestAdapter.Builder()
                .setEndpoint(Url)
                .setClient(new OkClient(new OkHttpClient()))
                .setRequestInterceptor(requestInterceptor)
                .build();
        mAPIService = mRestAdapter.create(APIService.class);

        NavDrawerItemList.GetInstance().PrepareNavDrawerLists(getApplicationContext());

        if (CommonDefinitions.ENABLE_INTEGRATED_SERVICES_PAGE) {
            Intent intent = new Intent(getApplicationContext(), IntegratedServicesScreen.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            startActivity(intent);
        } else {
            Intent intent = new Intent(getApplicationContext(), CategoryActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            int[] lineageRoot = new int[1];
            lineageRoot[0] = -1;
            intent.putExtra(CategoryActivity.LINEAGE, lineageRoot);

            startActivity(intent);
        }
    }
}

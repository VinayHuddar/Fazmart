package com.fazmart.androidapp.Common;

import com.fazmart.androidapp.FazmartApplication;
import com.fazmart.androidapp.Model.TokenResponse;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by Admin on 02-05-2015.
 */
public class AuthenticationData {
    String mAuthToken = null;
    public void SetAuthenticationToken (String token) {
        mAuthToken = token;
    }

    public String GetAuthenticationToken () {
        return mAuthToken;
        //return "Bearer z6bBDxp3OJjwH7Pmtsux4zjfDUeNtlIxLuH11o80";
    }

    private static AuthenticationData instance = null;
    public static AuthenticationData GetInstance () {
        if (instance == null) {
            instance = new AuthenticationData();
        }
        return instance;
    }
}

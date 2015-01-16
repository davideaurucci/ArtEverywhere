package com.example.francesco.art;

/**
 * Created by Francesco on 25/12/2014.
 */
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import cod.com.appspot.endpoints_final.testGCS.*;

import javax.annotation.Nullable;

import android.accounts.Account;
import android.accounts.AccountManager;

import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;


public class AppConstants {

    public static final String WEB_CLIENT_ID = "517964427197-5pgn6p9fgpajehmg3cq2psaa50ie3i2n.apps.googleusercontent.com";

    public static final String AUDIENCE = "server:client_id:" + WEB_CLIENT_ID;

    /**
     * Class instance of the JSON factory.
     */
    public static final JsonFactory JSON_FACTORY = new AndroidJsonFactory();

    /**
     * Class instance of the HTTP transport.
     */
    public static final HttpTransport HTTP_TRANSPORT = AndroidHttp.newCompatibleTransport();


    /**
     * Retrieve a Helloworld api service handle to access the API.
     */
    /*
    VECCHIO - PRIMA VERSIONE
    public static TestGCS getApiServiceHandle() {
        // Use a builder to help formulate the API request.
        TestGCS.Builder helloWorld = new TestGCS.Builder(AppConstants.HTTP_TRANSPORT,
                AppConstants.JSON_FACTORY,null);

        return helloWorld.build();
    }
    */
    public static TestGCS getApiServiceHandle(@Nullable GoogleAccountCredential credential) {
        // Use a builder to help formulate the API request.
        TestGCS.Builder helloWorld = new TestGCS.Builder(AppConstants.HTTP_TRANSPORT,
                AppConstants.JSON_FACTORY,credential);
        return helloWorld.build();
    }

    public static int countGoogleAccounts(Context context) {
        AccountManager am = AccountManager.get(context);
        Account[] accounts = am.getAccountsByType(GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE);
        if (accounts == null || accounts.length < 1) {
            return 0;
        } else {
            return accounts.length;
        }
    }

    public static boolean checkGooglePlayServicesAvailable(Activity activity) {
        final int connectionStatusCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(activity);
        if (GooglePlayServicesUtil.isUserRecoverableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(activity, connectionStatusCode);
            return false;
        }
        return true;
    }

    public static void showGooglePlayServicesAvailabilityErrorDialog(final Activity activity,
                                                                     final int connectionStatusCode) {
        final int REQUEST_GOOGLE_PLAY_SERVICES = 0;
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Dialog dialog = GooglePlayServicesUtil.getErrorDialog(
                        connectionStatusCode, activity, REQUEST_GOOGLE_PLAY_SERVICES);
                dialog.show();
            }
        });
    }





}

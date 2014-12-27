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
import cod.com.appspot.omega_terrain_803.testGCS.*;

import javax.annotation.Nullable;

public class AppConstants {

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
    public static TestGCS getApiServiceHandle() {
        // Use a builder to help formulate the API request.
        TestGCS.Builder helloWorld = new TestGCS.Builder(AppConstants.HTTP_TRANSPORT,
                AppConstants.JSON_FACTORY,null);

        return helloWorld.build();
    }

}

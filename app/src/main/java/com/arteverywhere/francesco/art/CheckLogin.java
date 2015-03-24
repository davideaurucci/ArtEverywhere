package com.arteverywhere.francesco.art;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;

import cod.com.appspot.art_everywhere.artEverywhere.ArtEverywhere;
import cod.com.appspot.art_everywhere.artEverywhere.model.MainArtistRequestMessage;
import cod.com.appspot.art_everywhere.artEverywhere.model.MainDefaultResponseMessage;

/**
 * Created by Francesco on 11/02/2015.
 */
public class CheckLogin extends AsyncTask<Void, Void, MainArtistRequestMessage> {
                Context mContext;
                TaskCallbackLoginArtist mCallback;
                String email;

                public CheckLogin(Context context) {
                    mContext = context;
                }

                public CheckLogin(Context context, String email, TaskCallbackLoginArtist mCallback) {
                    mContext = context;
                    this.mCallback = mCallback;
                    this.email = email;
                }

                protected MainArtistRequestMessage doInBackground(Void... unused) {
                    // Retrieve service handle.
                    ArtEverywhere apiServiceHandle = AppConstants.getApiServiceHandle(null);

                    Log.d("DB","doInBack");
                    
                    try {
                        MainArtistRequestMessage greeting = new MainArtistRequestMessage();
                        greeting.setEmail(email);

                        ArtEverywhere.Check.Checklogin put = apiServiceHandle.check().checklogin(greeting);
                        MainDefaultResponseMessage response = put.execute();
                        if(response.getMessage().equals("Artist Registered!")){
                            return greeting;
                        }else{
                            return null;
                        }
                    } catch (IOException e) {
                        Toast.makeText(mContext, "Exception during API call!", Toast.LENGTH_LONG).show();
                    }
                    return null;
                }


                protected void onPostExecute(MainArtistRequestMessage greeting) {
                    if (greeting!=null) {
                        Log.d("DEBUG","User è artista!");
                        mCallback.done(true, greeting.getEmail().toString());
                    }else{
                        Log.d("DEBUG","User NON è artista REGISTRATO!");
                        mCallback.done(false, "");
                    }
                }

}

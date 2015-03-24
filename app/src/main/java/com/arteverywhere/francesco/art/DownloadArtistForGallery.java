package com.arteverywhere.francesco.art;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;

import cod.com.appspot.art_everywhere.artEverywhere.ArtEverywhere;
import cod.com.appspot.art_everywhere.artEverywhere.model.MainArtistDetailsMessage;

/**
 * Created by Francesco on 11/02/2015.
 */
public class DownloadArtistForGallery extends AsyncTask<String, Void, MainArtistDetailsMessage> {
                Context mContext;
                TaskCallbackDownloadArtistForGallery mCallback;
                String email;

                public DownloadArtistForGallery(Context context) {
                    mContext = context;
                }

                public DownloadArtistForGallery(Context context, String email, TaskCallbackDownloadArtistForGallery mCallback) {
                    mContext = context;
                    this.mCallback = mCallback;
                    this.email = email;
                }

                protected MainArtistDetailsMessage doInBackground(String... strings) {
                    // Retrieve service handle.
                    ArtEverywhere apiServiceHandle = AppConstants.getApiServiceHandle(null);

                    Log.d("DB","doInBack");

                    try {
                        ArtEverywhere.Getinfo.Getartist get = apiServiceHandle.getinfo().getartist(email);
                        //TestGCS.Display.Getphotos get = apiServiceHandle.display().getphotos((long)AppConstants.numFoto);
                        Log.d("LOG", "Sono qui");

                        MainArtistDetailsMessage greeting = get.execute();

                        Log.d("SIZE",""+greeting.size());
                        Log.d("LOG","Sono qui");

                        return greeting;
                    } catch (IOException e) {
                        Toast.makeText(mContext, "Exception during API call!", Toast.LENGTH_LONG).show();
                    }
                    return null;
                }


                protected void onPostExecute(MainArtistDetailsMessage greeting) {
                    if (greeting!=null) {
                        Log.d("SIZE", "" + greeting.size());
                        //Log.d("NUM FOTO IN GREETING", "" + greeting.getPhotos().size());

                            String nomecognome = greeting.getNome() + " " + greeting.getCognome();
                            String sito = greeting.getSito();
                            String bio = greeting.getBio();
                            String pic = greeting.getPic();
                            String nickname = greeting.getNickname();
                            String email = greeting.getEmail();


                            mCallback.done(email, nomecognome,pic, nickname, bio, sito);

                    } else {
                        Toast.makeText(mContext, "No greetings were returned by the API.", Toast.LENGTH_LONG).show();
                    }
                }

}

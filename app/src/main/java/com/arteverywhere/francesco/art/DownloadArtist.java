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
public class DownloadArtist extends AsyncTask<String, Void, MainArtistDetailsMessage> {
                Context mContext;
                TaskCallbackDownloadArtist mCallback;
                String email;

                public DownloadArtist(Context context) {
                    mContext = context;
                }

                public DownloadArtist(Context context, String email, TaskCallbackDownloadArtist mCallback) {
                    mContext = context;
                    this.mCallback = mCallback;
                    this.email = email;
                }

                protected MainArtistDetailsMessage doInBackground(String... strings) {
                    // Retrieve service handle.
                    ArtEverywhere apiServiceHandle = AppConstants.getApiServiceHandle(null);
                    try {
                        ArtEverywhere.Getinfo.Getartist get = apiServiceHandle.getinfo().getartist(email);
                        MainArtistDetailsMessage greeting = get.execute();
                        return greeting;
                    } catch (IOException e) {
                        Toast.makeText(mContext, "Exception during API call!", Toast.LENGTH_LONG).show();
                    }
                    return null;
                }


                protected void onPostExecute(MainArtistDetailsMessage greeting) {
                    if (greeting!=null) {
                            String nomecognome = greeting.getNome() + " " + greeting.getCognome();
                            String sito = greeting.getSito();
                            String bio = greeting.getBio();
                            String pic = greeting.getPic();
                            String nickname = greeting.getNickname();
                            mCallback.done(nomecognome,pic, nickname, bio, sito);

                    } else {
                        Toast.makeText(mContext, "No greetings were returned by the API.", Toast.LENGTH_LONG).show();
                    }
                }

}

package com.arteverywhere.francesco.art;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;

import cod.com.appspot.art_everywhere.artEverywhere.ArtEverywhere;
import cod.com.appspot.art_everywhere.artEverywhere.model.MainPictureDetailsCollection;

/**
 * Created by Francesco on 11/02/2015.
 */
public class GetArtworksOfArtist extends AsyncTask<String, Void, MainPictureDetailsCollection> {
                Context mContext;
                String mail;
                TaskCallbackArtworksOfArtist mCallback;

                public GetArtworksOfArtist(Context context) {
                    mContext = context;
                }

                public GetArtworksOfArtist(Context context, String mail, TaskCallbackArtworksOfArtist mCallback) {
                    mContext = context;
                    this.mail = mail;
                    this.mCallback = mCallback;
                }

                protected MainPictureDetailsCollection doInBackground(String... strings) {
                    // Retrieve service handle.
                    ArtEverywhere apiServiceHandle = AppConstants.getApiServiceHandle(null);

                    Log.d("DB","doInBack");
                    
                    try {
                        ArtEverywhere.Artworks.Getartworks get = apiServiceHandle.artworks().getartworks(mail);
                        Log.d("LOG", "Sono qui");
                        MainPictureDetailsCollection greeting = get.execute();

                        Log.d("SIZE",""+greeting.size());
                        Log.d("LOG","Sono qui");

                        return greeting;
                    } catch (IOException e) {
                        Toast.makeText(mContext, "Exception during API call!", Toast.LENGTH_LONG).show();
                    }
                    return null;
                }


                protected void onPostExecute(MainPictureDetailsCollection greeting) {
                    if (greeting!=null) {

                        if(greeting.getPhotos() == null){
                            mCallback.done(null);
                            return;
                        }

                        int quanteFotoCaricate = greeting.getPhotos().size();
                        String[] urlPhoto = new String[quanteFotoCaricate];
                        for(int i = 0; i < quanteFotoCaricate; i++){
                            String url = greeting.getPhotos().get(i).getUrl();
                            urlPhoto[i] = url;
                        }
                         /* SCARICO LE ULTIME 4 FOTO
                        String[] urlPhoto = new String[4];

                            if(quanteFotoCaricate < 4){
                                for (int i = 0; i < quanteFotoCaricate; i++) {
                                    String url = greeting.getPhotos().get(i).getPhoto();
                                    urlPhoto[i] = url;
                                }
                            }else {
                                for (int i = 0; i < 4; i++) {
                                    String url = greeting.getPhotos().get(i).getPhoto();
                                    urlPhoto[i] = url;
                                }
                            }
                        */

                            mCallback.done(urlPhoto);

                    } else {
                        Toast.makeText(mContext, "No greetings were returned by the API.", Toast.LENGTH_LONG).show();
                    }
                }

}

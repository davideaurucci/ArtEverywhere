package com.arteverywhere.francesco.art;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;

import cod.com.appspot.art_everywhere.artEverywhere.ArtEverywhere;
import cod.com.appspot.art_everywhere.artEverywhere.model.MainDownloadResponseCollection;


/**
 * Created by Francesco on 11/02/2015.
 */
public class DownloadArtworksByTechinique extends AsyncTask<Integer, Void, MainDownloadResponseCollection> {
                Context mContext;
                DatabaseArtwork db;
                TaskCallbackDownloadFilteredArtworks mCallback;
                String tecnica;

                public DownloadArtworksByTechinique(Context context) {
                    mContext = context;
                }

                public DownloadArtworksByTechinique(Context context, DatabaseArtwork db, TaskCallbackDownloadFilteredArtworks mCallback, String tecnica) {
                    mContext = context;
                    this.db = db;
                    this.mCallback = mCallback;
                    this.tecnica = tecnica;
                }

                protected MainDownloadResponseCollection doInBackground(Integer... integers) {
                    // Retrieve service handle.
                    ArtEverywhere apiServiceHandle = AppConstants.getApiServiceHandle(null);

                    Log.d("DB","doInBack");
                    
                    try {
                        ArtEverywhere.Displaytechnique.Getphotos get = apiServiceHandle.displaytechnique().getphotos((long)AppConstants.numFotoFiltri,tecnica);
                        //Log.d("LOG", "Sono qui");
                        MainDownloadResponseCollection greeting = get.execute();

                        //Log.d("SIZE",""+greeting.size());
                        //Log.d("LOG","Sono qui");

                        return greeting;
                    } catch (IOException e) {
                        Toast.makeText(mContext, "Exception during API call!", Toast.LENGTH_LONG).show();
                    }
                    return null;
                }


                protected void onPostExecute(MainDownloadResponseCollection greeting) {
                    if (greeting!=null) {
                        //Log.d("SIZE", "" + greeting.size());
                        //Log.d("NUM FOTO IN GREETING", "" + greeting.getPhotos().size());

                        if(greeting.getPhotos() == null){
                            Toast.makeText(mContext, "Non ci sono foto!!!", Toast.LENGTH_LONG).show();
                            return;
                        }
                        int quanteFotoCaricate = greeting.getPhotos().size();

                            for(int i = 0; i < quanteFotoCaricate;i++){
                                //System.out.println(i + "<>" + quanteFotoCaricate);
                                String filename = greeting.getPhotos().get(i).getTitle();
                                String photo = greeting.getPhotos().get(i).getUrl();
                                System.out.println(photo);
                                String artista = greeting.getPhotos().get(i).getArtist();
                                String descrizione = greeting.getPhotos().get(i).getDescr();
                                String dimensioni = greeting.getPhotos().get(i).getDim();
                                String luogo = greeting.getPhotos().get(i).getLuogo();
                                String tecnica = greeting.getPhotos().get(i).getTechnique();
                                long likes = greeting.getPhotos().get(i).getLikes();
                                String data = greeting.getPhotos().get(i).getDateTime();

                               Artwork art = new Artwork(filename,photo,artista,descrizione,dimensioni,luogo,tecnica,likes,data);
                               if(controllo(art)) {
                                   db.insert(art, db.getWritableDatabase());
                               }
                            }

                            Log.d("DB","<>getAllArt:" + db.getAllArtworks().size());

                            mCallback.done(0);

                    } else {
                        Toast.makeText(mContext, "No greetings were returned by the API.", Toast.LENGTH_LONG).show();
                    }
                }

        public boolean controllo(Artwork art){
            if(db.getArtworkFromUrl(art.getPhoto()) == null){
                return true;
            }
            return false;
        }
}

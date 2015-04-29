package com.arteverywhere.francesco.art;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;

import cod.com.appspot.art_everywhere.artEverywhere.ArtEverywhere;
import cod.com.appspot.art_everywhere.artEverywhere.model.MainArtistInfoMessage;
import cod.com.appspot.art_everywhere.artEverywhere.model.MainDefaultResponseMessage;

/**
 * Created by Francesco on 11/02/2015.
 */
public class RegisterArtist extends AsyncTask<Void, Void, MainArtistInfoMessage> {
                Context mContext;
                String email;
                String nome;
                String cognome;
                String nick;
                String photo;
                String bio;
                String sito;
                TaskCallbackRegisterArtist mCallback;

                public RegisterArtist(Context context) {
                    mContext = context;
                }

                public RegisterArtist(Context context, String email, String nome, String cognome, String nick, String photo, String bio, String sito, TaskCallbackRegisterArtist mCallback) {
                    mContext = context;
                    this.email = email;
                    this.nome = nome;
                    this.cognome = cognome;
                    this.nick = nick;
                    this.photo = photo;
                    this.bio = bio;
                    this.sito = sito;
                    this.mCallback = mCallback;
                }

                protected MainArtistInfoMessage doInBackground(Void... unused) {
                    // Retrieve service handle.
                    ArtEverywhere apiServiceHandle = AppConstants.getApiServiceHandle(null);
                    try {
                        MainArtistInfoMessage greeting = new MainArtistInfoMessage();
                        greeting.setEmail(email);
                        greeting.setCognome(cognome);
                        greeting.setNome(nome);
                        greeting.setPic(photo);

                        if(nick.length() > 0) greeting.setNickname(nick);
                        if(bio.length() > 0) greeting.setBio(bio);
                        if(sito.length() > 0) greeting.setSito(sito);

                        ArtEverywhere.Registration.Registerartist get = apiServiceHandle.registration().registerartist(greeting);


                        MainDefaultResponseMessage response = get.execute();
                        if(response.getMessage().equals("Artist already exists!")){
                            return null;
                        }else{
                            return greeting;
                        }


                    } catch (IOException e) {
                        Toast.makeText(mContext, "Exception during API call!", Toast.LENGTH_LONG).show();
                    }
                    return null;
                }


                protected void onPostExecute(MainArtistInfoMessage greeting) {
                    if (greeting!=null) {
                        Toast.makeText(mContext, "Registrazione avvenuta con successo!", Toast.LENGTH_LONG).show();
                        mCallback.done(true);


                    } else {
                        Toast.makeText(mContext, "Email già presente! Registrazione fallita!", Toast.LENGTH_LONG).show();
                        mCallback.done(false);
                    }
                }

}

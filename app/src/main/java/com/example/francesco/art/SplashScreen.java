package com.example.francesco.art;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.IOException;


import cod.com.appspot.endpoints_final.testGCS.TestGCS;
import cod.com.appspot.endpoints_final.testGCS.TestGCS.Display.Getphotos;
import cod.com.appspot.endpoints_final.testGCS.model.MainDownloadResponseCollection;
import cod.com.appspot.endpoints_final.testGCS.model.MainDownloadResponseMessage;


public class SplashScreen extends Activity {
    protected long numFoto = 20;
    protected String[] urlPhoto;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        AsyncTask<Integer, Void, MainDownloadResponseCollection> getAndDisplayGreeting =
                new AsyncTask<Integer, Void, MainDownloadResponseCollection> () {
                    @Override
                    protected MainDownloadResponseCollection doInBackground(Integer... integers) {
                        // Retrieve service handle.
                        TestGCS apiServiceHandle = AppConstants.getApiServiceHandle(null);

                        try {
                            Getphotos get = apiServiceHandle.display().getphotos(numFoto);
                            Log.d("LOG","Sono qui");
                            MainDownloadResponseCollection greeting = get.execute();

                            Log.d("SIZE",""+greeting.size());
                            Log.d("LOG","Sono qui");

                            return greeting;
                        } catch (IOException e) {
                            Toast.makeText(getApplicationContext(), "Exception during API call!", Toast.LENGTH_LONG).show();
                        }
                        return null;
                    }

                    @Override
                    protected void onPostExecute(MainDownloadResponseCollection greeting) {
                        if (greeting!=null) {
                            Log.d("SIZE", "" + greeting.size());
                            Log.d("LOG", "Sono qui");
                            Log.d("NUM FOTO IN GREETING", ""+greeting.getPhotos().size());

                            int quanteFotoCaricate = greeting.getPhotos().size();
                            if(quanteFotoCaricate < numFoto) {
                                urlPhoto = new String[quanteFotoCaricate];
                                for(int i = 0; i < quanteFotoCaricate; i++) {
                                    String url = greeting.getPhotos().get(i).getPhoto();
                                    urlPhoto[i] = url;
                                    Log.d("URL", url);
                                }
                            }else{
                                urlPhoto = new String[(int)numFoto];
                                for(int i = 0; i < numFoto; i++) {
                                    String url = greeting.getPhotos().get(i).getPhoto();
                                    urlPhoto[i] = url;
                                    Log.d("URL", url);
                                }
                            }

                            avviaApp(urlPhoto);

                        } else {
                            Toast.makeText(getApplicationContext(), "No greetings were returned by the API.", Toast.LENGTH_LONG).show();
                        }
                    }
                };

        getAndDisplayGreeting.execute();

    }

    private void avviaApp(String[] urlPhoto){
        //avvio MainActivity e passo l'array di url generato dagli endpoint
        Intent myIntent = new Intent(SplashScreen.this, MainActivity.class);
        myIntent.putExtra("urls",urlPhoto);
        this.startActivity(myIntent);
        this.finish();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_splash_screen, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

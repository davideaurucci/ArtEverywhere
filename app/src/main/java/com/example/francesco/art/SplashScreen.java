package com.example.francesco.art;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.Toast;


public class SplashScreen extends Activity implements TaskCallbackDownloadArtworks {
    ProgressBar progressBar;
    SharedPreferences pref;
    String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        email = pref.getString("email_artista",null);

        if(isOnline()){
            //Log.d("DB","creo istanza di DB");
            DatabaseArtwork db = new DatabaseArtwork(getApplicationContext());
            Log.d("DB","ho creato istanza di DB ed eseguo download artwork");
            db.clear(db.getWritableDatabase());
            new DownloadArtworks(getApplicationContext(),db,this).execute();
        }else{
            Toast.makeText(getApplicationContext(), "Connessione internet assente!", Toast.LENGTH_LONG).show();
            done();
        }


    }

    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public void done() {
        if(email==null) {
            Intent myIntent = new Intent(SplashScreen.this, Login.class);
            this.startActivity(myIntent);
            this.finish();
        }else{
            Intent myIntent = new Intent(SplashScreen.this, MainActivity.class);
            this.startActivity(myIntent);
            this.finish();
        }
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

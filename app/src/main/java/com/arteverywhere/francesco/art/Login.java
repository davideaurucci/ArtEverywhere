package com.arteverywhere.francesco.art;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;

public class Login extends Activity implements OnClickListener, ConnectionCallbacks, OnConnectionFailedListener, TaskCallbackLoginArtist {

    private static final int RC_SIGN_IN = 0;
    private static final String TAG = "LoginActivity";

    private GoogleApiClient mGoogleApiClient;
    private boolean mIntentInProgress;
    private boolean mSignInClicked;
    private ConnectionResult mConnectionResult;
    private Button btnSignIn;
    private Button btnSignOut, btnRevokeAccess, btnContinua;
    private Button accedi;

    SharedPreferences pref;
    boolean token;
    private boolean tokenLogin = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        btnSignIn = (Button) findViewById(R.id.btn_sign_in);
        btnSignOut = (Button) findViewById(R.id.btn_sign_out);
        accedi = (Button) findViewById(R.id.btn_login);
        btnRevokeAccess = (Button) findViewById(R.id.btn_revoke_access);
        btnSignIn.setOnClickListener(this);
        btnSignOut.setOnClickListener(this);
        btnRevokeAccess.setOnClickListener(this);
        accedi.setOnClickListener(this);
        pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();


        String x = pref.getString("email_artista",null);
        if(x!=null) {
            avviaGallery2();
        }


        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).addApi(Plus.API, null)
                .addScope(Plus.SCOPE_PLUS_PROFILE).build();
    }

    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    public void disconnetti(){

        if (mGoogleApiClient.isConnected()) {
            Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
            mGoogleApiClient.disconnect();
            pref.edit().putBoolean("token",false).commit();
            mGoogleApiClient.connect();
        }
    }

    private void resolveSignInError() {
        if (mConnectionResult.hasResolution()) {
            try {
                mIntentInProgress = true;
                mConnectionResult.startResolutionForResult(this, RC_SIGN_IN);
            } catch (SendIntentException e) {
                mIntentInProgress = false;
                mGoogleApiClient.connect();
            }
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        if (!result.hasResolution()) {
            GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(), this, 0).show();
            return;
        }

        if (!mIntentInProgress) {
            mConnectionResult = result;

            if (mSignInClicked)
                resolveSignInError();

        }

    }

    @Override
    protected void onActivityResult(int requestCode, int responseCode, Intent intent) {
          if (requestCode == RC_SIGN_IN) {
                if (responseCode != RESULT_OK) {
                    mSignInClicked = false;
                }

                mIntentInProgress = false;

                if (!mGoogleApiClient.isConnecting()) {
                    mGoogleApiClient.connect();
                }
            }

    }

    @Override
    public void onConnected(Bundle arg0) {
        mSignInClicked = false;
        token = pref.getBoolean("token",false);

        if(token){
            disconnetti();
        }else{
            getProfileInformation();
        }

    }

    private void getProfileInformation() {
        try {
            if (Plus.PeopleApi.getCurrentPerson(mGoogleApiClient) != null) {
                Person currentPerson = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);
                String personPhotoUrl = currentPerson.getImage().getUrl();
                String email = Plus.AccountApi.getAccountName(mGoogleApiClient);

                String cognome = currentPerson.getName().getFamilyName();
                String nome = currentPerson.getName().getGivenName();

                personPhotoUrl = personPhotoUrl.substring(0, personPhotoUrl.length() - 6);

                if(tokenLogin){ /* E' stato cliccato il bottone per effettuare il Login */
                    tokenLogin=false;
                    /* Devo verificare che la mail con cui l'utente ha effettuato l'accesso è presente nel nostro DB come artista */
                    if(checkNetwork()) new CheckLogin(getApplicationContext(),email,this).execute();

                    /* Il risultato della chiamata CheckLogin lo trovo in done(boolean,string) */
                }else { /* E' stato cliccalto il bottone per la registrazione */
                    Intent myIntent = new Intent(Login.this, Registration.class);
                    myIntent.putExtra("nome", nome);
                    myIntent.putExtra("cognome", cognome);
                    myIntent.putExtra("email", email);
                    myIntent.putExtra("foto", personPhotoUrl);
                    this.startActivity(myIntent);
                    this.finish();
                }
            } else {
                // SOLO PER DEBUG
                String nome = "nome";
                String cognome = "cognome";
                String email = "ciao@ciao.it";
                String personPhotoUrl = "http://www.francescocucari.it/artist.jpg";


                Intent myIntent = new Intent(Login.this, Registration.class);
                myIntent.putExtra("nome",nome);
                myIntent.putExtra("cognome",cognome);
                myIntent.putExtra("email",email);
                myIntent.putExtra("foto",personPhotoUrl);

                this.startActivity(myIntent);
                Toast.makeText(getApplicationContext(), "Errore nel Login", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onConnectionSuspended(int arg0) {
        mGoogleApiClient.connect();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_login, menu);
        return true;
    }

    /**
     * Button on click listener
     * */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_sign_in:
                signInWithGplus();
                break;


            case R.id.btn_sign_out:
                signOutFromGplus();
                break;

            case R.id.btn_revoke_access:
                revokeGplusAccess();
                break;

            case R.id.btn_login:
                tokenLogin = true;
                signInWithGplus();
                break;
        }
    }

    private void signInWithGplus() {
        if (!mGoogleApiClient.isConnecting()) {
            mSignInClicked = true;
            resolveSignInError();
        }
    }

    public void signOutFromGplus() {
        if (mGoogleApiClient.isConnected()) {
            Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
            mGoogleApiClient.disconnect();
            mGoogleApiClient.connect();
        }
    }


    private void revokeGplusAccess() {
        if (mGoogleApiClient.isConnected()) {
            Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
            Plus.AccountApi.revokeAccessAndDisconnect(mGoogleApiClient)
                    .setResultCallback(new ResultCallback<Status>() {
                        @Override
                        public void onResult(Status arg0) {
                            Log.e(TAG, "User access revoked!");
                            mGoogleApiClient.connect();
                        }

                    });
        }
    }



    private void avviaGallery2(){
        Intent myIntent = new Intent(Login.this, MainActivity.class);
        this.startActivity(myIntent);
    }

    @Override
    public void done(boolean x, String email) {
        if(x){ //Utente è artista! Può accedere !!
            Toast.makeText(getApplicationContext(), "Login eseguito con successo!", Toast.LENGTH_LONG).show();

            pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
            SharedPreferences.Editor editor = pref.edit();
            editor.putString("email_artista", email);
            editor.commit();

            Intent myIntent = new Intent(Login.this, MainActivity.class);
            this.startActivity(myIntent);
            this.finish();
        }else{ //Login fallito perchè email non è associata ad un'artista
            disconnetti();
            Toast.makeText(getApplicationContext(), "Login fallito! Devi registrarti come artista!", Toast.LENGTH_LONG).show();
        }
    }

    public void onBackPressed(){
        System.exit(0);
        return;
    }

    public boolean checkNetwork() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        boolean isOnline = (netInfo != null && netInfo.isConnectedOrConnecting());
        if(isOnline) {
            return true;
        }else{
            new AlertDialog.Builder(this)
                    .setTitle("Ops..qualcosa è andato storto!")
                    .setMessage("Sembra che tu non sia collegato ad internet! ")
                    .setPositiveButton("Impostazioni", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // continue with delete
                            Intent callGPSSettingIntent = new Intent(Settings.ACTION_SETTINGS);
                            startActivityForResult(callGPSSettingIntent,0);
                        }
                    }).show();
            return false;
        }
    }
}

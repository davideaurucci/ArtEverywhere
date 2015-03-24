package com.arteverywhere.francesco.art;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.content.SharedPreferences;
import android.os.Bundle;
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
        //btnContinua = (Button) findViewById(R.id.btn_continua);
        accedi = (Button) findViewById(R.id.btn_login);

        btnRevokeAccess = (Button) findViewById(R.id.btn_revoke_access);


        // Button click listeners
        btnSignIn.setOnClickListener(this);
        btnSignOut.setOnClickListener(this);
        btnRevokeAccess.setOnClickListener(this);
        //btnContinua.setOnClickListener(this);
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

    /**
     * Method to resolve any signin errors
     * */
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
            // Store the ConnectionResult for later usage
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


    /**
     * Fetching user's information name, email, profile pic
     * */
    private void getProfileInformation() {
        try {
            if (Plus.PeopleApi.getCurrentPerson(mGoogleApiClient) != null) {
                //System.out.println("NON è NULL");
                Person currentPerson = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);
                String personPhotoUrl = currentPerson.getImage().getUrl();
                String email = Plus.AccountApi.getAccountName(mGoogleApiClient);

                String cognome = currentPerson.getName().getFamilyName();
                String nome = currentPerson.getName().getGivenName();


                /*
                Log.e(TAG, "Name: " + personName + ", plusProfile: "
                        + personGooglePlusProfile + ", email: " + email
                        + ", Image: " + personPhotoUrl);
                */

                personPhotoUrl = personPhotoUrl.substring(0, personPhotoUrl.length() - 6);

                if(tokenLogin){ /* E' stato cliccato il bottone per effettuare il Login */
                    tokenLogin=false;
                    /* Devo verificare che la mail con cui l'utente ha effettuato l'accesso è presente nel nostro DB come artista */
                    new CheckLogin(getApplicationContext(),email,this).execute();

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
        // Inflate the menu; this adds items to the action bar if it is present.
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
                // Signin button clicked
                signInWithGplus();
                break;
            /*case R.id.btn_continua:
                //AVVIA APP COME VISITATORE
                avviaGallery(true);*/

            case R.id.btn_sign_out:
                // Signout button clicked
                signOutFromGplus();
                break;

            case R.id.btn_revoke_access:
                // Revoke access button clicked
                revokeGplusAccess();
                break;

            case R.id.btn_login:
                //autenticazione();
                tokenLogin = true;
                signInWithGplus();
                break;
        }
    }

    /**
     * Sign-in into google
     * */
    private void signInWithGplus() {
        if (!mGoogleApiClient.isConnecting()) {
            mSignInClicked = true;
            resolveSignInError();
        }
    }

    /**
     * Sign-out from google
     * */
    public void signOutFromGplus() {
        if (mGoogleApiClient.isConnected()) {
            Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
            mGoogleApiClient.disconnect();
            mGoogleApiClient.connect();
        }
    }

    /**
     * Revoking access from google
     * */
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


    private void avviaGallery(boolean visitatore){
        Intent myIntent = new Intent(Login.this, MainActivity.class);
        myIntent.putExtra("visitatore",visitatore);
        this.startActivity(myIntent);
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
}

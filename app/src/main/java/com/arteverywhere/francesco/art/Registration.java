package com.arteverywhere.francesco.art;

/**
 * Created by Davide on 13/02/15.
 */


import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;


import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Registration extends ActionBarActivity implements TaskCallbackRegisterArtist {
    private String artist;
    TextView nomeText, emailText, nomeArteText, biografiaText, sitoText;
    ImageView immagine;
    String nome, cognome, foto, email, nickname, sito, bio;
    EditText inserisciNome, inserisciCognome, inserisciEmail, inserisciBiografia, inserisciSito, inserisciNick;
    TextView contatore;
    SharedPreferences pref;

    public static final String URL_REGEX = "^((https?|ftp)://|(www|ftp)\\.)?[a-z0-9-]+(\\.[a-z0-9-]+)+([/?].*)?$";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration_artist);


        nomeText = (TextView) findViewById(R.id.nome);
        emailText = (TextView) findViewById(R.id.email);
        nomeArteText = (TextView) findViewById(R.id.nomearte);
        biografiaText = (TextView) findViewById(R.id.biografia);
        sitoText = (TextView) findViewById(R.id.sito);
        immagine = (ImageView) findViewById(R.id.immagine);
        contatore = (TextView)findViewById(R.id.contatore);

        /* VISUALIZZO ACTION BAR CON LOGO */
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setLogo(R.drawable.logo_trasparente);
        getSupportActionBar().setDisplayShowTitleEnabled(true);


        inserisciEmail = (EditText) findViewById(R.id.insertEmail);
        inserisciEmail.setFocusable(false);
        inserisciEmail.setClickable(false);

        inserisciNome = (EditText) findViewById(R.id.insertNome);
        inserisciCognome = (EditText)findViewById(R.id.insertCognome);
        inserisciBiografia = (EditText) findViewById(R.id.insertBiografia);
        inserisciSito = (EditText) findViewById(R.id.insertSito);
        inserisciNick = (EditText)findViewById(R.id.editText2);
        inserisciBiografia.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int aft){
            }

            @Override
            public void afterTextChanged(Editable s) {
                // this will show characters remaining
                contatore.setText("(" + (s.toString().length()) + "/500)");
            }
        });
        Bundle extras = getIntent().getExtras();
        nome = extras.getString("nome");
        cognome = extras.getString("cognome");
        foto = extras.getString("foto");
        email = extras.getString("email");

        inserisciNome.setText(nome);
        inserisciCognome.setText(cognome);
        inserisciEmail.setText(email);

        Picasso.with(Registration.this).load(foto).into(immagine);


    }

    public void done(boolean successo){
        System.out.println("torno da  Registrazione");
        if(successo){ //EMAIL NON PRESENTE. REGISTRAZIONE EFFETTUATA CON SUCCESSO
            pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
            Editor editor = pref.edit();
            editor.putString("email_artista", email);
            editor.commit();
            Intent myIntent = new Intent(Registration.this, MainActivity.class);
            this.startActivity(myIntent);
            this.finish();
        }else{
            Intent myIntent = new Intent(Registration.this, Login.class);
            pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
            Editor editor = pref.edit();
            editor.putBoolean("token", true).commit();
            //myIntent.putExtra("token",true);
            this.startActivity(myIntent);
            this.finish();
        }
    }

    public void onBackPressed(){
        //do whatever you want the 'Back' button to do
        //as an example the 'Back' button is set to start a new Activity named 'NewActivity'

        this.startActivity(new Intent(Registration.this,Login.class));
        pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        Editor editor = pref.edit();
        editor.putBoolean("token", true).commit();
        return;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_registration, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.registrati) {
            if (inserisciNome.getText().length() > 1 && inserisciCognome.getText().length() > 1) {

                sito = inserisciSito.getText().toString();
                bio = inserisciBiografia.getText().toString();
                nickname = inserisciNick.getText().toString();

                new RegisterArtist(getApplicationContext(), email, nome, cognome, nickname,foto,bio,sito, this).execute();

            }
            else {
                Toast.makeText(getApplicationContext(),"Compilare i campi obbligatori", Toast.LENGTH_LONG).show();
            }
            return true;
        }

            return super.onOptionsItemSelected(item);
    }
}







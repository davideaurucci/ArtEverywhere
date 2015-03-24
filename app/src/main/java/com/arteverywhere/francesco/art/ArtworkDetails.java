package com.arteverywhere.francesco.art;


import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.Signature;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.widget.FacebookDialog;
import com.facebook.widget.WebDialog;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


public class ArtworkDetails extends ActionBarActivity implements TaskCallbackLike, TaskCallbackDelete,TaskCallbackCheckLike, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, TaskCallbackDownloadArtist, TaskCallbackInsertComment, AdapterView.OnItemClickListener, TaskCallbackDownloadComments, TaskCallbackDownloadArtworks {
    ImageView img;
    ImageView imgArtista;
    TextView titolo;
    TextView nomeArtista;
    TextView descrizione;
    TextView tecnica;
    TextView luogo;
    TextView size;

    TextView piace;
    String artista;
    AlertDialog dialog;
    Menu m;
    MenuItem menu;

    private UiLifecycleHelper uiHelper;
    private PopupWindow popWindow;

    String url;
    Artwork artwork;

    TextView commenti;
    EditText commentoInScrittura;
    Button btnOkComment;

    SharedPreferences pref;
    String[] listaAutori;
    String[] listaCommenti;
    String[] listaDate;
    String email;

    //per verificare se l'utente ha messo un like sull'artwork in precedenza, così da fargli vedere il bottone in un certo modo.
    boolean messoLike;
    boolean fatto=false;

    private static final int RC_SIGN_IN = 0;
    private static final String TAG = "LoginVisitatore";

    private GoogleApiClient mGoogleApiClient;
    private boolean mIntentInProgress;
    private boolean mSignInClicked;
    private ConnectionResult mConnectionResult;

    EditText titleBox;
    boolean forComment = false;
    boolean forLike = false;

    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;
    String[] timeAgo;
    String[] listaPic;
    String[] listaEmail;

    private TaskCallbackLike l;
    private TaskCallbackCheckLike check;
    private TaskCallbackDelete d;
    boolean visitatore;
    String emailPersona;
    boolean tokenDaCommento = false;
    String emailOfItemSelectedInComment = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artwork_details);
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,WindowManager.LayoutParams.FLAG_SECURE);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).addApi(Plus.API, null)
                .addScope(Plus.SCOPE_PLUS_PROFILE).build();

        piace = (TextView) findViewById(R.id.piaceA);
        l = this;
        check = this;

        Bundle extras = getIntent().getExtras();
        url = extras.getString("photo");

        DatabaseArtwork db = new DatabaseArtwork(getApplicationContext());
        artwork = db.getArtworkFromUrl(url);
        titolo = (TextView)findViewById(R.id.textView3);
        boolean modifica=false;
        if(getIntent().getExtras().get("titolomod")!=null){
            titolo.setText(getIntent().getExtras().get("titolomod").toString());
            artwork.setFilename(titolo.getText().toString());
            modifica=true;
        }


        /* VISUALIZZO ACTION BAR CON LOGO */
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setLogo(R.drawable.logo_trasparente);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle(artwork.getFilename());




        img = (ImageView)findViewById(R.id.imageView);
        imgArtista = (ImageView)findViewById(R.id.imageView2);


        nomeArtista = (TextView)findViewById(R.id.textView4);
        descrizione = (TextView)findViewById(R.id.desc);
        tecnica = (TextView)findViewById(R.id.tec);
        luogo = (TextView)findViewById(R.id.luogo);
        size = (TextView)findViewById(R.id.size);

        pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();

        //email dell'artista loggato
        email = pref.getString("email_artista",null);

        if(email != null){
            CheckLike ch = new CheckLike(url, email, getApplicationContext(), check);
            ch.execute();
        }else{
            visitatore = true;

        }


        /*
        tecnica.setText("Tecnica: " + artwork.getTecnique());

        if(artwork.getDescrizione().equals("null")){
            descrizione.setText("Descrizione non disponibile");
        }else{
            descrizione.setText("Descrizione: " + artwork.getDescrizione());
        }

        if(artwork.getLuogo().equals("null")){
            luogo.setText("Luogo non disponibile");
        }else{
            luogo.setText("Luogo: " + artwork.getLuogo());
        }

        if(artwork.getDimensioni().equals("null")){
            size.setVisibility(View.GONE);
        }else{
            size.setText("Dimensioni: "+ artwork.getDimensioni());
        }

        long likes = artwork.getLikes();
        if (likes != 0) {
            if (likes == 1)
                piace.setText("Piace a " + likes + " persona");
            else
                piace.setText("Piace a " + likes + " persone");
        }else{
            piace.setVisibility(View.GONE);
        }
        */



        tecnica.setText("Tecnica: " + artwork.getTecnique());

        if(getIntent().getExtras().get("descmod")!=null){
            String de=getIntent().getExtras().get("descmod").toString();
            if(de.equals("")) {
                descrizione.setText("Descrizione non disponibile");
                artwork.setDescrizione("null");
            }
            else {
                artwork.setDescrizione(de);
                descrizione.setText("Descrizione: " + de);
            }
            modifica=true;
        }
        else if (artwork.getDescrizione().equals("null") ||artwork.getDescrizione().equals("") || artwork.getDescrizione().equals("Descrizione")) {
            descrizione.setText("Descrizione non disponibile");

        }  else {
            descrizione.setText("Descrizione: " + artwork.getDescrizione());
        }

        if(getIntent().getExtras().get("luogomod")!=null){
            String lu=getIntent().getExtras().get("luogomod").toString();
            if(lu.equals("")){
                luogo.setText("Luogo non disponibile");
                artwork.setLuogo("null");
            }
            else {
                artwork.setLuogo(lu);
                luogo.setText("Luogo: " + lu);
            }
            modifica=true;
        }
        else if (artwork.getLuogo().equals("null") || artwork.getLuogo().equals("") || artwork.getLuogo().equals("Luogo")) {
            luogo.setText("Luogo non disponibile");
        } else {
            luogo.setText("Luogo: " + artwork.getLuogo());
        }

        if(getIntent().getExtras().get("dimmod")!=null){
            String di=getIntent().getExtras().get("dimmod").toString();
            if(di.equals("")) {
                size.setVisibility(View.GONE);
                artwork.setDimensioni("null");
            }
            else {
                artwork.setDimensioni(di);
                size.setText("Dimensioni: " + di);
            }
            modifica=true;
        }
        else if (artwork.getDimensioni().equals("null") || artwork.getDimensioni().equals("") || artwork.getDimensioni().equals("Dimensioni")) {
            size.setVisibility(View.GONE);
        } else {
            size.setText("Dimensioni: " + artwork.getDimensioni());
        }


        //per modificare l'artwork anche nel database locale.
        if(modifica){
            db.removeArtwork(url, db.getWritableDatabase());
            db.insert(artwork, db.getWritableDatabase());
        }

        //ora posso eseguire il display corretto.
        new DownloadArtist(getApplicationContext(),artwork.getArtista(),this).execute();
        //

        long likes = artwork.getLikes();

        if (likes != 0) {
            if (likes == 1)
                piace.setText("Piace a " + likes + " persona");
            else
                piace.setText("Piace a " + likes + " persone");
        }else{
            piace.setVisibility(View.GONE);
        }


        uiHelper = new UiLifecycleHelper(this, null);
        uiHelper.onCreate(savedInstanceState);

        commenti = (TextView) findViewById(R.id.commenti);
        commentoInScrittura = (EditText) findViewById(R.id.new_comment);
        btnOkComment=(Button) findViewById(R.id.bottone_new_comment);

        commenti.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(listaCommenti != null){
                    onShowPopup(v);
                }else{
                    Toast.makeText(getApplicationContext(), "Nessun commento da visualizzare", Toast.LENGTH_LONG).show();
                }
            }
        });



       final TaskCallbackInsertComment callComment = this;
       btnOkComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.bottone_new_comment) {
                    btnOkComment.setEnabled(false);

                    pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
                    SharedPreferences.Editor editor = pref.edit();
                    email = pref.getString("email_artista", null);

                    titleBox = (EditText) findViewById(R.id.new_comment);
                    if(email == null){ //se è null non è un artista, ma un visitatore. Devo fargli fare il login!
                        System.out.println("SONO VISITATORE");
                        forComment=true;
                        if(!mGoogleApiClient.isConnected()){
                            signInWithGplus();
                        }else{
                            getProfileInformation();
                        }

                    }else {
                        if(visitatore){
                            String commentoInScrittura = titleBox.getText().toString();
                            if (commentoInScrittura.length() > 1) {
                                new InsertComment(getApplicationContext(), emailPersona, commentoInScrittura, url, callComment).execute();
                                commentoInScrittura = "";
                            } else {
                                Toast.makeText(getApplicationContext(), "Commento non può essere vuoto!", Toast.LENGTH_LONG).show();
                            }
                        }else{
                            String commentoInScrittura = titleBox.getText().toString();
                            if (commentoInScrittura.length() > 1) {
                                new InsertComment(getApplicationContext(), email, commentoInScrittura, url, callComment).execute();
                                commentoInScrittura = "";
                            } else {
                                Toast.makeText(getApplicationContext(), "Commento non può essere vuoto!", Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                    btnOkComment.setEnabled(true);

               }
            }
        });
    }

    /*
    private void printKeyHash() {
        // Add code to print out the key hash
        try {
            PackageInfo info = getPackageManager().getPackageInfo("com.arteverywhere.francesco.art", PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("KeyHash:", e.toString());
        } catch (NoSuchAlgorithmException e) {
            Log.e("KeyHash:", e.toString());
        }
    }
    */

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String str = (String) parent.getItemAtPosition(position);
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
    }

    private void signInWithGplus() {
        if (!mGoogleApiClient.isConnecting()) {
            mSignInClicked = true;
            resolveSignInError();
        }
    }

    private void resolveSignInError() {
        System.out.println("sono qui 4");
        if (mConnectionResult.hasResolution()) {
            System.out.println("sono qui 5 ");
            try {
                mIntentInProgress = true;
                mConnectionResult.startResolutionForResult(this, RC_SIGN_IN);
            } catch (IntentSender.SendIntentException e) {
                mIntentInProgress = false;
                mGoogleApiClient.connect();
            }
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        System.out.println("entro in connection failed");
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
        }else{
            super.onActivityResult(requestCode, responseCode, intent);

            uiHelper.onActivityResult(requestCode, responseCode, intent, new FacebookDialog.Callback() {
                @Override
                public void onError(FacebookDialog.PendingCall pendingCall, Exception error, Bundle data) {
                    Log.e("Activity", String.format("Error: %s", error.toString()));
                }

                @Override
                public void onComplete(FacebookDialog.PendingCall pendingCall, Bundle data) {
                    Log.i("Activity", "Success!");
                }
            });
        }



    }

    @Override
    public void onConnected(Bundle arg0) {
        mSignInClicked = false;
        getProfileInformation();
    }


    /**
     * Fetching user's information name, email, profile pic
     * */
    private void getProfileInformation() {
        try {
            if (Plus.PeopleApi.getCurrentPerson(mGoogleApiClient) != null) {
                System.out.println("NON è NULL");
                //Person currentPerson = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);
                 emailPersona = Plus.AccountApi.getAccountName(mGoogleApiClient);
                //System.out.println("*email:" + email);

                if(forComment) {
                    forComment = false;
                    String commentoInScrittura = titleBox.getText().toString();
                    if (commentoInScrittura.length() > 0) {
                        new InsertComment(getApplicationContext(), emailPersona, commentoInScrittura, url, this).execute();
                    } else {
                        Toast.makeText(getApplicationContext(), "Commento non può essere vuoto!", Toast.LENGTH_LONG).show();
                    }
                }

                if(forLike){
                    System.out.println("email: " + emailPersona);

                    new CheckLike(url, emailPersona, getApplicationContext(), check).execute();
                    System.out.println("fatto " + fatto);
                    if (menu.getIcon().getConstantState().equals(getResources().getDrawable(R.drawable.liked).getConstantState())) {
                        menu.setIcon(getResources().getDrawable(R.drawable.unliked));
                        artwork.setLikes(artwork.getLikes() - 1);
                    } else {
                        menu.setIcon(getResources().getDrawable(R.drawable.liked));
                        artwork.setLikes(artwork.getLikes() + 1);
                    }
                    System.out.println(artwork.getFilename() + " " + artwork.getLikes());
                    long likes = artwork.getLikes();
                    if (likes != 0) {
                        if (likes == 1)
                            piace.setText("Piace a " + likes + " persona");
                        else
                            piace.setText("Piace a " + likes + " persone");
                    } else piace.setText("");
                    // chiama la funzione di libreria per aggiungere il like
                    PutLike p = new PutLike(url, emailPersona, getApplicationContext(), l);
                    //aspetto che checklike abbia impostato il testo del bottone like.

                    /*
                    while (!fatto) {
                    }
                    */

                    System.out.println("fatto " + fatto);

                    //imposto i like dell'artwork e cambio il testo del bottone
                    //if (like.getText().equals("Non mi piace più")) {

                    //eseguo l'operazione lato server.
                    p.execute();

                    forLike = false;

                }


            } else {
                // SOLO PER DEBUG

                String nome = "nome";
                String cognome = "cognome";
                String email = "ciao@ciao.it";

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onConnectionSuspended(int arg0) {
        mGoogleApiClient.connect();
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
        /*
        if (mGoogleApiClient.isConnected()) {
            Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
            mGoogleApiClient.disconnect();
            mGoogleApiClient.connect();
        }
        */

        pref.edit().putBoolean("token",true).commit();
    }


    //callback per downloadartworks
    public void done(){
        Intent myIntent = new Intent(ArtworkDetails.this, MainActivity.class);
        this.startActivity(myIntent);
        this.finish();
    }
    // callback from insertComment
    public void done(String x){
        //il parametro non serve (serve per differenziare i diversi done()
        commentoInScrittura.setHint("Scrivi un commento..");
        commentoInScrittura.setText("");
        new DownloadComments(this,this,url).execute();
        disconnetti();
    }

    // callback from PutLike
    public void done(boolean x){
        DatabaseArtwork db = new DatabaseArtwork(getApplicationContext());
        db.removeArtwork(url, db.getWritableDatabase());
        db.insert(artwork, db.getWritableDatabase());
        disconnetti();
    }

    //callback from like
    public void done(boolean l, boolean inutile){
        //precedentemente ho già messo il like.
        messoLike=l;

        System.out.println("sono in done from check like");

        if(forLike){
            if (!messoLike) {
                //like.setText("Mi piace");
                //MenuItem item = m.getItem(0);
                menu.setIcon(getResources().getDrawable(R.drawable.unliked));
            } else {
                //like.setText("Non mi piace più");
                //MenuItem item = m.getItem(0);
                menu.setIcon(getResources().getDrawable(R.drawable.liked));
            }
        }else {
            if (!messoLike) {
                //like.setText("Mi piace");
                MenuItem item = m.getItem(0);
                item.setIcon(getResources().getDrawable(R.drawable.unliked));
            } else {
                //like.setText("Non mi piace più");
                MenuItem item = m.getItem(0);
                item.setIcon(getResources().getDrawable(R.drawable.liked));
            }
        }
        fatto=true;
    }

    // callback from removeartwork
    public void done(int x){
        Toast.makeText(getApplicationContext(), "La tua foto è stata rimossa.", Toast.LENGTH_SHORT).show();
        if(isOnline()){
            //Log.d("DB","creo istanza di DB");
            DatabaseArtwork db = new DatabaseArtwork(getApplicationContext());
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

    //callback from DownloadArtist
    public void done(final String nc, final String pic2, final String nick, final String bio, final String sito){
        if(!tokenDaCommento) {
            artista = nc;

            //immagine artwork
            Picasso.with(ArtworkDetails.this)
                    .load(url)
                    .into(img);

            //immagine circolare per artista
            Picasso.with(ArtworkDetails.this).load(pic2).transform(new CircleTransform()).into(imgArtista);

            // NOME ARTISTA !!!!
            nomeArtista.setText(nc);
            titolo.setText(artwork.getFilename());

            imgArtista.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(ArtworkDetails.this, ArtistProfile.class);
                    intent.putExtra("nomecognome", nc);
                    intent.putExtra("bio", bio);
                    intent.putExtra("sito", sito);
                    intent.putExtra("nickname", nick);
                    intent.putExtra("email", artwork.getArtista());
                    intent.putExtra("pic", pic2);
                    startActivity(intent);

                }
            });

            new DownloadComments(this, this, url).execute();
        }else {
            tokenDaCommento = false;
            Intent intent = new Intent(ArtworkDetails.this, ArtistProfile.class);
            intent.putExtra("nomecognome", nc);
            intent.putExtra("bio", bio);
            intent.putExtra("sito", sito);
            intent.putExtra("nickname", nick);
            intent.putExtra("email", emailOfItemSelectedInComment);
            intent.putExtra("pic", pic2);
            startActivity(intent);
        }
    }

    //callback from DownloadComments
    @Override
    public void done(String[] autori, String[] comm, String[] date, String[] pic, String[] mails) {
        this.listaAutori = autori;
        this.listaCommenti = comm;
        this.listaDate = date;
        this.listaPic = pic;
        this.listaEmail = mails;

        long[] dateMillis = new long[listaDate.length];
        //String givenDateString = "2015-03-17 19:03:53";

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:SS");
        for(int i=0;i<listaDate.length;i++) {
            String givenDate = listaDate[i];
            //System.out.println(givenDate);
            int ora = Character.getNumericValue(givenDate.charAt(12));
            String givenDateString = givenDate.substring(0,12) + "" + (ora+1) + "" + givenDate.substring(13,19);
            try {
                Date mDate = sdf.parse(givenDateString);
                long timeInMilliseconds = mDate.getTime();
                dateMillis[i] = timeInMilliseconds;
            //    System.out.println("Date in milli :: " + timeInMilliseconds);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        timeAgo = new String[dateMillis.length];
        for(int i = 0; i< dateMillis.length;i++){
            timeAgo[i] = getTimeAgo(dateMillis[i],getApplicationContext());
        }


        if(comm.length == 1){
            commenti.setText(comm.length + " commento");
        }else{
            commenti.setText(comm.length + " commenti");
        }
    }


    // call this method when required to show popup
    public void onShowPopup(View v){

        LayoutInflater layoutInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // inflate the custom popup layout
        final View inflatedView = layoutInflater.inflate(R.layout.popup_layout, null,false);
        // find the ListView in the popup layout
        ListView listView = (ListView)inflatedView.findViewById(R.id.commentsListView);

        // get device size
        Display display = getWindowManager().getDefaultDisplay();
        final Point size = new Point();
        display.getSize(size);
        int mDeviceHeight = size.y;

        // fill the data to the list items
        setSimpleList(listView);


        // set height depends on the device size
        popWindow = new PopupWindow(inflatedView, size.x - 50,size.y - 400, true );
        // set a background drawable with rounders corners
        popWindow.setBackgroundDrawable(getResources().getDrawable(R.drawable.popup_bg));
        // make it focusable to show the keyboard to enter in `EditText`
        popWindow.setFocusable(true);
        // make it outside touchable to dismiss the popup window
        popWindow.setOutsideTouchable(true);

        // show the popup at bottom of the screen and set some margin at bottom ie,
        popWindow.showAtLocation(v, Gravity.BOTTOM, 0,100);

        EditText comme = (EditText)inflatedView.findViewById(R.id.writeComment);
        comme.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                popWindow.dismiss();
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            }
        });

    }

    void setSimpleList(ListView listView){

        CustomListCommenti adapter = new CustomListCommenti(ArtworkDetails.this, listaCommenti, listaPic, timeAgo, listaAutori);
        listView.setAdapter(adapter);

        final TaskCallbackDownloadArtist cal = this;

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                System.out.println(listaAutori[position]);
                if(!listaAutori[position].equalsIgnoreCase("Visitatore")){
                    tokenDaCommento = true;
                    emailOfItemSelectedInComment = listaEmail[position];
                    new DownloadArtist(getApplicationContext(),listaEmail[position],cal).execute();
                }
            }
        });


    }

    public static String getTimeAgo(long time, Context ctx) {
        if (time < 1000000000000L) {
            // if timestamp given in seconds, convert to millis
            time *= 1000;
        }

        long now = System.currentTimeMillis(); //getCurrentTime(ctx);
        if (time > now || time <= 0) {
            return null;
        }

        // TODO: localize
        final long diff = now - time;
        if (diff < MINUTE_MILLIS) {
            return "adesso";
        } else if (diff < 2 * MINUTE_MILLIS) {
            return "1 minuto fa";
        } else if (diff < 50 * MINUTE_MILLIS) {
            return diff / MINUTE_MILLIS + " minuti fa";
        } else if (diff < 90 * MINUTE_MILLIS) {
            return "1 ora fa";
        } else if (diff < 24 * HOUR_MILLIS) {
            return diff / HOUR_MILLIS + " ore fa";
        } else if (diff < 48 * HOUR_MILLIS) {
            return "ieri";
        } else {
            return diff / DAY_MILLIS + " giorni fa";
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_artwork_details, menu);
        if(email!=null && email.equals(artwork.getArtista())){
            getMenuInflater().inflate(R.menu.menu_artwork_details, menu);
            m=menu;
        }else if(email!=null && !email.equals(artwork.getArtista())){
            getMenuInflater().inflate(R.menu.menu_artwork_details_artista, menu);
            m=menu;
        }
        else {
            System.out.println(artwork.getArtista());
            getMenuInflater().inflate(R.menu.menu_artwork_details_visitatore, menu);
            m=menu;
        }
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
        }else if (id == R.id.menu_item_rimuovi) {
            d = this;
            AlertDialog mDialog = new AlertDialog.Builder(this)
                    .setTitle("Vuoi davvero rimuovere la tua opera?")
                    .setPositiveButton("Si",
                            new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    DeleteArtwork a = new DeleteArtwork(url, getApplicationContext(), d);
                                    a.execute();
                                }
                            })

                    .setNegativeButton("No",
                            new android.content.DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            }).create();
            mDialog.show();

            return true;
        } else if (id == R.id.menu_item_modifica) {
            Intent intent = new Intent(ArtworkDetails.this, ModificaArtwork.class);
            //passo al modificaArtwork l'url della foto
            intent.putExtra("url", url);

            if (!visitatore)
                intent.putExtra("email", email);

            intent.putExtra("visitatore", visitatore);
            startActivity(intent);
            this.finish();
            return true;
        } else if (id == R.id.menu_item_share) {
            final AlertDialog.Builder alertDialog = new AlertDialog.Builder(ArtworkDetails.this);
            final LayoutInflater inflater = getLayoutInflater();
            final View convertView = (View) inflater.inflate(R.layout.custom, null);
            alertDialog.setView(convertView);
            alertDialog.setTitle("Condividi con");

            final String[] filtri = {"Facebook", "Facebook Messenger", "Whatsapp", "Twitter"};
            final int[] immagini = {R.drawable.fb, R.drawable.me, R.drawable.wa, R.drawable.tw};

            CustomListInt adapter = new CustomListInt(ArtworkDetails.this, filtri, immagini);
            ListView lv = (ListView) convertView.findViewById(R.id.listView1);

            lv.setAdapter(adapter);
            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String item = filtri[position];
                    dialog.dismiss();

                    if (item.equalsIgnoreCase("Facebook")) {
                        eseguiShareFacebook();
                    } else if (item.equalsIgnoreCase("Facebook Messenger")) {
                        sendFacebookMessage(findViewById(android.R.id.content));
                    } else if (item.equalsIgnoreCase("Whatsapp")) {
                        eseguiShareWhatsapp();
                    } else if (item.equalsIgnoreCase("Twitter")) {
                        shareTwitter();
                    }
                }
            });

            dialog = alertDialog.show();
            return true;
        } else if (id == R.id.like) {
            System.out.println("ho cliccato like");

            if (visitatore) {
                System.out.println("sono visitatore");
                forLike=true;
                menu = item;
                if(!mGoogleApiClient.isConnected()){
                    signInWithGplus();
                }else{
                    getProfileInformation();
                }
            } else {
                //se l'utente è registrato ho la mail

                PutLike p = new PutLike(url, email, getApplicationContext(), l);
                //aspetto che checklike abbia impostato il testo del bottone like.
                while (!fatto) {
                }
                //imposto i like dell'artwork e cambio il testo del bottone
                //if (like.getText().equals("Non mi piace più")) {
                if (item.getIcon().getConstantState().equals(getResources().getDrawable(R.drawable.liked).getConstantState())) {
                    item.setIcon(getResources().getDrawable(R.drawable.unliked));
                    artwork.setLikes(artwork.getLikes() - 1);
                } else {
                    item.setIcon(getResources().getDrawable(R.drawable.liked));
                    artwork.setLikes(artwork.getLikes() + 1);
                }


                System.out.println(artwork.getFilename() + " " + artwork.getLikes());
                long likes = artwork.getLikes();
                if (likes != 0) {
                    if (likes == 1)
                        piace.setText("Piace a " + likes + " persona");
                    else
                        piace.setText("Piace a " + likes + " persone");
                } else piace.setText("");

                //eseguo l'operazione lato server.
                p.execute();
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }


        @Override
        protected void onResume() {
            super.onResume();
            uiHelper.onResume();
        }

        @Override
        protected void onSaveInstanceState(Bundle outState) {
            super.onSaveInstanceState(outState);
            uiHelper.onSaveInstanceState(outState);
        }

        @Override
        public void onPause() {
            super.onPause();
            uiHelper.onPause();
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            uiHelper.onDestroy();
        }


    Session.StatusCallback callback = new Session.StatusCallback() {

        @Override
        public void call(Session session, SessionState state, Exception exception) {

            if (state.isOpened()) {
                publishFeedDialog();
            }
        }
    };

    public void eseguiShareFacebook() {
        if (Session.getActiveSession() == null || !Session.getActiveSession().isOpened()) {
            Session.openActiveSession(ArtworkDetails.this, true, callback);
        } else {
            publishFeedDialog();
        }
    }


    public void eseguiShareWhatsapp() {
        Uri bmpUri = getLocalBitmapUri(img);

        if (bmpUri != null) {
            // Construct a ShareIntent with link to image
            Intent shareIntent = new Intent();
            shareIntent.setPackage("com.whatsapp");
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_STREAM, bmpUri);
            shareIntent.putExtra(Intent.EXTRA_TEXT, url);
            shareIntent.putExtra(Intent.EXTRA_TEXT, "Segui '" + artista.toUpperCase() + "' su Art Everywhere e scopri tutte le sue opere! Scarica ora l'app! " + "http://bit.ly/ArtEverywhereDownload");

            shareIntent.setType("image/*");
            // Launch sharing dialog for image
            startActivity(Intent.createChooser(shareIntent, "Share Image"));
        }
        else {
            Toast.makeText(getApplicationContext(), "Devi prima installare Whatsapp", Toast.LENGTH_LONG).show();
        }
    }


    //METODO PER CONDIVISIONE SU FACEBOOK MESSENGER
    public void sendFacebookMessage(View v) {
        FacebookDialog.MessageDialogBuilder builder = new FacebookDialog.MessageDialogBuilder(
                this)
                .setLink("http://bit.ly/ArtEverywhereDownload")
                .setName("Segui '" + artista.toUpperCase() + "' su Art Everywhere e scopri tutte le sue opere! Scarica ora l'app!")
                .setPicture(url)
                .setDescription("Art Everywhere - a new way to promote art!");

        // If the Facebook Messenger app is installed and we can present the share dialog
        if (builder.canPresent()) {
            // Enable button or other UI to initiate launch of the Message Dialog
            FacebookDialog dialog = builder.build();
            uiHelper.trackPendingDialogCall(dialog.present());
        } else {
            // Disable button or other UI for Message Dialog
            Toast.makeText(getApplicationContext(), "Devi prima installare Facebook Messenger", Toast.LENGTH_SHORT).show();
            v.setEnabled(false);
        }
    }

    public static Bitmap getBitmapFromURL(String src) {
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            // Log exception
            return null;
        }
    }

    private void shareTwitter(){
        try {
            Intent tweetIntent = new Intent(Intent.ACTION_SEND);
            Uri bmpUri = getLocalBitmapUri(img);

            tweetIntent.putExtra(Intent.EXTRA_TEXT, "Scopri '" + artista.toUpperCase() + "' su Art Everywhere! Scarica ora l'app! http://bit.ly/ArtEverywhereDownload");
            tweetIntent.putExtra(Intent.EXTRA_STREAM, bmpUri);
            tweetIntent.setType("image/jpeg");
            PackageManager pm = ArtworkDetails.this.getPackageManager();
            List<ResolveInfo> lract = pm.queryIntentActivities(tweetIntent, PackageManager.MATCH_DEFAULT_ONLY);
            boolean resolved = false;
            for (ResolveInfo ri : lract) {
                if (ri.activityInfo.name.contains("twitter")) {
                    tweetIntent.setClassName(ri.activityInfo.packageName,
                            ri.activityInfo.name);
                    resolved = true;
                    break;
                }
            }

            startActivity(resolved ?
                    tweetIntent :
                    Intent.createChooser(tweetIntent, "Choose one"));
        } catch (final ActivityNotFoundException e) {
            Toast.makeText(ArtworkDetails.this, "Devi prima installare Twitter", Toast.LENGTH_SHORT).show();
        }
    }




    private void publishFeedDialog() {
        Bundle params = new Bundle();
        params.putString("name", "Segui '" + artista.toUpperCase() + "' su Art Everywhere e scopri tutte le sue opere! Scarica ora l'app!");
        params.putString("caption", "Art Everywhere");
        params.putString("description","Art Everywhere is a new way to share and promote your artworks!");
        params.putString("link", "http://bit.ly/ArtEverywhereDownload");
        params.putString("picture", url);
        Session session = Session.getActiveSession();
        WebDialog feedDialog = (
                new WebDialog.FeedDialogBuilder(ArtworkDetails.this,
                        session,
                        params))
                .setOnCompleteListener(new WebDialog.OnCompleteListener() {

                    @Override
                    public void onComplete(Bundle values,
                                           FacebookException error) {
                        if (error == null) {
                            // When the story is posted, echo the success
                            // and the post Id.
                            final String postId = values.getString("post_id");
                            if (postId != null) {
                                Toast.makeText(ArtworkDetails.this,
                                        "Posted story, id: "+postId,
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                // User clicked the Cancel button
                                Toast.makeText(ArtworkDetails.this.getApplicationContext(),
                                        "Publish cancelled",
                                        Toast.LENGTH_SHORT).show();
                            }
                        } else if (error instanceof FacebookOperationCanceledException) {
                            // User clicked the "x" button
                            Toast.makeText(ArtworkDetails.this.getApplicationContext(),
                                    "Publish cancelled",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            // Generic, ex: network error
                            Toast.makeText(ArtworkDetails.this.getApplicationContext(),
                                    "Error posting story",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                })
                .build();
        feedDialog.show();
    }



    public Uri getLocalBitmapUri(ImageView imageView) {
        // Extract Bitmap from ImageView drawable
        Drawable drawable = imageView.getDrawable();
        Bitmap bmp = null;
        if (drawable instanceof BitmapDrawable){
            bmp = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        } else {
            return null;
        }
        // Store image to default external storage directory
        Uri bmpUri = null;
        try {
            File file =  new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS), "share_image_" + System.currentTimeMillis() + ".png");
            file.getParentFile().mkdirs();
            FileOutputStream out = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.close();
            bmpUri = Uri.fromFile(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bmpUri;
    }



}

class CircleTransform implements Transformation {
    @Override
    public Bitmap transform(Bitmap source) {
        int size = Math.min(source.getWidth(), source.getHeight());

        int x = (source.getWidth() - size) / 2;
        int y = (source.getHeight() - size) / 2;

        Bitmap squaredBitmap = Bitmap.createBitmap(source, x, y, size, size);
        if (squaredBitmap != source) {
            source.recycle();
        }

        Bitmap bitmap = Bitmap.createBitmap(size, size, source.getConfig());

        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        BitmapShader shader = new BitmapShader(squaredBitmap,
                BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP);
        paint.setShader(shader);
        paint.setAntiAlias(true);

        float r = size / 2f;
        canvas.drawCircle(r, r, r, paint);

        squaredBitmap.recycle();
        return bitmap;
    }

    @Override
    public String key() {
        return "circle";
    }
}

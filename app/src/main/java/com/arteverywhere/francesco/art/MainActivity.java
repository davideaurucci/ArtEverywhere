package com.arteverywhere.francesco.art;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.squareup.picasso.Picasso;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import cod.com.appspot.art_everywhere.artEverywhere.ArtEverywhere;
import cod.com.appspot.art_everywhere.artEverywhere.model.MainArtistBriefCollection;
import cod.com.appspot.art_everywhere.artEverywhere.model.MainTechniqueResponseCollection;



public class MainActivity extends ActionBarActivity implements TaskCallbackDownloadArtworks, TaskCallbackRefreshArtworks, TaskCallbackDownloadFilteredArtworks, TaskCallbackDownloadArtistForGallery {

    String[] urlPhoto = new String[AppConstants.numFoto];
    SharedPreferences pref;
    String email;
    boolean visitatore = false;

    public static SwipeRefreshLayout swipeView;
    DatabaseArtwork db;
    GridView gridview;

    AlertDialog dialog;
    String[] tecniche;
    String tecnicaScelta;

    public DownloadArtworksByDate d;
    TaskCallbackDownloadArtworks callback;
    int pagina;

    boolean isFiltri = false;
    boolean isSelected; //serve nel dialog per filtro città
    String selectedCity; // serve nel dialog per filtro città
    String[] artists;
    String[] artistsPic;
    String[] artistsMail;
    String selectedArtist; //email dell'artista selezionato
    String picArtista; //url della picture profile dell'artista selezionato

    View fabUpload; //Floating action bar

    // Filtered list of techniques
    private ArrayList<String> partialNames = new ArrayList<String>();
    // List of names matching criteria are listed here
    private ListView myList;
    // Field where user enters his search criteria
    private EditText nameCapture;
    // Adapter for myList
    private ArrayAdapter<String> myAdapter;
    TextView emptyText;

    private CustomListArtists adapterArtists;
    private ArrayList<String> imagesurl = new ArrayList<String>();
    private ArrayList<String> artistsemails = new ArrayList<String>();
    private ArrayList<Artist> artistlist = new ArrayList<Artist>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //BLOCCO SCREENSHOT
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);

        pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        email = pref.getString("email_artista",null);

        if(email==null){
            visitatore=true;
        }
        if(!visitatore) { //visualizzo il floating button per l'upload solo se è artista
            fabUpload = findViewById(R.id.fab);
            fabUpload.setVisibility(View.VISIBLE);
            fabUpload.bringToFront();
            fabUpload.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent myIntent = new Intent(MainActivity.this, UploadActivity.class);
                    myIntent.putExtra("email", email);
                    startActivity(myIntent);
                }
            });
        }


        /* VISUALIZZO ACTION BAR CON LOGO */
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setLogo(R.drawable.logo_trasparente);
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        swipeView = (SwipeRefreshLayout) findViewById(R.id.activity_main_swipe_refresh_layout);
        swipeView.setEnabled(false);

        gridview = (GridView) findViewById(R.id.gridView);
        gridview.setAdapter(new ImageAdapter(this));

        db = new DatabaseArtwork(getApplicationContext());
        urlPhoto = db.getPhotos();

        swipeView.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeView.setRefreshing(true);

                ( new Handler()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        swipeView.setRefreshing(false);

                        /* Devo chiamare l'API */
                        chiamaApi();
                    }
                }, 2000);

            }
        });

        callback=this; /* Serve per poter chiamare la funz di libr (con callback) all'interno dell'OnScrollListener */
        gridview.setOnScrollListener(new EndlessScrollListener() {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {

                // Triggered only when new data needs to be appended to the list
                pagina=page;
                //ho già le foto nel database. non devo ricaricarle

                //Se filtro la ricerca "disabilito" l'endless scroll
                if(!isFiltri) {
                    System.out.println("******"+urlPhoto.length+"******"+db.getAllArtworks().size());
                    if(db.getAllArtworks().size()>=urlPhoto.length+4){

                        String [] b=db.getPhotosAfterScroll(((pagina/2)+1)*4+20);
                        urlPhoto=new String[b.length];
                        urlPhoto=b;
                        gridview.setSelection(gridview.getFirstVisiblePosition());
                        gridview.setAdapter(new ImageAdapter(getApplicationContext()));

                    }
                   else{

                        String data=db.getArtworkFromUrl(urlPhoto[urlPhoto.length-1]).getData();
                        new DownloadArtworksByDate(getApplicationContext(),db, callback, data).execute();

                    }

                }
            }

        });



        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, ArtworkDetails.class);
                String url = urlPhoto[position];
                intent.putExtra("photo",url);

                startActivity(intent);
            }
        });

    }


    public void chiamaApi(){
        if(checkNetwork()) new DownloadArtworksForRefresh(getApplicationContext(),db,this).execute();
        // Dopo aver completato l'asyntask viene chiamato done(boolean,boolean)
    }

    public void done(boolean x, boolean a){
        /* I parametri x e a non servono a nulla */

        /* Faccio una ricerca per data sul DB */
        urlPhoto = db.getArtworksOrderByDate();

        /* Chiamo gridview.setAdapter(new ImageAdapter(this)); */
        gridview.setAdapter(new ImageAdapter(this));
    }

    /* Serve per l'endless scroll */
    private String[] toArray(List<Artwork> l){
        int c=0;
        Iterator i =l.iterator();
        while(i.hasNext())
            c++;
        String [] f=new String[c];
        Iterator t=l.iterator();
        int g=0;
        while(t.hasNext()) {
            f[g] = l.get(g).getPhoto();
            g++;
        }
        return f;
    }

    /* Serve per l'endless scroll - TaskCallback Download artworks */
    @Override
    public void done() {
        String [] a=db.getPhotosAfterScroll(((pagina/2)+1)*4+20);
        urlPhoto=new String[a.length];
        urlPhoto=a;

        for(int i=0; i<urlPhoto.length; i++){
            if(urlPhoto[i]!=null){
                System.out.println(urlPhoto[i]);

            }
        }
        gridview.setSelection(gridview.getFirstVisiblePosition());
        gridview.setAdapter(new ImageAdapter(this));
    }

    //Custom adapter
    private class ImageAdapter extends BaseAdapter {
        private Context mContext;

        public ImageAdapter(Context context) {
            mContext = context;
        }

        @Override
        public int getCount() {
            return urlPhoto.length;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView,
                            ViewGroup parent) {


            ImageView imageView;
            //check to see if we have a view
            if (convertView == null) {
            //no view - so create a new one
                imageView = new ImageView(mContext);
            } else {
            //use the recycled view object
                imageView = (ImageView) convertView;
            }

            //Picasso.with(MainActivity.this).setDebugging(true);

            DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metrics);

            int size = metrics.widthPixels;

            Picasso.with(MainActivity.this)
                    .load("" + urlPhoto[position] + "")
                    //.placeholder(R.raw.place_holder)
                    .error(R.raw.place_holder)
                    .noFade().resize(size/2, size/2)
                    .centerCrop()
                    .into(imageView);
            return imageView;

       }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        if(visitatore){
            //non deve visualizzare nulla nella action bar
            getMenuInflater().inflate(R.menu.menu_main_per_visitatori, menu);
        }else {
            getMenuInflater().inflate(R.menu.menu_main, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.logout) {
            //Login.signOutFromGplus();
            pref.edit().remove("email_artista").commit();
            pref.edit().putBoolean("token", true).commit();

            Intent i = new Intent(MainActivity.this, Login.class);
            this.startActivity(i);
            Toast.makeText(getApplicationContext(), "Logout eseguito!", Toast.LENGTH_LONG).show();
            this.finish();
            return true;
        } else if (id == R.id.menu_filtro) {
                /* Faccio visualizzare l'alert dialog con la lista dei filtri possibli */
            final AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
            final LayoutInflater inflater = getLayoutInflater();
            final View convertView = (View) inflater.inflate(R.layout.custom, null);
            alertDialog.setView(convertView);
            alertDialog.setTitle("Filtra ricerca");
            ListView lv = (ListView) convertView.findViewById(R.id.listView1);

            final String[] filtri = {"Più recenti", "Tecnica", "Luogo", "Artista"};

            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String item = filtri[position];
                    System.out.println("Ho scelto: " + item);
                    dialog.dismiss();
                    //isTecnica = true;
                    //effettuaDownload();

                    if (item.equalsIgnoreCase("Più recenti")) {
                        seeMoreRecent();
                    } else if (item.equalsIgnoreCase("Tecnica")) { //è stato selezionato -> TECNICA
                        getTecniche();
                    } else if (item.equalsIgnoreCase("Luogo")) { //è stato selezionato -> LUOGO
                        seePlace();
                    } else if (item.equalsIgnoreCase("Artista")) { //è stato selezionato -> ARTISTA
                        seeArtists();
                    }
                }
            });
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.list_item_black, filtri);
            lv.setAdapter(adapter);
            dialog = alertDialog.show();
            return true;
        } else if (id == R.id.menu_profile) {
            if(checkNetwork()) new DownloadArtistForGallery(getApplicationContext(), email, this).execute();
        } else if (id == R.id.menu_feedback) {
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("message/rfc822");
            i.putExtra(Intent.EXTRA_EMAIL, new String[]{"arteverywhere00@gmail.com"});
            i.putExtra(Intent.EXTRA_SUBJECT, "Feedback - Art Everywhere");
            i.putExtra(Intent.EXTRA_TEXT, "");
            try {
                startActivity(Intent.createChooser(i, "Invia feedback..."));
            } catch (android.content.ActivityNotFoundException ex) {
                Toast.makeText(MainActivity.this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
            }
            return true;
        } else if (id == R.id.menu_facebook) {
            try {
                getApplicationContext().getPackageManager().getPackageInfo("com.facebook.katana", 0);
                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("fb://page/1596309167253264"));
                startActivity(i);
            } catch (Exception e) {
                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/ArtEverywhereApp"));
                startActivity(i);
            }
        } else if (id == R.id.menu_valutaci){
            Uri uri = Uri.parse("market://details?id=" + getApplicationContext().getPackageName());
            Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
            try {
                startActivity(goToMarket);
            } catch (ActivityNotFoundException e) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + getApplicationContext().getPackageName())));
            }
        }

        else if (id==R.id.menu_consiglia){
            final AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
            final LayoutInflater inflater = getLayoutInflater();
            final View convertView = (View) inflater.inflate(R.layout.custom, null);
            alertDialog.setView(convertView);
            alertDialog.setTitle("Consiglia Art Everywhere ai tuoi amici");

            final String[] filtri = { "Twitter", "Whatsapp", "Email"};
            final int[] immagini = {R.drawable.tw, R.drawable.wa, R.drawable.em};

            CustomListInt adapter = new CustomListInt(MainActivity.this, filtri, immagini);
            ListView lv = (ListView) convertView.findViewById(R.id.listView1);

            lv.setAdapter(adapter);
            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String item = filtri[position];
                    dialog.dismiss();

                    if (item.equalsIgnoreCase("Whatsapp")) {
                        eseguiShareWhatsapp();
                    } else if (item.equalsIgnoreCase("Twitter")) {
                        shareTwitter();
                    } else if (item.equalsIgnoreCase("Email")) {
                        shareEmail();
                    }
                }
            });

            dialog = alertDialog.show();
            return true;
        }


        else if(id==R.id.artista){
            pref.edit().putBoolean("token",true).commit();
            Intent myIntent = new Intent(MainActivity.this, Login.class);
            this.startActivity(myIntent);
            this.finish();
        }

        return super.onOptionsItemSelected(item);
    }

    public void seeMoreRecent(){
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle("Galleria");
        urlPhoto = db.getPhotos();
        gridview.setAdapter(new ImageAdapter(this));
    }

    /* Scarico le tecniche da visualizzare nel dialog */
    private void getTecniche(){
        AsyncTask<Void, Void, MainTechniqueResponseCollection> getTec = new AsyncTask<Void, Void, MainTechniqueResponseCollection>() {

            @Override
            protected MainTechniqueResponseCollection doInBackground(Void... unused) {
                // Retrieve service handle.
                ArtEverywhere apiServiceHandle = AppConstants.getApiServiceHandle(null);

                try {
                    MainTechniqueResponseCollection greeting = new MainTechniqueResponseCollection();
                    ArtEverywhere.Techniques.Gettechniques get = apiServiceHandle.techniques().gettechniques();
                    greeting = get.execute();
                    return greeting;
                } catch (IOException e) {
                    Toast.makeText(getApplicationContext(), "Exception during API call - tecniche!", Toast.LENGTH_LONG).show();
                    //Log.d("ERRORE",e.getMessage());
                }
                return null;
            }

            @Override
            protected void onPostExecute(MainTechniqueResponseCollection greeting) {
                if (greeting != null) {

                    tecniche = new String[greeting.getTechniques().size()];
                    for(int i = 0; i < greeting.getTechniques().size(); i++){
                        tecniche[i] = greeting.getTechniques().get(i).getTechnique();
                    }

                    final AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
                    final LayoutInflater inflater = getLayoutInflater();
                    final View convertView = (View) inflater.inflate(R.layout.custom_autocomplete_listview, null);
                    alertDialog.setView(convertView);
                    alertDialog.setTitle("Tecniche");

                    myList = (ListView) convertView.findViewById(R.id.listView1);
                    nameCapture = (EditText) convertView.findViewById(R.id.name);
                    nameCapture.setHint("Digita la tecnica che vuoi ricercare");
                    emptyText = (TextView) convertView.findViewById(android.R.id.empty);
                    myList.setEmptyView(emptyText);

                    emptyText.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent i = new Intent(Intent.ACTION_SEND);
                            i.setType("message/rfc822");
                            i.putExtra(Intent.EXTRA_EMAIL  , new String[]{"arteverywhere00@gmail.com"});
                            i.putExtra(Intent.EXTRA_SUBJECT, "[Suggerimento] Tecnica Mancante");
                            i.putExtra(Intent.EXTRA_TEXT   , "Voglio suggerire la seguente tecnica mancante: " + nameCapture.getText().toString());
                            try {
                                startActivity(Intent.createChooser(i, "Invia suggerimento..."));
                            } catch (android.content.ActivityNotFoundException ex) {
                                Toast.makeText(MainActivity.this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });



                    myList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            tecnicaScelta = partialNames.get(position);
                            dialog.dismiss();
                            effettuaDownloadPerTecnica(tecnicaScelta);
                        }
                    });

                    for(int i = 0; i < tecniche.length;i++) partialNames.add(i, tecniche[i]);

                    myAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.list_item_black, partialNames);
                    myList.setAdapter(myAdapter);

                    nameCapture.addTextChangedListener(new TextWatcher() {

                        // As the user types in the search field, the list is
                        @Override
                        public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                            AlterAdapter();
                        }

                        // Not used for this program
                        @Override
                        public void afterTextChanged(Editable arg0) {

                        }

                        // Not uses for this program
                        @Override
                        public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                            // TODO Auto-generated method stub

                        }
                    });

                    dialog = alertDialog.show();
                } else {
                    Toast.makeText(getApplicationContext(), "No greetings were returned by the API.", Toast.LENGTH_LONG).show();
                }
            }
        };
        if(checkNetwork()) getTec.execute();
    }

    private void AlterAdapter() {
        if (nameCapture.getText().toString().isEmpty()) {
            partialNames.clear();
            for(int i = 0; i < tecniche.length;i++) partialNames.add(i,tecniche[i]);
            myAdapter.notifyDataSetChanged();
        }
        else {
            partialNames.clear();
            for (int i = 0; i < tecniche.length; i++) {
                if (tecniche[i].toString().toUpperCase().contains(nameCapture.getText().toString().toUpperCase())) {
                    partialNames.add(tecniche[i].toString());
                }
                myAdapter.notifyDataSetChanged();
            }
        }

        if(myAdapter.getCount()==0){
            emptyText.setVisibility(View.VISIBLE);
            emptyText.setText("Clicca per segnalare tecnica mancante!");
        }
    }

    private void AlterAdapterArtists() {
        if (nameCapture.getText().toString().isEmpty()) {
            partialNames.clear();
            imagesurl.clear();
            artistsemails.clear();
            artistlist.clear();
            for(int i = 0; i < artists.length;i++){
                partialNames.add(i, artists[i]);
                imagesurl.add(i, artistsPic[i]);
                artistsemails.add(i, artistsMail[i]);
                Artist a = new Artist(artists[i],artistsPic[i],artistsMail[i]);
                artistlist.add(a);
            }
            adapterArtists.notifyDataSetChanged();
        }
        else {
            partialNames.clear();
            imagesurl.clear();
            artistsemails.clear();
            artistlist.clear();
            for (int i = 0; i < artists.length; i++) {
                if (artists[i].toString().toUpperCase().contains(nameCapture.getText().toString().toUpperCase())) {
                    System.out.println(artists[i].toString());
                    System.out.println(artistsPic[i].toString());
                    partialNames.add(artists[i]);
                    imagesurl.add(artistsPic[i]);
                    artistsemails.add(artistsMail[i]);
                    Artist a = new Artist(artists[i],artistsPic[i],artistsMail[i]);
                    artistlist.add(a);
                }
                adapterArtists.notifyDataSetChanged();
            }
        }

        if(adapterArtists.getCount()==0){
            emptyText.setVisibility(View.VISIBLE);
            emptyText.setText("Nessun artista trovato!");
        }
    }

    public void effettuaDownloadPerTecnica(String tecnicaScelta){
        if(checkNetwork()) new DownloadArtworksByTechinique(getApplicationContext(),db,this,tecnicaScelta).execute();
    }

    public void seePlace(){
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
        final LayoutInflater inflater = getLayoutInflater();
        final View convertView = (View) inflater.inflate(R.layout.custom_filter_place, null);
        alertDialog.setView(convertView);
        alertDialog.setTitle("Luogo");

        isSelected = false;
        AutoCompleteTextView place = (AutoCompleteTextView) convertView.findViewById(R.id.editText3);
        place.setAdapter(new PlacesAutoCompleteAdapter(this, R.layout.list_item));
        place.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                isSelected = true;
                String str = (String) parent.getItemAtPosition(position);
                selectedCity = str;
                Log.d("PLACE", str);
            }
        });

        Button cerca = (Button) convertView.findViewById(R.id.buttonOk);
        cerca.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isSelected){
                    Toast.makeText(getApplicationContext(), "Attenzione! Devi selezionare uno dei suggerimenti proposti", Toast.LENGTH_LONG).show();
                }else{
                    dialog.dismiss();
                    effettuaDownloadPerLuogo(selectedCity);
                }
            }
        });

        dialog = alertDialog.show();
    }

    public void effettuaDownloadPerLuogo(String city){
        if(checkNetwork()) new DownloadArtworksByPlace(getApplicationContext(),db,this,city).execute();
    }

    public void seeArtists(){
        AsyncTask<Void, Void, MainArtistBriefCollection> getArtists = new AsyncTask<Void, Void, MainArtistBriefCollection>() {

            @Override
            protected MainArtistBriefCollection doInBackground(Void... unused) {
                ArtEverywhere apiServiceHandle = AppConstants.getApiServiceHandle(null);

                try {
                    MainArtistBriefCollection greeting = new MainArtistBriefCollection();
                    ArtEverywhere.Artistlist.Getartists get = apiServiceHandle.artistlist().getartists();
                    greeting = get.execute();
                    return greeting;
                } catch (IOException e) {
                    Toast.makeText(getApplicationContext(), "Exception during API call - get Artists!", Toast.LENGTH_LONG).show();
                }
                return null;
            }

            @Override
            protected void onPostExecute(MainArtistBriefCollection greeting) {
                if (greeting != null) {
                    artists = new String[greeting.getArtists().size()];
                    artistsPic = new String[greeting.getArtists().size()];
                    artistsMail = new String[greeting.getArtists().size()];

                    for(int i= 0;i < greeting.getArtists().size(); i++){
                        artists[i] = greeting.getArtists().get(i).getCognome() + " " + greeting.getArtists().get(i).getNome();
                        artistsPic[i] = greeting.getArtists().get(i).getPic();
                        artistsMail[i] = greeting.getArtists().get(i).getEmail();
                    }

                    final AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
                    final LayoutInflater inflater = getLayoutInflater();
                    final View convertView = (View) inflater.inflate(R.layout.custom_autocomplete_listview, null);
                    alertDialog.setView(convertView);
                    alertDialog.setTitle("Artisti");


                    for(int i = 0; i < artists.length;i++){
                        partialNames.add(i, artists[i]);
                        imagesurl.add(i, artistsPic[i]);
                        artistsemails.add(i, artistsMail[i]);
                        Artist a = new Artist(artists[i],artistsPic[i],artistsMail[i]);
                        artistlist.add(a);
                    }

                    //adapterArtists = new CustomListArtists(MainActivity.this, partialNames, imagesurl);
                    adapterArtists = new CustomListArtists(MainActivity.this, artistlist);

                    myList = (ListView) convertView.findViewById(R.id.listView1);
                    nameCapture = (EditText) convertView.findViewById(R.id.name);
                    nameCapture.setHint("Digita l'artista che vuoi ricercare");
                    emptyText = (TextView) convertView.findViewById(android.R.id.empty);
                    myList.setEmptyView(emptyText);
                    myList.setAdapter(adapterArtists);

                    nameCapture.addTextChangedListener(new TextWatcher() {

                        // As the user types in the search field, the list is
                        @Override
                        public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                            AlterAdapterArtists();
                        }

                        // Not used for this program
                        @Override
                        public void afterTextChanged(Editable arg0) {

                        }

                        // Not uses for this program
                        @Override
                        public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                            // TODO Auto-generated method stub

                        }
                    });


                    myList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            selectedArtist = artistlist.get(position).getEmail();
                            picArtista = artistlist.get(position).getPhoto();
                            dialog.dismiss();
                            getArtistInfo();

                        }
                    });


                    dialog = alertDialog.show();

                } else {
                    Toast.makeText(getApplicationContext(), "No greetings were returned by the API.", Toast.LENGTH_LONG).show();
                }
            }
        };
        if(checkNetwork()) getArtists.execute();
    }

    public void getArtistInfo(){
        if(checkNetwork()) new DownloadArtistForGallery(getApplicationContext(),selectedArtist,this).execute();
    }

    @Override
    public void done(String email, String nc, String pic, String nick, String bio, String sito) {
        Intent intent = new Intent(MainActivity.this, ArtistProfile.class);
        intent.putExtra("nomecognome",nc);
        intent.putExtra("bio",bio);
        intent.putExtra("sito",sito);
        intent.putExtra("nickname",nick);
        intent.putExtra("email",email);
        intent.putExtra("pic", pic);

        startActivity(intent);
    }

    //metodo di ritorno dalle funzioni di libreria dei filtri
    public void done(int x){
        isFiltri = true;
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        if(x==0){ //callback from DownloadArtworksByTechnique
            urlPhoto = db.getArtworksFromTechinique(tecnicaScelta);
            getSupportActionBar().setTitle(tecnicaScelta);

        }else if(x==1){ //callback from DownloadArtworksByPlace
            urlPhoto = db.getArtworksFromPlace(selectedCity);
            getSupportActionBar().setTitle(selectedCity);

        }
        gridview.setSelection(gridview.getFirstVisiblePosition());
        gridview.setAdapter(new ImageAdapter(this));

    }


    private void shareTwitter() {
        try {
            Uri uri = Uri.parse("android.resource://com.arteverywhere.francesco.art/drawable/logo");
            Intent tweetIntent = new Intent(Intent.ACTION_SEND);
            tweetIntent.putExtra(Intent.EXTRA_TEXT, "Scarica Art Everywhere e scopri i nuovi artisti emergenti! L'app è completamente gratuita! http://bit.ly/AEDownload");
            tweetIntent.putExtra(Intent.EXTRA_STREAM, uri);
            tweetIntent.setType("image/jpeg");
            PackageManager pm = MainActivity.this.getPackageManager();
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
            Toast.makeText(MainActivity.this, "Devi prima installare Twitter!", Toast.LENGTH_SHORT).show();
        }
    }

    public void eseguiShareWhatsapp() {
            Uri uri = Uri.parse("android.resource://com.arteverywhere.francesco.art/drawable/logo");
         Intent shareIntent = new Intent();
            shareIntent.setPackage("com.whatsapp");
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            shareIntent.putExtra(Intent.EXTRA_TEXT, "Scarica Art Everywhere e scopri tutti gli artisti emergenti! Scarica ora l'app! " + "http://bit.ly/AEDownload");
            shareIntent.setType("image/*");
            startActivity(Intent.createChooser(shareIntent, "Share Image"));
        }


    public void shareEmail(){
        Uri uri = Uri.parse("android.resource://com.arteverywhere.francesco.art/drawable/logo");

        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("message/rfc822");
        i.putExtra(Intent.EXTRA_SUBJECT, "Scarica Art Everywhere!");
        i.putExtra(Intent.EXTRA_TEXT, "Ciao! Scarica Art Everywhere e scopri tutti i nuovi artisti emergenti! Scarica ora l'app! CLICCA IL SEGUENTE LINK: http://bit.ly/AEDownload");
        i.putExtra(Intent.EXTRA_STREAM, uri);
        try {
            startActivity(Intent.createChooser(i, "Consiglia Art Everywhere.."));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(MainActivity.this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
        }
    }

    public Uri getLocalBitmapUri(ImageView imageView) {
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

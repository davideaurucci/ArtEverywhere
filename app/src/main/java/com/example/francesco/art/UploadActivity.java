package com.example.francesco.art;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsContract;
import android.provider.MediaStore;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import cod.com.appspot.endpoints_final.testGCS.TestGCS;
import cod.com.appspot.endpoints_final.testGCS.TestGCS.Techniques.Gettechniques;
import cod.com.appspot.endpoints_final.testGCS.TestGCS.Upload.Putphoto;
import cod.com.appspot.endpoints_final.testGCS.model.MainDefaultResponseMessage;
import cod.com.appspot.endpoints_final.testGCS.model.MainTechniqueResponseCollection;
import cod.com.appspot.endpoints_final.testGCS.model.MainUploadRequestMessage;

import java.io.ByteArrayOutputStream;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;


import android.util.Base64;

import android.app.NotificationManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;

import com.google.common.io.ByteStreams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class UploadActivity extends ActionBarActivity implements AdapterView.OnItemClickListener {
    AlertDialog dialog;
    Button btn_test;

    private String artist;
    boolean isFoto = false;
    boolean isTecnica = false;

    Button btn_tecniche;
    String[] tecniche;
    String tecnicaScelta = "";
    ImageButton btn_choose;

    TextView descr;
    TextView count;

    EditText filename;
    EditText description;
    TextView tecnique;
    EditText size;
    AutoCompleteTextView place;

    String image;
    byte[] ba;

    private NotificationManager mNotifyManager;
    private Builder mBuilder;
    private final int id = 1;

    private Uri outputFileUri;
    static final int REQUEST_TAKE_PHOTO = 1;
    String mCurrentPhotoPath;
    String photoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        btn_choose = (ImageButton)findViewById(R.id.button2);
        btn_tecniche = (Button)findViewById(R.id.buttonTec);

        descr = (TextView)findViewById(R.id.textView5);
        count = (TextView)findViewById(R.id.textView6);
        tecnique = (TextView)findViewById(R.id.textView7);

        filename = (EditText)findViewById(R.id.editText);
        description = (EditText)findViewById(R.id.editText2);
        size = (EditText)findViewById(R.id.editText4);

        Bundle extras = getIntent().getExtras();
        artist = extras.getString("email");

        System.out.println("UPLOAD artista: " + artist);


        getTecniche();

        /* Aggiorno il contatore dei caratteri rimanenti in descrizione */
        description.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int aft){
            }

            @Override
            public void afterTextChanged(Editable s) {
                // this will show characters remaining
                count.setText("(" + (s.toString().length()) + "/250)");
            }
        });

           /* Autocomplete per il campo Luogo */
        place = (AutoCompleteTextView) findViewById(R.id.editText3);
        place.setAdapter(new PlacesAutoCompleteAdapter(this, R.layout.list_item));
        place.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String str = (String) parent.getItemAtPosition(position);
                Log.d("PLACE",str);
            }
        });

        /* Scelta tecnica */
        btn_tecniche.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder alertDialog = new AlertDialog.Builder(UploadActivity.this);
                final LayoutInflater inflater = getLayoutInflater();
                final View convertView = (View) inflater.inflate(R.layout.custom, null);
                alertDialog.setView(convertView);
                alertDialog.setTitle("Tecniche");
                ListView lv = (ListView) convertView.findViewById(R.id.listView1);
                lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        //System.out.println(tecniche[position]);
                        tecnique.setText("Tecnica*: " + tecniche[position]);
                        tecnicaScelta = tecniche[position];
                        btn_tecniche.setText("CAMBIA TECNICA");
                        dialog.dismiss();
                        isTecnica = true;
                        //System.out.println("*"+description.getText().toString()+"*");
                    }
                });
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.list_item_black, tecniche);
                lv.setAdapter(adapter);
                dialog = alertDialog.show();
            }
        });

       /* Scelta artwork */
        btn_choose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                // Ensure that there's a camera activity to handle the intent
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    // Create the File where the photo should go
                    File photoFile = null;
                    try {
                        photoFile = createImageFile();
                    } catch (IOException ex) {
                        // Error occurred while creating the File
                        return;
                    }
                    // Continue only if the File was successfully created
                    if (photoFile != null) {
                        outputFileUri = Uri.fromFile(photoFile);
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
                        // This intent presents applications that allow the user to choose a picture
                        Intent pickIntent = new Intent();
                        pickIntent.setType("image/*");
                        pickIntent.setAction(Intent.ACTION_PICK);
                        // This intent prompts the user to choose from a list of possible intents.
                        String pickTitle = "Scegli o scatta una foto";
                        Intent chooserIntent = Intent.createChooser(pickIntent, pickTitle);
                        chooserIntent.putExtra(
                                Intent.EXTRA_INITIAL_INTENTS,
                                new Intent[] { takePictureIntent });
                        startActivityForResult(chooserIntent, REQUEST_TAKE_PHOTO);
                    }
                }

            }
        });
 }


    public void onItemClick(AdapterView <?> adapterView, View view, int position, long id) {
        String str = (String) adapterView.getItemAtPosition(position);
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "IMG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        //File storageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "ArtEverywhere");
        File image = File.createTempFile(
                imageFileName, /* prefix */
                ".jpg", /* suffix */
                storageDir /* directory */
        );
        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        photoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        System.out.println("Version installed: " + Build.VERSION.SDK_INT + "| Vers KikKat: " + Build.VERSION_CODES.KITKAT);
        System.out.println("ResCode " + resultCode + " - Res OK " + RESULT_OK);
        System.out.println("ReqCode " + requestCode + " - REQ TAKE PHOTO " + REQUEST_TAKE_PHOTO);
        if(data != null){
            System.out.println("NON NULLO");
            System.out.println("**"+data.getData());
            System.out.println("**"+data.getScheme());
        }

        if (resultCode == RESULT_OK){
            if(requestCode == REQUEST_TAKE_PHOTO){
                final boolean isCamera;
                if (data == null) {
                    isCamera = true;
                } else {
                    final String action = data.getAction();
                    if (action == null) {
                        isCamera = false;
                    } else {
                        isCamera = action.equals(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    }
                }
                Uri selectedImageUri;
                if (isCamera) {
                    try {
                        selectedImageUri = outputFileUri;
                        String path = getPath(this, selectedImageUri);
                        FileInputStream in = new FileInputStream(path);
                        final byte[] array = ByteStreams.toByteArray(in);
                        Bitmap bmp = BitmapFactory.decodeByteArray(array, 0, array.length);

                        //final Bitmap bitmapOrg = BitmapFactory.decodeStream(imageStream);
                        ByteArrayOutputStream bao = new ByteArrayOutputStream();
                        bmp.compress(Bitmap.CompressFormat.JPEG, 100, bao);
                        ba = bao.toByteArray();
                        //image=Base64.encodeToString(ba,Base64.DEFAULT);

                        isFoto = true;
                        Bitmap thumb = Bitmap.createScaledBitmap(bmp,300,250,false);
                        btn_choose.setImageBitmap(thumb);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    selectedImageUri = data == null ? null : data.getData();
                    try {
                        System.out.println("*" + selectedImageUri.toString());
                        final InputStream imageStream = getContentResolver().openInputStream(selectedImageUri);
                        final Bitmap bmp = BitmapFactory.decodeStream(imageStream);
                        //se ho selezionato la foto dalla galleria devo cancellare la foto "vuota" che nel frattempo ho creato nell'sd
                        File f = new File(photoPath);
                        boolean b = f.delete();
                        System.out.println(photoPath);
                        System.out.println(">" + b);

                        ByteArrayOutputStream bao = new ByteArrayOutputStream();
                        bmp.compress(Bitmap.CompressFormat.JPEG, 100, bao);
                        ba = bao.toByteArray();
                        //image=Base64.encodeToString(ba,Base64.DEFAULT);

                        isFoto = true;
                        Bitmap thumb = Bitmap.createScaledBitmap(bmp,300,250,false);
                        btn_choose.setImageBitmap(thumb);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }
        }
    }

    private boolean ValidoCampo(String s) {
        if(s.length()>0) return true;
        return false;
    }

    private void getTecniche(){
        AsyncTask<Void, Void, MainTechniqueResponseCollection> getTec = new AsyncTask<Void, Void, MainTechniqueResponseCollection>() {

            @Override
            protected MainTechniqueResponseCollection doInBackground(Void... unused) {
                // Retrieve service handle.
                TestGCS apiServiceHandle = AppConstants.getApiServiceHandle(null);

                try {
                    MainTechniqueResponseCollection greeting = new MainTechniqueResponseCollection();
                    Gettechniques get = apiServiceHandle.techniques().gettechniques();
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
                    System.out.println("SONO QUI");
                    System.out.println("tecniche: " + greeting.size());
                    System.out.println("tecniche: " + greeting.getTechniques().size());
                    tecniche = new String[greeting.getTechniques().size()];
                    for(int i = 0; i < greeting.getTechniques().size(); i++){
                        tecniche[i] = greeting.getTechniques().get(i).getTechnique();
                        //Log.d("TECNICA",greeting.getTechniques().get(i).getTechnique());
                    }
                    //Toast.makeText(getApplicationContext(), "Upload successfull!", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), "No greetings were returned by the API.", Toast.LENGTH_LONG).show();
                }
            }
        };
        getTec.execute();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_upload, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        // POST ARTWORK
        if (id == R.id.upload_artwork){
            if(!isFoto){
                Toast.makeText(getApplicationContext(), "Devi scegliere una foto!", Toast.LENGTH_LONG).show();
            }else if(!ValidoCampo(filename.getText().toString())){
                Toast.makeText(getApplicationContext(), "Inserisci un titolo", Toast.LENGTH_LONG).show();
            }else if(!isTecnica){
                Toast.makeText(getApplicationContext(), "Devi scegliere una tecnica!", Toast.LENGTH_LONG).show();
            }else{
                AsyncTask<Void, Void, MainUploadRequestMessage> upPhoto = new AsyncTask<Void, Void, MainUploadRequestMessage>() {

                    @Override
                    protected MainUploadRequestMessage doInBackground(Void... unused) {
                        // Retrieve service handle.
                        TestGCS apiServiceHandle = AppConstants.getApiServiceHandle(null);

                        try {
                            MainUploadRequestMessage greeting = new MainUploadRequestMessage();
                            greeting.setFilename(filename.getText().toString());
                            greeting.encodePhoto(ba);
                            //greeting.setPhoto(image);
                            greeting.setArtist(artist);
                            greeting.setTechnique(tecnicaScelta);

                            /*
                            System.out.println("Artist: " + artist);
                            System.out.println("Filename: " + !filename.getText().toString().equals("") +"/"+ filename.getText().toString());
                            System.out.println("Tec: " + isTecnica);
                            System.out.println("Des: " + !description.getText().toString().equals(""));
                            System.out.println("Size: " + !size.getText().toString().equals(""));
                            System.out.println("Place: " + !place.getText().toString().equals(""));
                            */

                            if(!description.getText().toString().equals("")) greeting.setDescr(description.getText().toString());
                            if(!size.getText().toString().equals("")) greeting.setDim(size.getText().toString());
                            if(!place.getText().toString().equals("")) greeting.setLuogo(place.getText().toString());


                            Putphoto up = apiServiceHandle.upload().putphoto(greeting);

                            MainDefaultResponseMessage m = up.execute();
                            if(m.getMessage().equals("Artist not found!")){
                                Toast.makeText(getApplicationContext(), "Upload fallito! Devi essere utente registrato!", Toast.LENGTH_LONG).show();
                                return null;
                            }else if(m.getMessage().equals("Technique not found!")){
                                Toast.makeText(getApplicationContext(), "Upload fallito! Tecnica non trovata", Toast.LENGTH_LONG).show();
                                return null;
                            }

                            //System.out.println(m.getMessage().toString());

                            return greeting;
                        } catch (IOException e) {
                            Toast.makeText(getApplicationContext(), "Exception during API call - tecniche!", Toast.LENGTH_LONG).show();
                            //Log.d("ERRORE",e.getMessage());
                        }
                        return null;
                    }


                    @Override
                    protected void onPreExecute() {
                        super.onPreExecute();
                        // Displays the progress bar for the first time.
                        mBuilder.setProgress(100, 0, false);
                        mBuilder.setProgress(0, 0, true);
                        //mNotifyManager.notify(id, mBuilder.build());
                        mNotifyManager.notify(1, mBuilder.build());
                    }

                    @Override
                    protected void onPostExecute(MainUploadRequestMessage greeting) {
                        if (greeting != null) {
                            mBuilder.setContentText("Upload complete");
                            // Removes the progress bar
                            mBuilder.setProgress(0, 0, false);
                            //mNotifyManager.notify(id, mBuilder.build());
                            mNotifyManager.notify(1, mBuilder.build());

                            Toast.makeText(getApplicationContext(), "Upload successfull!", Toast.LENGTH_LONG).show();
                        } else {
                            //Toast.makeText(getApplicationContext(), "No greetings were returned by the API.", Toast.LENGTH_LONG).show();
                        }
                    }
                };

                //setto le impostazioni per la notifica dell'upload progress
                mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                mBuilder = new NotificationCompat.Builder(UploadActivity.this);
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT){
                    mBuilder.setContentTitle("Art Everywhere")
                            .setContentText("Artwork upload in progress")
                            .setSmallIcon(R.drawable.ic_launcher);

                }else{
                    mBuilder.setContentTitle("Art Everywhere")
                            .setContentText("Artwork upload in progress")
                            .setSmallIcon(R.drawable.ic_notifica_lollipop);
                }


                System.out.println("avvio upload");
                upPhoto.execute();

                Intent i = new Intent(UploadActivity.this, MainActivity.class);
                startActivity(i);
                this.finish();
            }
            return true;
        }


        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("NewApi")
    public static String getPath(final Context context, final Uri uri) {
        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            Log.d("PATH","eccomi");
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                Log.d("PATH","eccomi 2");
                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }


            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
                Log.d("PATH","eccomi 3");
                return getDataColumn(context, contentUri, null, null);

            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                    Log.d("PATH","eccomi 4");
                }
                final String selection = "_id=?";
                final String[] selectionArgs = new String[] {
                        split[1]
                };
                Log.d("PATH","eccomi 4+");
                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            // Return the remote address
            if (isGooglePhotosUri(uri)) {
                Log.d("PATH", "eccomi 5");
                return uri.getLastPathSegment();
            }
            Log.d("PATH","eccomi 6");
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            Log.d("PATH","eccomi 7");
            return uri.getPath();
        }
        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri The Uri to query.
     * @param selection (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }
    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }
    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }
    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }
    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }
}

package com.example.francesco.art;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import cod.com.appspot.endpoints_final.testGCS.TestGCS;
import cod.com.appspot.endpoints_final.testGCS.TestGCS.Upload.Putphoto;
import cod.com.appspot.endpoints_final.testGCS.model.MainUploadRequestMessage;

import java.io.ByteArrayOutputStream;

import java.io.File;
import java.io.FileDescriptor;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class UploadActivity extends ActionBarActivity implements AdapterView.OnItemClickListener {

    Button btn_test;

    Button btn_upload;
    ImageButton btn_choose;

    TextView descr;
    TextView count;

    EditText filename;
    EditText description;
    EditText tecnique;
    EditText size;
    EditText place;

    String image;
    byte[] ba;

    private NotificationManager mNotifyManager;
    private Builder mBuilder;
    private final int id = 1;

    private final int SELECT_PHOTO = 1;

    private Uri fileUri;

    private static Uri getOutputMediaFileUri(){
        return Uri.fromFile(getOutputMediaFile());
    }

    /** Create a File for saving an image or video */
    private static File getOutputMediaFile(){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "MyCameraApp");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                "IMG_"+ timeStamp + ".jpg");

        return mediaFile;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        btn_choose = (ImageButton)findViewById(R.id.button2);

        btn_test = (Button)findViewById(R.id.button3);

        descr = (TextView)findViewById(R.id.textView5);
        count = (TextView)findViewById(R.id.textView6);

        filename = (EditText)findViewById(R.id.editText);
        description = (EditText)findViewById(R.id.editText2);
        tecnique = (EditText)findViewById(R.id.editText5);
        size = (EditText)findViewById(R.id.editText4);

        //Aggiorno il contatore dei caratteri rimanenti in descrizione
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

        AutoCompleteTextView place = (AutoCompleteTextView) findViewById(R.id.editText3);
        place.setAdapter(new PlacesAutoCompleteAdapter(this, R.layout.list_item));
        place.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String str = (String) parent.getItemAtPosition(position);
                Log.d("PLACE",str);
            }
        });




        btn_choose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");



                Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                fileUri = getOutputMediaFileUri(); // create a file to save the image

                String pickTitle = "Select or take a new Picture"; // Or get from strings.xml
                Intent chooserIntent = Intent.createChooser(photoPickerIntent, pickTitle);
                chooserIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
                chooserIntent.putExtra
                        (Intent.EXTRA_INITIAL_INTENTS,new Intent[] { takePhotoIntent });
                startActivityForResult(chooserIntent, SELECT_PHOTO);

            }
        });
 }


    public void onItemClick(AdapterView <?> adapterView, View view, int position, long id) {
        String str = (String) adapterView.getItemAtPosition(position);
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
    }

    private boolean ValidoCampo(String s) {
        if(s.length()>0){
            return true;
        }
        return false;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        switch(requestCode) {
            case SELECT_PHOTO:
                if(resultCode == RESULT_OK){

                  try {
                        final Uri imageUri = imageReturnedIntent.getData();
                        final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                        final Bitmap bitmapOrg = BitmapFactory.decodeStream(imageStream);
                        ByteArrayOutputStream bao = new ByteArrayOutputStream();
                        bitmapOrg.compress(Bitmap.CompressFormat.JPEG, 100, bao);
                        ba = bao.toByteArray();
                        image=Base64.encodeToString(ba,Base64.DEFAULT);
                        BitmapDrawable bd = new BitmapDrawable(getResources(), bitmapOrg);
                        btn_choose.setBackground(bd);

                    } catch (FileNotFoundException e) {
                      e.printStackTrace();
                  }

                }else{
                    Toast.makeText(getApplicationContext(), "Error during choosing artwork!", Toast.LENGTH_LONG).show();
                }
        }
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
            if(ValidoCampo(filename.getText().toString())) {

                AsyncTask<Void, Void, MainUploadRequestMessage> uploadPhoto = new AsyncTask<Void, Void, MainUploadRequestMessage>() {

                    @Override
                    protected MainUploadRequestMessage doInBackground(Void... unused) {
                        // Retrieve service handle.
                        TestGCS apiServiceHandle = AppConstants.getApiServiceHandle(null);

                        try {
                            MainUploadRequestMessage greeting = new MainUploadRequestMessage();
                            greeting.setFilename(filename.getText().toString());
                            greeting.encodePhoto(ba);
                            Putphoto put = apiServiceHandle.upload().putphoto(greeting);
                            put.execute();
                            return greeting;
                        } catch (IOException e) {
                            Toast.makeText(getApplicationContext(), "Exception during API call!", Toast.LENGTH_LONG).show();
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
                            Toast.makeText(getApplicationContext(), "No greetings were returned by the API.", Toast.LENGTH_LONG).show();
                        }
                    }
                };

            //setto le impostazioni per la notifica dell'upload progress
            mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mBuilder = new NotificationCompat.Builder(UploadActivity.this);
            mBuilder.setContentTitle("Art Everywhere")
                    .setContentText("Artwork upload in progress")
                    .setSmallIcon(R.drawable.ic_launcher);


            //eseguo l'upload della foto
            uploadPhoto.execute((Void)null);


            }else{
                Toast.makeText(getApplicationContext(), "Enter a valid artwork's title!", Toast.LENGTH_LONG).show();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

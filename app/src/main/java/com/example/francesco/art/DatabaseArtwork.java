package com.example.francesco.art;

/**
 * Created by Francesco on 11/02/2015.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import cod.com.appspot.endpoints_final.testGCS.TestGCS;
import cod.com.appspot.endpoints_final.testGCS.model.MainDownloadResponseCollection;

public class DatabaseArtwork extends SQLiteOpenHelper {
    // Database Version
    private static final int DATABASE_VERSION = 1;
    // Database Name
    private static final String DATABASE_NAME = "ArtEverywhereDB";
    //Table Name
    private static final String TABLE_ARTWORKS = "artworks";

    public DatabaseArtwork(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // SQL statement to create book table
            Log.d("DB","creo tabella");

            //String DELETE_ARTWORK_TABLE = "DELETE * from artworks";
            //String DELETE_ARTWORK_TABLE = "DROP TABLE IF EXISTS artworks";

            String CREATE_ARTWORK_TABLE = "CREATE TABLE IF NOT EXISTS artworks ( " +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "filename TEXT, "+
                "photo TEXT, " +
                "artista TEXT, "+
                "descrizione TEXT, "+
                "dimensioni TEXT, "+
                "luogo TEXT, "+
                "tecnica TEXT, "+
                "likes INTEGER, "+
                "data TEXT "+
                ")";


        //prima di creare una tabella, cancello quella precedente
       // db.execSQL(DELETE_ARTWORK_TABLE);
        //db.delete("artworks",null,null );
        db.execSQL(CREATE_ARTWORK_TABLE);

    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d("DB","upgrade");
        // Drop older books table if existed
        db.execSQL("DROP TABLE IF EXISTS artworks");

        // create fresh books table
        this.onCreate(db);
    }

    public void clear(SQLiteDatabase db){
        db.execSQL("DELETE FROM artworks");
    }

    protected void insert(Artwork aw, SQLiteDatabase db){
        try {
            SQLiteStatement insStmt = db.compileStatement(
                    "INSERT INTO artworks ('filename','photo','artista','descrizione','dimensioni','luogo','tecnica','likes','data') VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);");
            db.beginTransaction();

            insStmt.bindString(1, "" + aw.getFilename());
            insStmt.bindString(2, "" + aw.getPhoto());
            insStmt.bindString(3, "" + aw.getArtista());
            insStmt.bindString(4, "" + aw.getDescrizione());
            insStmt.bindString(5, "" + aw.getDimensioni());
            insStmt.bindString(6, "" + aw.getLuogo());
            insStmt.bindString(7, "" + aw.getTecnique());
            insStmt.bindLong(8, aw.getLikes());
            insStmt.bindString(9, "" + aw.getData());

            insStmt.executeInsert();

        }catch(Exception e){
            e.printStackTrace();
        } finally {
            db.setTransactionSuccessful();
            db.endTransaction();
        }

        /*
        Log.d("insertArtwork", aw.toString());
        // 1. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();

        // 2. create ContentValues to add key "column"/value
        ContentValues values = new ContentValues();
        values.put("filename", aw.getFilename());
        values.put("photo", aw.getPhoto());
        values.put("artista", aw.getArtista());
        values.put("descrizione", aw.getDescrizione());
        values.put("dimensioni", aw.getDimensioni());
        values.put("luogo", aw.getLuogo());
        values.put("tecnica", aw.getTecnique());
        values.put("likes", aw.getLikes());
        values.put("data", aw.getData());

        // 3. insert
        db.insert(TABLE_ARTWORKS, // table
                null, //nullColumnHack
                values); // key/value -> keys = column names/ values = column values



        // 4. close
        db.close();
        */
    }

    // Get All Books
    public List<Artwork> getAllArtworks() {
        List<Artwork> artworks = new LinkedList<Artwork>();

        // 1. build the query
        String query = "SELECT * FROM " + TABLE_ARTWORKS;

        // 2. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        // 3. go over each row, build book and add it to list
        Artwork artwork = null;
        if (cursor.moveToFirst()) {
            do {
                artwork = new Artwork();
                artwork.setId(Integer.parseInt(cursor.getString(0)));
                artwork.setFilename((cursor.getString(1)));
                artwork.setPhoto(cursor.getString(2));
                artwork.setArtista(cursor.getString(3));
                artwork.setDescrizione(cursor.getString(4));
                artwork.setDimensioni(cursor.getString(5));
                artwork.setLuogo(cursor.getString(6));
                artwork.setTecnique(cursor.getString(7));
                artwork.setLikes(Integer.parseInt(cursor.getString(8)));
                artwork.setDimensioni(cursor.getString(9));

                // Add book to books
                artworks.add(artwork);
            } while (cursor.moveToNext());
        }

        Log.d("getAllBooks()", artworks.toString());

        // return books
        return artworks;
    }

    public String[] getPhotos() {
        //List<Artwork> artworks = new LinkedList<Artwork>();
        String[] photos = new String[AppConstants.numFoto];


        // 1. build the query
        String query = "SELECT photo FROM " + TABLE_ARTWORKS;
        //String query = "SELECT * FROM " + TABLE_ARTWORKS;

        // 2. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        // 3. go over each row, build book and add it to list
        //Artwork artwork = null;
        String ph;
        int i = 0;
        if (cursor.moveToFirst()) {
            do {
                ph = cursor.getString(0);

                // Add photo to photos
                photos[i] = ph;
                if(i!=(AppConstants.numFoto-1)) i++;
            } while (cursor.moveToNext());
        }

        //Log.d("get All Photos()", "lunghezza"+photos.length);

        // return books
        return photos;
    }

    public Artwork getArtworkFromUrl(String url) {

        // 1. build the query
        //String query = "SELECT * FROM " + TABLE_ARTWORKS + "WHERE photo="+ url;
        String query = "SELECT * FROM " + TABLE_ARTWORKS + " WHERE photo=? ";

        // 2. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();
        //Cursor cursor = db.rawQuery(query, null);
        Cursor cursor = db.rawQuery(query, new String[] { url });

        // 3. go over each row, build book and add it to list
        Artwork artwork = null;
        if (cursor.moveToFirst()) {
            do {
                artwork = new Artwork();
                artwork.setId(Integer.parseInt(cursor.getString(0)));
                artwork.setFilename((cursor.getString(1)));
                artwork.setPhoto(cursor.getString(2));
                artwork.setArtista(cursor.getString(3));
                artwork.setDescrizione(cursor.getString(4));
                artwork.setDimensioni(cursor.getString(5));
                artwork.setLuogo(cursor.getString(6));
                artwork.setTecnique(cursor.getString(7));
                artwork.setLikes(Integer.parseInt(cursor.getString(8)));
                artwork.setData(cursor.getString(9));

                // Add book to books
            } while (cursor.moveToNext());
        }

        Log.d("get artwork", artwork.toString());

        // return artwork
        return artwork;
    }



}

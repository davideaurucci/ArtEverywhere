package com.arteverywhere.francesco.art;

/**
 * Created by Francesco on 15/03/2015.
 */
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
public class CustomListArtists extends ArrayAdapter<Artist>{
    private final Activity context;
    private final ArrayList<Artist> artist;
    public CustomListArtists(Activity context, ArrayList<Artist> artist) {
        super(context, R.layout.list_single, artist);
        this.context = context;

        this.artist = artist;
    }
    @Override
    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView= inflater.inflate(R.layout.list_single, null, true);
        TextView txtTitle = (TextView) rowView.findViewById(R.id.txt);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.img);
        txtTitle.setText(artist.get(position).getName());


        Picasso.with(getContext()).load(artist.get(position).getPhoto()).transform(new CircleTransform()).into(imageView);

        return rowView;
    }
}
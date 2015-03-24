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

public class CustomListCommenti extends ArrayAdapter<String>{
    private final Activity context;
    private final String[] testo;
    private final String[] icone;
    private final String[] date;
    private final String[] autore;


    public CustomListCommenti(Activity context, String[] web, String[] imageId, String[] date, String[] autore) {
        super(context, R.layout.list_single_commenti, web);
        this.context = context;
        this.testo = web;
        this.icone = imageId;
        this.date = date;
        this.autore = autore;
    }
    @Override
    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView= inflater.inflate(R.layout.list_single_commenti, null, true);
        TextView commento = (TextView) rowView.findViewById(R.id.txt1);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.img);
        TextView data = (TextView) rowView.findViewById(R.id.txt2);
        TextView aut = (TextView) rowView.findViewById(R.id.txt0);

        commento.setText(testo[position]);
        data.setText(date[position]);
        aut.setText(autore[position]);

        Picasso.with(getContext()).load(icone[position]).transform(new CircleTransform()).into(imageView);

        return rowView;
    }
}
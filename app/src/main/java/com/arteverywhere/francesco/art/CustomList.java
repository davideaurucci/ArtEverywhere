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

public class CustomList extends ArrayAdapter<String>{
    private final Activity context;
    private final String[] web;
    private final String[] imageId;
    public CustomList(Activity context, String[] web, String[] imageId) {
        super(context, R.layout.list_single, web);
        this.context = context;
        this.web = web;
        this.imageId = imageId;
    }
    @Override
    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView= inflater.inflate(R.layout.list_single, null, true);
        TextView txtTitle = (TextView) rowView.findViewById(R.id.txt);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.img);
        txtTitle.setText(web[position]);

        Picasso.with(getContext()).load(imageId[position]).transform(new CircleTransform()).into(imageView);

        return rowView;
    }
}
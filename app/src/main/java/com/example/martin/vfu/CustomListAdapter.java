package com.example.martin.vfu;

/**
 * Created by Martin on 2016-06-04.
 */
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class CustomListAdapter extends ArrayAdapter<String> {

    private final Activity context;
    private final String[] itemname;
    private final int[] chColors;

    public CustomListAdapter(Activity context, String[] itemname, int[] chColors) {
        super(context, R.layout.mylist, itemname);
        // TODO Auto-generated constructor stub

        this.context = context;
        this.itemname = itemname;
        this.chColors = chColors;
    }

    public View getView(int position,View view,ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.mylist, null,true);
        rowView.setBackgroundColor(chColors[position]);

        TextView txtTitle = (TextView) rowView.findViewById(R.id.item);

        txtTitle.setText(itemname[position]);
        return rowView;

    };
}
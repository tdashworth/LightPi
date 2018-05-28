package uk.co.tdashworth.lightpi;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Thomas on 27/03/2016.
 */
public class FavouriteAdapter extends BaseAdapter {
    private Context mContext;

    public FavouriteAdapter(Context c) {
        mContext = c;
    }

    public int getCount() {
        return texts.size();
    }

    public String getItem(int position) {
        return texts.get(position);
    }

    public long getItemId(int position) {
        return 0;
    }

    // create a new ImageView for each item referenced by the Adapter
    public TextView getView(int position, View convertView, ViewGroup parent) {
        TextView textView;
        if (convertView == null) {
            // if it's not recycled, initialize some attributes
            textView = new TextView(mContext);
            textView.setGravity(Gravity.CENTER);
            textView.setTextSize(20);
            textView.setPadding(5,15,5,15);
        } else {
            textView = (TextView) convertView;
        }

        textView.setText(texts.get(position));
        return textView;
    }

    public void addItem(String item) {
        texts.add(item);
        this.notifyDataSetChanged();
    }

    public void removeItem(String item) {
        texts.remove(item);
        this.notifyDataSetChanged();
    }

    // references to our images
    private ArrayList<String> texts = new ArrayList<>();

}
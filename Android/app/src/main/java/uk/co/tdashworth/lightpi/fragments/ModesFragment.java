package uk.co.tdashworth.lightpi.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Toast;

import com.larswerkman.holocolorpicker.ColorPicker;

import java.io.IOException;
import java.math.RoundingMode;

import uk.co.tdashworth.lightpi.Controller;
import uk.co.tdashworth.lightpi.FavouriteAdapter;
import uk.co.tdashworth.lightpi.PresetAdapter;
import uk.co.tdashworth.lightpi.R;
import uk.co.tdashworth.lightpi.Remote;

import static android.app.AlertDialog.*;


public class ModesFragment extends Fragment{

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.fragment_modes, null);

        final SeekBar presetSeek = (SeekBar) view.findViewById(R.id.presetSeek);
        GridView presetGrid = (GridView) view.findViewById(R.id.presetGrid);
        final PresetAdapter presetAdapter = new PresetAdapter(view.getContext());
        presetGrid.setAdapter(presetAdapter);
        presetAdapter.addItem("Fade");
        presetAdapter.addItem("Strobe");
        presetAdapter.addItem("Wake");
        presetAdapter.addItem("Police");
        presetAdapter.addItem("Party");
        presetAdapter.addItem("Dusk");

        presetGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                Toast.makeText(view.getContext(), "" + presetAdapter.getItem(position), Toast.LENGTH_SHORT).show();

                Controller.sendPreset(presetAdapter.getItem(position),presetSeek.getProgress());
            }
        });



        GridView favGrid = (GridView) view.findViewById(R.id.favouritesGrid);
        final FavouriteAdapter favAdapter = new FavouriteAdapter(view.getContext());
        favGrid.setAdapter(favAdapter);
        Remote.getFavourites(favAdapter);


        favGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                Remote.setFavourite(favAdapter.getItem(position));
            }
        });

        favGrid.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                Builder builder = new Builder(view.getContext());

                builder.setTitle("Delete Favorite");
                builder.setMessage("Delete " + favAdapter.getItem(position) + "?");

                builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Remote.removeFavourite(favAdapter.getItem(position));
                        dialog.dismiss();
                    }

                });

                builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();

                return true;
            }
        });


        return view;
    }

}


package uk.co.tdashworth.lightpi.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.larswerkman.holocolorpicker.ColorPicker;
import com.larswerkman.holocolorpicker.SaturationBar;
import com.larswerkman.holocolorpicker.ValueBar;

import uk.co.tdashworth.lightpi.Controller;
import uk.co.tdashworth.lightpi.R;
import uk.co.tdashworth.lightpi.Remote;


public class ManualFragment extends Fragment{

    ColorPicker mColorPicker;
    ImageButton favButton;

    View view;

    Integer lastColour = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_manual, null);

        mColorPicker = (ColorPicker) view.findViewById(R.id.picker);
        SaturationBar saturationBar = (SaturationBar) view.findViewById(R.id.saturationbar);
        ValueBar valueBar = (ValueBar) view.findViewById(R.id.valuebar);

        mColorPicker.setShowOldCenterColor(false);
        mColorPicker.addSaturationBar(saturationBar);
        mColorPicker.addValueBar(valueBar);

        //JSONArray colors = Controller.getColor();
        //mColorPicker.setColor(Color.HSVToColor(colors));

        final ImageButton setButton = (ImageButton) view.findViewById(R.id.setButton);
        setButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Get color and send
                Controller.sendColor(mColorPicker.getColor());
            }
        });

        favButton = (ImageButton) view.findViewById(R.id.favButton);
        favButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Add to favourites
                favorite(mColorPicker.getColor());
            }
        });


        mColorPicker.setOnColorSelectedListener(new ColorPicker.OnColorSelectedListener() {
            @Override
            public void onColorSelected(int color) {
                Toast.makeText(view.getContext(), "OnColorSelected", Toast.LENGTH_SHORT).show();
                favButton.setBackgroundResource(R.drawable.ic_heart_outline_black_48dp);
                int color1 = mColorPicker.getColor();

                int r = (color1 / 256 / 256 % 256);
                int g = (color1 / 256 % 256);
                int b = (color1 % 256);

                int current = r+g+b;

                //System.out.print("Current = "+current);
                //System.out.println(" RGB = "+r+" "+g+" "+b);

                if (Math.abs(lastColour - current) > 20) {
                    Controller.sendColor(mColorPicker.getColor());
                    lastColour = current;
                    System.out.println("last color " + lastColour);
                }
            }
        });

        mColorPicker.setOnColorChangedListener(new ColorPicker.OnColorChangedListener() {
            @Override
            public void onColorChanged(int color) {
                Toast.makeText(view.getContext(), "OnColorChanged", Toast.LENGTH_SHORT).show();
                favButton.setBackgroundResource(R.drawable.ic_heart_outline_black_48dp);
                int color1 = mColorPicker.getColor();

                int r = (color1 / 256 / 256 % 256);
                int g = (color1 / 256 % 256);
                int b = (color1 % 256);

                int current = r+g+b;

                //System.out.print("Current = "+current);
                //System.out.println(" RGB = "+r+" "+g+" "+b);

                if (Math.abs(lastColour - current) > 20) {
                    Controller.sendColor(mColorPicker.getColor());
                    lastColour = current;
                    System.out.println("last color " + lastColour);
                }
            }
        });

        return view;
    }

    private void favorite(final int color) {
        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
        builder.setTitle("Add Favourite");

        // Set up the input
        final EditText input = new EditText(view.getContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setHint("Name");
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                favButton.setBackgroundResource(R.drawable.ic_heart_black_48dp);
                Remote.addFavourite(input.getText().toString(), color);
                Toast.makeText(view.getContext(), input.getText().toString(), Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(view.getContext(), R.color.colorPrimary));
        dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(view.getContext(), R.color.colorPrimary));
    }
}

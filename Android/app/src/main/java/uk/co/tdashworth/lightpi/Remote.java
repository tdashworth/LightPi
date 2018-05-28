package uk.co.tdashworth.lightpi;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.widget.ImageButton;
import android.widget.Toast;

import com.larswerkman.holocolorpicker.ColorPicker;

import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Thomas on 30/03/2016.
 */
public class Remote {
    public static FavouriteAdapter favouriteAdapter;
    public static HashMap<String,Integer> favouriteList = new HashMap<>();

    private static Activity mainActivity;

    static SharedPreferences favouritesSP;
    static SharedPreferences defaultSP;

    public static void setActivity(Activity act) {
        mainActivity = act;
        favouritesSP = mainActivity.getSharedPreferences("uk.co.tdashworth.lightpi.favourites",Context.MODE_PRIVATE);
        defaultSP = mainActivity.getSharedPreferences("uk.co.tdashworth.lightpi.default",Context.MODE_PRIVATE);
    }


    public static void getFavourites(FavouriteAdapter favAdapter) {
        favouriteAdapter = favAdapter;
        for( Map.Entry entry : favouritesSP.getAll().entrySet() ) {
            favouriteList.put(entry.getKey().toString(), Integer.parseInt(entry.getValue().toString()));
            if (entry.getKey().toString() != null) {
                favouriteAdapter.addItem(entry.getKey().toString());}
        }
    }

    public static void addFavourite(String name, Integer color) {
        favouriteAdapter.addItem(name);
        favouriteList.put(name, color);
        Controller.sendColor(color);
        save();
    }

    public static void removeFavourite(String name) {
        favouriteAdapter.removeItem(name);
        favouriteList.remove(name);
        save();
    }

    public static void setFavourite(String name) {
        ((ColorPicker) mainActivity.findViewById(R.id.picker)).setColor(favouriteList.get(name));
        mainActivity.findViewById(R.id.favButton).setBackgroundResource(R.drawable.ic_heart_black_48dp);
        Controller.sendColor(favouriteList.get(name));
        Toast.makeText(mainActivity.getApplicationContext(), name + " set!", Toast.LENGTH_SHORT).show();
    }

    public static void save(){
        SharedPreferences.Editor editor = favouritesSP.edit();
        editor.clear();
        for( Map.Entry entry : favouriteList.entrySet() ) {
            editor.putInt((String) entry.getKey(), (Integer) entry.getValue());
        }
        editor.apply();
        System.out.println("Saved!");
    }

    public static String getDefault() {
        return defaultSP.getString("default","");
    }

    public static void setDefault(){
        SharedPreferences.Editor editor = defaultSP.edit();
        editor.putString("default",Controller.name);
        editor.apply();

        Toast.makeText(mainActivity.getApplicationContext(), Controller.name + " is your default controller", Toast.LENGTH_SHORT).show();
    }
}

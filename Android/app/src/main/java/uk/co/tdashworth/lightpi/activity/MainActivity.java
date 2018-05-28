package uk.co.tdashworth.lightpi.activity;

import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.menu.ActionMenuItemView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.larswerkman.holocolorpicker.ColorPicker;

import org.json.JSONArray;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

import uk.co.tdashworth.lightpi.Controller;
import uk.co.tdashworth.lightpi.R;
import uk.co.tdashworth.lightpi.Remote;
import uk.co.tdashworth.lightpi.fragments.ModesFragment;
import uk.co.tdashworth.lightpi.fragments.SunFragment;
import uk.co.tdashworth.lightpi.fragments.ManualFragment;

public class MainActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Remote.setActivity(this);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        viewPager.setCurrentItem(1);

        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        if (mWifi.isConnected()) {
            if (Controller.normal != "") {
                new Thread() {
                    @Override
                    public void run() {
                        Controller.find();

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (Controller.controllers.keySet().contains(Controller.normal) && Controller.normal.length() > 0) {
                                    Controller.set(Controller.normal, (ColorPicker) findViewById(R.id.picker));
                                    ActionMenuItemView bulbIcon = (ActionMenuItemView) findViewById(R.id.action_bulb);
                                    Toast.makeText(getApplicationContext(), "Connected to " + Controller.normal, Toast.LENGTH_SHORT).show();
                                    bulbIcon.setIcon(getDrawable(R.drawable.ic_lightbulb_white_48dp));

                                    Intent intent = getIntent();
                                    if ("com.google.android.gms.actions.SEARCH_ACTION".equals(intent.getAction())) {
                                        String query = intent.getStringExtra(SearchManager.QUERY);
                                        doMySearch(query);
                                        finish();
                                    }
                                } else {
                                    Toast.makeText(getApplicationContext(), Controller.normal + " not found", Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    }
                }.start();
            }
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("WiFi not connected!")
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                            dialog.dismiss();
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        }

    }

    private void doMySearch(final String query) {
        Toast.makeText(getApplicationContext(), query, Toast.LENGTH_SHORT).show();

        Controller.sendQueue.add(query);
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new ModesFragment(), "MODES");
        adapter.addFragment(new ManualFragment(), "MANUAL");
        adapter.addFragment(new SunFragment(), "SUN");
        viewPager.setAdapter(adapter);
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_bar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_bulb:
                // User chose the "Connect" action, select controller
                new Task().execute();
                return true;

            case R.id.action_power:
                // User chose the "Lights off" action, set value to 1
                if (Controller.ip != "") {
                    JSONArray hsvArray = new JSONArray();

                    hsvArray.put(-1);
                    hsvArray.put(-1);
                    hsvArray.put(0);

                    Controller.sendColor(-16777216);
                }
                return true;

            case R.id.action_default:
                Remote.setDefault();

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    class Task extends AsyncTask<Void, String,  Boolean> {
        View v= getLayoutInflater().inflate(R.layout.dialog_list, null);
        ColorPicker picker = (ColorPicker) v.findViewById(R.id.picker);
        TextView failureText = (TextView) v.findViewById(R.id.controllerFailureText);
        ListView listView = (ListView) v.findViewById(R.id.controllerList);
        ArrayList<String> names = new ArrayList<>();
        ArrayAdapter<String> listAdapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, names);
        ProgressBar progressBar = (ProgressBar) v.findViewById(R.id.controllerProgress);
        ActionMenuItemView bulbIcon = (ActionMenuItemView) findViewById(R.id.action_bulb);
        AlertDialog.Builder alertDialog;
        AlertDialog ad;

        @Override
        protected void onPreExecute() {
            bulbIcon.setIcon(getDrawable(R.drawable.ic_lightbulb_outline_white_48dp));
            Controller.ip = "";
            Controller.name = "";
            Controller.sendQueue.clear();

            alertDialog = new AlertDialog.Builder(MainActivity.this);
            alertDialog.setView(v);
            alertDialog.setTitle("Controllers");
            listView.setAdapter(listAdapter);
            progressBar.getIndeterminateDrawable().setColorFilter(0xFF8224E3, android.graphics.PorterDuff.Mode.MULTIPLY);
            ad = alertDialog.show();

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String option = listView.getItemAtPosition(position).toString();
                    System.out.println(option + " SELECTED");
                    Toast.makeText(getApplicationContext(), "Connected to " + option, Toast.LENGTH_LONG).show();
                    ad.dismiss();

                    Controller.set(option, picker);

                    bulbIcon.setIcon(getDrawable(R.drawable.ic_lightbulb_white_48dp));

                }
            });
        }

        @Override
        protected Boolean doInBackground(Void... v) {
            try {
                DatagramSocket broadcastSocket = new DatagramSocket();
                broadcastSocket.setBroadcast(true);

                broadcastSocket.send(new DatagramPacket("REQUEST".getBytes(), "REQUEST".getBytes().length, InetAddress.getByName("255.255.255.255"), 5625));

                broadcastSocket.setSoTimeout(4000);

                Controller.controllers.clear();

                try {
                    while (true) {
                        DatagramPacket receivePacket = new DatagramPacket(new byte[15000], new byte[15000].length);
                        broadcastSocket.receive(receivePacket);

                        String name = new String(receivePacket.getData()).trim();
                        String ip = receivePacket.getAddress().toString().substring(1);

                        Controller.controllers.put(name, ip);
                        publishProgress(name);
                        System.out.println(name + " (" + ip + ")");
                    }
                } catch (SocketException e) {}

            } catch (Exception e) {
                e.printStackTrace();
            }

            if (Controller.controllers.isEmpty()){
                Controller.controllers.put("No controllers found.","");
                System.out.println("None");
            }

            return true;
        }

        @Override
        protected void onProgressUpdate(String... name) {
            System.out.println(name[0]);
            names.add(name[0]);
            listAdapter.notifyDataSetChanged();
        }

        @Override
        protected void onPostExecute(Boolean b) {
            progressBar.setVisibility(View.GONE);

            if (listAdapter.isEmpty()) {failureText.setVisibility(View.VISIBLE);}
        }
    }
}


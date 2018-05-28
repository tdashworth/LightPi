package uk.co.tdashworth.lightpi;



import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;

import com.larswerkman.holocolorpicker.ColorPicker;

import org.json.JSONArray;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Queue;

import org.json.simple.parser.JSONParser;

/**
 * Created by Thomas on 25/03/2016.
 */
public class Controller {

    public static String ip = "";
    public static String name = "";

    public static String normal = Remote.getDefault();
    public static HashMap<String,String> controllers = new HashMap<>();

    public static Queue<String> sendQueue = new PriorityQueue<String>();

    static JSONParser parser = new JSONParser();

    public static void set(String controllerName, final ColorPicker picker) {
        name = controllerName;
        ip = controllers.get(name);

        queueHandler();

        new Runnable() {
            @Override
            public void run() {
                try {
                    DatagramSocket UDPSocket = new DatagramSocket();

                    UDPSocket.send(new DatagramPacket("COL0UR".getBytes(), "COLOUR".getBytes().length, InetAddress.getByName(ip), 5625));

                    DatagramPacket receivePacket = new DatagramPacket(new byte[15000], new byte[15000].length);
                    UDPSocket.receive(receivePacket);

                    String color = new String(receivePacket.getData()).trim();
                    System.out.println("Current colour: "+color);


                    //runOnUiThread(new Runnable() {
                    //    @Override
                    //    public void run() {
                    //        picker.setColor(-1);
                    //    }
                    //});


                } catch (Exception e) {e.printStackTrace();}
            }
        };

    }

    public static void sendColor(Integer color){
        final String data = color.toString();

        sendQueue.add(data);

        //System.out.println(data);
    }

    public static void queueHandler(){
        new Thread() {
            @Override
            public void run() {
                while (true)
                {
                    final String data = sendQueue.poll();

                    if (data != null){
                        System.out.println("Sending: "+data);
                        try {
                            DatagramSocket UDPSocket = new DatagramSocket();
                            UDPSocket.send(new DatagramPacket(data.getBytes(), data.getBytes().length, InetAddress.getByName(ip), 5625));

                        } catch (Exception e) {e.printStackTrace();}

                        //try {
                            //Thread.sleep(1000);
                        //} catch (InterruptedException e) {
                        //    e.printStackTrace();
                        //}
                    }
                }
            }
        }.start();
    }

    public static void sendPreset(String name, Integer speed){
        final String data = name + speed;

        sendQueue.add(data);
    }

    public static void find() {
        try {
            DatagramSocket broadcastSocket = new DatagramSocket();
            broadcastSocket.setBroadcast(true);

            broadcastSocket.send(new DatagramPacket("REQUEST".getBytes(), "REQUEST".getBytes().length, InetAddress.getByName("255.255.255.255"), 5625));

            broadcastSocket.setSoTimeout(500);

            controllers.clear();

            try {
                while (true) {
                    DatagramPacket receivePacket = new DatagramPacket(new byte[15000], new byte[15000].length);
                    broadcastSocket.receive(receivePacket);

                    String name = new String(receivePacket.getData()).trim();
                    String ip = receivePacket.getAddress().toString().substring(1);

                    controllers.put(name, ip);
                    System.out.println(name + " (" + ip + ")");
                }
            } catch (SocketException e) {}

        } catch (Exception e) {
            e.printStackTrace();
        }

        if (controllers.isEmpty()){
            controllers.put("No controllers found.","");
            System.out.println("None");
        }
    }

}


//new Thread() {
//@Override
//public void run() {}
// }.start();

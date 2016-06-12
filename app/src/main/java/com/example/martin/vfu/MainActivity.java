package com.example.martin.vfu;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;


public class MainActivity extends Activity {

    private String[] defaultColors = new String[]{"Red", "Green", "Blue", "Yellow",
            "Magenta", "Cyan", "Brown", "Orange"};

    private String[] channelStrings = new String[]{"Channel1", "Channel2", "Channel3", "Channel4",
            "Channel5", "Channel6", "Channel7", "Channel8"};

    private String device;
    private String mode;

    public GUI gui;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //create the graphical user interface
        gui = new GUI(getApplicationContext());

        LinearLayout layout = (LinearLayout) findViewById(R.id.main);
        layout.setBackgroundResource(R.drawable.custom_background);

        Button setup = gui.largeBtn("Setup");
        setup.setBackgroundResource(R.drawable.custom_main_button);
        layout.addView(setup);

        Button display = gui.largeBtn("Display");
        display.setBackgroundResource(R.drawable.custom_main_button);
        layout.addView(display);

        Button graph = gui.largeBtn("Graph");
        graph.setBackgroundResource(R.drawable.custom_main_button);
        layout.addView(graph);

        Button bluetooth = gui.largeBtn("Connect to Bluetooth");
        bluetooth.setBackgroundResource(R.drawable.custom_main_button);
        layout.addView(bluetooth);

        //Channelsettings, set default values and try to read txt file, if txt file doesn't exist
        //create a file and write default settings
        for(int c = 0; c < channelStrings.length; c++) {
            loadSettings(c, channelStrings[c]);
        }

        //Generalsettings, set default values and try to read txt file, if txt file doesn't exist
        //create a file and write default settings
        loadSettings(0, "General Info");
        loadSettings(0, "Device");

        //create intents to run when a certain event occurrs
        final Intent setupActivity = new Intent(this, Setup.class);
        final Intent displayActivity = new Intent(this, Display.class);
        final Intent graphActivity = new Intent(this, Graph.class);
        final Intent blueBrowserActivity = new Intent(this, BluetoothBrowser.class);

        //set button on click listeners
        bluetooth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mode.equals("Slave")) {
                    Toast enableBT = Toast.makeText(getApplicationContext(),
                            "Choose Master as mode to connect to Bluetooth!", Toast.LENGTH_SHORT);
                    enableBT.show();
                } else {
                    startActivityForResult(blueBrowserActivity, 2);
                }
            }
        });

        setup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(setupActivity);
            }
        });

        display.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(displayActivity);
            }
        });

        graph.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(graphActivity);
            }
        });
    }

    public void loadSettings(int i, String filename){
        FileInputStream fis = null;
        try {
            fis = openFileInput(filename);
            DataInputStream dataIn = new DataInputStream(fis);

            byte[] buffer = new byte[fis.available()];
            dataIn.read(buffer);

            String inputFromFile = new String(buffer);

            String[] fileInfo = inputFromFile.split("\\r?\\n");

            if(filename.equals("General Info")){
                mode = fileInfo[8];
            }else if(filename.equals("Device")){
                device = fileInfo[0];
            }

            fis.close();
            dataIn.close();

        } catch (IOException e) {
            saveDefaultSettings(i, filename);
            e.printStackTrace();
        }
    }

    public void saveDefaultSettings(int i, String filename){
        FileOutputStream outputStream;

        String settings = "";
        if(filename.equals(channelStrings[i])){
            settings = channelStrings[i] + "\n" + null + "\n" + 3 + "\n" +
                    15 + "\n" + 0 + "\n" + 3 + "\n" + defaultColors[i] + "\n";
        }else if(filename.equals("General Info")){
            settings = 0 + "\n" + 1000 + "\n" + 100 + "\n" + 0 + "\n" +
                    "" + "\n" + 0 + "\n" + 1000 + "\n" + "Continuous growth" + "\n" + "Slave" + "\n";
        }else if(filename.equals("Device")){
            settings = "NULL";
        }

        try {
            outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
            outputStream.write(settings.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected synchronized void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 2){
            if(resultCode == RESULT_OK) {
                device = data.getDataString();
                Log.i("Log", "Device recieved: " + device);
                saveDeviceSettings("Device");   //save the device name of the chosen device
            }
        }
    }

    private void saveDeviceSettings(String filename){
        try {
            FileOutputStream outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
            outputStream.write(device.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

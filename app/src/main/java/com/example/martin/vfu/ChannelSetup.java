package com.example.martin.vfu;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Martin on 2015-12-08.
 */
public class ChannelSetup extends Activity {

    TextView test;

    EditText name, decimals, max, min, samples;

    EditText[] editTexts = new EditText[5];

    Spinner colorSpinner;

    Button finishBtn;

    String channelInfo = "";

    String presentChannel = "";
    String channelName = "";

    int setDecimals;
    int setMax;
    int setMin;
    int setSamples;
    String chosenColor = "";
    String graphChNames = "";

    TextView[] spinnerItems;

    String[] defaultColors = new String[]{"Red", "Green", "Blue", "Yellow",
            "Magenta", "Cyan", "Brown", "Orange"};

    String[] channelStrings = new String[]{"Channel1", "Channel2", "Channel3", "Channel4",
            "Channel5", "Channel6", "Channel7", "Channel8"};

    int[] defaultColorCodes = new int[]{R.color.Red, R.color.Green, R.color.Blue, R.color.Yellow,
            R.color.Magenta, R.color.Cyan, R.color.Brown, R.color.Orange};

    int channelID;

    int[] setValues = new int[4];

    GUI gui;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_channelsetup);

        gui = new GUI(getApplicationContext());

        Bundle bIn = getIntent().getExtras();
        channelID = bIn.getInt("channelID");
        graphChNames = bIn.getString("graphChNames");

        loadSettings(channelID, channelStrings[channelID]);

        setValues[0] = setDecimals;
        setValues[1] = setMax;
        setValues[2] = setMin;
        setValues[3] = setSamples;

        enableGUI();
    }

    public void loadSettings(int i, String filename){
        FileInputStream fis = null;
        try {
            fis = openFileInput(filename);
            DataInputStream dataIn = new DataInputStream(fis);

            byte[] buffer = new byte[fis.available()];
            dataIn.read(buffer);

            String inputFromFile = new String(buffer);

            String[] channelInfo = inputFromFile.split("\\r?\\n");

            if(channelInfo[0].equals(channelStrings[i])){
                presentChannel = channelInfo[0];
                channelName = channelInfo[1];
                setDecimals = Integer.parseInt(channelInfo[2]);
                setMax = Integer.parseInt(channelInfo[3]);
                setMin = Integer.parseInt(channelInfo[4]);
                setSamples = Integer.parseInt(channelInfo[5]);
                chosenColor = channelInfo[6];
            }else if(channelInfo[0].equals("deactivated")){
                presentChannel = null;
            }

            fis.close();
            dataIn.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void enableGUI(){

        LinearLayout chSetupLayout = (LinearLayout) findViewById(R.id.chSetupLayout);
        chSetupLayout.setBackgroundResource(R.drawable.custom_background);

        test = (TextView) findViewById(R.id.test);

        name = (EditText) findViewById(R.id.name);
        decimals = (EditText) findViewById(R.id.decimals);
        max = (EditText) findViewById(R.id.max);
        min = (EditText) findViewById(R.id.min);
        samples = (EditText) findViewById(R.id.alarmSamples);

        editTexts[0] = name;
        editTexts[1] = decimals;
        editTexts[2] = max;
        editTexts[3] = min;
        editTexts[4] = samples;

        for(int i = 0; i < defaultColors.length; i++){
            if(defaultColors[i].equals(chosenColor)){
                channelID = i;
            }
        }

        colorSpinner = (Spinner) findViewById(R.id.color);
        colorSpinner.setSelection(channelID);
        colorSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                chosenColor = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        channelInfo = presentChannel + "\n";

        if(!channelName.equals("null")){
            test.setText(presentChannel + "(" + channelName + ")");
            editTexts[0].setText(channelName);
        }else{
            test.setText(presentChannel);
            editTexts[0].setText(graphChNames);
        }

        int a = 1;
        for(int i = 0; i < setValues.length; i++){
            editTexts[a].setText(String.valueOf(setValues[i]));
            a++;
        }

        LinearLayout chSetupSeventhRow = (LinearLayout) findViewById(R.id.chSetupSeventhRow);

        finishBtn = gui.largeBtn("Finish");
        finishBtn.setBackgroundResource(R.drawable.custom_main_button);
        chSetupSeventhRow.addView(finishBtn);
        finishBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] settings = new String[6];
                for(int i = 0; i < settings.length; i++){
                    if(i == 5){
                        settings[i] = chosenColor;
                    }else {
                        settings[i] = editTexts[i].getText().toString();
                    }
                    channelInfo += settings[i] + "\n";
                }

                writeToFile(channelInfo);
                finishSetup();
            }
        });
    }

    public void finishSetup(){
        Intent intent = new Intent(getApplicationContext(), Setup.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        startActivity(intent);
    }

    private void writeToFile(String data) {
        String filename = presentChannel;

        FileOutputStream outputStream;
        try {
            outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
            outputStream.write(data.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

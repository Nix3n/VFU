package com.example.martin.vfu;

import android.app.Activity;
import android.content.Context;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Martin on 2015-12-25.
 */
public class GeneralSetup extends Activity {

    EditText phoneNbr, sampleFreq, recSamples, skipSamples, databaseIP, databasePORT, logFileSize;

    EditText[] editTexts = new EditText[7];

    Spinner logfileSpinner, modeSpinner;
    int logfileSpinnerPos, modeSpinnerPos;

    Button saveBtn;

    String setMode, setLogfileMode;

    String[] generalInfo;

    LinearLayout layout;
    TextView FIFO;
    EditText setFIFOLimit;
    int FIFOLimit;

    LinearLayout.LayoutParams textViewLp;
    LinearLayout.LayoutParams editTextLp;

    GUI gui;
    int screenWidth;
    int screenHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generalsetup);

        //Extra class to handle GUI implementation
        gui = new GUI(getApplicationContext());

        screenWidth = gui.getWidth();
        screenHeight = gui.getHeight();

        LinearLayout generalSetupLayout = (LinearLayout) findViewById(R.id.generalSetupLayout);
        generalSetupLayout.setBackgroundResource(R.drawable.custom_background);

        LinearLayout saveBtnLayout = (LinearLayout) findViewById(R.id.saveBtnLayout);

        phoneNbr = (EditText) findViewById(R.id.nbr);
        sampleFreq = (EditText) findViewById(R.id.freq);
        recSamples = (EditText) findViewById(R.id.record);
        skipSamples = (EditText) findViewById(R.id.skipsamples);
        databaseIP = (EditText) findViewById(R.id.databaseIP);
        databasePORT = (EditText) findViewById(R.id.databasePort);
        logFileSize = (EditText) findViewById(R.id.logFileSize);

        logfileSpinner = (Spinner) findViewById(R.id.logfilemode);
        modeSpinner = (Spinner) findViewById(R.id.mode);

        saveBtn = gui.largeBtn("Save Settings");
        saveBtn.setBackgroundResource(R.drawable.custom_main_button);
        saveBtnLayout.addView(saveBtn);

        editTexts[0] = phoneNbr;
        editTexts[1] = sampleFreq;
        editTexts[2] = recSamples;
        editTexts[3] = skipSamples;
        editTexts[4] = databaseIP;
        editTexts[5] = databasePORT;
        editTexts[6] = logFileSize;

        FIFOLimit = 10;

        FileInputStream fis = null;
        try{
            fis = openFileInput("General Info");
            DataInputStream dataIn = new DataInputStream(fis);

            byte[] buffer = new byte[fis.available()];
            dataIn.read(buffer);

            dataIn.close();
            fis.close();

            String strInput = new String(buffer);
            generalInfo = strInput.split("\\r?\\n");

            for(int i = 0; i < editTexts.length; i++){
                editTexts[i].setText(generalInfo[i]);
            }

            setLogfileMode = generalInfo[7];
            setMode = generalInfo[8];
            if(generalInfo.length > 9) {
                FIFOLimit = Integer.parseInt(generalInfo[9]);
            }
        }catch(IOException e){
            e.printStackTrace();
        }

        if(setLogfileMode.equals("FIFO")){
            logfileSpinnerPos = 0;
        }else{
            logfileSpinnerPos = 1;
        }

        if(setMode.equals("Slave")){
            modeSpinnerPos = 0;
        }else{
            modeSpinnerPos = 1;
        }

        textViewLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        textViewLp.gravity = Gravity.LEFT;

        editTextLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.FILL_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        editTextLp.gravity = Gravity.RIGHT;

        logfileSpinner.setSelection(logfileSpinnerPos);
        logfileSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setLogfileMode = parent.getItemAtPosition(position).toString();
                if(setLogfileMode.equals("FIFO")){
                    layout = (LinearLayout) findViewById(R.id.FIFOLayout);

                    FIFO = new TextView(getApplicationContext());
                    FIFO.setText("Set limit for FIFO: ");
                    FIFO.setTextSize(20);
                    FIFO.setTextColor(getResources().getColor(android.R.color.black));
                    FIFO.setLayoutParams(textViewLp);

                    setFIFOLimit = new EditText(getApplicationContext());
                    setFIFOLimit.setText(String.valueOf(FIFOLimit));
                    setFIFOLimit.setInputType(InputType.TYPE_CLASS_NUMBER);
                    setFIFOLimit.setTextSize(20);
                    setFIFOLimit.setTextColor(getResources().getColor(android.R.color.black));
                    setFIFOLimit.setLayoutParams(editTextLp);

                    layout.addView(FIFO);
                    layout.addView(setFIFOLimit);
                }else{
                    if(layout != null){
                        layout.removeView(FIFO);
                        layout.removeView(setFIFOLimit);
                        layout = null;
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        modeSpinner.setSelection(modeSpinnerPos);
        modeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setMode = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String generalInfoStr = "";
                generalInfo = new String[9];
                for (int i = 0; i < generalInfo.length; i++) {
                    if (i == 8) {
                        generalInfo[i] = setMode;
                    } else if (i == 7) {
                        generalInfo[i] = setLogfileMode;
                    }else {
                        generalInfo[i] = editTexts[i].getText().toString();
                    }
                    generalInfoStr += generalInfo[i] + "\n";
                }

                if(setLogfileMode.equals("FIFO")){
                    if(setFIFOLimit.getText() != null){
                        generalInfoStr += setFIFOLimit.getText() + "\n";
                    }else{
                        generalInfoStr += FIFOLimit + "\n";
                    }
                }

                writeToFile(generalInfoStr);
                finishSetup();
            }
        });
    }

    public void finishSetup(){
        Intent intent = new Intent(getApplicationContext(), Setup.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        startActivity(intent);
    }

    public void writeToFile(String data){
        String filename = "General Info";
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

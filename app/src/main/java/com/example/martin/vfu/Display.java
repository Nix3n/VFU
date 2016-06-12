package com.example.martin.vfu;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.SmsManager;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Martin on 2015-12-17.
 */
public class Display extends Activity implements Serializable {

    EditText name, size, IP, PORT;
    double[] data;
    int dataIndex = 0;
    int dateIndex = 0;
    String[] dateAndTime;
    double[][] multipleCkVal = new double[10][8];
    double[] ck = new double[8];

    double[] minValue = new double[8];
    double[] maxValue = new double[8];

    String[] completeFiles;

    int nbrOfSlaveSamples;
    int nbrOfMasterSamples;

    Button single, continuous, record, read, save;
    CheckBox autosave;

    TextView ch1Data, ch2Data, ch3Data, ch4Data,
            ch5Data, ch6Data, ch7Data, ch8Data, timeStamp;

    TextView[] channelData = new TextView[]{ch1Data, ch2Data, ch3Data, ch4Data,
            ch5Data, ch6Data, ch7Data, ch8Data};

    int[] channelDataId = new int[]{R.id.ch1data, R.id.ch2data, R.id.ch3data, R.id.ch4data,
            R.id.ch5data, R.id.ch6data, R.id.ch7data, R.id.ch8data};

    TextView ch1, ch2, ch3, ch4, ch5, ch6, ch7, ch8;

    TextView[] channels = new TextView[]{ch1, ch2, ch3, ch4, ch5, ch6, ch7, ch8};

    int[] channelId = new int[]{R.id.ch1, R.id.ch2, R.id.ch3, R.id.ch4,
            R.id.ch5, R.id.ch6, R.id.ch7, R.id.ch8};

    String[] activatedChannels = new String[8];
    String[] channelNames = new String[8];
    int[] decimals = new int[8];
    int[] maxLimit = new int[8];
    int[] minLimit = new int[8];
    int[] alarmSamples = new int[8];

    int phoneNbr;
    int sampleFreq;
    int recSamples;
    int skipSamples;
    String databaseIP;
    int databasePORT;
    int logFileSize;
    int FIFOLimit = 0;

    String[] channelStrings = new String[]{"Channel1", "Channel2", "Channel3", "Channel4",
            "Channel5", "Channel6", "Channel7", "Channel8"};

    String[] channelSaveNames = new String[]{"Channel 1", "Channel 2", "Channel 3", "Channel 4",
            "Channel 5", "Channel 6", "Channel 7", "Channel 8"};

    String[] stringData;
    String[] contChannelData;
    String[][] divideInputData;
    int amountOfData;

    TextView statusText, showNbrOfSamples;
    int nbrOfSamplesCollected = 0;
    String logfileMode;
    String mode;
    String device;

    BluetoothArduino mBlue;
    boolean isConnected = false;

    int autosaveIndex = 0;
    int autosaveFiles = 1;
    String autosaveFilename;
    String nextAutosaveFilename;

    View menuView;
    View saveView;
    View autosaveView;
    AlertDialog alert;
    ArrayAdapter<String> adapter;
    String[] files;
    String currentDir = "";
    boolean pressedBefore = false;

    Thread flow;
    final Handler flowHandler = new Handler();
    boolean running = false;

    String dirPath;
    String alarmDirPath;
    String dataDirPath;
    int[] alarmIndex = new int[8];
    int x = 0;
    double[][] savedXValue;
    double[][] savedYValue;
    int index = 0;

    Button[] functionButtons = new Button[10];

    String[] functionButtonsNames = {"F1", "F2", "F3", "F4", "F5", "F6", "F7", "F8", "F9", "F10"};

    String[] functionButtonTextFiles = {"function1", "function2", "function3", "function4",
            "function5", "function6", "function7", "function8", "function9", "function10"};

    GUI gui;
    int screenWidth;
    int screenHeight;

    private boolean saveBtnPressed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        for(int i = 0; i < channels.length; i++){
            channels[i] = (TextView) findViewById(channelId[i]);
            channelData[i] = (TextView) findViewById(channelDataId[i]);
        }

        //set number of alarm samples to 0 for all channels
        for (int i = 0; i < alarmIndex.length; i++) {
            alarmIndex[i] = 0;
        }

        //read in the calibration algorithm to make the raw data into Voltage
        FileInputStream fis = null;
        try{
            AssetManager assets = getAssets();
            InputStream is = assets.open("ATsvcLog.txt");

            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            is.close();

            String copyCalValues = new String(buffer);

            FileOutputStream fos = openFileOutput("ATsvcLog.txt", Context.MODE_PRIVATE);
            fos.write(copyCalValues.getBytes());
            fos.close();

            fis = openFileInput("ATsvcLog.txt");

            buffer = new byte[fis.available()];
            fis.read(buffer);
            fis.close();

            String readVars = new String(buffer);
            String[] readAllVars = readVars.split("\\r?\\n");
            String readSingle = "";
            for(int i = 0; i < readAllVars.length; i++){
                readSingle += readAllVars[i] + "\t";
            }
            String[] readSingleVars = readSingle.split("\\t");

            String[][] divideChVars = new String[11][9];

            int pos = 0;
            for(int i = 0; i < 11; i++){
                for(int a = 0; a < 9; a++){
                    divideChVars[i][a] = readSingleVars[pos];
                    pos++;
                }
            }

            for(int k = 0; k < 10; k++){
                for(int c = 0; c < 8; c++){
                    multipleCkVal[k][c] = Double.parseDouble(divideChVars[k+1][c+1]);
                }
            }

        }catch (IOException e){
            e.printStackTrace();
        }

        for(int i = 0; i < channelStrings.length; i++){
            loadSettings(i, channelStrings[i]);
        }
        loadSettings(0, "General Info");
        loadSettings(0, "Device");

        //create arrays for timestamp and data depending on FIFO or Continuous growth in general setup
        if (FIFOLimit != 0) {
            data = new double[8 * FIFOLimit];
            dateAndTime = new String[FIFOLimit];
        } else{
            data = new double[320];
            dateAndTime = new String[40];
        }

        //create arrays to store the data when any channels data goes outside the max or min boundary
        savedXValue = new double[8][alarmSamples.length];
        savedYValue = new double[8][alarmSamples.length];

        //set up the content of the GUI depending on whether the channel is activated in setup or not
        for(int i = 0; i < channelNames.length; i++){
            if(activatedChannels[i] != null) {
                if (!channelNames[i].equals("null")) {
                    channels[i].setText(channelNames[i]);
                } else {
                    channels[i].setText(channelStrings[i]);
                }
            }else{
                channels[i].setText("Deactivated");
            }
        }

        //set up the Graphical user interface
        gui = new GUI(getApplicationContext());

        screenWidth = gui.getWidth();
        screenHeight = gui.getHeight();

        timeStamp = (TextView) findViewById(R.id.timeStamp);

        LayoutInflater inflater = getLayoutInflater();
        menuView = inflater.inflate(R.layout.load_view, null);
        saveView = inflater.inflate(R.layout.save_view, null);
        autosaveView = inflater.inflate(R.layout.autosave_view, null);
        adapter = new ArrayAdapter<>(getApplicationContext(), R.layout.simple_list_item_1);

        LinearLayout autosaveRow = (LinearLayout) findViewById(R.id.autosave);
        LinearLayout displayFirstRow = (LinearLayout) findViewById(R.id.displayFirstRow);
        LinearLayout displaySecondRow = (LinearLayout) findViewById(R.id.displaySecondRow);
        LinearLayout displayThirdRow = (LinearLayout) findViewById(R.id.displayThirdRow);
        LinearLayout displayFourthRow = (LinearLayout) findViewById(R.id.displayFourthRow);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(5,0,0,0);

        autosave = gui.autoSave("Autosave");
        autosave.setLayoutParams(params);
        autosaveRow.addView(autosave);
        autosave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCheckBoxClicked(v);
            }
        });
        autosave.setButtonDrawable(android.R.drawable.checkbox_off_background);

        params = new LinearLayout.LayoutParams(screenWidth/2, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(5,0,0,0);

        showNbrOfSamples = gui.status(screenWidth/2);
        showNbrOfSamples.setLayoutParams(params);
        showNbrOfSamples.setGravity(Gravity.START);
        displayFirstRow.addView(showNbrOfSamples);
        showNbrOfSamples.setText("Number of samples collected: " + nbrOfSamplesCollected);

        statusText = gui.status(screenWidth/2);
        statusText.setLayoutParams(params);
        displayFirstRow.addView(statusText);

        params = new LinearLayout.LayoutParams(screenWidth/5 - 10, screenHeight/14);
        params.setMargins(5,5,5,5);

        //set up function buttons (F1, F2, ... , F10)
        for(int i = 0; i < functionButtons.length; i++){
            functionButtons[i] = gui.smallBtn(functionButtonsNames[i], R.drawable.custom_setup_button, screenWidth/6, screenHeight/14);
            functionButtons[i].setLayoutParams(params);
            if(i > 4){
                displayThirdRow.addView(functionButtons[i]);
            }else{
                displaySecondRow.addView(functionButtons[i]);
            }

            final int finalInt = i;

            functionButtons[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mode.equals("Slave")){
                        Toast toast = Toast.makeText(getApplicationContext(), "Choose Master as mode in Setup!", Toast.LENGTH_SHORT);
                        toast.show();
                    }else if(!mBlue.isConnected()) {
                        Toast toast = Toast.makeText(getApplicationContext(), "Connect to datalogger before using these functions!", Toast.LENGTH_SHORT);
                        toast.show();
                    }else{
                        String bufferData = "";
                        try {
                            AssetManager assets = getAssets();
                            InputStream is = assets.open(functionButtonTextFiles[finalInt] + ".txt");

                            byte[] buffer = new byte[is.available()];

                            is.read(buffer);
                            is.close();

                            bufferData = new String(buffer);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        String[] commands = bufferData.split("\\r?\\n");

                        for (int i = 0; i < commands.length; i++) {
                            if (commands[i].contains("[b")) {
                                String fetchAmountOfSamples = commands[i];
                                String[] divideString = fetchAmountOfSamples.split(",");
                                nbrOfMasterSamples = Integer.valueOf(divideString[1]);
                            }
                        }

                        single.setTag("on");
                        activateMasterContinuous(commands, true);
                    }
                }
            });
        }

        params = new LinearLayout.LayoutParams(screenWidth/4 - 10, screenHeight/14);
        params.setMargins(5,5,5,5);

        //set up single button
        single = gui.smallBtn("Single", R.drawable.custom_setup_button, screenWidth/4, screenHeight/14);
        single.setLayoutParams(params);
        displayFourthRow.addView(single);
        single.setTag("off");
        single.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mode.equals("Slave")) {
                    if (stringData == null) {
                        Toast toast = Toast.makeText(getApplicationContext(),
                                "Load a logfile before activating single shot!",
                                Toast.LENGTH_SHORT);
                        toast.show();
                    } else {
                        activateSlaveSingle();
                    }
                }else if(mode.equals("Master")){
                    if(!mBlue.isConnected()){
                        Toast toast = Toast.makeText(getApplicationContext(),
                                "Choose Slave as mode in General Setup or connect to the datalogger!",
                                Toast.LENGTH_SHORT);
                        toast.show();
                    }else {
                        if (continuous.getTag().equals("off")) {
                            nbrOfMasterSamples = 1;

                            String[] commands = new String[3];
                            commands[0] = "[a," + sampleFreq;
                            commands[1] = "[b," + nbrOfMasterSamples;
                            commands[2] = "[d";

                            single.setTag("on");
                            activateMasterContinuous(commands, true);
                        } else if (continuous.getTag().equals("on")) {
                            nbrOfMasterSamples = 1;
                            running = false;

                            continuous.setTag("off");
                            continuous.setBackgroundResource(R.drawable.custom_setup_button);

                            String[] commands = new String[3];
                            commands[0] = "[a," + sampleFreq;
                            commands[1] = "[b," + nbrOfMasterSamples;
                            commands[2] = "[d";

                            single.setTag("on");
                            activateMasterContinuous(commands, true);
                        }
                    }
                }
            }
        });

        //set up run/stop button
        continuous = gui.smallBtn("Run/Stop", R.drawable.custom_setup_button, screenWidth/4, screenHeight/14);
        continuous.setLayoutParams(params);
        displayFourthRow.addView(continuous);
        continuous.setTag("off");
        continuous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mode.equals("Slave")) {
                    if (stringData == null) {
                        Toast toast = Toast.makeText(getApplicationContext(),
                                "Load a logfile before activating continuous flow!",
                                Toast.LENGTH_SHORT);
                        toast.show();
                    } else {
                        if (continuous.getTag().equals("off")) {
                            running = true;
                            continuous.setTag("on");
                            continuous.setBackgroundResource(R.drawable.custom_setup_button_enabled);
                            activateSlaveContinuous();
                        } else if (continuous.getTag().equals("on")) {
                            running = false;
                            continuous.setTag("off");
                            continuous.setBackgroundResource(R.drawable.custom_setup_button);
                        }
                    }
                }else if(mode.equals("Master")){
                    if(!mBlue.isConnected()){
                        Toast toast = Toast.makeText(getApplicationContext(),
                                "Choose Slave as mode in General Setup or connect to the datalogger!",
                                Toast.LENGTH_SHORT);
                        toast.show();
                    }else {
                        if (continuous.getTag().equals("off")) {
                            nbrOfMasterSamples = 1;
                            continuous.setTag("on");
                            continuous.setBackgroundResource(R.drawable.custom_setup_button_enabled);

                            String[] commands = new String[3];
                            commands[0] = "[a," + sampleFreq;
                            commands[1] = "[b," + nbrOfMasterSamples;
                            commands[2] = "[d";

                            activateMasterContinuous(commands, true);
                        } else {
                            continuous.setTag("off");
                            continuous.setBackgroundResource(R.drawable.custom_setup_button);
                            running = false;
                            if (isConnected) {
                                mBlue.resetStringBuilder();
                            }
                        }
                    }
                }
            }
        });

        //set up record button
        record = gui.smallBtn("Record", R.drawable.custom_setup_button, screenWidth/4, screenHeight/14);
        record.setLayoutParams(params);
        displayFourthRow.addView(record);
        record.setTag("off");
        record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mode.equals("Master")) {
                    if (!mBlue.isConnected()) {
                        Toast toast = Toast.makeText(getApplicationContext(),
                                "Connect to the datalogger before recording!",
                                Toast.LENGTH_SHORT);
                        toast.show();
                    } else {
                        if(continuous.getTag().equals("on")){
                            continuous.setTag("off");
                            continuous.setBackgroundResource(R.drawable.custom_setup_button);
                            running = false;
                        }
                        Toast toast = Toast.makeText(getApplicationContext(),
                                "Collecting data...",
                                Toast.LENGTH_SHORT);
                        toast.show();

                        if (record.getTag().equals("off")) {
                            record.setTag("on");
                            nbrOfMasterSamples = recSamples;

                            String[] commands = new String[3];
                            commands[0] = "[a," + sampleFreq;
                            commands[1] = "[b," + nbrOfMasterSamples;
                            commands[2] = "[d";

                            activateMasterContinuous(commands, true);
                        }
                    }
                } else {
                    Toast toast = Toast.makeText(getApplicationContext(),
                            "Choose Master in General Setup to record!",
                            Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        });

        //Set up read button
        read = gui.smallBtn("Read", R.drawable.custom_setup_button, screenWidth/4, screenHeight/14);
        read.setLayoutParams(params);
        displayFourthRow.addView(read);
        read.setTag("off");
        read.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mode.equals("Master")){
                    if (mBlue.isConnected()) {
                        Toast toast = Toast.makeText(getApplicationContext(),
                                "Connect to the datalogger before reading!",
                                Toast.LENGTH_SHORT);
                        toast.show();
                    } else {
                        if(continuous.getTag().equals("on")){
                            continuous.setTag("off");
                            continuous.setBackgroundResource(R.drawable.custom_main_button);
                            running = false;
                        }
                        LinearLayout readOptions = new LinearLayout(getApplicationContext());
                        LinearLayout.LayoutParams readOptionsParams = new LinearLayout.LayoutParams(screenWidth/2, screenHeight/2);
                        readOptionsParams.weight = 1.0f;
                        readOptionsParams.gravity = Gravity.CENTER;
                        readOptions.setOrientation(LinearLayout.VERTICAL);
                        readOptions.setLayoutParams(readOptionsParams);
                        readOptions.setGravity(Gravity.CENTER);

                        final PopupWindow popupWindow = new PopupWindow(readOptions, screenWidth - screenWidth/6, screenHeight/3);
                        popupWindow.setFocusable(true);
                        popupWindow.setBackgroundDrawable(getResources().getDrawable(android.R.drawable.toast_frame));

                        final EditText nbrOfSamples = gui.editText();
                        readOptions.addView(nbrOfSamples);

                        Button completeReading = gui.mediumBtn("Read", android.R.drawable.btn_default, screenWidth/3, screenHeight/9);
                        readOptions.addView(completeReading);
                        completeReading.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if(nbrOfSamples.getText().toString().equals("")){
                                    Toast toast = Toast.makeText(getApplicationContext(), "Enter a number of samples to collect!", Toast.LENGTH_SHORT);
                                    toast.show();
                                }else{
                                    read.setTag("on");
                                    nbrOfMasterSamples = Integer.valueOf(nbrOfSamples.getText().toString());

                                    String[] commands = new String[2];
                                    commands[0] = "[a," + sampleFreq;
                                    commands[1] = "[e";

                                    activateMasterContinuous(commands, true);

                                    popupWindow.dismiss();
                                }
                            }
                        });
                        popupWindow.showAtLocation(statusText, Gravity.CENTER, 0, 0);
                    }
                }else{
                    Toast toast = Toast.makeText(getApplicationContext(),
                            "Choose Master as mode before reading!",
                            Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        });


        //run the initiation sequence if mode is set to master
        //and the Android phone is connected to the data logger
        if (mode.equals("Master")) {
            mBlue = new BluetoothArduino(device);
            if (mBlue.Connect()) {
                isConnected = true;

                String[] commands = new String[6];

                commands[0] = "[o,0:1:2:3:4:5:6:7";
                commands[1] = "[c,1:1:0:1";
                commands[2] = "[a,1000";
                commands[3] = "[b,1";
                commands[4] = "[d";
                commands[5] = "[e";

                for(int i = 0; i < commands.length; i++) {
                    String[] status = sendAndReturn(commands[i]);
                }

                statusText.setText("Connected to: " + device);
            }else{
                statusText.setText("Couldn't connect to: " + device);
            }
        }

        //Create directory if it doesn't exist with logfiles, if it exists just assign the path
        dirPath = getFilesDir().getAbsolutePath() + File.separator + "LogFiles";

        File projDir = new File(dirPath);
        if (!projDir.exists()) {
            projDir.mkdirs();
        }

        alarmDirPath = dirPath + File.separator + "AlarmLogFiles";
        dataDirPath = dirPath + File.separator + "DataLogFiles";

        projDir = new File(alarmDirPath);
        if (!projDir.exists()) {
            projDir.mkdirs();
        }

        projDir = new File(dataDirPath);
        if (!projDir.exists()) {
            projDir.mkdirs();
        }
    }

    public void onCheckBoxClicked(View view){
        if(mode.equals("Slave")){
            Toast toast = Toast.makeText(getApplicationContext(), "Choose Master in General Setup to Autosave", Toast.LENGTH_SHORT);
            toast.show();
            autosave.setChecked(false);
        }else if(autosave.isChecked()){
            final PopupWindow popupWindow = new PopupWindow(
                    autosaveView,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);

            popupWindow.setFocusable(true);
            popupWindow.setBackgroundDrawable(getResources().getDrawable(R.drawable.custom_black_border_no_edges));

            name = (EditText) autosaveView.findViewById(R.id.name);
            size = (EditText) autosaveView.findViewById(R.id.fileSize);

            size.setText(String.valueOf(logFileSize));

            saveBtnPressed = false;

            final LinearLayout saveBtnLayout = (LinearLayout) autosaveView.findViewById(R.id.contentLayout);

            save = gui.mediumBtn("Start Autosave", R.drawable.custom_main_button, screenWidth/2, screenHeight/14);
            saveBtnLayout.addView(save);
            save.setOnClickListener(new Button.OnClickListener() {
                @Override
                public void onClick(View v) {
                    saveBtnPressed = true;

                    logFileSize = Integer.parseInt(size.getText().toString());

                    data = new double[logFileSize*8];
                    dateAndTime = new String[logFileSize];

                    SharedPreferences mShared = getSharedPreferences("mShared", Context.MODE_PRIVATE);
                    SharedPreferences.Editor prefEditor = mShared.edit();

                    int nameCounter = mShared.getInt("nameCounter", 0);

                    try{
                        if(name.getText().toString().equals("")){
                            autosaveFilename = "Test" + nameCounter;
                            prefEditor.putInt("nameCounter", ++nameCounter).apply();
                        }else {
                            autosaveFilename = name.getText().toString();
                        }
                        nextAutosaveFilename = autosaveFilename;
                        FileOutputStream fos = new FileOutputStream(dataDirPath + File.separator + autosaveFilename);

                        String text = "Date/Time" + "\t" + channelSaveNames[0] + "\t" +
                                channelSaveNames[1] + "\t" + channelSaveNames[2] + "\t" +
                                channelSaveNames[3] + "\t" + channelSaveNames[4] + "\t" +
                                channelSaveNames[5] + "\t" + channelSaveNames[6] + "\t" +
                                channelSaveNames[7] + "\n";

                        fos.write(text.getBytes());
                        fos.close();

                        adapter.notifyDataSetChanged();
                    }catch(IOException e){
                        e.printStackTrace();
                    }
                    popupWindow.dismiss();
                }
            });

            popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                    if(!saveBtnPressed){
                        autosave.setChecked(false);
                    }else{
                        autosave.setChecked(true);
                    }
                    saveBtnLayout.removeView(save);
                }
            });

            popupWindow.showAtLocation(statusText, Gravity.CENTER, 0, 0);
        }else if(!autosave.isChecked()){
            try {
                FileInputStream fis = new FileInputStream(dataDirPath + File.separator + autosaveFilename);
                DataInputStream dataIn = new DataInputStream(fis);

                int size = fis.available();
                byte[] buffer = new byte[size];
                dataIn.read(buffer);

                String text = new String(buffer);
                String[] splitText = text.split("\n");

                if(splitText.length < 2){
                    File deleteFile = new File(dataDirPath, autosaveFilename);
                    if(deleteFile.delete()){
                        SharedPreferences mShared = getSharedPreferences("mShared", Context.MODE_PRIVATE);
                        SharedPreferences.Editor prefEditor = mShared.edit();

                        int nameCounter = mShared.getInt("nameCounter", 0);

                        prefEditor.putInt("nameCounter", --nameCounter).apply();

                        Toast.makeText(getApplicationContext(), autosaveFilename + " deleted because of no data!", Toast.LENGTH_SHORT).show();
                    }
                }
            }catch (IOException e){
                e.printStackTrace();
            }
        }
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

            if(fileInfo[0].equals(filename)){
                activatedChannels[i] = fileInfo[0];
                channelNames[i] = fileInfo[1];
                decimals[i] = Integer.parseInt(fileInfo[2]);
                maxLimit[i] = Integer.parseInt(fileInfo[3]);
                minLimit[i] = Integer.parseInt(fileInfo[4]);
                alarmSamples[i] = Integer.parseInt(fileInfo[5]);
            }else if(fileInfo[0].equals("deactivated")){
                activatedChannels[i] = null;
            }

            if(filename.equals("General Info")){
                phoneNbr = Integer.parseInt(fileInfo[0]);
                sampleFreq = Integer.parseInt(fileInfo[1]);
                recSamples = Integer.parseInt(fileInfo[2]);
                skipSamples = Integer.parseInt(fileInfo[3]);
                databaseIP = fileInfo[4];
                databasePORT = Integer.parseInt(fileInfo[5]);
                logFileSize = Integer.parseInt(fileInfo[6]);
                logfileMode = fileInfo[7];
                mode = fileInfo[8];
                if(fileInfo.length > 9){
                    FIFOLimit = Integer.parseInt(fileInfo[9]);
                }
            }

            if(filename.equals("Device")){
                device = fileInfo[0];
            }

            fis.close();
            dataIn.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(isConnected) {
            mBlue.Disconnect();
        }
    }

    public static double round(double value, int places) {
        if (places < 0){
            return value;
        }
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, BigDecimal.ROUND_HALF_UP);
        return bd.doubleValue();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.display_popup, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.load) {
            printFileDirectory(dirPath);
            return true;
        }else if(id == R.id.save){
            saveFile();
            return true;
        }else if(id == R.id.readDatabase){
            updatedReadFromDatabase();
            return true;
        }else if(id == R.id.saveDatabase){
            saveToDatabase();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //method used to display and choose one of the saved logfiles
    public void printFileDirectory(final String filePath) {
        File file = new File(filePath);

        adapter = new ArrayAdapter<>(getApplicationContext(), R.layout.simple_list_item_1, file.list());
        adapter.notifyDataSetChanged();

        files = new String[adapter.getCount()];

        for(int i = 0; i < adapter.getCount(); i++){
            files[i] = adapter.getItem(i);
        }

        View loadView = getLayoutInflater().inflate(R.layout.load_view, null);

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Display.this);
        alertDialog.setView(loadView);
        alertDialog.setTitle(filePath);
        alertDialog.setItems(files, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //which is the item number in the list which you can use
                //to do things accordingly
                String updatedFilePath = filePath + File.separator + files[which];
                File file = new File(updatedFilePath);
                if (file.isDirectory()) {
                    printFileDirectory(updatedFilePath);
                } else {
                    statusText.setText("read from: " + files[which]);
                    setChannelData(files[which], filePath, "null");
                    pressedBefore = true;
                }
            }
        });

        AlertDialog alert = alertDialog.create();
        alert.show();
    }

    //method to print out the data from the chosen logfile
    public void setChannelData(String filename, String filePath, String text){
        FileInputStream fis = null;
        try {
            if(!filename.equals("null")) {
                fis = new FileInputStream(filePath + File.separator + filename);
                DataInputStream dataIn = new DataInputStream(fis);

                int size = fis.available();
                byte[] buffer = new byte[size];
                dataIn.read(buffer);

                text = new String(buffer);

                fis.close();
                dataIn.close();
            }

            String[] str = text.split("\\r?\\n");
            String s = str[0] + "\t";
            nbrOfSlaveSamples = 0;
            for (int i = 1; i < str.length; i++) {
                s += str[i] + "\t";
                nbrOfSlaveSamples++;
            }
            index = nbrOfSlaveSamples;
            stringData = s.split("\\t");

            divideInputData = new String[9][stringData.length/9];

            int pos = 0;
            for(int i = 0; i < stringData.length/9; i++){
                for(int a = 0; a < 9; a++){
                    divideInputData[a][i] = stringData[pos];
                    pos++;
                }
            }

            amountOfData = (stringData.length-(8+str.length))/8;

            activateSlaveSingle();

        }catch (IOException e){
            e.printStackTrace();
        }
    }

    //method to go to the next sample
    public void activateSlaveSingle(){
        for(int a = 0; a < channelData.length; a++){
            if(index == amountOfData){
                index = 0;
            }
            timeStamp.setText(divideInputData[0][index+1]);
            channelData[a].setText(divideInputData[a+1][index+1]);
        }
        index++;
    }

    //method to loop through the samples in the chosen logfile
    public void activateSlaveContinuous(){
        flow = new Thread(new Runnable(){
            @Override
            public void run(){
                if(running) {
                    for(int a = 0; a < channelData.length; a++){
                        if(index == amountOfData){
                            index = 0;
                        }
                        timeStamp.setText(divideInputData[0][index+1]);
                        channelData[a].setText(divideInputData[a+1][index+1]);
                    }
                    index++;
                    flowHandler.postDelayed(this, 100);
                }
            }
        });
        flowHandler.postDelayed(flow, 100);
    }

    //method to communicate with the datalogger
    public String[] sendAndReturn(String command){
        String[] data = new String[9];
        try{
            mBlue.resetStringBuilder();
            mBlue.sendMessage(command + "\r");
            Thread.sleep(200);
            if(command.contains("[a") || command.contains("[b")){
                while(true){
                    StringBuilder stringBuilder = mBlue.bluetoothInput();
                    String check = stringBuilder.toString();

                    String[] inputLines = check.split("\\r?\\n");

                    if (inputLines[inputLines.length - 1].equals("OK")) {
                        break;
                    }
                }
            }else if(command.contains("[d")){
                while(true){
                    StringBuilder stringBuilder = mBlue.bluetoothInput();
                    String check = stringBuilder.toString();

                    String[] inputLines = check.split("\\r?\\n");
                    if (inputLines.length == 5) {
                        String dataLine = inputLines[4];
                        String[] channelData = dataLine.split(" ");
                        if (channelData[channelData.length - 1].equals("END")) {
                            data = new String[channelData.length];
                            data = channelData;
                            break;
                        }
                    }
                }
            }else if(command.contains("[c") || command.contains("[o")) {
                while(true){
                    StringBuilder stringBuilder = mBlue.bluetoothInput();
                    String check = stringBuilder.toString();

                    if (check.contains("OK")) {
                        break;
                    }
                }
            }else{
                while(true){
                    StringBuilder stringBuilder = mBlue.bluetoothInput();
                    String check = stringBuilder.toString();

                    String[] inputLines = check.split("\\r?\\n");
                    if (inputLines.length == 4) {
                        String dataLine = inputLines[3];
                        String[] channelData = dataLine.split(" ");
                        if (channelData[channelData.length - 1].equals("END")) {
                            data = new String[channelData.length];
                            data = channelData;
                            break;
                        }
                    }
                }
            }
        }catch(InterruptedException ie){
            ie.printStackTrace();
        }
        return data;
    }

    //method to acquire data from the datalogger
    public void activateMasterContinuous(final String[] commands, boolean run){
        running = run;
        flow = new Thread(new Runnable() {
            @Override
            public void run() {
                if(running) {

                    if (skipSamples > 0) {
                        String printData = "";
                        for(int s = 0; s < skipSamples; s++){
                            for (int i = 0; i < commands.length; i++) {
                                contChannelData = sendAndReturn(commands[i]);
                            }

                            SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,S");//dd/MM/yyyy
                            Date now = new Date();

                            for(int i = 0; i < contChannelData.length; i++){
                                if(i == contChannelData.length-1) {
                                    printData += contChannelData[i] + "\n";
                                }else if(i == 0){
                                    printData += sdfDate.format(now) + "\t";
                                }else{
                                    printData += contChannelData[i];
                                }
                            }

                            statusText.setText(printData);
                        }
                    }

                    for (int i = 0; i < commands.length; i++) {
                        contChannelData = sendAndReturn(commands[i]);
                    }

                    double valueOfChData;
                    double finishedValue = 0;

                    if (contChannelData.length == (nbrOfMasterSamples * 8 + 1)) {
                        for (int a = 0; a < nbrOfMasterSamples; a++) {
                            for (int i = 0; i < channelData.length; i++) {
                                valueOfChData = Double.parseDouble(contChannelData[i]);

                                int k = 0;
                                ck[i] = multipleCkVal[k][i] + (multipleCkVal[k + 1][i] * valueOfChData) +
                                        (multipleCkVal[k + 2][i] * Math.pow(valueOfChData, 2)) +
                                        (multipleCkVal[k + 3][i] * Math.pow(valueOfChData, 3)) +
                                        (multipleCkVal[k + 4][i] * Math.pow(valueOfChData, 4)) +
                                        (multipleCkVal[k + 5][i] * Math.log(valueOfChData + 0.000000000001)) +
                                        (multipleCkVal[k + 6][i] * Math.exp(multipleCkVal[k + 7][i] * valueOfChData)) +
                                        (multipleCkVal[k + 8][i] * Math.pow(valueOfChData, multipleCkVal[k + 9][i] + 0.000000000001));

                                finishedValue = round(ck[i], decimals[i]);
                                channelData[i].setText(String.valueOf(finishedValue));

                                if (activatedChannels[i] != null) {
                                    if (maxLimit[i] == 0) {
                                        maxLimit[i] = 15;
                                        maxValue[i] = maxLimit[i];
                                    } else {
                                        maxValue[i] = maxLimit[i];
                                    }
                                    minValue[i] = minLimit[i];
                                }

                                if (dataIndex == data.length) {
                                    for (int dataPos = 0; dataPos < data.length - 1; dataPos++) {
                                        data[dataPos] = data[dataPos + 1];
                                    }
                                    data[data.length - 1] = finishedValue;
                                } else {
                                    data[dataIndex] = finishedValue;
                                    dataIndex++;
                                }

                                if (activatedChannels[i] != null) {
                                    if (finishedValue > maxValue[i]) {
                                        savedXValue[i][alarmIndex[i]] = x;
                                        savedYValue[i][alarmIndex[i]] = finishedValue;
                                        alarmIndex[i]++;
                                    } else if (finishedValue < minValue[i]) {
                                        savedXValue[i][alarmIndex[i]] = x;
                                        savedYValue[i][alarmIndex[i]] = finishedValue;
                                        alarmIndex[i]++;
                                    } else {
                                        alarmIndex[i] = 0;
                                    }

                                    if (alarmIndex[i] == alarmSamples[i]) {
                                        String number = String.valueOf(phoneNbr);

                                        SmsManager sms = SmsManager.getDefault();
                                        String allTheErrors = "";
                                        for (int z = 0; z < alarmIndex[i]; z++) {
                                            allTheErrors += "(" + savedXValue[i][z] + "," + savedYValue[i][z] + ") ";
                                        }
                                        if (number.equals("")) {
                                            Toast toast = Toast.makeText(getApplicationContext(),
                                                    "Please choose a phone number in Setup!",
                                                    Toast.LENGTH_SHORT);
                                            toast.show();
                                        } else if (channelNames[i] != null) {
                                            sms.sendTextMessage(number, null, "An Error occured at " +
                                                    channelNames[i] + ": " + allTheErrors, null, null);
                                        } else {
                                            sms.sendTextMessage(number, null, "An Error occured at " +
                                                    channelStrings[i] + ": " + allTheErrors, null, null);
                                        }
                                        alarmIndex[i] = 0;

                                        saveAlarmFile();
                                        running = false;
                                        continuous.setTag("off");
                                        continuous.setBackgroundColor(getResources().getColor(R.color.Grey));
                                    }
                                }
                            }
                            SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,S");//dd/MM/yyyy
                            Date now = new Date();
                            String strDate = sdfDate.format(now);
                            timeStamp.setText(strDate);
                            if (dateIndex == dateAndTime.length) {
                                for (int datePos = 0; datePos < dateAndTime.length - 1; datePos++) {
                                    dateAndTime[datePos] = dateAndTime[datePos + 1];
                                }
                                dateAndTime[dateAndTime.length - 1] = strDate;
                            } else {
                                dateAndTime[dateIndex] = strDate;
                                dateIndex++;
                            }

                            if (autosave.isChecked()) {
                                try {
                                    String text = "";
                                    if (autosaveIndex == logFileSize * autosaveFiles) {
                                        nextAutosaveFilename = autosaveFilename + " " + dateAndTime[dateAndTime.length - 1];
                                        FileOutputStream fos = new FileOutputStream(dataDirPath + File.separator + nextAutosaveFilename);

                                        text = "Date/Time" + "\t" + channelSaveNames[0] + "\t" +
                                                channelSaveNames[1] + "\t" + channelSaveNames[2] + "\t" +
                                                channelSaveNames[3] + "\t" + channelSaveNames[4] + "\t" +
                                                channelSaveNames[5] + "\t" + channelSaveNames[6] + "\t" +
                                                channelSaveNames[7] + "\n" + dateAndTime[dateAndTime.length - 1] +
                                                "\t" + data[data.length - 8] + "\t" + data[data.length - 7] +
                                                "\t" + data[data.length - 6] + "\t" + data[data.length - 5] +
                                                "\t" + data[data.length - 4] + "\t" + data[data.length - 3] +
                                                "\t" + data[data.length - 2] + "\t" + data[data.length - 1];

                                        fos.write(text.getBytes());
                                        fos.close();

                                        ConnectToDatabase connectToDatabase = new ConnectToDatabase(databaseIP, databasePORT);
                                        connectToDatabase.execute();

                                        try {
                                            Thread.sleep(300);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }

                                        File fileToSend = new File(dataDirPath + File.separator + nextAutosaveFilename);

                                        String transferResults = connectToDatabase.sendFileToDatabase(fileToSend, nextAutosaveFilename);
                                        statusText.setText(transferResults);
                                        //connectToDatabase.endConnection();

                                        autosaveIndex++;
                                        autosaveFiles++;
                                    } else if (autosaveIndex > logFileSize) {
                                        FileOutputStream fos = new FileOutputStream(dataDirPath + File.separator + nextAutosaveFilename, true);

                                        text = "\n" + dateAndTime[dateAndTime.length - 1] +
                                                "\t" + data[data.length - 8] + "\t" + data[data.length - 7] +
                                                "\t" + data[data.length - 6] + "\t" + data[data.length - 5] +
                                                "\t" + data[data.length - 4] + "\t" + data[data.length - 3] +
                                                "\t" + data[data.length - 2] + "\t" + data[data.length - 1];

                                        fos.write(text.getBytes());
                                        fos.close();

                                        ConnectToDatabase connectToDatabase = new ConnectToDatabase(databaseIP, databasePORT);
                                        connectToDatabase.execute();

                                        try {
                                            Thread.sleep(300);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }

                                        File fileToSend = new File(dataDirPath + File.separator + nextAutosaveFilename);

                                        String transferResults = connectToDatabase.sendFileToDatabase(fileToSend, nextAutosaveFilename);
                                        statusText.setText(transferResults);
                                        //connectToDatabase.endConnection();

                                        autosaveIndex++;
                                    } else {
                                        FileOutputStream fos = new FileOutputStream(dataDirPath + File.separator + autosaveFilename, true);

                                        text = "\n" + dateAndTime[dateIndex - 1] + "\t" + data[dataIndex - 8] +
                                                "\t" + data[dataIndex - 7] + "\t" + data[dataIndex - 6] +
                                                "\t" + data[dataIndex - 5] + "\t" + data[dataIndex - 4] +
                                                "\t" + data[dataIndex - 3] + "\t" + data[dataIndex - 2] +
                                                "\t" + data[dataIndex - 1];

                                        fos.write(text.getBytes());
                                        fos.close();

                                        ConnectToDatabase connectToDatabase = new ConnectToDatabase(databaseIP, databasePORT);
                                        connectToDatabase.execute();

                                        try {
                                            Thread.sleep(300);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }

                                        File fileToSend = new File(dataDirPath + File.separator + autosaveFilename);

                                        String transferResults = connectToDatabase.sendFileToDatabase(fileToSend, autosaveFilename);
                                        statusText.setText(transferResults);
                                        //connectToDatabase.endConnection();

                                        autosaveIndex++;
                                    }

                                    adapter.notifyDataSetChanged();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            x++;
                            nbrOfSamplesCollected++;
                            showNbrOfSamples.setText("Number of samples collected: " + nbrOfSamplesCollected);
                        }

                        if (record.getTag().equals("on")) {
                            record.setTag("off");
                            running = false;
                        } else if (single.getTag().equals("on")) {
                            single.setTag("off");
                            running = false;
                        }else if(read.getTag().equals("on")){
                            read.setTag("off");
                            running = false;
                        }

                    } else {
                        statusText.setText("Failure in collecting data, try again!");
                    }
                    flowHandler.postDelayed(this, 100);
                }
            }
        });
        flowHandler.postDelayed(flow, 100);
    }

    public void saveAlarmFile(){
        try{
            String filename = "AlarmLogFile " + dateAndTime[0];
            FileOutputStream fos = new FileOutputStream(alarmDirPath + File.separator + filename);

            String text = saveStructure();

            fos.write(text.getBytes());
            fos.close();

            adapter.notifyDataSetChanged();
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public void saveFile(){
        final PopupWindow popupWindow = new PopupWindow(
                saveView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        popupWindow.setBackgroundDrawable(getResources().getDrawable(R.drawable.custom_black_border_no_edges));
        popupWindow.setFocusable(true);

        name = (EditText) saveView.findViewById(R.id.name);

        Button save = (Button)saveView.findViewById(R.id.save);
        save.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    if(name.getText().toString().equals("")){
                        Toast.makeText(getApplicationContext(), "Name needs to be atleast one character!", Toast.LENGTH_SHORT).show();
                    }else {
                        String text = saveStructure();

                        if(text != null) {
                            String filename = name.getText().toString();
                            FileOutputStream fos = new FileOutputStream(dataDirPath + File.separator + filename);
                            fos.write(text.getBytes());
                            fos.close();
                        }else{
                            Toast toast = Toast.makeText(getApplicationContext(), "You need to save atleast 1 sample.", Toast.LENGTH_SHORT);
                            toast.show();
                        }

                        adapter.notifyDataSetChanged();
                        popupWindow.dismiss();
                    }
                }catch(IOException e){
                    e.printStackTrace();
                }
            }
        });
        popupWindow.showAtLocation(statusText, Gravity.CENTER, 0, 0);
    }

    public String saveStructure(){
        String[] str = new String[dataIndex];

        for(int i = 0; i < dataIndex; i++){
            str[i] = Double.toString(data[i]);
        }

        int size = dateIndex;
        String[] finalStr = new String[size+1];
        int a = 0;
        for(int i = 0; i < str.length - 7; i+=8){
            if(a == 0){
                finalStr[a] = "Date/Time" + "\t" + channelSaveNames[i] + "\t" +
                        channelSaveNames[i+1] + "\t" + channelSaveNames[i+2] + "\t" +
                        channelSaveNames[i+3] + "\t" + channelSaveNames[i+4] + "\t" +
                        channelSaveNames[i+5] + "\t" + channelSaveNames[i+6] + "\t" +
                        channelSaveNames[i+7] + "\n";
                a++;
            }

            if(i == str.length - 8){
                finalStr[a] = dateAndTime[a-1] + "\t" + str[i] + "\t" +
                        str[i + 1] + "\t" + str[i + 2] + "\t" + str[i + 3] + "\t" +
                        str[i + 4] + "\t" + str[i + 5] + "\t" + str[i + 6] + "\t" +
                        str[i + 7];
            }else {
                finalStr[a] = dateAndTime[a-1] + "\t" + str[i] + "\t" +
                        str[i + 1] + "\t" + str[i + 2] + "\t" + str[i + 3] + "\t" +
                        str[i + 4] + "\t" + str[i + 5] + "\t" + str[i + 6] + "\t" +
                        str[i + 7] + "\n";
                a++;
            }
        }

        String text = finalStr[0];
        for(int i = 1; i < finalStr.length; i++){
            text += finalStr[i];
        }

        return text;
    }

    public void updatedReadFromDatabase(){
        ConnectToDatabase connectToDatabase = new ConnectToDatabase(databaseIP, databasePORT);
        connectToDatabase.execute();

        try{
            Thread.sleep(300);
        }catch (Exception e){
            e.printStackTrace();
        }

        String data = connectToDatabase.retreiveDatabase(null, null, "$");

        String[] splitData = data.split("/");
        String[] fileNames = new String[splitData.length/2];
        for(int i = 0; i < fileNames.length; i++){
            fileNames[i] = splitData[i * 2 + 1];
        }
        completeFiles = new String[splitData.length/2];
        for(int i = 1; i <= completeFiles.length; i++){
            completeFiles[i-1] = "Date/Time" + "\t" + channelSaveNames[0] + "\t" +
                    channelSaveNames[1] + "\t" + channelSaveNames[2] + "\t" +
                    channelSaveNames[3] + "\t" + channelSaveNames[4] + "\t" +
                    channelSaveNames[5] + "\t" + channelSaveNames[6] + "\t" +
                    channelSaveNames[7] + "\n" + splitData[i * 2];
        }

        View loadView = getLayoutInflater().inflate(R.layout.load_view, null);

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Display.this);
        alertDialog.setView(loadView);
        alertDialog.setTitle("Database");
        alertDialog.setItems(fileNames, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //which is the item number in the list which you can use
                //to do things accordingly
                index = 0;
                setChannelData("null", "null", completeFiles[which]);
            }
        });

        AlertDialog alert = alertDialog.create();
        alert.show();

        connectToDatabase.endConnection();
    }

    public void saveToDatabase(){
        LayoutInflater layoutInflater
                = (LayoutInflater)getBaseContext()
                .getSystemService(LAYOUT_INFLATER_SERVICE);
        final View popupView = layoutInflater.inflate(R.layout.database_save_popup, null);
        final PopupWindow popupWindow = new PopupWindow(
                popupView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        popupWindow.setBackgroundDrawable(getResources().getDrawable(R.drawable.custom_black_border_no_edges));
        popupWindow.setFocusable(true);

        LinearLayout saveBtnLayout = (LinearLayout) popupView.findViewById(R.id.saveBtnLayout);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(screenWidth/4, screenHeight/14);
        params.setMargins(0,0,0,10);

        Button save = gui.mediumBtn("Save", R.drawable.custom_main_button, screenWidth/4, screenHeight/14);
        save.setLayoutParams(params);
        saveBtnLayout.addView(save);
        final EditText name = (EditText) popupView.findViewById(R.id.name);
        IP = (EditText) popupView.findViewById(R.id.ip);
        PORT = (EditText) popupView.findViewById(R.id.port);

        popupWindow.showAtLocation(statusText, Gravity.CENTER, 0, 0);

        IP.setText(databaseIP);
        PORT.setText(String.valueOf(databasePORT));

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ConnectToDatabase connectToDatabase = new ConnectToDatabase(IP.getText().toString(), Integer.valueOf(PORT.getText().toString()));
                connectToDatabase.execute();

                try{
                    Thread.sleep(300);
                }catch (Exception e){
                    e.printStackTrace();
                }

                File fileToSend = new File(dataDirPath + File.separator + name.getText().toString());

                String transferResults = connectToDatabase.sendFileToDatabase(fileToSend, name.getText().toString());
                statusText.setText(transferResults);
                connectToDatabase.endConnection();
                popupWindow.dismiss();
            }
        });
    }
}

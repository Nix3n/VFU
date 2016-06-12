package com.example.martin.vfu;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
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
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Martin on 2015-11-26.
 */
public class Graph extends Activity implements Serializable {

    GraphView graph;
    private SlidingUpPanelLayout slidingLayout;

    EditText name, size, IP, PORT;
    String autosaveFilename;
    String nextAutosaveFilename;
    double[] data;

    Button[] buttons = new Button[8];
    Button[] functionButtons = new Button[10];
    Button continuous, single, record, showBtn, hideBtn, clearGraph, read, save;
    CheckBox autosave;

    String[] functionButtonsNames = {"F1", "F2", "F3", "F4", "F5", "F6", "F7", "F8", "F9", "F10"};

    String[] functionButtonTextFiles = {"function1", "function2", "function3", "function4",
            "function5", "function6", "function7", "function8", "function9", "function10"};

    int[] buttonId = {R.id.ch1, R.id.ch2, R.id.ch3,
            R.id.ch4, R.id.ch5, R.id.ch6,
            R.id.ch7, R.id.ch8};

    String[] activatedChannels = new String[8];
    String[] channelNames = new String[8];
    int[] decimals = new int[8];
    int[] maxLimit = new int[8];
    int[] minLimit = new int[8];
    int[] alarmSamples = new int[8];

    int nbrOfSlaveSamples;
    int nbrOfMasterSamples;

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

    String[] channelDefaultNames = new String[]{"Ch1", "Ch2", "Ch3", "Ch4",
            "Ch5", "Ch6", "Ch7", "Ch8"};

    String[] channelSaveNames = new String[]{"Channel 1", "Channel 2", "Channel 3", "Channel 4",
            "Channel 5", "Channel 6", "Channel 7", "Channel 8"};

    View saveView;
    View autosaveView;
    ArrayAdapter<String> adapter;
    String[] files;
    String[] completeFiles;

    String[] stringData;
    LineGraphSeries[] chSeries = new LineGraphSeries[8];
    LineGraphSeries[] chSeriesMax = new LineGraphSeries[8];
    LineGraphSeries[] chSeriesMin = new LineGraphSeries[8];
    DataPoint[][] ch;
    DataPoint[][] min;
    DataPoint[][] max;
    DataPoint[][] loopCh;
    DataPoint[][] loopMax;
    DataPoint[][] loopMin;
    double[][] multiChData;
    int amountOfData;
    double[] minValue = new double[8];
    double[] maxValue = new double[8];

    Thread flow;
    final Handler flowHandler = new Handler();
    boolean running;
    int dataIndex = 0;
    int dateIndex = 0;
    int autosaveIndex = 0;
    int autosaveFiles = 1;
    String[] dateAndTime;
    int a = 0;
    int x = 0;
    float xAcc = 0;
    float yAcc = 0;
    float zAcc = 0;
    float[][] savedXAccValues;
    float[][] savedYAccValues;
    float[][] savedZAccValues;

    String dirPath;
    String alarmDirPath;
    String dataDirPath;
    String mode;
    String logfileMode;

    BluetoothArduino mBlue;

    TextView test, showNbrOfSamples;
    int nbrOfSamplesCollected = 0;
    String device;

    boolean isConnected = false;

    String[] channelData;

    double[][] multipleCkVal = new double[10][8];
    double[] ck = new double[8];
    int[] alarmIndex = new int[8];
    double[][] savedXValue;
    double[][] savedYValue;
    double savedMaximum = 0;
    double savedMinimum = 0;

    String[] color = new String[8];

    int[] channelColors = new int[8];

    int[] defaultColorCodes = new int[]{R.color.Red, R.color.Green, R.color.Blue, R.color.Yellow,
            R.color.Magenta, R.color.Cyan, R.color.Brown, R.color.Orange};

    String[] defaultColors = new String[]{"Red", "Green", "Blue", "Yellow",
            "Magenta", "Cyan", "Brown", "Orange"};

    public GUI gui;

    LinearLayout customLayout;
    int screenWidth;
    int screenHeight;
    ListView customLegendRenderer;

    boolean saveBtnPressed;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);

        //Extra class to handle GUI implementation
        gui = new GUI(getApplicationContext());

        screenWidth = gui.getWidth();
        screenHeight = gui.getHeight();

        LinearLayout arrowDown = (LinearLayout) findViewById(R.id.arrowDown);
        LinearLayout firstRow = (LinearLayout) findViewById(R.id.popupFirstRow);
        LinearLayout secondRow = (LinearLayout) findViewById(R.id.popupSecondRow);
        LinearLayout popupLayout = (LinearLayout) findViewById(R.id.popupLayout);
        LinearLayout graphFirstRow = (LinearLayout) findViewById(R.id.graphFirstRow);
        LinearLayout graphSecondRow = (LinearLayout) findViewById(R.id.graphSecondRow);
        customLayout = (LinearLayout) findViewById(R.id.customLegendRenderer);

        //function to keep the screen on at all times when activity is running
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        graph = (GraphView) findViewById(R.id.graph);

        //Set up slide-up bar
        slidingLayout = (SlidingUpPanelLayout)findViewById(R.id.sliding_layout);
        slidingLayout.setPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {

            }

            @Override
            public void onPanelCollapsed(View panel) {
                showBtn.setVisibility(View.VISIBLE);
                test.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPanelExpanded(View panel) {

            }

            @Override
            public void onPanelAnchored(View panel) {

            }

            @Override
            public void onPanelHidden(View panel) {

            }
        });
        slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);

        for(int i = 0; i < alarmIndex.length; i++){
            alarmIndex[i] = 0;
        }

        //Load all settings
        for(int i = 0; i < channelStrings.length; i++) {
            loadSettings(i, channelStrings[i]);
        }
        loadSettings(0, "General Info");
        loadSettings(0, "Device");

        savedXValue = new double[8][];
        savedYValue = new double[8][];
        savedXAccValues = new float[8][];
        savedYAccValues = new float[8][];
        savedZAccValues = new float[8][];

        if(FIFOLimit != 0){
            data = new double[8*FIFOLimit];
            dateAndTime = new String[FIFOLimit];
        }else {
            data = new double[logFileSize*8];
            dateAndTime = new String[logFileSize];
        }

        //Set graph colors
        for(int i = 0; i < 8; i++) {
            if(color[i] != null) {
                if (!color[i].equals(defaultColors[i])) {
                    if (color[i].equals("Red")) {
                        channelColors[i] = defaultColorCodes[0];
                    } else if (color[i].equals("Green")) {
                        channelColors[i] = defaultColorCodes[1];
                    } else if (color[i].equals("Blue")) {
                        channelColors[i] = defaultColorCodes[2];
                    } else if (color[i].equals("Yellow")) {
                        channelColors[i] = defaultColorCodes[3];
                    } else if (color[i].equals("Magenta")) {
                        channelColors[i] = defaultColorCodes[4];
                    } else if (color[i].equals("Cyan")) {
                        channelColors[i] = defaultColorCodes[5];
                    } else if (color[i].equals("Brown")) {
                        channelColors[i] = defaultColorCodes[6];
                    } else if (color[i].equals("Orange")) {
                        channelColors[i] = defaultColorCodes[7];
                    }
                } else {
                    channelColors[i] = defaultColorCodes[i];
                }
            }else{
                channelColors[i] = defaultColorCodes[i];
            }
        }

        //Load the calibration values
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

        //Set up the channel buttons
        for(int a = 0; a < buttons.length; a++){
            buttons[a] = gui.mediumBtn(channelDefaultNames[a], R.drawable.custom_setup_button, screenWidth/6, screenHeight/7);
            if(a > 3){
                secondRow.addView(buttons[a]);
            }else{
                firstRow.addView(buttons[a]);
            }

            if(activatedChannels[a] == null){
                buttons[a].setBackgroundResource(R.drawable.custom_graph_button_disabled);
                buttons[a].setText(channelDefaultNames[a]);
            }else{
                if(!channelNames[a].equals("null")){
                    buttons[a].setText(channelNames[a]);
                }else{
                    buttons[a].setText(channelDefaultNames[a]);
                }
                savedXValue[a] = new double[alarmSamples[a]];
                savedYValue[a] = new double[alarmSamples[a]];
                savedXAccValues[a] = new float[alarmSamples[a]];
                savedYAccValues[a] = new float[alarmSamples[a]];
                savedZAccValues[a] = new float[alarmSamples[a]];

                buttons[a].setTag("on");
                buttons[a].setBackgroundResource(R.drawable.custom_setup_button_enabled);
            }
        }

        customLegendRenderer();
        setLegendRendererVisibility(View.INVISIBLE);

        //add button to straighten out first row
        Button extraBtn = gui.mediumBtn("", android.R.drawable.btn_default, screenWidth/6, screenHeight/7);
        firstRow.addView(extraBtn);
        extraBtn.setVisibility(View.INVISIBLE);
        extraBtn.setFocusable(false);
        extraBtn.setEnabled(false);

        test = gui.status((screenWidth/17)*8);
        test.setGravity(Gravity.LEFT);
        popupLayout.addView(test);

        RelativeLayout graphMainLayout = (RelativeLayout) findViewById(R.id.graphMainLayout);

        LinearLayout arrowUpLayout = new LinearLayout(this);
        LinearLayout.LayoutParams arrowUpLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT);
        arrowUpLayout.setLayoutParams(arrowUpLayoutParams);
        arrowUpLayout.setOrientation(LinearLayout.VERTICAL);
        arrowUpLayout.setGravity(Gravity.CENTER);
        graphMainLayout.addView(arrowUpLayout);

        TextView textView = new TextView(this);
        LinearLayout.LayoutParams textViewParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, screenHeight/8);
        textView.setLayoutParams(textViewParams);
        arrowUpLayout.addView(textView);

        showBtn = gui.arrow(android.R.drawable.arrow_up_float);
        showBtn.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
        LinearLayout.LayoutParams arrowUpParams = new LinearLayout.LayoutParams(screenWidth/17, screenHeight/17);
        showBtn.setLayoutParams(arrowUpParams);
        arrowUpLayout.addView(showBtn);
        showBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
                test.setVisibility(View.INVISIBLE);
                showBtn.setVisibility(View.INVISIBLE);
            }
        });

        showNbrOfSamples = gui.status((screenWidth/17)*8);
        showNbrOfSamples.setGravity(Gravity.LEFT);
        popupLayout.addView(showNbrOfSamples);

        hideBtn = gui.arrow(android.R.drawable.arrow_down_float);
        hideBtn.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
        LinearLayout.LayoutParams arrowDownParams = new LinearLayout.LayoutParams(screenWidth/17, screenHeight/17);
        arrowDownParams.setMargins(0,0,0,25);
        hideBtn.setLayoutParams(arrowDownParams);
        arrowDown.addView(hideBtn);
        hideBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
                test.setVisibility(View.VISIBLE);
                showBtn.setVisibility(View.VISIBLE);
            }
        });


        TextView secondTextView = new TextView(this);
        textViewParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        secondTextView.setLayoutParams(textViewParams);
        arrowDown.addView(secondTextView);


        for (int c = 0; c < buttons.length; c++) {
            final int finalInt = c;
            buttons[c].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (chSeries[finalInt] != null) {
                        if (channelStrings[finalInt].equals(activatedChannels[finalInt])) {
                            if (buttons[finalInt].getTag() == "off") {
                                graph.addSeries(chSeries[finalInt]);
                                graph.addSeries(chSeriesMin[finalInt]);
                                graph.addSeries(chSeriesMax[finalInt]);

                                graph.getViewport().setYAxisBoundsManual(true);
                                if (maxValue[finalInt] > savedMaximum) {
                                    savedMaximum = maxValue[finalInt];
                                    graph.getViewport().setMaxY(savedMaximum + 5);
                                } else {
                                    graph.getViewport().setMaxY(savedMaximum + 5);
                                }
                                if (minValue[finalInt] < savedMinimum) {
                                    savedMinimum = minValue[finalInt];
                                    graph.getViewport().setMinY(savedMinimum - 5);
                                } else {
                                    graph.getViewport().setMinY(savedMinimum - 5);
                                }

                                buttons[finalInt].setTag("on");
                                buttons[finalInt].setBackgroundResource(R.drawable.custom_setup_button_enabled);

                            } else {
                                graph.removeSeries(chSeries[finalInt]);
                                graph.removeSeries(chSeriesMin[finalInt]);
                                graph.removeSeries(chSeriesMax[finalInt]);

                                buttons[finalInt].setTag("off");
                                buttons[finalInt].setBackgroundResource(R.drawable.custom_main_button);

                                graph.getViewport().setYAxisBoundsManual(true);
                                if (maxValue[finalInt] == savedMaximum) {
                                    savedMaximum = 0;
                                    for (int i = 0; i < buttons.length; i++) {
                                        if (activatedChannels[i] != null) {
                                            if (buttons[i].getTag().equals("on")) {
                                                if (maxValue[i] > savedMaximum) {
                                                    savedMaximum = maxValue[i];
                                                    graph.getViewport().setMaxY(savedMaximum + 5);
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    graph.getViewport().setMaxY(savedMaximum + 5);
                                }
                                if (minValue[finalInt] == savedMinimum) {
                                    savedMinimum = 0;
                                    for (int i = 0; i < buttons.length; i++) {
                                        if (activatedChannels[i] != null) {
                                            if (buttons[i].getTag().equals("on")) {
                                                if (minValue[i] < savedMinimum) {
                                                    savedMinimum = minValue[i];
                                                    graph.getViewport().setMinY(savedMinimum - 5);
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    graph.getViewport().setMinY(savedMinimum - 5);
                                }
                            }
                        }
                    }
                }
            });
        }

        clearGraph = gui.mediumBtn("Clear", R.drawable.custom_main_button, screenWidth/6, screenHeight/7);
        secondRow.addView(clearGraph);
        clearGraph.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearData();
            }
        });

        //set up function buttons (F1, F2, ... , F10)
        for(int i = 0; i < functionButtons.length; i++){
            functionButtons[i] = gui.smallBtn(functionButtonsNames[i], R.drawable.custom_main_button, screenWidth/10, screenHeight/10);
            if(i > 4){
                graphSecondRow.addView(functionButtons[i]);
            }else{
                graphFirstRow.addView(functionButtons[i]);
            }

            final int finalInt = i;

            functionButtons[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if(mode.equals("Slave")){
                        Toast toast = Toast.makeText(getApplicationContext(), "Choose Master in General Setup to use these functions.", Toast.LENGTH_SHORT);
                        toast.show();
                    }else {
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
                        plotGraphMaster(commands, true);
                    }
                }
            });
        }

        //Set up acceleration sensor
        SensorManager senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        senSensorManager.registerListener(new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                xAcc = event.values[0];
                yAcc = event.values[1];
                zAcc = event.values[2];
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        }, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, screenHeight/10);
        params.setMargins(0,0,10,0);

        //Set up single button
        single = gui.smallBtn("Single", R.drawable.custom_setup_button, screenWidth/10, screenHeight/10);
        single.setLayoutParams(params);
        graphFirstRow.addView(single);
        single.setTag("off");
        single.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mode.equals("Slave")){
                    if(stringData != null){
                        if(continuous.getTag().equals("off")){
                            single.setTag("on");
                            activateSlaveSingle();
                        }else if(continuous.getTag().equals("on")){
                            single.setTag("on");
                            continuous.setTag("off");
                            continuous.setBackgroundResource(R.drawable.custom_setup_button);
                            running = false;
                            activateSlaveSingle();
                        }
                    }else{
                        Toast toast = Toast.makeText(getApplicationContext(),
                                "Load a logfile before activating single!",
                                Toast.LENGTH_SHORT);
                        toast.show();
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
                            plotGraphMaster(commands, true);
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
                            plotGraphMaster(commands, true);
                        }
                    }
                }
            }
        });

        //Set up run/stop button
        continuous = gui.smallBtn("Run/Stop", R.drawable.custom_setup_button, screenWidth/10, screenHeight/10);
        continuous.setLayoutParams(params);
        graphSecondRow.addView(continuous);
        continuous.setTag("off");
        continuous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mode.equals("Slave")){
                    if(stringData != null) {
                        if(continuous.getTag().equals("off")){
                            continuous.setTag("on");
                            continuous.setBackgroundResource(R.drawable.custom_setup_button_enabled);
                            running = true;
                            activateSlaveContinuous();
                        }else if(continuous.getTag().equals("on")){
                            continuous.setTag("off");
                            continuous.setBackgroundResource(R.drawable.custom_setup_button);
                            running = false;
                        }
                    }else{
                        Toast toast = Toast.makeText(getApplicationContext(),
                                "Load a logfile before activating continuous!",
                                Toast.LENGTH_SHORT);
                        toast.show();
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

                            plotGraphMaster(commands, true);
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

        //Set up record button
        record = gui.smallBtn("Record", R.drawable.custom_setup_button, screenWidth/10, screenHeight/10);
        record.setLayoutParams(params);
        graphSecondRow.addView(record);
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

                            plotGraphMaster(commands, true);
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
        read = gui.smallBtn("Read", R.drawable.custom_setup_button, screenWidth/10, screenHeight/10);
        read.setLayoutParams(params);
        graphFirstRow.addView(read);
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
                        LinearLayout readOptions = new LinearLayout(getApplicationContext());
                        LinearLayout.LayoutParams readOptionsParams = new LinearLayout.LayoutParams(screenWidth/2, screenHeight/2);
                        readOptionsParams.weight = 1.0f;
                        readOptionsParams.gravity = Gravity.CENTER;
                        readOptions.setOrientation(LinearLayout.VERTICAL);
                        readOptions.setLayoutParams(readOptionsParams);
                        readOptions.setGravity(Gravity.CENTER);

                        final PopupWindow popupWindow = new PopupWindow(readOptions, screenWidth/2, screenHeight/2);
                        popupWindow.setFocusable(true);
                        popupWindow.setBackgroundDrawable(getResources().getDrawable(android.R.drawable.toast_frame));

                        final EditText nbrOfSamples = gui.editText();
                        readOptions.addView(nbrOfSamples);

                        Button completeReading = gui.mediumBtn("Read", android.R.drawable.btn_default, screenWidth/5, screenHeight/6);
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

                                    plotGraphMaster(commands, true);

                                    popupWindow.dismiss();
                                }
                            }
                        });
                        popupWindow.showAtLocation(test, Gravity.CENTER, 0, 0);
                    }
                }else{
                    Toast toast = Toast.makeText(getApplicationContext(),
                            "Choose Master as mode before reading!",
                            Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        });

        autosave = gui.autoSave("Autosave");
        graphSecondRow.addView(autosave);
        autosave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCheckBoxClicked(v);
            }
        });
        autosave.setButtonDrawable(android.R.drawable.checkbox_off_background);

        //run initiation command to datalogger
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

                test.setText("Connected to: " + device);
            } else {
                test.setText("Couldn't connect to: " + device);
            }
        }


        //Set up the inflater and adapter for load browser and inflater for save menu
        LayoutInflater inflater = getLayoutInflater();
        saveView = inflater.inflate(R.layout.save_view, null);
        autosaveView = inflater.inflate(R.layout.autosave_view, null);
        adapter = new ArrayAdapter<>(getApplicationContext(), R.layout.simple_list_item_1);

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

    public void clearData(){
        x = 0;

        dataIndex = 0;
        dateIndex = 0;

        if(FIFOLimit != 0){
            data = new double[8*FIFOLimit];
            dateAndTime = new String[FIFOLimit];
        }else {
            data = new double[320];
            dateAndTime = new String[40];
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

            save = gui.mediumBtn("Start Autosave", R.drawable.custom_main_button, screenWidth/2, screenHeight/10);
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

            popupWindow.showAtLocation(test, Gravity.CENTER, 0, 0);
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
                color[i] = fileInfo[6];
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

    public static double round(double value, int places) {
        if (places < 0){
            return value;
        }
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, BigDecimal.ROUND_HALF_UP);
        return bd.doubleValue();
    }


    @Override
    protected void onPause() {
        super.onPause();
        if(isConnected) {
            mBlue.Disconnect();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.graph_popup, menu);
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
            running = false;
            saveFile();
            return true;
        }else if(id == R.id.readDatabase){
            readFromDatabase();
            return true;
        }else if(id == R.id.saveDatabase) {
            saveToDatabase();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void readFromDatabase(){
        ConnectToDatabase connectToDatabase = new ConnectToDatabase(databaseIP, databasePORT);
        connectToDatabase.execute();

        try{
            Thread.sleep(300);
        }catch (Exception e){
            e.printStackTrace();
        }

        String data = connectToDatabase.retreiveDatabase(null, null, "$");

        String[] splitData = data.split("/");
        final String[] fileNames = new String[splitData.length/2];
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

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Graph.this);
        alertDialog.setView(loadView);
        alertDialog.setTitle("Database");
        alertDialog.setItems(fileNames, new DialogInterface.OnClickListener() {
            String[] copyFileNames = fileNames;
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //which is the item number in the list which you can use
                //to do things accordingly
                //clearData();
                plotGraph("null", "null", completeFiles[which]);
                test.setText("Read from: " + copyFileNames[which]);
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
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(screenWidth/10, screenHeight/10);
        params.setMargins(0,0,0,10);

        Button save = gui.mediumBtn("Save", R.drawable.custom_main_button, screenWidth/10, screenHeight/10);
        save.setLayoutParams(params);
        saveBtnLayout.addView(save);
        final EditText name = (EditText) popupView.findViewById(R.id.name);
        IP = (EditText) popupView.findViewById(R.id.ip);
        PORT = (EditText) popupView.findViewById(R.id.port);

        popupWindow.showAtLocation(test, Gravity.CENTER, 0, 0);

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
                test.setText(transferResults);
                connectToDatabase.endConnection();
                popupWindow.dismiss();
            }
        });
    }

    public void plotGraph(String filename, String filePath, String text){
        FileInputStream fis = null;
        try {
            if(!filename.equals("null")) {
                fis = new FileInputStream(filePath + File.separator + filename);
                DataInputStream dataIn = new DataInputStream(fis);

                int size = fis.available();
                byte[] buffer = new byte[size];
                dataIn.read(buffer);

                text = new String(buffer);
                test.setText("Read from: " + filename);

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
            x = nbrOfSlaveSamples;
            stringData = s.split("\\t");

            String[][] divideInputData = new String[9][stringData.length/9];

            int pos = 0;
            for(int i = 0; i < stringData.length/9; i++){
                for(int a = 0; a < 9; a++){
                    divideInputData[a][i] = stringData[pos];
                    pos++;
                }
            }

            //amountOfData = (stringData.length-(8+str.length))/8;

            multiChData = new double[8][nbrOfSlaveSamples];
            minValue = new double[8];
            maxValue = new double[8];

            for(int y = 0; y < nbrOfSlaveSamples; y++){
                for(int x = 0; x < 8; x++){
                    multiChData[x][y] = Double.parseDouble(divideInputData[x+1][y+1]);
                    if(maxLimit[x] == 0){
                        maxLimit[x] = 15;
                        maxValue[x] = maxLimit[x];
                    }else {
                        maxValue[x] = maxLimit[x];
                    }
                    minValue[x] = minLimit[x];

                    if (dataIndex == data.length) {
                        for (int dataPos = 0; dataPos < data.length - 1; dataPos++) {
                            data[dataPos] = data[dataPos + 1];
                        }
                        data[data.length - 1] = multiChData[x][y];
                    } else {
                        data[dataIndex] = multiChData[x][y];
                        dataIndex++;
                    }
                }
                if (dateIndex == dateAndTime.length) {
                    for (int datePos = 0; datePos < dateAndTime.length - 1; datePos++) {
                        dateAndTime[datePos] = dateAndTime[datePos + 1];
                    }
                    dateAndTime[dateAndTime.length - 1] = divideInputData[0][y+1];
                } else {
                    dateAndTime[dateIndex] = divideInputData[0][y+1];
                    dateIndex++;
                }
            }

            ch = new DataPoint[8][nbrOfSlaveSamples];
            min = new DataPoint[8][nbrOfSlaveSamples];
            max = new DataPoint[8][nbrOfSlaveSamples];

            for(double index = 0; index < nbrOfSlaveSamples; index++){
                for(int x = 0; x < ch.length; x++) {
                    ch[x][(int)index] = new DataPoint(index, multiChData[x][(int)index]);
                    min[x][(int)index] = new DataPoint(index, minValue[x]);
                    max[x][(int)index] = new DataPoint(index, maxValue[x]);
                }
            }

            for(int c = 0; c < chSeries.length; c++){
                if(chSeries[c] != null) {
                    //clearData();
                    chSeries[c].resetData(ch[c]);
                    chSeriesMax[c].resetData(max[c]);
                    chSeriesMin[c].resetData(min[c]);
                }else{
                    chSeries[c] = new LineGraphSeries(ch[c]);
                    chSeriesMax[c] = new LineGraphSeries(max[c]);
                    chSeriesMin[c] = new LineGraphSeries(min[c]);

                    if(color[c] != null) {
                        chSeries[c].setColor(getResources().getColor(channelColors[c]));
                        Paint paint = new Paint();
                        paint.setStyle(Paint.Style.STROKE);
                        paint.setStrokeWidth(10);
                        paint.setColor(getResources().getColor(channelColors[c]));
                        paint.setPathEffect(new DashPathEffect(new float[]{8, 5}, 0));
                        chSeriesMax[c].setCustomPaint(paint);
                        chSeriesMin[c].setCustomPaint(paint);
                    }
                }
            }

            setLegendRendererVisibility(View.VISIBLE);

            for(int i = 0; i < buttons.length; i++){
                if (channelStrings[i].equals(activatedChannels[i])) {
                    if(buttons[i].getTag().equals("on")) {
                        graph.addSeries(chSeries[i]);
                        graph.addSeries(chSeriesMax[i]);
                        graph.addSeries(chSeriesMin[i]);

                        graph.getViewport().setYAxisBoundsManual(true);
                        if(maxValue[i] > savedMaximum){
                            savedMaximum = maxValue[i];
                            graph.getViewport().setMaxY(savedMaximum);
                        }else{
                            graph.getViewport().setMaxY(savedMaximum);
                        }
                        if(minValue[i] < savedMinimum){
                            savedMinimum = minValue[i];
                            graph.getViewport().setMinY(savedMinimum);
                        }else{
                            graph.getViewport().setMinY(savedMinimum);
                        }

                        graph.getViewport().setXAxisBoundsManual(true);
                        if (nbrOfSlaveSamples > 40) {
                            graph.getViewport().setMaxX(nbrOfSlaveSamples);
                        } else if (x > 35) {
                            graph.getViewport().setMinX(x - 35);
                            graph.getViewport().setMaxX(x + 5);
                        } else {
                            graph.getViewport().setMinX(0);
                            graph.getViewport().setMaxX(40);
                        }

                        graph.getViewport().setScalable(true);
                    }
                }
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public void customLegendRenderer(){
        if(customLegendRenderer == null) {
            int count = 0;
            for (int i = 0; i < activatedChannels.length; i++) {
                if (activatedChannels[i] != null) {
                    count++;
                }
            }

            String[] chNames = new String[count];
            int[] chColors = new int[count];
            count = 0;
            for (int i = 0; i < activatedChannels.length; i++) {
                if (activatedChannels[i] != null) {
                    if(!channelNames[i].equals("null")){
                        chNames[count] = channelNames[i];
                    }else{
                        chNames[count] = channelDefaultNames[i];
                    }
                    chColors[count] = getResources().getColor(channelColors[i]);
                    count++;
                }
            }

            customLegendRenderer = new ListView(this);
            CustomListAdapter adapter = new CustomListAdapter(this, chNames, chColors);
            customLegendRenderer.setAdapter(adapter);
            customLegendRenderer.setBackgroundResource(R.drawable.custom_background);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(screenWidth/7, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(0,20,20,0);
            customLegendRenderer.setLayoutParams(params);

            customLayout.addView(customLegendRenderer);
            customLayout.setGravity(Gravity.RIGHT);
        }
    }

    public void setLegendRendererVisibility(int visibility){
        if(customLegendRenderer != null){
            customLegendRenderer.setVisibility(visibility);
        }
    }

    public void activateSlaveSingle(){
        if (x == nbrOfSlaveSamples) {
            clearData();
            ch = new DataPoint[8][1];
            max = new DataPoint[8][1];
            min = new DataPoint[8][1];
            for(double index = 0; index < x+1; index++){
                for(int x = 0; x < ch.length; x++) {
                    ch[x][(int)index] = new DataPoint(index, multiChData[x][(int)index]);
                    min[x][(int)index] = new DataPoint(index, minValue[x]);
                    max[x][(int)index] = new DataPoint(index, maxValue[x]);
                }
            }
        }

        for (int i = 0; i < chSeries.length; i++) {
            if (x == 0) {
                if(chSeries[i] != null) {
                    chSeries[i].resetData(ch[i]);
                    chSeriesMax[i].resetData(max[i]);
                    chSeriesMin[i].resetData(min[i]);
                }
            } else {
                chSeries[i].appendData(new DataPoint(x, multiChData[i][x]), false, 40);
                chSeriesMax[i].appendData(new DataPoint(x, maxValue[i]), false, 40);
                chSeriesMin[i].appendData(new DataPoint(x, minValue[i]), false, 40);
            }
        }
        x++;
    }

    public void activateSlaveContinuous(){
        flow = new Thread(new Runnable(){
            @Override
            public void run(){
                if(running) {
                    if (x == nbrOfSlaveSamples) {
                        clearData();
                        ch = new DataPoint[8][1];
                        max = new DataPoint[8][1];
                        min = new DataPoint[8][1];
                        for(double index = 0; index < x+1; index++){
                            for(int x = 0; x < ch.length; x++) {
                                ch[x][(int)index] = new DataPoint(index, multiChData[x][(int)index]);
                                min[x][(int)index] = new DataPoint(index, minValue[x]);
                                max[x][(int)index] = new DataPoint(index, maxValue[x]);
                            }
                        }
                    }

                    for (int i = 0; i < chSeries.length; i++) {
                        if (x == 0) {
                            if(chSeries[i] != null) {
                                chSeries[i].resetData(ch[i]);
                                chSeriesMax[i].resetData(max[i]);
                                chSeriesMin[i].resetData(min[i]);
                            }
                        } else {
                            chSeries[i].appendData(new DataPoint(x, multiChData[i][x]), false, 40);
                            chSeriesMax[i].appendData(new DataPoint(x, maxValue[i]), false, 40);
                            chSeriesMin[i].appendData(new DataPoint(x, minValue[i]), false, 40);
                        }
                    }
                    x++;
                    flowHandler.postDelayed(this, 500);
                }
            }
        });
        flowHandler.postDelayed(flow, 500);
    }

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

    public void plotGraphMaster(final String[] commands, boolean run) {
        running = run;
        flow = new Thread(new Runnable(){
            @Override
            public void run(){
                if(running) {

                    for(int i = 0; i < commands.length; i++){
                        channelData = sendAndReturn(commands[i]);
                    }

                    if (channelData.length == (nbrOfMasterSamples * 8 + 1)) {
                        test.setText("Data collected!");

                        multiChData = new double[8][nbrOfMasterSamples];

                        ch = new DataPoint[8][nbrOfMasterSamples];
                        min = new DataPoint[8][nbrOfMasterSamples];
                        max = new DataPoint[8][nbrOfMasterSamples];

                        int k = 0;
                        for (int c = 0; c < 8; c++) {
                            ck[c] = multipleCkVal[k][c] + (multipleCkVal[k + 1][c] * Double.parseDouble(channelData[c])) +
                                    (multipleCkVal[k + 2][c] * Math.pow(Double.parseDouble(channelData[c]), 2)) +
                                    (multipleCkVal[k + 3][c] * Math.pow(Double.parseDouble(channelData[c]), 3)) +
                                    (multipleCkVal[k + 4][c] * Math.pow(Double.parseDouble(channelData[c]), 4)) +
                                    (multipleCkVal[k + 5][c] * Math.log(Double.parseDouble(channelData[c]) + 0.000000000001)) +
                                    (multipleCkVal[k + 6][c] * Math.exp(multipleCkVal[k + 7][c] * Double.parseDouble(channelData[c]))) +
                                    (multipleCkVal[k + 8][c] * Math.pow(Double.parseDouble(channelData[c]), multipleCkVal[k + 9][c] + 0.000000000001));
                        }

                        /*------------------------------------------------*/

                        for (int a = 0; a < nbrOfMasterSamples; a++) {
                            for (int i = 0; i < 8; i++) {
                                multiChData[i][a] = round(ck[i], decimals[i]);
                                if (maxLimit[i] == 0) {
                                    maxLimit[i] = 15;
                                    maxValue[i] = maxLimit[i];
                                } else {
                                    maxValue[i] = maxLimit[i];
                                }
                                minValue[i] = minLimit[i];

                                if (dataIndex == data.length) {
                                    for (int dataPos = 0; dataPos < data.length - 1; dataPos++) {
                                        data[dataPos] = data[dataPos + 1];
                                    }
                                    data[data.length - 1] = multiChData[i][a];
                                } else {
                                    data[dataIndex] = multiChData[i][a];
                                    dataIndex++;
                                }

                                ch[i][a] = new DataPoint(x, multiChData[i][a]);
                                min[i][a] = new DataPoint(x, minValue[i]);
                                max[i][a] = new DataPoint(x, maxValue[i]);
                            }

                            SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,S");//dd/MM/yyyy
                            Date now = new Date();
                            String strDate = sdfDate.format(now);
                            if (dateIndex == dateAndTime.length) {
                                for (int datePos = 0; datePos < dateAndTime.length - 1; datePos++) {
                                    dateAndTime[datePos] = dateAndTime[datePos + 1];
                                }
                                dateAndTime[dateAndTime.length - 1] = strDate;
                            } else {
                                dateAndTime[dateIndex] = strDate;
                                dateIndex++;
                            }

                            try{
                                String text = "";
                                if(autosaveIndex == dateAndTime.length * autosaveFiles){
                                    nextAutosaveFilename = autosaveFilename + " " + dateAndTime[dateAndTime.length-1];
                                    FileOutputStream fos = new FileOutputStream(dataDirPath + File.separator + nextAutosaveFilename);

                                    text = "Date/Time" + "\t" + channelSaveNames[0] + "\t" +
                                            channelSaveNames[1] + "\t" + channelSaveNames[2] + "\t" +
                                            channelSaveNames[3] + "\t" + channelSaveNames[4] + "\t" +
                                            channelSaveNames[5] + "\t" + channelSaveNames[6] + "\t" +
                                            channelSaveNames[7] + "\n" + dateAndTime[dateAndTime.length - 1] +
                                            "\t" + data[data.length - 8] + "\t" + data[data.length - 7] +
                                            "\t" + data[data.length - 6] + "\t" + data[data.length - 5] +
                                            "\t" + data[data.length - 4] + "\t" + data[data.length - 3] +
                                            "\t" + data[data.length - 2] + "\t" + data[data.length - 1] + "\n";

                                    fos.write(text.getBytes());
                                    fos.close();

                                    autosaveIndex++;
                                    autosaveFiles++;
                                }else if(autosaveIndex > dateAndTime.length) {
                                    FileOutputStream fos = new FileOutputStream(dataDirPath + File.separator + nextAutosaveFilename, true);

                                    text = dateAndTime[dateAndTime.length - 1] +
                                            "\t" + data[data.length - 8] + "\t" + data[data.length - 7] +
                                            "\t" + data[data.length - 6] + "\t" + data[data.length - 5] +
                                            "\t" + data[data.length - 4] + "\t" + data[data.length - 3] +
                                            "\t" + data[data.length - 2] + "\t" + data[data.length - 1] + "\n";

                                    fos.write(text.getBytes());
                                    fos.close();

                                    autosaveIndex++;
                                }else{
                                    FileOutputStream fos = new FileOutputStream(dataDirPath + File.separator + autosaveFilename, true);

                                    text = dateAndTime[dateIndex - 1] + "\t" + data[dataIndex - 8] +
                                            "\t" + data[dataIndex - 7] + "\t" + data[dataIndex - 6] +
                                            "\t" + data[dataIndex - 5] + "\t" + data[dataIndex - 4] +
                                            "\t" + data[dataIndex - 3] + "\t" + data[dataIndex - 2] +
                                            "\t" + data[dataIndex - 1] + "\n";

                                    fos.write(text.getBytes());
                                    fos.close();

                                    autosaveIndex++;
                                }

                                adapter.notifyDataSetChanged();
                            }catch (IOException e){
                                e.printStackTrace();
                            }

                            x++;
                            nbrOfSamplesCollected++;
                        }

                        showNbrOfSamples.setText("Number of samples collected: " + nbrOfSamplesCollected);

                        /*------------------------------------------------*/

                        if(x > 35){
                            graph.getViewport().setXAxisBoundsManual(true);
                            graph.getViewport().setMinX(x - 35);
                            graph.getViewport().setMaxX(x + 5);
                        }

                        /*------------------------------------------------*/

                        for (int c = 0; c < chSeries.length; c++) {
                            if (activatedChannels[c] != null && chSeries[c] == null) {
                                chSeries[c] = new LineGraphSeries(ch[c]);
                                chSeriesMax[c] = new LineGraphSeries(max[c]);
                                chSeriesMin[c] = new LineGraphSeries(min[c]);

                                if(channelNames[c] != null) {
                                    chSeries[c].setTitle(channelNames[c]);
                                }else{
                                    chSeries[c].setTitle(channelStrings[c]);
                                }
                                graph.getLegendRenderer().setVisible(true);
                                graph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);

                                if (color[c] != null) {
                                    chSeries[c].setColor(getResources().getColor(channelColors[c]));
                                    Paint paint = new Paint();
                                    paint.setStyle(Paint.Style.STROKE);
                                    paint.setStrokeWidth(10);
                                    paint.setColor(getResources().getColor(channelColors[c]));
                                    paint.setPathEffect(new DashPathEffect(new float[]{8, 5}, 0));
                                    chSeriesMax[c].setCustomPaint(paint);
                                    chSeriesMin[c].setCustomPaint(paint);
                                }

                                graph.addSeries(chSeries[c]);
                                graph.addSeries(chSeriesMax[c]);
                                graph.addSeries(chSeriesMin[c]);

                                graph.getViewport().setYAxisBoundsManual(true);
                                if (maxValue[c] > savedMaximum) {
                                    savedMaximum = maxValue[c];
                                    graph.getViewport().setMaxY(savedMaximum + 5);
                                } else {
                                    graph.getViewport().setMaxY(savedMaximum + 5);
                                }
                                if (minValue[c] < savedMinimum) {
                                    savedMinimum = minValue[c];
                                    graph.getViewport().setMinY(savedMinimum - 5);
                                } else {
                                    graph.getViewport().setMinY(savedMinimum - 5);
                                }

                                graph.getViewport().setXAxisBoundsManual(true);
                                if (nbrOfMasterSamples > 40) {
                                    graph.getViewport().setMaxX(nbrOfMasterSamples);
                                } else if (x > 35) {
                                    graph.getViewport().setMinX(x - 35);
                                    graph.getViewport().setMaxX(x + 5);
                                } else {
                                    graph.getViewport().setMinX(0);
                                    graph.getViewport().setMaxX(40);
                                }

                                graph.getViewport().setScalable(true);
                            }else if(activatedChannels[c] != null){
                                for(int i = 0; i < nbrOfMasterSamples; i++) {
                                    chSeries[c].appendData(ch[c][i], false, 30);
                                    chSeriesMax[c].appendData(max[c][i], false, 40);
                                    chSeriesMin[c].appendData(min[c][i], false, 40);
                                }
                            }

                            if(activatedChannels[c] != null) {
                                for (int i = 0; i < nbrOfMasterSamples; i++) {
                                    if (multiChData[c][i] > maxValue[c]) {
                                        savedXAccValues[c][alarmIndex[c]] = xAcc;
                                        savedYAccValues[c][alarmIndex[c]] = yAcc;
                                        savedZAccValues[c][alarmIndex[c]] = zAcc;
                                        savedXValue[c][alarmIndex[c]] = x;
                                        savedYValue[c][alarmIndex[c]] = multiChData[c][a];
                                        alarmIndex[c]++;
                                    } else if (multiChData[c][i] < minValue[c]) {
                                        savedXAccValues[c][alarmIndex[c]] = xAcc;
                                        savedYAccValues[c][alarmIndex[c]] = yAcc;
                                        savedZAccValues[c][alarmIndex[c]] = zAcc;
                                        savedXValue[c][alarmIndex[c]] = x;
                                        savedYValue[c][alarmIndex[c]] = multiChData[c][a];
                                        alarmIndex[c]++;
                                    } else {
                                        alarmIndex[c] = 0;
                                    }

                                    if (alarmIndex[c] == alarmSamples[c]) {
                                        String number = String.valueOf(phoneNbr);

                                        SmsManager sms = SmsManager.getDefault();
                                        String allTheErrors = "";
                                        for (int z = 0; z < alarmIndex[c]; z++) {
                                            allTheErrors += "(" + savedXValue[c][z] + "," + savedYValue[c][z] + ") ";
                                        }

                                        if (number.equals("0")) {
                                            Toast toast = Toast.makeText(getApplicationContext(),
                                                    "Please choose a phone number in Setup!",
                                                    Toast.LENGTH_SHORT);
                                            toast.show();

                                            NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext());
                                            builder.setSmallIcon(R.drawable.notification_icon);
                                            builder.setContentTitle("An Alarm has occurred in XViewer!");
                                            if (!channelNames[c].equals("null")) {
                                                builder.setContentText(channelNames[c] + ": " + allTheErrors);
                                            } else {
                                                builder.setContentText(channelStrings[c] + ": " + allTheErrors);
                                            }

                                            NotificationManager mNotifyMgr =
                                                    (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                                            mNotifyMgr.notify(001, builder.build());
                                        } else if (!channelNames[c].equals("null")) {
                                            sms.sendTextMessage(number, null, "An Error occurred at " +
                                                    channelNames[c] + ": " + allTheErrors, null, null);
                                        } else {
                                            sms.sendTextMessage(number, null, "An Error occured at " +
                                                    channelStrings[c] + ": " + allTheErrors, null, null);
                                        }
                                        alarmIndex[c] = 0;

                                        saveAlarmFile();
                                        running = false;
                                        continuous.setTag("off");
                                        continuous.setBackgroundResource(R.drawable.custom_setup_button);

                                        /*
                                        String testAcc = "";
                                        for (int b = 0; b < savedYValue[c].length; b++) {
                                            testAcc += savedXAccValues[c][b] + "\n";
                                            testAcc += savedYAccValues[c][b] + "\n";
                                            testAcc += savedZAccValues[c][b] + "\n\n";
                                        }

                                        test.setText(testAcc);*/
                                    }
                                }
                            }
                        }

                        /*------------------------------------------------*/

                        if (record.getTag().equals("on")) {
                            record.setTag("off");
                            running = false;
                        }else if(single.getTag().equals("on")){
                            single.setTag("off");
                            running = false;
                        }else if(read.getTag().equals("on")){
                            read.setTag("off");
                            running = false;
                        }

                    } else {
                        test.setText("Failure in collecting data, try again!");
                    }
                    flowHandler.postDelayed(this, 100);
                }
            }
        });
        flowHandler.postDelayed(flow, 100);
    }

    public void printFileDirectory(final String filePath){
        File file = new File(filePath);

        adapter = new ArrayAdapter<>(getApplicationContext(), R.layout.simple_list_item_1, file.list());
        adapter.notifyDataSetChanged();

        files = new String[adapter.getCount()];

        for(int i = 0; i < adapter.getCount(); i++){
            files[i] = adapter.getItem(i);
        }

        View loadView = getLayoutInflater().inflate(R.layout.load_view, null);

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Graph.this);
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
                    test.setText(files[which]);
                    printFileDirectory(updatedFilePath);
                } else {
                    setLegendRendererVisibility(View.INVISIBLE);
                    clearData();
                    plotGraph(files[which], filePath, "null");
                }
            }
        });

        AlertDialog alert = alertDialog.create();
        alert.show();
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

        clearData();
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
        popupWindow.showAtLocation(test, Gravity.CENTER, 0, 0);
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
        test.setText(text);

        //kanske clearData();

        return text;
    }
}

class ConnectToDatabase extends AsyncTask<Void, Void, Void>{

    String dstAdr, bytesReceived;
    int dstPort;
    int count;
    StringBuilder dataReceived = new StringBuilder();
    Socket clientConnection = null;
    private InputStream inFromServer = null;
    private OutputStream outToServer = null;
    private DataOutputStream out = null;
    private DataInputStream in = null;

    ConnectToDatabase(String dstAdr, int dstPort){
        this.dstAdr = dstAdr;
        this.dstPort = dstPort;
    }

    @Override
    protected Void doInBackground(Void... params) {

        try {
            clientConnection = new Socket(dstAdr, dstPort);
            inFromServer = clientConnection.getInputStream();
            outToServer = clientConnection.getOutputStream();
            out = new DataOutputStream(outToServer);
            in = new DataInputStream(inFromServer);

            byte[] buffer = new byte[1024];
            count = 0;
            while (count != -1) {
                count = in.read(buffer);
                bytesReceived = new String(buffer, 0, count);
                dataReceived.append(bytesReceived);
            }
        }catch (IOException e){
            e.printStackTrace();
        }

        return null;
    }

    public String retreiveDatabase(File fileToRetreive, String filename, String mode){
        String data = "test";
        if(out != null){
            try{
                if(mode.equals("$")) {
                    out.writeUTF(mode);
                    out.flush();

                    Thread.sleep(200);

                    while(true){
                        Thread.sleep(200);
                        String input = getData().toString();

                        String[] splitData = input.split("&");
                        if(splitData.length > 1) {
                            if (splitData[1].equals("OK")) {
                                data = splitData[0];
                                resetStringBuilder();
                                break;
                            }
                        }
                    }
                }else{
                    int filenameLength = filename.length();
                    String fileLength = String.valueOf(fileToRetreive.length());

                    out.writeUTF(fileLength);
                    out.flush();

                    Thread.sleep(200);

                    out.writeInt(filenameLength);
                    out.flush();

                    Thread.sleep(200);

                    out.writeBytes(filename);
                    out.flush();

                    Thread.sleep(200);

                    out.write("$".getBytes());
                    out.flush();

                    Thread.sleep(200);

                    if (in.readInt() < 0) {
                        Thread.sleep(200);
                        data = getData().toString();
                        resetStringBuilder();
                    }
                }
            }catch (IOException | InterruptedException e){
                e.printStackTrace();
            }
        }else{
            data = "Error, check server connection.";
        }
        return data;
    }

    public String sendFileToDatabase(File fileToSend, String filename){
        String results = "";
        if(out != null) {
            try {
                int filenameLength = filename.length();
                String fileLength = String.valueOf(fileToSend.length());

                out.writeUTF(fileLength);
                out.flush();

                Thread.sleep(200);

                out.writeInt(filenameLength);
                out.flush();

                Thread.sleep(200);

                out.writeBytes(filename);
                out.flush();

                Thread.sleep(200);

                byte[] dataToSend = new byte[(int) fileToSend.length()];

                InputStream in = new FileInputStream(fileToSend);

                int count;
                while ((count = in.read(dataToSend)) >= 0) {
                    out.write(dataToSend, 0, count);
                }
                out.flush();
                in.close();

                results = "File succesfully transfered.";
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
        }else{
            results = "Error, check server connection.";
        }
        endConnection();
        return results;
    }

    public StringBuilder getData(){
        return dataReceived;
    }

    public void resetStringBuilder(){
        dataReceived.delete(0, dataReceived.length());
    }

    public void endConnection(){
        try {
            if(out != null) {
                out.close();
            }
            if(in != null) {
                in.close();
            }
            if(outToServer != null){
                outToServer.close();
            }
            if(inFromServer != null){
                inFromServer.close();
            }
            if(clientConnection != null) {
                clientConnection.close();
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
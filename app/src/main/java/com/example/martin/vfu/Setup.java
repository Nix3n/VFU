package com.example.martin.vfu;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.view.ViewGroup.LayoutParams;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Martin on 2015-11-26.
 */
public class Setup extends Activity {

    Button[] buttons = new Button[8];

    Button saveSetup, generalSetup;

    String[] activatedChannels = new String[8];

    String[] channelStrings = new String[]{"Channel1", "Channel2", "Channel3", "Channel4",
            "Channel5", "Channel6", "Channel7", "Channel8"};

    String[] graphChNames = new String[]{"Ch1", "Ch2", "Ch3", "Ch4",
            "Ch5", "Ch6", "Ch7", "Ch8"};

    String[] defaultColors = new String[]{"Red", "Green", "Blue", "Yellow",
            "Magenta", "Cyan", "Brown", "Orange"};

    String[] channelNames = new String[8];
    int[] decimals = new int[8];
    int[] max = new int[8];
    int[] min = new int[8];
    int[] alarmSamples = new int[8];
    String[] color = new String[8];

    int[] channelID = new int[]{0, 1, 2, 3, 4, 5, 6, 7};

    String inputFromFile;

    GUI gui;
    private int screenWidth;
    private int screenHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        gui = new GUI(getApplicationContext());

        screenWidth = gui.getWidth();
        screenHeight = gui.getHeight();

        LinearLayout setupLayout = (LinearLayout) findViewById(R.id.setupLayout);
        setupLayout.setBackgroundResource(R.drawable.custom_background);

        LinearLayout setupFirstRow = (LinearLayout) findViewById(R.id.setupFirstRow);
        LinearLayout setupSecondRow = (LinearLayout) findViewById(R.id.setupSecondRow);
        LinearLayout setupThirdRow = (LinearLayout) findViewById(R.id.setupThirdRow);
        LinearLayout setupFourthRow = (LinearLayout) findViewById(R.id.setupFourthRow);
        LinearLayout setupFifthRow = (LinearLayout) findViewById(R.id.setupFifthRow);
        LinearLayout setupSixthRow = (LinearLayout) findViewById(R.id.setupSixthRow);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams((screenWidth/3)*2 + 20, LayoutParams.WRAP_CONTENT);
        params.setMargins(10,10,10,10);

        generalSetup = gui.largeBtn("General Setup");
        generalSetup.setBackgroundResource(R.drawable.custom_main_button);
        generalSetup.setLayoutParams(params);
        setupFifthRow.addView(generalSetup);
        saveSetup = gui.largeBtn("Save Setup");
        saveSetup.setBackgroundResource(R.drawable.custom_main_button);
        saveSetup.setLayoutParams(params);
        setupSixthRow.addView(saveSetup);

        params = new LinearLayout.LayoutParams(screenWidth/3, LayoutParams.WRAP_CONTENT);
        params.setMargins(10,10,10,10);

        for(int i = 0; i < channelStrings.length; i++) {
            //Load channelsettings
            loadSettings(i, channelStrings[i]);
        }

        for(int i = 0; i < activatedChannels.length; i++){
            buttons[i] = gui.largeBtn(graphChNames[i]);
            buttons[i].setBackgroundResource(R.drawable.custom_setup_button);
            buttons[i].setLayoutParams(params);
            buttons[i].setTag("off");
            if(i < 2){
                setupFirstRow.addView(buttons[i]);
            }else if(i > 1 && i < 4){
                setupSecondRow.addView(buttons[i]);
            }else if(i > 3 && i < 6){
                setupThirdRow.addView(buttons[i]);
            }else if(i > 5 && i < 8){
                setupFourthRow.addView(buttons[i]);
            }

            if(activatedChannels[i] != null){
                buttons[i].setBackgroundResource(R.drawable.custom_setup_button_enabled);
                buttons[i].setTag("on");
                if(!channelNames[i].equals("null")) {
                    buttons[i].setText(channelNames[i]);
                }
            }
        }
        enableButtons();
    }

    public void loadSettings(int i, String filename){
        FileInputStream fis = null;
        try {
            fis = openFileInput(filename);
            DataInputStream dataIn = new DataInputStream(fis);

            byte[] buffer = new byte[fis.available()];
            dataIn.read(buffer);

            inputFromFile = new String(buffer);

            String[] channelInfo = inputFromFile.split("\\r?\\n");

            if(channelInfo[0].equals(channelStrings[i])){
                activatedChannels[i] = channelInfo[0];
                channelNames[i] = channelInfo[1];
                decimals[i] = Integer.parseInt(channelInfo[2]);
                max[i] = Integer.parseInt(channelInfo[3]);
                min[i] = Integer.parseInt(channelInfo[4]);
                alarmSamples[i] = Integer.parseInt(channelInfo[5]);
                color[i] = channelInfo[6];
            }else if(channelInfo[0].equals("deactivated")){
                activatedChannels[i] = null;
            }

            fis.close();
            dataIn.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void enableButtons(){

        final Intent channelSetup = new Intent(this, ChannelSetup.class);

        for(int i = 0; i < 8; i++){
            final int finalInt = i;

            buttons[i].setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    if(buttons[finalInt].getTag().equals("off")){
                        buttons[finalInt].setTag("on");
                        buttons[finalInt].setBackgroundResource(R.drawable.custom_setup_button_enabled);
                        activatedChannels[finalInt] = channelStrings[finalInt];

                        saveDefaultSettings(finalInt, channelStrings[finalInt]);

                        Bundle bOut = new Bundle();

                        bOut.putInt("channelID", channelID[finalInt]);
                        bOut.putString("graphChNames", graphChNames[finalInt]);

                        channelSetup.putExtras(bOut);
                        startActivity(channelSetup);
                    }else{
                        LayoutInflater layoutInflater
                                = (LayoutInflater)getBaseContext()
                                .getSystemService(LAYOUT_INFLATER_SERVICE);
                        final View popupView = layoutInflater.inflate(R.layout.channel_edit, null);
                        final PopupWindow popupWindow = new PopupWindow(
                                popupView,
                                LayoutParams.WRAP_CONTENT,
                                LayoutParams.WRAP_CONTENT);

                        popupWindow.setFocusable(true);
                        popupWindow.setBackgroundDrawable(getResources().getDrawable(R.drawable.custom_black_border_no_edges));

                        LinearLayout btnLayout = (LinearLayout) popupView.findViewById(R.id.contentLayout);
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(screenWidth - screenWidth/3, LayoutParams.WRAP_CONTENT);
                        params.setMargins(0,10,0,0);

                        Button edit = gui.largeBtn("Edit");
                        edit.setBackgroundResource(R.drawable.custom_main_button);
                        edit.setLayoutParams(params);
                        btnLayout.addView(edit);
                        edit.setOnClickListener(new Button.OnClickListener(){
                            @Override
                            public void onClick(View v) {
                                Bundle bOut = new Bundle();

                                bOut.putInt("channelID", channelID[finalInt]);
                                bOut.putString("graphChNames", graphChNames[finalInt]);

                                channelSetup.putExtras(bOut);
                                startActivity(channelSetup);
                            }
                        });

                        Button deactivate = gui.largeBtn("Deactivate");
                        deactivate.setBackgroundResource(R.drawable.custom_main_button);
                        deactivate.setLayoutParams(params);
                        btnLayout.addView(deactivate);
                        deactivate.setOnClickListener(new Button.OnClickListener(){
                            @Override
                            public void onClick(View v){
                                buttons[finalInt].setTag("off");
                                buttons[finalInt].setBackgroundResource(R.drawable.custom_setup_button);
                                activatedChannels[finalInt] = null;
                                removeButton(finalInt);
                                popupWindow.dismiss();
                            }
                        });

                        popupWindow.showAsDropDown(buttons[finalInt], 50, -30);
                    }
                }
            });
        }

        saveSetup.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                startActivity(intent);
            }
        });

        generalSetup.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(getApplicationContext(), GeneralSetup.class);

                startActivity(intent);
            }
        });
    }

    public void removeButton(int i){
        String filename = channelStrings[i];
        String deactivated = "deactivated";
        FileOutputStream outputStream;

        try {
            outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
            outputStream.write(deactivated.getBytes());
            outputStream.close();
        } catch (Exception e) {
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
                    "" + "\n" + "Continuous growth" + "\n" + "Slave" + "\n";
        }

        try {
            outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
            outputStream.write(settings.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

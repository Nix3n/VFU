package com.example.martin.vfu;

/**
 * Created by Martin on 2015-11-26.
 */

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class BluetoothArduino extends Thread {
    private BluetoothAdapter mBlueAdapter = null;
    private BluetoothSocket mBlueSocket = null;
    private BluetoothDevice mBlueUnit = null;

    OutputStream mOut;
    InputStream mIn;
    private boolean unitFound = false;
    private boolean connected = false;
    private String unitName;
    private String TAG = "BluetoothConnector";

    String receivedMessage = "";
    StringBuilder stringBuilder = new StringBuilder();
    int bytes;

    boolean collectingData;

    public BluetoothArduino(String Name){
        try {
            for(int i = 0; i < 2048; i++){
                List<String> mMessages = new ArrayList<String>();
                mMessages.add("");
            }
            unitName = Name;
            mBlueAdapter = BluetoothAdapter.getDefaultAdapter();
            if (mBlueAdapter == null) {
                LogError("\t\t[#]Phone does not support bluetooth!!");
                return;
            }
            if (!isBluetoothEnabled()) {
                LogError("[#]Bluetooth is not activated!!");
            }

            Set<BluetoothDevice> paired = mBlueAdapter.getBondedDevices();
            if (paired.size() > 0) {
                for (BluetoothDevice d : paired) {
                    if (d.getName().equals(unitName)) {
                        mBlueUnit = d;
                        unitFound = true;
                        break;
                    }
                }
            }

            if (!unitFound)
                LogError("\t\t[#]There is no device paired!!");

        }catch (Exception e){
            LogError("\t\t[#]Error creating Bluetooth! : " + e.getMessage());
        }
    }

    public boolean isBluetoothEnabled(){
        return mBlueAdapter.isEnabled();
    }

    public boolean Connect(){
        if(!unitFound)
            return false;
        try{
            LogMessage("\t\tConnecting to " + unitName + "...");

            UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
            mBlueSocket = mBlueUnit.createRfcommSocketToServiceRecord(uuid);
            if(!mBlueSocket.isConnected()) {
                Thread.sleep(300);
                mBlueSocket.connect();
            }
            mOut = mBlueSocket.getOutputStream();
            mIn = mBlueSocket.getInputStream();
            connected = true;
            collectingData = true;
            this.start();
            LogMessage("\t\t\t" + mBlueAdapter.getName());
            LogMessage("\t\tOk!!");
            return true;

        }catch (Exception closeException){
            LogError("\t\t[#]Error while connecting: " + closeException.getMessage());
            try {
                mBlueSocket.close();
            }catch (IOException error){
                LogError("\t\t[#]Error while closing socket: " + error.getMessage());
            }
            return false;
        }
    }

    public boolean isConnected(){
        boolean isConnected = false;

        if(mBlueSocket != null){
            isConnected = mBlueSocket.isConnected();
        }

        return isConnected;
    }

    public void sendMessage(String msg){
        try {
            if(connected) {
                mOut.write(msg.getBytes());
            }
        } catch (IOException e){
            LogError("->[#]Error while sending message: " + e.getMessage());
        }
    }

    public void run(){
        // Keep looping to listen for received messages
        byte[] buffer = new byte[1024];

        while (true) {
            try {
                bytes = mIn.read(buffer);            //read bytes from input buffer
                receivedMessage = new String(buffer, 0, bytes);
                stringBuilder.append(receivedMessage);
            } catch (IOException e) {
                break;
            }
        }
    }

    public void resetStringBuilder(){
        stringBuilder.delete(0, stringBuilder.length());
    }

    public StringBuilder bluetoothInput(){
        return stringBuilder;
    }

    private void LogMessage(String msg){
        Log.d(TAG, msg);
    }

    private void LogError(String msg){
        Log.e(TAG, msg);
    }

    public void Disconnect(){
        try {
            mIn.close();
            mOut.close();
            mBlueSocket.close();
            Log.i("DC", "Disconnected from: " + unitName);
        }catch (Exception e){
            Log.i("DC", "Couldn't disconnect from: " + unitName);
        }
    }
}

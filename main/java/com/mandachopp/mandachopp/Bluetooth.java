/*****************************************************************************
 PROJECT: MANDA CHOPP
 AUTHOR : JOSÉ ALBERTO GURGEL CARDOSO NETO
 DATE   : 06/02/2018
 PLACE  : MANAUS, AMAZONAS
 /*****************************************************************************/
package com.mandachopp.mandachopp;

import java.util.UUID;
import java.util.Arrays;
import android.util.Log;
import android.os.Handler;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import android.os.ParcelUuid;
import android.content.Intent;
import android.content.Context;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import android.bluetooth.BluetoothSocket;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothAdapter;

public class Bluetooth
{
    /** Constants */
    private static final String            DEVICE                    = "TAP1";
    private static final int               PASSWORD                  = 1234;
    private static final String            ARDUINO_BLUETOOTH_ADDRESS = "20:14:04:18:29:85";
    public  static final int               MAX_DATA_BYTES            = 1024;
    private static final long              MILLIS_TO_GET_NEW_READ    = 1000;
    private static final long              MILLIS_TO_FIND_DEVICES    = 12000;
    private static final long              MILLIS_TO_REFRESH_UUIDS   = 14000;
    /** Members */
    private static       String            myUUID;
    private static       Handler           handler;
    private static       BluetoothDevice   arduino;
    private static       BluetoothSocket   mBluetoothSocket;
    private static       BluetoothAdapter  mBluetoothAdapter;
    private static       OutputStream      outputStream;
    private static       InputStream       inputStream;
    private static       IntentFilter      filter;

    /**Constructor*/
    public Bluetooth(Context context)
    {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        handler           = new Handler();
        outputStream      = null;
        inputStream       = null;
        filter            = new IntentFilter(BluetoothDevice.ACTION_PAIRING_REQUEST);
        context.registerReceiver(mPairingRequestReceiver, filter);
    }

    /**
     * Enable bluetooth adapter and start discover new devices around
     * @param condition
     */
    public void enable(boolean condition)
    {
        if(condition)
        {
            mBluetoothAdapter.enable();
            discoverDevices(true);
        }
        else
            mBluetoothAdapter.disable();
    }

    /**
     * Verify if the bluetooth adapter is turned on
     * @return If the bluetooth adapter is on or off
     */
    public boolean isEnabled()
    {
        return mBluetoothAdapter.isEnabled();
    }

    /**
     * Start or cancel the discovering of new devices
     * @param condition
     */
    public void discoverDevices(boolean condition)
    {
        if(condition)
            mBluetoothAdapter.startDiscovery();
        else
            mBluetoothAdapter.cancelDiscovery();
    }

    /**
     * Verify if this adapter and the target device has a socket connection
     * @return If this adapter and the target device is connected or not
     */
    public boolean isConnected()
    {
        return  mBluetoothSocket.isConnected();
    }

    /**
     * Connect this adapter to target device. The connection will try to be established after
     * MILLIS_TO_FIND_DEVICES, where this time is due to enable bluetooth adapter and discovering
     * new devices
     */
    public void connect()
    {
        try {
            enable(true);
            handler.postDelayed(bluetoothConnectionThread, MILLIS_TO_FIND_DEVICES);
        }
        catch (Exception e){
            Log.d("MANDA_CHOPP", "Error trying to connect at connect()");
        }
    }

    /**
     * Disconnect this adapter from the socket between it and target device.
     */
    public void disconnect()
    {
        try{
            mBluetoothSocket.close();
        }
        catch(Exception e) {
            Log.d("MANDA_CHOPP", "Error trying to disconnect");
        }
    }

    /**
     * Send a bytes array to target device
     * @param message
     */
    public void sendMessage(String message)
    {
        try{
            outputStream = mBluetoothSocket.getOutputStream();
            outputStream.write(message.getBytes());
        }
        catch (Exception e){
            Log.d("MANDA_CHOPP", "ERROR TRYING TO SEND A MESSAGE");
        }
    }

    /**
     * Keep listening to socket and verify if there are any message to this bluetooth adapter
     */
    public void readMessage()
    {
        try{
            handler.postDelayed(readMessageThread, MILLIS_TO_GET_NEW_READ);
        }
        catch (Exception e){
            Log.d("MANDA_CHOPP", "ERROR TRYING TO READ A MESSAGE");
        }
    }

    /**
     * This BroadcasReceiver listens if a bond request happens and try automatically bond
     */
    private final BroadcastReceiver mPairingRequestReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();

            if (action.equals(BluetoothDevice.ACTION_PAIRING_REQUEST))
            {
                try {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    int pin                = intent.getIntExtra("android.bluetooth.device.extra.PAIRING_KEY", PASSWORD);
                    Log.d("MANDA_CHOPP", "START PAIRING");
                    byte pinBytes[]        = (""+pin).getBytes("UTF-8");
                    device.setPin(pinBytes);
                    handler.postDelayed(bluetoothConnectionThread,0);
                } catch (Exception e) {
                    Log.e("MANDA_CHOPP", "Error occurs when trying to auto pair");
                    e.printStackTrace();
                }
            }
        }
    };

    /**
     * This thread will be executed until the connection of this adapter to target device occurs.
     * This thread is called by connect().
     */
    private Runnable bluetoothConnectionThread = new Runnable() {
        @Override
        public void run() {

            if( isEnabled() )
            {
                try
                {
                    //Get the desired device with the given bluetooth address
                    arduino = mBluetoothAdapter.getRemoteDevice(ARDUINO_BLUETOOTH_ADDRESS);
                    discoverDevices(false);
                    Log.d("MANDA_CHOPP", "Desired device found: "+ arduino.getName() + ", " +arduino.getAddress());

                    //If not paired, pair
                    if( arduino.getBondState() == BluetoothDevice.BOND_NONE )
                    {
                        arduino.createBond();
                        handler.removeCallbacks(this);
                        return;
                    }

                    //Get UUID to establish connection
                    ParcelUuid parcelUuid[] = arduino.getUuids();
                    myUUID                  = parcelUuid[0].toString();

                    //Get a BluetoothSocket to connect with the given BluetoothDevice
                    mBluetoothSocket = arduino.createRfcommSocketToServiceRecord( UUID.fromString(myUUID) );

                    if (mBluetoothSocket != null)
                    {
                        mBluetoothSocket.connect();
                        readMessage();
                        handler.removeCallbacks(this);
                    }
                    else
                        handler.postDelayed(this, 0);
                }
                catch (IllegalArgumentException e){
                    Log.d("MANDA_CHOPP", "Invalid address at getRemoteDevice()");
                    handler.postDelayed(this, 0);
                }
                catch (IOException e){
                    Log.d("MANDA_CHOPP", "Error creating socket");
                    handler.postDelayed(this, 0);
                }
                catch (Exception e){
                    Log.d("MANDA_CHOPP","ERROR UUID. SEARCHING SUPPORTED UUIDS AT REMOTE DEVICE");
                    arduino.fetchUuidsWithSdp();
                    handler.postDelayed(this, MILLIS_TO_REFRESH_UUIDS);
                }
            }
            else
            {
                enable(true);
                handler.postDelayed(this, MILLIS_TO_FIND_DEVICES);
            }
        }
    };

    /**
     * This thread will be executed while a socket between this adapter and target device
     * exists. This thread is capturing bytes coming to this adapter and this thread is called
     * by readMessage().
     */
    private Runnable readMessageThread = new Runnable() {
        @Override
        public void run() {
                try{
                    if(!isConnected() || !isEnabled())
                    {
                        disconnect();
                        handler.removeCallbacks(this);
                        return;
                    }

                    byte bytes[]     = new byte[MAX_DATA_BYTES];
                    inputStream      = mBluetoothSocket.getInputStream();
                    int howManyBytes = inputStream.read(bytes);

                    if(howManyBytes != -1 && howManyBytes < MAX_DATA_BYTES)
                        Log.d("MANDA_CHOPP", "Answer from "+ DEVICE +":" + new String(Arrays.copyOfRange(bytes,0,howManyBytes)));
                }
                catch(Exception e){
                    Log.d("MANDA_CHOPP", "ERROR TRYING READ MESSAGE");
                }

                handler.postDelayed(this, MILLIS_TO_GET_NEW_READ);
            }

    };
}
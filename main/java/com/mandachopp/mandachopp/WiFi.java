/*****************************************************************************
 PROJECT: MANDA CHOPP
 AUTHOR : JOSÉ ALBERTO GURGEL CARDOSO NETO
 DATE   : 05/31/2018
 PLACE  : MANAUS, AMAZONAS
 /*****************************************************************************/
package com.mandachopp.mandachopp;

import java.net.URL;
import java.util.List;
import android.util.Log;
import java.util.Arrays;
import android.os.Handler;
import java.io.InputStream;
import android.content.Context;
import java.net.HttpURLConnection;
import java.io.BufferedInputStream;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiConfiguration;

class WiFi
{
    /** Constants */
    private static final String            NAME                       = "Pi3-PIBIC";
    private static final String            PASSWORD                   = "password";
    private static final String            MY_URL                     = "http://192.168.1.1/tap2/hello.php";
    public  static final int               MAX_DATA_BYTES             = 1024;
    private static final int               MILLIS_TO_TRY_RECONNECTION = 15000;
    /** Members */
    private static       Context           mContext;
    private static       WifiManager       wifi;
    private static       WifiConfiguration configuration;
    private static       Handler           handler;
    private static       InputStream       inputStream;
    private static       byte[]            info;
    private static       int               infoSize;
    private static       String            answer;

    /**Constructor*/
    public WiFi(Context mContext)
    {
        this.mContext                   = mContext;
        this.wifi                       = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        this.configuration              = new WifiConfiguration();
        this.configuration.SSID         = "\""+ this.NAME +"\"";
        this.configuration.preSharedKey = "\""+ this.PASSWORD +"\"";
        this.wifi.addNetwork(configuration);
        this.handler                    = new Handler();
    }

    /**
     * Verify if the wifi adapter is turned on
     * @return If the wifi adapter is on or off
     */
    public boolean isWiFiEnabled()
    {
        return wifi.isWifiEnabled();
    }

    /**
     * Enable wifi adapter
     * @param condition
     */
    public void enableWiFi(boolean condition)
    {
        this.wifi.setWifiEnabled(condition);
    }

    /**
     * Verify if this adapter is connected to the desired network
     * @return If this adapter is connected to desired network
     */
    public boolean isConnected()
    {
        String state = wifi.getConnectionInfo().getSupplicantState().toString();
        String ssid  = wifi.getConnectionInfo().getSSID().toString();

        if(state.equals("COMPLETED") && ssid.equals(this.configuration.SSID))
            return true;
        return false;
    }

    /**
     * Get networks available
     * @return networks around
     */
    public List<WifiConfiguration> scanNetworks()
    {
        List<WifiConfiguration> list = wifi.getConfiguredNetworks();
        return list;
    }

    /**
     * Connects to the desired network
     */
    public void connect()
    {
        handler.postDelayed(connectionThread,0);
    }

    /**
     * Disconnects from the desired network
     */
    public void disconnect()
    {
        wifi.disconnect();
    }

    /**
     * Get answer from a web server
     */
    public String getWebResponse()
    {
        answer = null;

        if( isConnected())
        {
            try
            {
                URL url                         = new URL(MY_URL);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                inputStream                     = new BufferedInputStream(urlConnection.getInputStream());
                info                            = new byte[MAX_DATA_BYTES];
                infoSize                        = inputStream.read(info);

                if(infoSize > -1)
                {
                    answer = new String(Arrays.copyOfRange(info,0,infoSize));;
                    Log.d("MANDA_CHOPP", "Answer from Server:" + answer);
                }

                urlConnection.disconnect();
            }
            catch(Exception e)
            {
                Log.d("MANDA_CHOPP", e.getMessage());
            }
        }
        else
            Log.d("MANDA_CHOPP", "Error: This device isn't connected with server");

        return answer;
    }

    /**
     * Connection thread makes sure to try a connection until is not established
     */
    private Runnable connectionThread = new Runnable()
    {
        @Override
        public void run()
        {
            if( !isWiFiEnabled() )
                enableWiFi(true);

            try
            {
                List<WifiConfiguration> list = scanNetworks();

                for(int i = 0; i < list.size(); i++)
                {
                    if( list.get(i).SSID == configuration.SSID )
                    {
                        wifi.disconnect();
                        wifi.enableNetwork(list.get(i).networkId, true);
                        wifi.reconnect();
                    }
                }
            }
            catch(Exception e)
            {
                Log.d("MANDA_CHOPP","ERROR TRYING TO CONNECT TO " + NAME);
            }

            if( !isConnected() )
                handler.postDelayed(this, MILLIS_TO_TRY_RECONNECTION);
            else
                handler.removeCallbacks(this);
        }
    };
}
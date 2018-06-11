/*****************************************************************************
 PROJECT: MANDA CHOPP
 AUTHOR : JOSÉ ALBERTO GURGEL CARDOSO NETO
 DATE   : 05/31/2018
 PLACE  : MANAUS, AMAZONAS
 /*****************************************************************************/
package com.mandachopp.mandachopp;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

public class MainActivity extends AppCompatActivity
{
    private WiFi        wifi;
    private Handler     handler;
    private Bluetooth   bluetooth;
    private ImageView   bar;
    private ProgressBar progressBar;
    private String      serverResponse;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        wifi        = new WiFi(this);
        handler     = new Handler();
        bluetooth   = new Bluetooth(this);
        progressBar = findViewById(R.id.progressBar);

        wifi.connect();
        handler.postDelayed(wifiConnectionThread,0);
    }

    private Runnable wifiConnectionThread = new Runnable() {
        @Override
        public void run() {
            if( wifi.isConnected() )
            {
                bluetooth.connect();
                progressBar.setTranslationY(120);
                bar = findViewById(R.id.wifiConnectedBar);
                bar.setVisibility(View.VISIBLE);
                handler.removeCallbacks(this);
                handler.postDelayed(btConnectionThread,0);
            }
            else
                handler.postDelayed(this,2000);
        }
    };

    private Runnable btConnectionThread = new Runnable() {
        @Override
        public void run() {
            if( bluetooth.isConnected() )
            {
                progressBar.setTranslationY(240);
                bar = findViewById(R.id.bluetoothConnectedBar);
                bar.setVisibility(View.VISIBLE);
                handler.removeCallbacks(this);
                handler.postDelayed(serverConnectionThread,0);
            }
            else
                handler.postDelayed(this,2000);
        }
    };

    private Runnable serverConnectionThread = new Runnable() {
        @Override
        public void run() {
            serverResponse = wifi.getWebResponse();

            if( serverResponse != null )
            {
                bar = findViewById(R.id.serverConnectedBar);
                bar.setVisibility(View.VISIBLE);
                handler.removeCallbacks(this);
            }
            else
                handler.postDelayed(this,2000);
        }
    };
}
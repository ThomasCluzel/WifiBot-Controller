package com.pizz.wifibotcontroller;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;

/**
 * the "homepage" of our app
 *
 * Set the connection user-wifibot with th IP address given by the user
 * If the wifi is disable or if the connection failed, a short text will appear
 */

public class MainActivity extends AppCompatActivity {

    private static final long MAX_WAIT_MS = 3000; // we wait at most 3 seconds to connect to the bot

    private Button buttonConnexion;
    private EditText editTextGetIP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_layout);

        editTextGetIP = (EditText) findViewById(R.id.editTextMainGetIP);

        buttonConnexion = (Button) findViewById(R.id.buttonMainConnexion);
        buttonConnexion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ControllerSingleton.createCommandSender(ControllerSingleton.CONNECTION_WIFILAB, editTextGetIP.getText().toString());
                Thread t = new Thread(){
                    @Override
                    public void run() {
                        try {
                            ControllerSingleton.getCommandSender().openConnection();
                        } catch (IOException e) {
                            e.printStackTrace();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(MainActivity.this, R.string.toastMainConnectionFailed, Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                        long beginWait = System.currentTimeMillis();
                        // we wait at most MAX_WAIT_MS the connection to open
                        while(System.currentTimeMillis() - beginWait < MAX_WAIT_MS) {
                            if(ControllerSingleton.getCommandSender().isConnected()){
                                //if the connection succeed
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Intent intent = new Intent(MainActivity.this, ChoiceActivity.class);
                                        startActivity(intent);
                                    }
                                });
                                // then we end the while loop
                                break;
                            }
                        }
                    }
                };
                t.start();
            }
        });
    }


    /**
     * Used before the application runs
     */
    @Override
    public void onResume(){
        //check wifi status
        WifiManager wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
        if(wifiManager.getWifiState() == WifiManager.WIFI_STATE_DISABLED){
            buttonConnexion.setEnabled(false);
            Toast.makeText(this, R.string.toastMainWifiDisable, Toast.LENGTH_LONG).show();
        }
        else{
            buttonConnexion.setEnabled(true);
        }
        super.onResume(); // don't forget or it won't start
    }
}

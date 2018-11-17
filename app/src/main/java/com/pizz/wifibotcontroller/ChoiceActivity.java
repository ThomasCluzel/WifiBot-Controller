package com.pizz.wifibotcontroller;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;

/**
 * Created by bemunoz2 on 25/01/2017.
 */

/**
 * <b>If the connection succeeded, we can choose the wifibot control mode</b>
 *
 */
public class ChoiceActivity extends Activity {
    
    public static final String INTENT_PARAMETER ="inputMethod";
    public static final int INPUT_GYROSCOP = 1;
    public static final int INPUT_JOYSTICK = 2;

    private boolean drivingActivityLaunched; // if we launch the driving activity -> true -> do not close the connection else close it
    /**
    * the user chooses the control mode and the method onCreate manages the errors related to the sensors
    */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choice_layout);

        Button buttonVirtualJoystick = (Button) findViewById(R.id.buttonChoiceVirtualJoystick);
        buttonVirtualJoystick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drivingActivityLaunched = true;

                // we start service here in order not to have a delay when first touch the screen
                Intent intentService = new Intent(ChoiceActivity.this , ControllerService.class);
                intentService.putExtra("typeTreatment",Treatment.TREATMENT_JOYSTICK);
                startService(intentService);

                Intent intent = new Intent(ChoiceActivity.this, DrivingActivity.class);
                intent.putExtra(INTENT_PARAMETER, INPUT_JOYSTICK);
                startActivity(intent);
            }
        });

        Button buttonGyroscop = (Button) findViewById(R.id.buttonChoiceGyroscop);
        buttonGyroscop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drivingActivityLaunched = true;

                //check that the sensors are available
                SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
                Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                Sensor magneto = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
                if(accelerometer == null || magneto == null)
                    Toast.makeText(ChoiceActivity.this, R.string.toastChoiceSensorUnavailable , Toast.LENGTH_LONG);
                else { // otherwise the sensors are available, we can throw the intent
                    // we start service here in order not to have a delay when first touch the screen
                    Intent intentService = new Intent(ChoiceActivity.this , ControllerService.class);
                    intentService.putExtra("typeTreatment",Treatment.TREATMENT_GYROSCOP);
                    startService(intentService);

                    // then we start the activity
                    Intent intent = new Intent(ChoiceActivity.this, DrivingActivity.class);
                    intent.putExtra(INTENT_PARAMETER, INPUT_GYROSCOP);
                    startActivity(intent);
                }
            }
        });
    }

    @Override
    protected void onDestroy(){
        // we must close the connection only if we return to the MainActivity and not if we launch the DrivingActivity (it will need the connection)
        if(!drivingActivityLaunched)
            ControllerSingleton.getCommandSender().closeConnection(); // don't forget to close the connection
        super.onDestroy();
    }
    /**
      *<b>onStart manages the start of the choice activity</b>
      *
      *     in case of problem of connection, this method will inform the user with a short message
      *
     */
    @Override
    protected void onStart() {
        drivingActivityLaunched = false; // initialised here because onCreate is not called we come back to the activity

        if(!ControllerSingleton.getCommandSender().isConnected()) { // it's safer not to remove this -> if the DrivingActivity is backgrounded
            Thread t = new Thread(){
                @Override
                public void run() {
                    try {
                        ControllerSingleton.getCommandSender().openConnection(); // attempt to reopen the connection
                    } catch (IOException e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(ChoiceActivity.this, R.string.exceptionSenderReach , Toast.LENGTH_LONG).show();
                                finish(); // we finish the activity and come back to MainActivity
                            }
                        });
                    }
                }
            };
            t.start();
        }
        super.onStart();
    }
}

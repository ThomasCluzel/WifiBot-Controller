package com.pizz.wifibotcontroller;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import java.io.IOException;

/**
 * Created by bemunoz2 on 30/01/2017.
 */

public class DrivingActivity extends Activity {

    private int mode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driving_layout);

        getFragmentManager()
                .beginTransaction()
                .add(R.id.fragmentDrivingVideo, new VideoFragment()) // add the video in background
                .commit();

        mode = getIntent().getIntExtra(ChoiceActivity.INTENT_PARAMETER,ChoiceActivity.INPUT_GYROSCOP); // get back the type of input method to add in foreground
        switch (mode){
            case ChoiceActivity.INPUT_GYROSCOP:
                getFragmentManager()
                        .beginTransaction()
                        .add(R.id.fragmentDrivingInput, new GyroscopFragment())
                        .commit();
                break;
            case ChoiceActivity.INPUT_JOYSTICK:
                getFragmentManager()
                        .beginTransaction()
                        .add(R.id.fragmentDrivingInput, new JoystickFragment())
                        .commit();
                break;
        }
    }

    @Override
    protected void onStop() {
        // if the activity is stopped we must stop sending command to the bot
        Intent intent = new Intent(DrivingActivity.this, ControllerService.class);
        intent.putExtra("close", true);
        startService(intent);
        stopService(new Intent("com.pizz.wifibotcontroller.ControllerService"));
        /* and also close the connection
        ControllerSingleton.getCommandSender().closeConnection(); // it caused connection problems so we removed this line
        */
        super.onStop();
        finish(); // if the activity is no longer visible -> close it
    }

    @Override
    protected void onStart() {
        // if we are not connected to the bot -> there is a problem -> finish the activity
        if(!ControllerSingleton.getCommandSender().isConnected()) {
            finish();
        }
        super.onStart();
    }
}

package com.pizz.wifibotcontroller;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.IOException;

/**
 * Created by bemunoz2 on 02/02/2017.
 */

/*
 * Idea :
 * - Receive new information -> store them
 * - When it's time to send a new message to the bot compute the information
 * - Send the information
 * -> to avoid computing twice the same entries, use a boolean set to true when new information arrived and to false when they are treated
 */

public class ControllerService extends Service {
    //declaration of the sensors we need
    private SensorManager sensorManager = null;
    private Sensor accelerometer;
    private Sensor magneto;
    private SensorEventListener gyroListener;

    //these vectors store the values of each sensor
    private float[] accelerometerVector = new float[3];
    private float[] magneticVector=new float[3];

    //the resultMatrix is the rotation matrix generated by the previous vectors and values stores the rotation with the Y-axis
    private float[] resultMatrix=new float[9];
    private float[] values=new float[3];

    // the kind of treatment
    private Treatment treatment = null;
    private int typeTreatment = 0;

    // concerning the thread
    private Thread thread = null;
    private Bundle bundleSend = null;
    private Intent intentReceived = null;


    /** Initialisation of the sensors
     *
     * We use the accelerometer and the magnetic field sensor
     * because gyroscope is not available for a lot of devices
     *
     */
    public void initSensor(){
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magneto = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        if (magneto!=null && accelerometer!=null){
            gyroListener = new SensorEventListener() {
                @Override
                public void onSensorChanged(SensorEvent event) {
                    if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                        accelerometerVector = event.values;
                    }else if (event.sensor.getType()==Sensor.TYPE_MAGNETIC_FIELD){
                        magneticVector = event.values;
                    }

                    SensorManager.getRotationMatrix(resultMatrix, null, accelerometerVector, magneticVector);
                    SensorManager.getOrientation(resultMatrix, values);
                }

                @Override
                public void onAccuracyChanged(Sensor sensor, int accuracy) {
                    // nothing to do here
                }
            };
            sensorManager.registerListener(gyroListener, accelerometer,
                    SensorManager.SENSOR_DELAY_GAME);
            sensorManager.registerListener(gyroListener, magneto,
                    SensorManager.SENSOR_DELAY_GAME);
        }
    }

    /**
     * Create and init the thread
     *
     * @return the thread we need to send the commands to the wifibot
     */
    public Thread initThread(){
        return new Thread(new Runnable() {
            @Override
            public void run() {
                long beginning, now;
                // send every 10ms a command to the bot
                try {
                    treatment.updateLastComputeTime(); // in order the bot not to start at max speed
                    while(true){ // never stop
                        beginning = System.currentTimeMillis();
                        // check if a new intent is available
                        if (treatment!=null){
                            if(intentReceived!=null && intentReceived.getBooleanExtra("screenCurrentlyTouched",false)) {
                                compute(intentReceived); // if the screen is touched -> we need to accelerate or brake
                            }
                            // otherwise engine brake
                            else{
                                compute(null);
                            }
                        }
                        // send an instruction to the bot
                        if(ControllerSingleton.getCommandSender().isConnected()) {
                            ControllerSingleton.getCommandSender().send(
                                    bundleSend.getBoolean("forwardLeft"),
                                    bundleSend.getInt("speedLeft"),
                                    bundleSend.getBoolean("forwardRight"),
                                    bundleSend.getInt("speedRight")
                            );
                        }
                        // wait 10 ms at most until the next sending
                        now = System.currentTimeMillis();
                        if(10 - now + beginning > 0)
                            Thread.sleep(10 - now + beginning);
                    }
                } catch (IOException | InterruptedException e) {
                    Thread.currentThread().interrupt(); // stop
                }
            }
        });
    }


    /**
     * Prepare the thread
     */
    @Override
    public void onCreate() {
        super.onCreate();
    }


    /**
     *
     * @param intent
     * @param flags
     * @param startId
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) { // should we synchronized this method ? may be not because
        // first we check if we must stop the service
        if(intent.getBooleanExtra("close",false)){
            // we close the service
            stop();
            return super.onStartCommand(intent, flags, startId);
        }

        // update the information
        intentReceived = intent;

        // create the right treatment if needed
        if(treatment==null && intent.getIntExtra("typeTreatment",0)==Treatment.TREATMENT_GYROSCOP){
            treatment = new TreatmentGyroscop();
            typeTreatment = Treatment.TREATMENT_GYROSCOP;
            initSensor();
        }
        if(treatment==null && intent.getIntExtra("typeTreatment",0)==Treatment.TREATMENT_JOYSTICK){
            treatment = new TreatmentJoystick();
            typeTreatment = Treatment.TREATMENT_JOYSTICK;
        }

        if(thread==null || !thread.isAlive() && ControllerSingleton.getCommandSender().isConnected()){ // to start the thread that send the command
            // TODO : should we notify to the user that the connection to the bot was lost ? and how ?
            thread = initThread();
            thread.start(); // start the computation in background
        }

        return super.onStartCommand(intent, flags, startId);
    }

    /**
     *
     * @param intent
     */
    private void compute(Intent intent){
        if(typeTreatment == TreatmentGyroscop.TREATMENT_GYROSCOP){
            Bundle b = new Bundle();
            if(intent != null) {
                b.putBoolean("leftPressed", intent.getBooleanExtra("leftPressed", false));
                b.putBoolean("rightPressed", intent.getBooleanExtra("rightPressed", false));
            }
            b.putDouble("angle",getAngle());
            bundleSend = treatment.compute(b);
        }
        else{// virtual joystick
            Bundle b = new Bundle();
            if(intent != null){
                b.putFloat("xPosNorm",intent.getFloatExtra("xPosNorm",0.5f));
                b.putFloat("yPosNorm",intent.getFloatExtra("yPosNorm",0.5f));
            }
            bundleSend = treatment.compute(b);
        }
    }

    public double getAngle(){
        return -values[1];
    }

    /**
     * This function :
     * - release the sensors if needed
     * - interrupt the thread
     * - release the treatment
     */
    public void stop(){
        // the thread is stopped
        thread.interrupt();
        treatment = null;
        //release the sensor if needed
        if(sensorManager != null) {
            sensorManager.unregisterListener(gyroListener, accelerometer);
            sensorManager.unregisterListener(gyroListener, magneto);
            sensorManager = null;
        }
    }

    @Override
    public void onDestroy() { // seems never to be called even with stopService() -> so we use the onStartCommand with an extra in the intent
        stop();
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

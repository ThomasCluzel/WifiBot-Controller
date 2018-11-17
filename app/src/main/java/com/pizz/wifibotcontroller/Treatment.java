package com.pizz.wifibotcontroller;

import android.os.Bundle;

import java.util.ArrayList;

/**
 * Created by bemunoz2 on 31/01/2017.
 */

/**
 *
 * <b>This class is the base class for all treatments.</b>
 *
 */
public abstract class Treatment {

    public static final int TREATMENT_GYROSCOP = 1;
    public static final int TREATMENT_JOYSTICK = 2;

    protected long lastComputeTime;
    protected double speed = 0;

    /**
     *
     * @param b
     *          the bundle that contains the information to compute the new speed left and right
     * @return the bundle which contains the information to send to the wifibot
     *          "forwardLeft", boolean
     *          "forwardRight", boolean
     *          "speedLeft", int
     *          "speedRight", int
     */
    public abstract Bundle compute(Bundle b);

    /**
     * This last compute time will be equal to System.currentTimeMillis()
     */
    public void updateLastComputeTime() {
        lastComputeTime = System.currentTimeMillis();
    }

    /**
     * Return the timestamp of the last computation.
     *
     * @return the timestamp of the last computation
     */
    public long getLastComputeTime(){
        return this.lastComputeTime;
    }
}

package com.pizz.wifibotcontroller;

import android.os.Bundle;

/**
 * Created by bemunoz2 on 31/01/2017.
 */

/**
 *
 * <b>TreatmentGyroscop is the class which represents the mathematical treatment for the gyroscop</b>
 *
 */
public class TreatmentGyroscop extends Treatment {

    protected static final double COEF_ACCELERATION = 1.0*CommandSender.MAX_SPEED / 1500; // CommandSender.MAX_SPEED / timeToReachMaxSpeedInMs;
    protected static final double COEF_BRAKE = -1.0*CommandSender.MAX_SPEED / 250;
    protected static final double COEF_ENGINE_BRAKE = -1.0*CommandSender.MAX_SPEED / 3000;

    @Override
    /**
     * Returns all the information needed to use the gyroscopic mod
     *
     * @param Bundle b
     *          "rightPressed", boolean
     *          "leftPressed", boolean
     *               "angle", double [-PI/4 , PI/4]
     * @return Bundle bRet
     *          "forwardLeft", boolean
     *          "forwardRight", boolean
     *          "speedLeft", int
     *          "speedRight", int
     *
     */
    public Bundle compute(Bundle b) {
        boolean rightPressed = b.getBoolean("rightPressed",false);
        boolean leftPressed = b.getBoolean("leftPressed",false);
        long currentComputeTime = System.currentTimeMillis();

        Bundle bRet = new Bundle(); // to return all arguments send we will need

        // 1- compute the new speed (drawing a graph helps a lot to understand this formulas)
        if(rightPressed){
            if(speed>=0){ // the bot is going forward and it has to accelerate
                speed = speedAccelerate((long) (currentComputeTime - lastComputeTime + speed/COEF_ACCELERATION));
            }
            else{ // the bot is going backward and it has to brake
                speed = speedBrake((long) (lastComputeTime - currentComputeTime + speed/COEF_BRAKE)); // don't forget the -speed/COEF otherwise it's positive
            }
        }
        else if(leftPressed){
            if(speed<=0){ // the bot is going backward and it has to accelerate
                speed = speedAccelerate((long) (lastComputeTime - currentComputeTime + speed/COEF_ACCELERATION)); // don't forget to change current - last because the bot is going backward
            }
            else{ // the bot is going forward and it has to brake
                speed = speedBrake((long) (currentComputeTime - lastComputeTime + speed/COEF_BRAKE)); // don't forget the -speed/COEF otherwise it's negative
            }
        }
        else{ // engine brake
            if(speed>0){ // the bot is going forward and it has to slow down
                speed = speedEngineBrake((long) (currentComputeTime - lastComputeTime + speed/COEF_ENGINE_BRAKE));
            }
            else if(speed<0){
                speed = speedEngineBrake((long) (lastComputeTime - currentComputeTime + speed/COEF_ENGINE_BRAKE));
            }
            //if speed==0 then speed still 0
        }


        // 2 - tell if forward or backward
        bRet.putBoolean("forwardLeft",speed>=0);
        bRet.putBoolean("forwardRight",speed>=0);


        // 3 - compute the speed for each side
        double coef = rotationLeft(b.getDouble("angle",0));
        bRet.putInt("speedLeft", (int) (coef*Math.abs(speed)));

        coef = rotationRight(b.getDouble("angle",0));
        bRet.putInt("speedRight", (int) (coef*Math.abs(speed)));


        lastComputeTime = currentComputeTime; // update
        return bRet;
    }

    /**
     * Computes the acceleration
     *
     * @param duration
     *          Elapsed time button pressed
     * @return double speed
     *          Speed of the device
     */
    protected double speedAccelerate(long duration){
        double speed = COEF_ACCELERATION * duration;
        if(speed>CommandSender.MAX_SPEED)
            return CommandSender.MAX_SPEED;
        if(speed<-CommandSender.MAX_SPEED)
            return -CommandSender.MAX_SPEED;
        return speed;
    }

    /**
     * Uses the Brake
     *
     * @param duration
     *          Elapsed time button pressed
     * @return double speed
     *          Speed of the device
     */
    protected double speedBrake(long duration){
        double speed = COEF_BRAKE * duration;
        if(speed*this.speed < 0) // if we overreach 0 we return 0
            return 0;
        return speed;
    }

    /**
     * Uses the Engine Brake
     *
     * @param duration
     *          Elapsed time button pressed (ms)
     * @return double speed
     *          Speed of the device
     */
    protected double speedEngineBrake(long duration){
        double speed = COEF_ENGINE_BRAKE * duration;
        if(speed*this.speed < 0) // if we overreach 0 we return 0
            return 0;
        return speed;
    }

    /**
     * Return the coefficient of the left wheels
     *
     * @param angle
     *          angle between [-PI/4, PI/4]
     * @return
     *          the coefficient of the left wheels
     */
    protected double rotationLeft(double angle){
        if(angle<=-Math.PI/4)
            return 0;
        if(angle<0)
            return 4*angle/Math.PI + 1;
        return 1;
    }

    /**
     * Return the coefficient of the right wheels
     *
     * @param angle
     *          angle between [-PI/4, PI/4]
     * @return
     *          the coefficient of the right wheels
     */
    protected double rotationRight(double angle){
        if(angle<=0)
            return 1;
        if(angle<Math.PI/4)
            return -4*angle/Math.PI + 1;
        return 0;
    }
}

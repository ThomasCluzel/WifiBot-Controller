package com.pizz.wifibotcontroller;

import android.os.Bundle;

/**
 * Created by bemunoz2 on 20/03/2017.
 */

public class TreatmentJoystick extends Treatment {


    /**
     * @param b the bundle that contains the information to compute the new speed left and right
     *          "xPosNorm" : float in [0,1] in screen coordinates
     *          "yPosNorm" : float in [0,1]
     * @return the bundle which contains the information to send to the wifibot
     * "forwardLeft", boolean
     * "forwardRight", boolean
     * "speedLeft", int
     * "speedRight", int
     */
    @Override
    public Bundle compute(Bundle b) {
        Bundle bRet = new Bundle(); // to return all arguments send we will need

        float xPosNorm = constrain(changeNorm(1.2f, b.getFloat("xPosNorm",0.5f) ));
        float yPosNorm = constrain(-changeNorm(1.1f, b.getFloat("yPosNorm",0.5f) ));

        // 1- compute the new speed (drawing a graph helps a lot to understand this formulas)
        speed = speedAccelerate(yPosNorm);

        // 2 - tell if forward or backward
        bRet.putBoolean("forwardLeft",speed>=0);
        bRet.putBoolean("forwardRight",speed>=0);

        // 3 - compute the speed for each side
        double coef = rotationLeft(xPosNorm);
        bRet.putInt("speedLeft", (int) (coef*Math.abs(speed)));

        coef = rotationRight(xPosNorm);
        bRet.putInt("speedRight", (int) (coef*Math.abs(speed)));

        return bRet;
    }

    protected double speedAccelerate(float yPosScreen){
        return CommandSender.MAX_SPEED * yPosScreen;
    }

    protected double rotationLeft(float xPosNorm){
        if(xPosNorm < 0){
            return 1 + xPosNorm;
        }
        return 1;
    }

    protected double rotationRight(float xPosNorm){
        if(xPosNorm > 0){
            return 1 - xPosNorm;
        }
        return 1;
    }


    /**
     * Change the normalization: [0,1] -> [-a,a]
     * @param a the parameter of the function
     * @param x the variable in [0,1]
     * @return the float normalized in [-a,a]
     */
    protected float changeNorm(float a, float x){
        return 2*a*x - a;
    }

    /**
     * Constrain the value of x in [-1,1]
     *      if x>1 then x=1 else if x<-1 then x=-1 else x=x
     * @param x a float
     * @return a value in [-1,1]
     */
    protected float constrain(float x){
        if(Math.abs(x) > 1)
            return ( (x > 0) ? 1 : -1 );
        return x;
    }
}

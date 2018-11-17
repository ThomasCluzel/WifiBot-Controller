package com.pizz.wifibotcontroller;

import android.net.Uri;

import java.io.IOException;
import java.net.URI;

/**
 * Created by bemunoz2 on 25/01/2017.
 */
/**
 * this class defines the methods needed to make a CommandSender
 */
public abstract class CommandSender {

    public static final int MAX_SPEED = 127; // max speed is 240 but we reduce it for security reasons TODO : check if 240 is possible

    public abstract void openConnection()throws IOException;
    public abstract void closeConnection();
    public abstract void send(boolean forwardLeft, int speedLeft, boolean forwardRight, int speedRight) throws IOException;
    public abstract String getVideoUrl();
    public abstract boolean isConnected();
}

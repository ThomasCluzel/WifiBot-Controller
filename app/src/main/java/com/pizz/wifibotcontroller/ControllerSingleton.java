package com.pizz.wifibotcontroller;

/**
 * Created by bemunoz2 on 30/01/2017.
 */

public class ControllerSingleton {

    public static final int CONNECTION_WIFILAB = 1;
    //public static final int CONNECTION_ADHOC = 2;

    private static CommandSender cmdSender = null;

    //this is a factory inside the singleton
    public static void createCommandSender(int connectionType, String ip){
        switch (connectionType){
            case CONNECTION_WIFILAB:
                cmdSender = new CommandSenderWifiLab(ip);
                break;
            // ad-hoc ?
        }
    }

    // this is the get method of the singleton
    public static CommandSender getCommandSender(){
        return cmdSender;
    }
}

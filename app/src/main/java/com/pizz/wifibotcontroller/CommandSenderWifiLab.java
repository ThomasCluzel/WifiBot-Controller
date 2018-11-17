package com.pizz.wifibotcontroller;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Created by bemunoz2 on 27/01/2017.
 */

/**
 * CommandSenderWifiLab is the class which send all the information to the WifiLab
 *
 *
 *
 */
public class CommandSenderWifiLab extends CommandSender {

    protected String botIP; // 192.168.1.xxx
    protected byte[] command;
    protected Socket soc = null;
    protected OutputStream os = null;

    /**
     * Constructor CommandSenderWifiLab.
     * <p>
     *
     *
     * </p>
     * @param ip
     *          Bot's ip
     */
    public CommandSenderWifiLab(String ip){
        botIP = ip;
        command = new byte[9];
        command[0] = (byte) 0xFF;
        command[1] = (byte) 0x07;
        //2 speed left
        command[3] = 0;
        //4 speed right
        command[5] = 0;
        //6 flags
        //7,8 crc not used in tcp
    }

    /**
     * Open the connection bot-user
     *
     * @throws IOException
     *              If the connection failed
     *
     */
    @Override
    public void openConnection() throws IOException {
        try {
            if(soc!=null && isConnected())
                closeConnection();
            soc = new Socket(InetAddress.getByName(botIP),15020);
            os = new BufferedOutputStream(new DataOutputStream(soc.getOutputStream()),10);// we send 10 bytes
        } catch (IOException e){
            e.printStackTrace();
            throw new IOException(String.valueOf(R.string.exceptionSenderInitConnect));
        }
    }

    /**
     * Close the connection bot-user
     *
     */
    @Override
    public void closeConnection(){
        try {
            if(os!=null)
                os.close(); // should automatically close the socket
            if(soc!=null && !soc.isClosed()) // but why not check
                soc.close();
        } catch (IOException e) { // useless ?
            e.printStackTrace();
        }
    }

    /**
     * Send information to control the WifiBot
     * @param forwardLeft
     *          Boolean
     * @param speedLeft
     *          Int: speed of the left wheels
     * @param forwardRight
     *          Boolean
     * @param speedRight
     *          Int: speed of the right wheels
     * @throws IOException
     *          if the connection to the WifiBot failed
     */
    @Override
    public void send(boolean forwardLeft, int speedLeft, boolean forwardRight, int speedRight) throws IOException {
        command[2] = (byte) (speedLeft&0x7F);
        command[3] = (byte) (speedLeft>>7);
        command[4] = (byte) (speedRight&0x7F);
        command[5] = (byte) (speedRight>>7);
        command[6] = (byte) (((byte)((forwardLeft)?64:0)) + ((byte)((forwardRight)?16:0))); // look at my cast !
        Crc16();

        try {
            if(isConnected()){
                os.write(command);
                os.flush(); // why not ?
            }
        } catch (IOException e) {
            throw new IOException(String.valueOf(R.string.exceptionSenderReach));
        }
    }

    /**
     * Compute the CRC of the command and add it at the end
     *
     */
    private void Crc16(){
        int Crc = 0xFFFF;
        int Polynome = 0xA001;
        int CptOctet;
        int CptBit;
        int Parity;

        for ( CptOctet= 1 ; CptOctet < 7 ; CptOctet++){
            Crc ^= command[CptOctet];
            for ( CptBit = 0; CptBit <= 7 ; CptBit++){
                Parity = Crc;
                Crc >>= 1;
                if (Parity%2 == 1)
                    Crc ^= Polynome;
            }
        }
        command[7] = (byte) (Crc&0xFF);
        command[8] = (byte) (Crc>>8&0xFF);
    }

    /**
     * Get the URL of the stream of the WifiBot
     *
     * @return String Url
     *          Url of the stream
     */
    @Override
    public String getVideoUrl() {
        return "http://"+botIP+":8080/?action=stream";
    }

    /**
     * Test the connection between user and bot
     *
     * @return boolean
     *      Indicates the status of the connection
     */
    public boolean isConnected(){
        return (os!=null && soc!=null && soc.isConnected());
    }
}

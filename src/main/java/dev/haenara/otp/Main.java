package dev.haenara.otp;

public class Main {
    public static void main(String[] args){
        try{
            OtpReaderTray tray = new OtpReaderTray();
            tray.run();
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}



package dev.haenara.otp;

import dev.haenara.otp.config.Config;

import javax.mail.*;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;

/**
 * OTP retriever from email.
 * Notify new OTP to system tray when it finds it.
 */
public class OtpEmailReader implements OtpMinner{

    private final ArrayList<OtpObserver> observers = new ArrayList<OtpObserver>();
    private boolean isAlive = false;

    public static Store checkAuth(String id, String pwd) throws MessagingException {
        Properties properties = new Properties();
        // properties.put("mail.debug", "true");
        properties.put("mail." + Config.MAIL_TYPE + ".host", Config.HOST);
        properties.put("mail." + Config.MAIL_TYPE + ".port", "110");
        properties.put("mail." + Config.MAIL_TYPE + ".starttls.enable", "false");
        properties.put("mail." + Config.MAIL_TYPE + ".ssl.enable", "false");
        properties.put("mail." + Config.MAIL_TYPE + ".socketFactory.fallback", "true");
        Session emailSession = Session.getInstance(properties);

        //create the POP3 store object and connect with the pop server
        Store store = emailSession.getStore(Config.MAIL_TYPE);

        store.connect(Config.HOST, 110, id, pwd);
        //close the store and folder objects
        return store;
    }

    public void closeConnection(Store store){
        if (store!= null){
            try {
                store.close();
            } catch (MessagingException e) {
                e.printStackTrace();
            }
        }
    }

    boolean isOtpReading = false;

    public synchronized void readOtp(final String user, final String password) {
        System.out.println("READ OTP START");
        if (!isOtpReading) {

            isOtpReading = true;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        getOtp(user, password);
                    } catch (Exception e ){
                        e.printStackTrace();
                    }
                    isOtpReading = false;
                }
            }).start();
        } else {
            System.out.println("READ OTP is already START");
        }
    }

    /**
     * Keep checking email inboxs and finds out new otp.
     * When it founds it, notify to observers(system tray).
     * @param user
     * @param password
     */
    private void getOtp(String user, String password) {
        System.out.println("Get OTP Start");
        String lastOtp = "";
        Folder emailFolder = null;
        try {
            //create properties field
            long startTime = new Date().getTime();
            System.out.println("otp reading...");

            // 2019.08.19 Running Forever
            while (isAlive) {
                Store store = checkAuth(user, password);

                //create the folder object and open it
                emailFolder = store.getFolder("INBOX");
                emailFolder.open(Folder.READ_ONLY);

                // retrieve the messages from the folder in an array and print it
                String tempOtp = getLastVaildOtp(emailFolder.getMessages());

                //close the store and folder objects
                emailFolder.close(false);
                store.close();

                if (!tempOtp.isEmpty() && !lastOtp.equals(tempOtp)) {
                    lastOtp = tempOtp;
                    notifyObserver(tempOtp);
                    System.out.println("New OTP has found : " + tempOtp);
                } else {
                    try { Thread.sleep(Config.DURATION); } catch (Exception e) {;}
                    continue;
                }
            }

        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (MessagingException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (emailFolder != null && emailFolder.isOpen()) {
                    emailFolder.close();
                }
            } catch (MessagingException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * returns last and vaild OTP.
     * if last one is not valid(expired), returns empty string.
     * @param messages
     * @return
     * @throws MessagingException
     * @throws IOException
     */
    private String getLastVaildOtp(Message[] messages) throws MessagingException, IOException {
        String tempOtp = "";
        for (int i = messages.length - 1; i >= 0; i--) {
            Message message = messages[i];
            if (message.getSubject().startsWith(Config.OTP_TITLE) &&
                    message.getFrom()[0].toString().equals(Config.OTP_SENDER)) {
                String text = message.getContent().toString();
                String time = message.getHeader("Received")[0].split(";")[1].trim();
                System.out.println("Text : " + text + " time : " + time);
                // 2019.10.15 HaenaraShin.
                // Either local PC or mail server's system time could be set incorrect or different.
                // Unless you calculate the time gap between mail server and local PC,
                // OTP expiration time must not be checked.
                tempOtp = text.split(" : ")[1].substring(0, 6);
                System.out.println("OTP:" + tempOtp);
                break;
            }
        }

        return tempOtp;
    }

    private long compareTime(Date c1, Date c2){
        return (c1.getTime() - c2.getTime()) / 1000L;
    }

    private boolean isTimeVaild(String receivedTimeString) {
        try {
            Date now = new Date();
            // received Time format example -> 14 Aug 2019 10:27:11 +0900
            Date receivedTime = new SimpleDateFormat("dd MMM yyyy HH:mm:ss Z", new Locale("en", "US")).parse(receivedTimeString);
            return (compareTime(now, receivedTime) < Config.EXPIATION_DURATION);
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void subscribe(OtpObserver observer) {
        isAlive = true;
        observers.add(observer);
    }

    @Override
    public void unsubscribe(OtpObserver observer) {
        if (observers.contains(observer)) {
            observers.remove(observer);
        }
        if (observers.size() == 0) {
            isAlive = false;
        }
    }

    @Override
    public void notifyObserver(String otp) {
        for (OtpObserver observer : observers) {
            observer.update(otp);
        }
    }
}

package dev.haenara.otp;

import dev.haenara.otp.security.ISecurity;
import dev.haenara.otp.security.SecurityImpl;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Store and read a config text file with ID and password with encryption.
 */
public class DataManager {
    private ISecurity securityModule = SecurityImpl.getInstance();

    public void save(String id, String pwd) {
        try {
            OutputStream output = new FileOutputStream("config.txt");
            String str = securityModule.encrypt(id) + "|"
                    + securityModule.encrypt(pwd);
            byte[] by = str.getBytes();
            output.write(by);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String read() {
        String data = "";
        try {
            String filePath = "config.txt";
            FileInputStream fileStream = new FileInputStream(filePath);
            byte[] readBuffer = new byte[fileStream.available()];
            while (fileStream.read(readBuffer) != -1) { ; }
            data = new String(readBuffer);
            fileStream.close(); //스트림 닫기
        } catch (Exception e) {
            e.getStackTrace();
        }
        return data;
    }

    public String getId() {
        try {
            String encryptedId = read().split("\\|")[0];
            return securityModule.decrypt(encryptedId);
        } catch (IndexOutOfBoundsException e) {
            return "";
        }
    }

    public String getPwd() {
        try {
            String encryptedPwd = read().split("\\|")[1];
            return securityModule.decrypt(encryptedPwd);
        } catch (IndexOutOfBoundsException e) {
            return "";
        }
    }
}
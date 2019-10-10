package dev.haenara.otp.config;

/**
 * System configs
 */
public class Config {
    public static final String HOST = "mail.gmail.com";
    public static final String MAIL_DOMAIN = "@gmail.com";
    public static final String MAIL_TYPE = "pop3";
    public static final String OTP_SENDER = "otp@gmail.com";
    public static final String OTP_TITLE = "[DBSAFER] OTP";
    public static final String KEY = "XEwUsWSqXYs5GaCY";
    public static final long EXPIATION_DURATION = 180L; // OTP EXPIRE TIME (sec)
    public static final int DURATION = 500; // EMAIL reading duraion.
}


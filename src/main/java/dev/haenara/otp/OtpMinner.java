package dev.haenara.otp;

/**
 * A OTP retriever and notify to all observers.
 */
public interface OtpMinner {
    public void subscribe(OtpObserver observer);
    public void unsubscribe(OtpObserver observer);
    public void notifyObserver(String otp);
}

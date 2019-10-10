package dev.haenara.otp;


import dev.haenara.otp.config.Config;
import dev.haenara.otp.view.AboutFrame;

import javax.mail.MessagingException;
import javax.mail.Store;
import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;

/**
 * System tray that tracks OTP and let users to login to email.
 */
public class OtpReaderTray implements OtpObserver{
    private boolean isAuthenticated = false;
    private MenuItem aboutItem = new MenuItem("About");
    private MenuItem loginItem = new MenuItem("Email Login");
    private MenuItem exitItem = new MenuItem("Exit");
    private LoginFrame frame = new LoginFrame(this, "Login");

    private DataManager dataManager = new DataManager();
    private TrayIcon trayIcon;

    private OtpEmailReader otpEmailReader;


    public OtpReaderTray() {
        otpEmailReader = new OtpEmailReader();
        otpEmailReader.subscribe(this);
    }

    public void run() {
        //Check the SystemTray is supported
        if (!SystemTray.isSupported()) {
            System.out.println("SystemTray is not supported");
            return;
        }
        final PopupMenu popup = new PopupMenu();

        trayIcon = new TrayIcon(createImage());
        trayIcon.setImageAutoSize(true);
        final SystemTray tray = SystemTray.getSystemTray();

        popup.add(aboutItem);
        popup.add(loginItem);
        popup.addSeparator();
        popup.add(exitItem);

        aboutItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new AboutFrame();
            }
        });

        // Add ActionListener
        exitItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
               finish();
            }
        });

        loginItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showLoginFrame();
            }
        });

        trayIcon.setPopupMenu(popup);
        trayIcon.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 1) {
                    if (isAuthenticated) {
                        getOtp();
                    } else {
                        System.out.println("isAuthenticated is false");
                    }
                }
            }
        });

        try {
            tray.add(trayIcon);
            login();
        } catch (AWTException e) {
            System.out.println("TrayIcon could not be added.");
        }
    }

    public void finish(){
        otpEmailReader.unsubscribe(this);
        System.exit(0);

    }

    private void getOtp(){
        System.out.println("OTP button pressed.");
        String username = dataManager.getId();
        String password = dataManager.getPwd();
        otpEmailReader.readOtp(username, password);

    }

    private void showLoginFrame(){
        if (frame == null) {
            frame = new LoginFrame(this, "Login");
        }
        frame.setVisible(true);
    }
    private void login() {
        System.out.println("Login");
        isAuthenticated = tryLoginEmail();
        if (isAuthenticated){
            successLogin();
        } else {
            showLoginFrame();
            showErrorMessage("Fail to login.");
        }
    }

    private boolean tryLoginEmail(){
        String id = dataManager.getId();
        String pwd = dataManager.getPwd();
        System.out.println("tryLoginEmail - id : " + id + " pwd : " + pwd);
        try {
            if (!id.isEmpty() && !pwd.isEmpty()) {
                Store store = OtpEmailReader.checkAuth(id, pwd);
                if (store != null) {
                    return true;
                }
            }
        } catch (MessagingException e) {
            e.printStackTrace();
        }
        return false;
    }

    private Image createImage() {
        URL url = Main.class.getResource("/resources/icon/otp.png");
        return Toolkit.getDefaultToolkit().getImage(url);
    }

    public void showMessage(String message){
        trayIcon.displayMessage("OTP", message, TrayIcon.MessageType.NONE);
    }

    public void showErrorMessage(String message){
        trayIcon.displayMessage("OTP", message, TrayIcon.MessageType.ERROR);
    }

    public void isAuthenticated(boolean b) {
        isAuthenticated = b;
    }

    @Override
    public void update(String otp) {
        copyTextToClipboard(otp);
        showMessage("OTP number " + otp + " has copied on clipboard.");
    }

    private void copyTextToClipboard(String myString) {
        StringSelection stringSelection = new StringSelection(myString);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, null);
    }

    public boolean checkAuth(String id, String pwd){
        Store store = null;
        try {
            store = otpEmailReader.checkAuth(id, pwd);
            if (store != null) {
                otpEmailReader.closeConnection(store);
            }
        } catch (MessagingException e) {
            e.printStackTrace();
        }

        return (store != null);
    }

    public void successLogin(String id, String pwd) {
        dataManager.save(id, pwd);
        successLogin();
    }

    private void successLogin() {
        showMessage("Success to login.");
        isAuthenticated(true);
        getOtp();
    }
}

class LoginFrame extends JFrame {

    private LoginPanel panel = new LoginPanel();
    private JButton loginButton = new JButton("로그인");
    private DataManager dataManager = new DataManager();
    OtpReaderTray parent;

    public LoginFrame(OtpReaderTray parent, String title) throws HeadlessException {
        super(title);
        this.parent = parent;
        setBounds(400, 200, 400, 120);
        setLayout(new BorderLayout());
        setMaximumSize(new Dimension(400, 120));
        setMinimumSize(new Dimension(400, 120));

        Container container = getContentPane();
        container.add(panel, BorderLayout.CENTER);
        container.add(loginButton, BorderLayout.SOUTH);

        panel.setPwdFieldListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean result = login();
            }
        });

        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    boolean result = login();
                } catch (Exception e1) {
                    ;
                }
            }
        });
    }

    public boolean login() {
        String id = panel.getId();
        String pwd = panel.getPwd();
        id = checkIdFormat(id);
        if (parent.checkAuth(id, pwd)){
            setVisible(false);
            parent.successLogin(id, pwd);
            return true;
        }

        parent.showErrorMessage("Fail to login. Check your ID or Password again.");
        return false;
    }

    private String checkIdFormat(String id) {
        if (id.contains("@")) {
            return id;
        } else {
            return id + Config.MAIL_DOMAIN;
        }
    }

    class LoginPanel extends JPanel {
        JTextField idField = new JTextField();
        JPasswordField pwdField = new JPasswordField();
        JLabel idText = new JLabel("Email ID");
        JLabel pwdText = new JLabel("Email Password");

        public LoginPanel() {
            setLayout(new GridLayout(2, 2));
            setSize(400, 100);
            idText.setHorizontalAlignment(SwingConstants.CENTER);
            pwdText.setHorizontalAlignment(SwingConstants.CENTER);
            add(idText);
            add(idField);
            add(pwdText);
            add(pwdField);
            idField.setText(new DataManager().getId());
            pwdField.setText(new DataManager().getPwd());
        }

        public String getId() {
            return idField.getText();
        }

        public String getPwd() {
            return String.valueOf(pwdField.getPassword());
        }

        public void setPwdFieldListener(ActionListener listener) {
            pwdField.addActionListener(listener);
        }
    }
}


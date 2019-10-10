package dev.haenara.otp.view;

import dev.haenara.otp.Main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Frame introduce about developer.
 */
public class AboutFrame extends JFrame {
    private static final String TITLE = "About OTP Email Reader...";
    private static final int WIDTH = 400;
    private static final int HEIGHT = 400;
    private static final int X = 200;
    private static final int Y = 100;
    private static final String EMAIL = "hamster12345@gmail.com";

    private JLabel labelImage = new JLabel(new ImageIcon(Main.class.getResource("/resources/icon/otp.png")));

    public AboutFrame(){
        super(TITLE);
        setBounds(X, Y, WIDTH, HEIGHT);
        setLayout(new BorderLayout());
        add(new BottomPanel(), BorderLayout.SOUTH);
        labelImage.setSize(new Dimension(200, 200));
        add(labelImage, BorderLayout.CENTER);
        setMaximumSize(new Dimension(WIDTH, HEIGHT));
        setMinimumSize(new Dimension(WIDTH, HEIGHT));
        setVisible(true);
    }
    class BottomPanel extends JPanel {
        private JLabel labelDevelopedBy = new JLabel("Developed by ");
        private JLabel labelEmail = new JLabel("<html><a href=#>Haenara Shin</a></html>");
        private JLabel labelCopyright1 = new JLabel("ⓒ 2019. Haenara");
        private JLabel labelCopyright2 = new JLabel(" All rights reserved.");

        public BottomPanel() {
            setLayout(new GridLayout(3,2));
            add(labelDevelopedBy);
            add(labelEmail);
            add(labelCopyright1);
            add(labelCopyright2);
            add(new JLabel(""));

            labelDevelopedBy.setHorizontalAlignment(SwingConstants.RIGHT);
            labelEmail.setHorizontalAlignment(SwingConstants.LEFT);
            labelCopyright1.setHorizontalAlignment(SwingConstants.RIGHT);
            labelCopyright2.setHorizontalAlignment(SwingConstants.LEFT);

            labelEmail.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    try {
                        Desktop.getDesktop().mail(new URI("mailto:" + EMAIL));
                    } catch (URISyntaxException | IOException ex) {

                    }
                }
            });

        }
    }
}




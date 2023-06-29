package org.homemade.services.graphics;

import javax.swing.*;
import java.awt.*;

public class FrmPopUpInfo extends JDialog {
    public boolean isCancel = false;

    private final String message;

    public FrmPopUpInfo(String message){
        this.message = message;
        initComponents();
    }

    private void initComponents() {
        Dimension scrSize = Toolkit.getDefaultToolkit().getScreenSize();// size of the screen
        Insets toolHeight = Toolkit.getDefaultToolkit().getScreenInsets(getGraphicsConfiguration());// height of the task bar
        setLocation(scrSize.width - 275, scrSize.height - toolHeight.bottom - 120);
        ImageIcon image;

        setSize(225,120);
        setLayout(null);
        setUndecorated(true);
        setLayout(new GridBagLayout());

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.weightx = 1.0f;
        constraints.weighty = 1.0f;
        constraints.insets = new Insets(5, 5, 5, 5);
        constraints.fill = GridBagConstraints.BOTH;

        JLabel headingLabel = new JLabel(message);
        headingLabel.setOpaque(false);

        add(headingLabel, constraints);

        constraints.gridx++;
        constraints.weightx = 0f;
        constraints.weighty = 0f;
        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.NORTH;

        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        setVisible(true);
        setAlwaysOnTop(true);

        new Thread(){
            @Override
            public void run() {
                try {
                    Thread.sleep(5000); // time after which pop up will be disappeared.
                    dispose();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            };
        }.start();
    }
}

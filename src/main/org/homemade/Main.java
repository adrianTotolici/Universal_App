package org.homemade;

import org.homemade.main.MainGuiLogic;

import javax.swing.*;

public class Main {

    static JFrame window = new JFrame();

    public static void main(String[] args) {
        window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        window.setExtendedState(JFrame.MAXIMIZED_BOTH);
        window.setName(DefaultLang.windowName);
        window.setUndecorated(false);
        window.setVisible(true);
        MainGuiLogic.getInstance(window).init();
    }
}
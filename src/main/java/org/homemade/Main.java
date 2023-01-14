package org.homemade;

import org.homemade.main.MainGuiLogic;

import javax.swing.*;

public class Main {

    static JFrame window = new JFrame();

    public static void main(String[] args) {
        window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        window.setSize(Constants.WINDOW_SIZE);
        window.setName(DefaultLang.windowName);
        window.setVisible(true);
        MainGuiLogic.getInstance(window).init();
    }
}
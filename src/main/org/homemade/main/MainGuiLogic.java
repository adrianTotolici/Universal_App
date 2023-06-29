package org.homemade.main;

import org.homemade.Utils;
import org.homemade.services.ServicesLogic;
import org.homemade.stockmanager.GuiStockLogic;

import javax.swing.*;

public class MainGuiLogic {

    private static MainGuiLogic instance;
    private final JFrame jFrame;
    private JButton exitButton;
    private JButton stockButton;
    private JPanel mainMenuPanel;
    private JButton stockServiceButton;

    public static MainGuiLogic getInstance(JFrame jFrame){
        if (instance == null){
            Utils.Log("(Main Menu) Initialize single tone.");
            instance = new MainGuiLogic(jFrame);
        }
        return instance;
    }

    public MainGuiLogic(JFrame jFrame){
        this.jFrame = jFrame;
    }

    private void showMainMenu(){

        exitButton.setText(DefaultLang.exitButtonText);
        stockButton.setText(DefaultLang.stockManagerButtonText);
        stockServiceButton.setText(DefaultLang.stockServiceButtonText);

        Utils.Log("(Main Menu) Remove all JPanel from JFrame.");
        jFrame.getContentPane().removeAll();
        Utils.Log("(Main Menu) Add main menu panel to JFrame");
        jFrame.add(mainMenuPanel);
        Utils.Log("(Main Menu) Repaint to JFrame");
        jFrame.repaint();
    }

    public void init(){
        showMainMenu();

        exitButton.addActionListener(e -> {
            Utils.Log("(Main Menu) Exit application");
            System.exit(0);
        });
        stockButton.addActionListener(e -> {
            Utils.Log("(Main Menu) Call Stock_blob manager gui");
            GuiStockLogic.getInstance(jFrame).init();
        });
        stockServiceButton.addActionListener(e -> {
            Utils.Log("(Main Menu) Call stock background services");
            ServicesLogic.getInstance().callBackgroundService(jFrame);
        });
    }
}

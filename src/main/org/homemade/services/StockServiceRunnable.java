package org.homemade.services;

import javafx.application.Platform;
import java.awt.Toolkit;

public class StockServiceRunnable implements Runnable{
    @Override
    public void run() {
        initToolkit();
        ServicesLogic.getInstance().getTradingMetaData();
    }

    private void initToolkit() {
        // Initialize the JavaFX toolkit if it is not already initialized
//        if (!isToolkitInitialized()) {
        try {
            if (!Platform.isFxApplicationThread()) {
                Platform.startup(() -> {
                });
            }
        }catch (Exception e){
            e.printStackTrace();
        }

//        } else {
//            System.out.println("tool kit initialized");
//        }
    }

    private static boolean isToolkitInitialized() {
        try {
            Toolkit.getDefaultToolkit();
            return true;
        } catch (java.lang.InternalError e) {
            return false;
        }
    }
}

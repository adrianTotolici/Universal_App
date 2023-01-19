package org.homemade.stockmanager;

import java.awt.*;
import java.text.DecimalFormat;

public class Constants extends org.homemade.Constants {

    public static final Dimension spExitButton = new Dimension(0,0);
    public final static Dimension epExitButton = new Dimension(190, 30);

    public static final int settingsPanelHeight = 40;
    public static final int operationPanelHeight = 80;

    public static final String stockFilePath = "G:\\My Drive\\Universal App\\stock_file";

    public static final String[] columnNamesStockTable = {"Symbol", "Name", "Price", "Dividend / Quarter", "Own Shares",
            "Profit $",  "Profit Lei"};

    public static final String[] sectorComboBoxList = {"", "Consumer Cyclical", "Consumer Defensive", "Energy",
            "Financial Services", "Industrials", "Real Estate", "Technology", "Healthcare", "Communication Services",
            "Utilities", "Basic Materials"};


    public static final DecimalFormat currencyFormat = new DecimalFormat("#0.00");

}

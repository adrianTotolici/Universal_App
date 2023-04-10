package org.homemade.stockmanager;

import java.text.DecimalFormat;
import java.util.HashMap;

public class Constants extends org.homemade.Constants {

    public static final int USAIncomeTax = 10;
    public static final int GBIncomeTax = 0;
    public static final int FRIncomeTax = 25;
    public static final int IRIncomeTax = 25;
    public static final int TWIncomeTax = 21;

    public static final String Euro = "EUR";
    public static final String CanadianDollar = "CAD";
    public static final String Ron = "RON";
    public static final String Pounds = "GBP";

    public static final String stockFilePath = "G:\\My Drive\\Universal App\\stock_file";

    public static final String[] columnNamesStockTable = {"Symbol", "Name", "Price", "Dividend / Quarter", "Own Shares",
            "Investment", "Profit / Quarter", "Tax", "Profit / Quarter RON"};

    public static final String[] sectorComboBoxList = {"Consumer Cyclical", "Consumer Defensive", "Energy",
            "Financial Services", "Industrials", "Real Estate", "Technology", "Healthcare", "Communication Services",
            "Utilities", "Basic Materials"};
    private static final String[] consumerCyclicalIndustry = {"Auto & Truck Dealerships", "Restaurants", "Specialty Retail", "Luxury Goods"};
    private static final String[] consumerDefensiveIndustry = {"Household & Personal Products", "Beverages—Non-Alcoholic", "Tobacco", "Packaged Foods", "Food Distribution"};
    private static final String[] energyIndustry = {"Oil & Gas E&P", "Oil & Gas Midstream", "Oil & Gas Integrated", "Renewable Energy Infrastructure"};
    private static final String[] financialServicesIndustry = {"Asset Management", "Banks—Diversified", "Banks—Regional", "Insurance—Property & Casualty", "Credit Services"};
    private static final String[] industrialsIndustry = {"Specialty Industrial Machinery", "Aerospace & Defense", "Farm & Heavy Construction Machinery"};
    private static final String[] realEstateIndustry = {"REIT—Specialty", "REIT—Retail", "REIT—Diversified", "REIT—Mortgage"};
    public static final String[] technologyIndustry = {"Semiconductors", "Software—Infrastructure", "Consumer Electronics", "Information Technology Services"};
    public static final String[] healthcareIndustry = {"Drug Manufacturers—General"};
    public static final String[] communicationServicesIndustry = {"Telecom Services"};
    public static final String[] utilitiesIndustry = {"Utilities—Regulated Electric", "Utilities—Regulated Water", "Utilities—Regulated Gas"};
    public static final String[] basicMaterialsIndustry = {"Specialty Chemicals"};
    public static final DecimalFormat currencyFormat = new DecimalFormat("#0.00");
    public static final DecimalFormat dividendPayFormat = new DecimalFormat("#0.00");
    public static final HashMap<String, String[]> industryComboBoxList = initIndustry();
    public static final HashMap<String, String> shareSymbolReplacement = initSymbolReplacement();

    private static HashMap<String, String> initSymbolReplacement() {
        HashMap<String, String> shareSymbolReplacement = new HashMap<>();
        shareSymbolReplacement.put("LVMH", "MC.PA");
        shareSymbolReplacement.put("TRIG", "TRIG.L");
        shareSymbolReplacement.put("BSIF", "BSIF.L");
        return shareSymbolReplacement;
    }

    private static HashMap<String, String[]> initIndustry(){
        HashMap<String, String[]> industry = new HashMap<>();
        industry.put(sectorComboBoxList[0], consumerCyclicalIndustry);
        industry.put(sectorComboBoxList[1], consumerDefensiveIndustry);
        industry.put(sectorComboBoxList[2], energyIndustry);
        industry.put(sectorComboBoxList[3], financialServicesIndustry);
        industry.put(sectorComboBoxList[4], industrialsIndustry);
        industry.put(sectorComboBoxList[5], realEstateIndustry);
        industry.put(sectorComboBoxList[6], technologyIndustry);
        industry.put(sectorComboBoxList[7], healthcareIndustry);
        industry.put(sectorComboBoxList[8], communicationServicesIndustry);
        industry.put(sectorComboBoxList[9], utilitiesIndustry);
        industry.put(sectorComboBoxList[10], basicMaterialsIndustry);

        return industry;
    }
}

package org.homemade.stockmanager;

import org.homemade.Utils;
import org.homemade.stockmanager.blobs.Stock_blob;
import org.json.JSONObject;
import yahoofinance.Stock;
import yahoofinance.YahooFinance;

import java.io.*;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

import java.io.FileInputStream;
import java.io.IOException;

import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;

public class Logic {

    private static Logic instance;
    private HashMap<String, Object> stockList = new HashMap<>();
    private double exchangeRateRON;
    private double exchangeRateEUR;
    private double exchangeRateCAD;
    private double exchangeRateGBP;
    private final boolean development = true;
    private static String stockFilePath;

    public void setStockFilePath(String filePath){
        stockFilePath = filePath;
    }

    public static Logic getInstance(){
        if (instance == null){
            instance = new Logic();
        }
        return instance;
    }

    private Logic(){
        init();
    }

    private void init(){
        try {
            Path stockFile = Path.of(Constants.stockFilePath);
            if  ( ! Files.exists(stockFile)) {
                Files.createDirectories(stockFile.getParent());
                Files.createFile(stockFile);
            }
            getExchangeRates();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public void getExchangeRates(){
        exchangeRateRON = getExchangeRate(Constants.Ron);
        Utils.Log("Exchange rate for RON: " + exchangeRateRON);
        exchangeRateCAD = getExchangeRate(Constants.CanadianDollar);
        Utils.Log("Exchange rate for CAD: " + exchangeRateCAD);
        exchangeRateEUR = getExchangeRate(Constants.Euro);
        Utils.Log("Exchange rate for EUR: " + exchangeRateEUR);
        exchangeRateGBP = getExchangeRate(Constants.Pounds);
        Utils.Log("Exchange rate for GBP: " + exchangeRateGBP);
    }

    public double getExchangeRateRON() {
        return exchangeRateRON;
    }

    public double getExchangeRateEUR() {
        return exchangeRateEUR;
    }

    public double getExchangeRateCAD() {
        return exchangeRateCAD;
    }

    public double getExchangeRateGBP() {
        return exchangeRateGBP;
    }

    public void getStock(String name){
        try {
            Stock stock = YahooFinance.get(name);
            Stock_blob stockBlob = new Stock_blob();
            stockBlob.setName(stock.getName());
            stockBlob.setSymbol(stock.getSymbol());
            stockBlob.setValue(stock.getQuote(true).getPrice());

            if (! stockList.containsKey(stockBlob.getSymbol().toUpperCase())) {
                stockList.put(stockBlob.getSymbol(), stockBlob);
                saveStock();
            }

            loadStockData(stockFilePath);
        } catch (RuntimeException e){
            Utils.Log(e.getMessage());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings (value="unchecked")
    public HashMap<String, Stock_blob> loadStockData(String stockFilePath) {
        HashMap<String, Stock_blob> stockBlobs = new HashMap<>();
        try {
            FileInputStream fi = new FileInputStream(stockFilePath);
            ObjectInputStream oi = new ObjectInputStream(fi);

            stockList = (HashMap<String, Object>) oi.readObject();
            for (String key : stockList.keySet()) {
                Stock_blob stock = (Stock_blob) stockList.get(key);
                stockBlobs.put(key,stock);
            }

            oi.close();
            fi.close();

        } catch (IOException | ClassNotFoundException e) {
            Utils.Log("Something went wrong with FileInputStream variable.");
        }
        return stockBlobs;
    }

    public Stock_blob getAddedStock(String symbol){
        if (stockList.containsKey(symbol)){
            return (Stock_blob) stockList.get(symbol);
        }else {
            return null;
        }
    }

    public void removeStock(String symbol){
        if (stockList.containsKey(symbol)){
            stockList.remove(symbol);
            saveStock();
        }
    }

    public void updateStock(Stock_blob stockBlob){
        Stock_blob old_stock;
        if (stockList.get(stockBlob.getSymbol()) == null) {
            old_stock = new Stock_blob();
        }else {
            old_stock = (Stock_blob) stockList.get(stockBlob.getSymbol());
        }

        old_stock.setIndustry(stockBlob.getIndustry());
        old_stock.setDivPerQ(stockBlob.getDivPerQ());
        old_stock.setOwnShares(stockBlob.getOwnShares());
        old_stock.setInvestment(stockBlob.getInvestment());
        old_stock.setSector(stockBlob.getSector());
        old_stock.setPayData(stockBlob.getPayData());

        if (stockList.get(stockBlob.getSymbol()) != null) {
            stockList.replace(stockBlob.getSymbol(), old_stock);
        }else {
            stockList.put(stockBlob.getSymbol(), old_stock);
        }

        saveStock();
    }

    public double getExchangeRate(String currency) {

        if (development) {
            if (currency.equals(Constants.Euro)) return Constants.EURO;
            if (currency.equals(Constants.CanadianDollar)) return Constants.CAD;
            if (currency.equals(Constants.Pounds)) return Constants.GBP;
            if (currency.equals(Constants.Ron)) return Constants.RON;
            if (currency.equals("USD")) return 1;
        }else {
            try {
                // Your Open Exchange Rates app_id
                String appId = "4207fe17e2564aebb5f1627b5928f877";

                // URL for the Open Exchange Rates API
                String url = "https://openexchangerates.org/api/latest.json?app_id=" + appId;

                // Send the GET request to the API
                HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
                con.setRequestMethod("GET");

                // Read the API response
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuilder content = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }
                in.close();

                // Parse the JSON response
                JSONObject json = new JSONObject(content.toString());
                JSONObject rates = json.getJSONObject("rates");
                double exchangeRate = rates.getDouble(currency);

                // Print the exchange rate
                Utils.Log("Exchange rate for " + currency + ": " + exchangeRate);

                return exchangeRate;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return 0;
    }

    public void readXLSX(String xlsxPath) {
        int dividendIndexSheet = 4;
        int shareNameColumnIndex = 1;
        int shareIndustryColumnIndex = 13;
        int divPerQColumnIndex = 3;
        int ownSharesColumnIndex = 8;
        int investmentColumnIndex = 7;
        int sectorColumnIndex = 12;

        HashMap<String, String> shareSymbolReplacement = Constants.shareSymbolReplacement;

        try {
            FileInputStream fileInputStream = new FileInputStream(xlsxPath);
            XSSFWorkbook  workBook = new XSSFWorkbook (fileInputStream);
            XSSFSheet dividendAll = workBook.getSheetAt(dividendIndexSheet);
            for (Row row : dividendAll) {
                int rowNum = row.getRowNum();
                if (rowNum>=2){
                    String shareSymbol = row.getCell(shareNameColumnIndex).getStringCellValue();
                    for (String origSymbol : shareSymbolReplacement.keySet()) {
                        if (shareSymbol.equals(origSymbol)){
                            shareSymbol = shareSymbolReplacement.get(origSymbol);
                        }
                    }
                    getStock(shareSymbol);
                    String shareIndustry = row.getCell(shareIndustryColumnIndex).getStringCellValue();
                    double shareDivPerQ = (row.getCell(divPerQColumnIndex).getNumericCellValue())/100;
                    double ownShareNum = row.getCell(ownSharesColumnIndex).getNumericCellValue();
                    double investment = row.getCell(investmentColumnIndex).getNumericCellValue();
                    String sector = row.getCell(sectorColumnIndex).getStringCellValue();

                    Stock_blob stockBlob = getAddedStock(shareSymbol);
                    stockBlob.setIndustry(shareIndustry);
                    stockBlob.setDivPerQ(shareDivPerQ);
                    stockBlob.setOwnShares(ownShareNum);
                    stockBlob.setInvestment(investment);
                    stockBlob.setSector(sector);
                    switch (shareSymbol){
                        case "TRIG.L", "BSIF.L" -> {
                            stockBlob.setValue(BigDecimal.valueOf(stockBlob.getValue()/100));
                        }
                    }

                    updateStock(stockBlob);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void removeAllStock(){
        stockList.clear();
        saveStock();
    }

    public void saveStock() {
        Utils.saveData(stockList, stockFilePath);
        Utils.Log("The Object  was successfully written to a file");
    }

    public double getTotalInvested(){
        double totalInvested = 0;
        for (Object object : stockList.values()) {
            Stock_blob stockBlob = (Stock_blob) object;
            String shareSymbol = stockBlob.getSymbol();
            switch (shareSymbol){
                case "TRIG.L", "BSIF.L" ->
                    totalInvested += stockBlob.getInvestment()*getExchangeRateGBP();
                case "MC.PA" ->
                    totalInvested += stockBlob.getInvestment()*getExchangeRateEUR();
                case "ENB" ->
                    totalInvested += stockBlob.getInvestment()*getExchangeRateCAD();
                default ->
                    totalInvested += stockBlob.getInvestment();
            }
        }
        return totalInvested;
    }

    public double getTotalProfit(){
        double totalProfit = 0;
        for (Object object : stockList.values()) {
            Stock_blob stockBlob = (Stock_blob) object;
            String shareSymbol = stockBlob.getSymbol();
            switch (shareSymbol){
                case "TRIG.L", "BSIF.L" ->
                        totalProfit += stockBlob.getDivPerQ()*getExchangeRateGBP();
                case "MC.PA" ->
                        totalProfit += stockBlob.getDivPerQ()*getExchangeRateEUR();
                case "ENB" ->
                        totalProfit += stockBlob.getDivPerQ()*getExchangeRateCAD();
                default ->
                        totalProfit += stockBlob.getDivPerQ();
            }
        }
        Utils.Log("Total profit: "+totalProfit+" $");
        return totalProfit;
    }

    public double getTotalTax(){
        double totalTax = 0;
        for (Object object : stockList.values()) {
            Stock_blob stockBlob = (Stock_blob) object;
            String shareSymbol = stockBlob.getSymbol();
            switch (shareSymbol){
                case "TRIG.L", "BSIF.L" ->
                        totalTax += ((stockBlob.getDivPerQ()*Constants.GBIncomeTax) / 100)*getExchangeRateGBP();
                case "MC.PA" ->
                        totalTax += ((stockBlob.getDivPerQ()*Constants.FRIncomeTax) / 100)*getExchangeRateEUR();
                case "ENB" ->
                        totalTax += ((stockBlob.getDivPerQ()*Constants.USAIncomeTax) / 100)*getExchangeRateCAD();
                case "TSM" ->
                        totalTax += ((stockBlob.getDivPerQ())*Constants.TWIncomeTax) / 100;
                default ->
                        totalTax += ((stockBlob.getDivPerQ())*Constants.USAIncomeTax) / 100;
            }
        }
        Utils.Log("Total tax: "+totalTax+" $");
        return totalTax;
    }

    public double getInvestmentPercent(String sector){
        double totalSectorInvestment = 0;
        for (Object object : stockList.values()) {
            Stock_blob stockBlob = (Stock_blob) object;
            String shareSector = stockBlob.getSector();
            if (sector.equals(shareSector)){
                String shareSymbol = stockBlob.getSymbol();
                switch (shareSymbol){
                    case "TRIG.L", "BSIF.L" ->
                            totalSectorInvestment += stockBlob.getInvestment()*getExchangeRateGBP();
                    case "MC.PA" ->
                            totalSectorInvestment += stockBlob.getInvestment()*getExchangeRateEUR();
                    case "ENB" ->
                            totalSectorInvestment += stockBlob.getInvestment()*getExchangeRateCAD();
                    default ->
                            totalSectorInvestment += stockBlob.getInvestment();
                }
            }
        }
        double percent = (totalSectorInvestment * 100) / getTotalInvested();
        Utils.Log("Investment percent in "+sector+" sector: "+Constants.currencyFormat.format(percent)+" %");
        return percent;
    }

    public double getShareTax(String shareSymbol){
        double tax = 0;
        Stock_blob stockBlob = getAddedStock(shareSymbol);
        switch (shareSymbol){
            case "TRIG.L", "BSIF.L" ->
                    tax = ((stockBlob.getDivPerQ()*Constants.GBIncomeTax) / 100)*getExchangeRateGBP();
            case "MC.PA" ->
                    tax = ((stockBlob.getDivPerQ()*Constants.FRIncomeTax) / 100)*getExchangeRateEUR();
            case "ENB" ->
                    tax = ((stockBlob.getDivPerQ()*Constants.USAIncomeTax) / 100)*getExchangeRateCAD();
            case "TSM" ->
                    tax = ((stockBlob.getDivPerQ())*Constants.TWIncomeTax) / 100;
            default ->
                    tax = ((stockBlob.getDivPerQ())*Constants.USAIncomeTax) / 100;
        }
        Utils.Log("Tax for "+shareSymbol+" : "+tax+" $");
        return tax;
    }
}

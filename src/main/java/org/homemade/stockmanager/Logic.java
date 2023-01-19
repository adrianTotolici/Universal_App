package org.homemade.stockmanager;

import org.homemade.Utils;
import org.homemade.stockmanager.blobs.Stock_blob;
import org.json.JSONObject;
import yahoofinance.Stock;
import yahoofinance.YahooFinance;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

public class Logic {

    private static Logic instance;
    private HashMap<String, Object> stockList = new HashMap<>();
    private double exchangeRateRon;

    public static Logic getInstance(){
        if (instance == null) {
            instance = new Logic();

        }
        return instance;
    }

    public Logic(){
        init();
    }

    private void init(){
        exchangeRateRon = getExchangeRate("RON");
        try {
            Path stockFilePath = Path.of(Constants.stockFilePath);
            if  ( ! Files.exists(stockFilePath)) {
                Files.createDirectories(stockFilePath.getParent());
                Files.createFile(stockFilePath);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public double getExchangeRateRon() {
        return exchangeRateRon;
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
                Utils.saveData(stockList, Constants.stockFilePath);
                Utils.Log("The Object  was successfully written to a file");
            }

            loadStockData();
        } catch (RuntimeException e){
            Utils.Log(e.getMessage());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public HashMap<String, Stock_blob> loadStockData() {
        HashMap<String, Stock_blob> stockBlobs = new HashMap<>();
        try {
            FileInputStream fi = new FileInputStream(Constants.stockFilePath);
            ObjectInputStream oi = new ObjectInputStream(fi);

            stockList = (HashMap<String, Object>) oi.readObject();
            for (String key : stockList.keySet()) {
                Stock_blob stock = (Stock_blob) stockList.get(key);
                stockBlobs.put(key,stock);
            }

            oi.close();
            fi.close();

        } catch (IOException | ClassNotFoundException e) {
            System.out.print("df");
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
            Utils.saveData(stockList, Constants.stockFilePath);
        }
    }

    public void updateStock(Stock_blob stockBlob){
        Stock_blob old_stock = (Stock_blob) stockList.get(stockBlob.getSymbol());
        old_stock.setIndustry(stockBlob.getIndustry());
        old_stock.setDivPerQ(stockBlob.getDivPerQ());
        old_stock.setOwnShares(stockBlob.getOwnShares());
        old_stock.setInvestment(stockBlob.getInvestment());
        old_stock.setSector(stockBlob.getSector());

        stockList.replace(stockBlob.getSymbol(), old_stock);
        Utils.saveData(stockList, Constants.stockFilePath);
        Utils.Log("The Object  was successfully written to a file");

    }

    public double getExchangeRate(String currency) {

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
            StringBuffer content = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();

            // Parse the JSON response
            JSONObject json = new JSONObject(content.toString());
            JSONObject rates = json.getJSONObject("rates");
            double exchangeRate = rates.getDouble(currency);

            // Print the exchange rate
            System.out.println("Exchange rate for " + currency + ": " + exchangeRate);

            return exchangeRate;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}

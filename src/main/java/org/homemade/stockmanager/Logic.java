package org.homemade.stockmanager;

import org.homemade.Utils;
import org.homemade.stockmanager.blobs.Stock_blob;
import yahoofinance.Stock;
import yahoofinance.YahooFinance;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class Logic {

    private static Logic instance;
    private ArrayList<Object> stockList = new ArrayList<>();

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

    public void getStock(String name){
        try {
            Stock stock = YahooFinance.get(name);
            Stock_blob stockBlob = new Stock_blob();
            stockBlob.setName(stock.getName());
            stockBlob.setSymbol(stock.getSymbol());
            stockList.add(stockBlob);

            Utils.saveData(stockList,Constants.stockFilePath);
            Utils.Log("The Object  was successfully written to a file");

            loadStockData();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public ArrayList<Stock_blob> loadStockData() {
        ArrayList<Stock_blob> stockBlobs = new ArrayList<>();
        try {
            FileInputStream fi = new FileInputStream(Constants.stockFilePath);
            ObjectInputStream oi = new ObjectInputStream(fi);

            stockList = (ArrayList<Object>) oi.readObject();
            for (Object o : stockList) {
                Stock_blob stock = (Stock_blob) o;
                stockBlobs.add(stock);
            }

            oi.close();
            fi.close();

        } catch (IOException | ClassNotFoundException e) {
            System.out.print("df");
        }
        return stockBlobs;
    }

}

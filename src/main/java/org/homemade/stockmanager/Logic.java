package org.homemade.stockmanager;

import org.apache.poi.ss.usermodel.Cell;
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

import java.io.FileInputStream;
import java.io.IOException;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;

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

    private Logic(){
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

    @SuppressWarnings (value="unchecked")
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
            Utils.saveData(stockList, Constants.stockFilePath);
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

    public void readXLSX(String xlsxPath) {
        int dividendIndexSheet = 4;
        int shareNameColumnIndex = 1;
        int shareIndustryColumnIndex = 13;
        int divPerQColumnIndex = 2;
        int ownSharesColumnIndex = 8;
        int investmentColumnIndex = 7;
        int sectorColumnIndex = 12;

        HashMap<String, String> shareSymbolReplacement = new HashMap<>();
        shareSymbolReplacement.put("LVMH","MC.PA");
        shareSymbolReplacement.put("TRIG","TRIG.L");
        shareSymbolReplacement.put("BSIF","BSIF.L");

        try {
            FileInputStream fileInputStream = new FileInputStream("Dividende.xlsx");
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
                    double shareDivPerQ = row.getCell(divPerQColumnIndex).getNumericCellValue();
                    double ownShareNum = row.getCell(ownSharesColumnIndex).getNumericCellValue();
                    double investment = row.getCell(investmentColumnIndex).getNumericCellValue();
                    String sector = row.getCell(sectorColumnIndex).getStringCellValue();

                    Stock_blob stockBlob = getAddedStock(shareSymbol);
                    stockBlob.setIndustry(shareIndustry);
                    stockBlob.setDivPerQ(shareDivPerQ);
                    stockBlob.setOwnShares(ownShareNum);
                    stockBlob.setInvestment(investment);
                    stockBlob.setSector(sector);

                    updateStock(stockBlob);
                }
            }

//            XSSFSheet sheet = workBook.getSheetAt(4);
//            //evaluating cell type
//            FormulaEvaluator formulaEvaluator = workBook.getCreationHelper().createFormulaEvaluator();
//            for (Row row : sheet)     //iteration over row using for each loop
//            {
//                for (Cell cell : row)    //iteration over cell using for each loop
//                {
//                    switch (formulaEvaluator.evaluateInCell(cell).getCellType()) {
//                        //case Cell.CELL_TYPE_NUMERIC:   //field that represents numeric cell type
//                        case NUMERIC:   //field that represents numeric cell type
//                            //getting the value of the cell as a number
//                            System.out.print(cell.getNumericCellValue() + "\t\t");
//                            break;
//                        case STRING:    //field that represents string cell type
//                            //getting the value of the cell as a string
//                            System.out.print(cell.getStringCellValue() + "\t\t");
//                            break;
//
//                    }
//                }
//                System.out.println();
//            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void removeAllStock(){
        for (String symbol : stockList.keySet()) {
            removeStock(symbol);
        }
    }
}

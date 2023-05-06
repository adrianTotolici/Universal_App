package org.homemade.stockmanager;

import org.apache.commons.io.FileUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.homemade.Utils;
import org.homemade.stockmanager.blobs.Investment_blob;
import org.homemade.stockmanager.blobs.Stock_blob;
import org.json.JSONArray;
import org.json.JSONException;
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
import java.util.Objects;

import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;

public class Logic {

    private static Logic instance;
    private HashMap<String, Object> stockList = new HashMap<>();
    private HashMap<String, Object> investmentList = new HashMap<>();
    private double exchangeRateRON;
    private double exchangeRateEUR;
    private double exchangeRateCAD;
    private double exchangeRateGBP;
    private final boolean development = true;
    private static String stockFilePath;
    private String newsApiKey;
    private String exchangeRateApiKey;

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
            getApiKeys();
            getExchangeRates();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private void getApiKeys(){
        try {
            File apiKeyFile = new File(Constants.apiKey);
            exchangeRateApiKey = FileUtils.readLines(apiKeyFile).get(0);
            newsApiKey = FileUtils.readLines(apiKeyFile).get(1);
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
            Utils.Log(e.getMessage());
            Utils.Log("Yahoo server connection failed !!!!");
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
        old_stock.setPayDate(stockBlob.getPayDate());

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

                // URL for the Open Exchange Rates API
                String url = "https://openexchangerates.org/api/latest.json?app_id=" + exchangeRateApiKey;

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

        try {
            FileInputStream fileInputStream = new FileInputStream(xlsxPath);
            XSSFWorkbook  workBook = new XSSFWorkbook (fileInputStream);
            XSSFSheet dividendAll = workBook.getSheet(Constants.xmlDividendXMLSheetName);
            XSSFSheet qInvestmentDetail = workBook.getSheet(Constants.xmlQInvestmentDetail);

            readXLSAllShares(dividendAll);
            readXLSInvestment(qInvestmentDetail);

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
                        totalTax += ((stockBlob.getDivPerQ()*stockBlob.getOwnShares()*Constants.GBIncomeTax) / 100)*getExchangeRateGBP();
                case "MC.PA" ->
                        totalTax += ((stockBlob.getDivPerQ()*stockBlob.getOwnShares()*Constants.FRIncomeTax) / 100)*getExchangeRateEUR();
                case "ENB" ->
                        totalTax += ((stockBlob.getDivPerQ()*stockBlob.getOwnShares()*Constants.USAIncomeTax) / 100)*getExchangeRateCAD();
                case "TSM" ->
                        totalTax += ((stockBlob.getDivPerQ())*stockBlob.getOwnShares()*Constants.TWIncomeTax) / 100;
                default ->
                        totalTax += ((stockBlob.getDivPerQ())*stockBlob.getOwnShares()*Constants.USAIncomeTax) / 100;
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
                    tax = ((stockBlob.getDivPerQ()*stockBlob.getOwnShares()*Constants.GBIncomeTax) / 100)*getExchangeRateGBP();
            case "MC.PA" ->
                    tax = ((stockBlob.getDivPerQ()*stockBlob.getOwnShares()*Constants.FRIncomeTax) / 100)*getExchangeRateEUR();
            case "ENB" ->
                    tax = ((stockBlob.getDivPerQ()*stockBlob.getOwnShares()*Constants.USAIncomeTax) / 100)*getExchangeRateCAD();
            case "TSM" ->
                    tax = ((stockBlob.getDivPerQ())*stockBlob.getOwnShares()*Constants.TWIncomeTax) / 100;
            default ->
                    tax = ((stockBlob.getDivPerQ())*stockBlob.getOwnShares()*Constants.USAIncomeTax) / 100;
        }
        Utils.Log("Tax for "+shareSymbol+" : "+tax+" $");
        return tax;
    }

    public String getShareLatestNews(String shareSymbol){
        String urlString = "https://newsapi.org/v2/everything?q=" + shareSymbol + "&apiKey=" + newsApiKey;

        StringBuilder news= new StringBuilder();

        try {
            URL url = new URL(urlString);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            JSONObject jsonObject = new JSONObject(response.toString());
            JSONArray articles = jsonObject.getJSONArray("articles");
            for (int i = 0; i < articles.length(); i++) {
                JSONObject article = articles.getJSONObject(i);
                news.append(article.getString("title")).append("\n\n");
                news.append(article.getString("description")).append("\n\n");
                news.append(article.getString("url")).append("\n");
                news.append("-------------\n");
                news.append("\n");
            }
        } catch (IOException | JSONException e) {
            System.out.println(e.getMessage());
            news.append(DefaultLang.noNewsInfo).append(shareSymbol);
        }
        return news.toString();
    }

    public Investment_blob getAddedInvestment(String symbol){
        if (investmentList.containsKey(symbol)){
            return (Investment_blob) investmentList.get(symbol);
        }else {
            return null;
        }
    }

    private void readXLSAllShares(XSSFSheet dividendAll){
        int shareNameColumnIndex = 1;
        int div_shareIndustryColumnIndex = 13;
        int div_divPerQColumnIndex = 3;
        int div_ownSharesColumnIndex = 8;
        int div_investmentColumnIndex = 7;
        int div_sectorColumnIndex = 12;

        HashMap<String, String> shareSymbolReplacement = Constants.shareSymbolReplacement;

        for (Row row : dividendAll) {
            int rowNum = row.getRowNum();
            if (rowNum >= 2) {
                String shareSymbol = row.getCell(shareNameColumnIndex).getStringCellValue();
                for (String origSymbol : shareSymbolReplacement.keySet()) {
                    if (shareSymbol.equals(origSymbol)) {
                        shareSymbol = shareSymbolReplacement.get(origSymbol);
                    }
                }
                getStock(shareSymbol);
                String shareIndustry = row.getCell(div_shareIndustryColumnIndex).getStringCellValue();
                double shareDivPerQ = (row.getCell(div_divPerQColumnIndex).getNumericCellValue()) / 100;
                double ownShareNum = row.getCell(div_ownSharesColumnIndex).getNumericCellValue();
                double investment = row.getCell(div_investmentColumnIndex).getNumericCellValue();
                String sector = row.getCell(div_sectorColumnIndex).getStringCellValue();

                Stock_blob stockBlob = getAddedStock(shareSymbol);
                stockBlob.setIndustry(shareIndustry);
                stockBlob.setDivPerQ(shareDivPerQ);
                stockBlob.setOwnShares(ownShareNum);
                stockBlob.setInvestment(investment);
                stockBlob.setSector(sector);
                switch (shareSymbol) {
                    case "TRIG.L", "BSIF.L" -> {
                        stockBlob.setValue(BigDecimal.valueOf(stockBlob.getValue() / 100));
                    }
                }

                updateStock(stockBlob);
            }
        }
    }

    private void readXLSInvestment(XSSFSheet qInvestmentDetail){
        int filedColumns = 1;
        HashMap<String, String> shareSymbolReplacement = Constants.shareSymbolReplacement;

        Row row_header = qInvestmentDetail.getRow(1);
        for (int i = 1; i < row_header.getLastCellNum(); i++) {
            Cell cell = row_header.getCell(i);
            if (!cell.getStringCellValue().equals("")){
                filedColumns +=1;
            }
        }

        for (Row row : qInvestmentDetail) {
            int rowNum = row.getRowNum();
            if (rowNum>=2) {
                String shareSymbol = row.getCell(0).getStringCellValue();
                for (String origSymbol : shareSymbolReplacement.keySet()) {
                    if (shareSymbol.equals(origSymbol)){
                        shareSymbol = shareSymbolReplacement.get(origSymbol);
                    }
                }

                Investment_blob investmentBlob = getAddedInvestment(shareSymbol);
                if (investmentBlob == null){
                    investmentBlob = new Investment_blob();
                    investmentBlob.setStockSymbol(shareSymbol);
                }

                short prompter_exchange = 4;
                short prompter_investment = 5;
                short prompter_price = 6;
                short i=0;

                double exchange;
                double price;
                double investment;

                while (prompter_price<=filedColumns){
                    exchange = row.getCell(prompter_exchange).getNumericCellValue();
                    price = row.getCell(prompter_price).getNumericCellValue();
                    investment = row.getCell(prompter_investment).getNumericCellValue();
                    investmentBlob.setInvestment(investment,exchange,price);

                    if (i<=3){
                        prompter_price+=3;
                        prompter_exchange+=3;
                        prompter_investment+=3;
                        i+=1;
                    }else {
                        prompter_price+=4;
                        prompter_investment+=4;
                        prompter_exchange+=4;
                        i=0;
                    }
                }
            }
        }
    }
}

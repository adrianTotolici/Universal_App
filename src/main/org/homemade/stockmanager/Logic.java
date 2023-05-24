package org.homemade.stockmanager;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
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
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;

import javax.swing.*;

public class Logic {

    private static Logic instance;
    private HashMap<String, Object> stockList = new HashMap<>();
    private HashMap<String, Object> investmentList = new HashMap<>();
    private double exchangeRateRON;
    private double exchangeRateEUR;
    private double exchangeRateCAD;
    private double exchangeRateGBP;
    private static String stockFilePath;
    private static String investmentFilePath;
    private String newsApiKey;
    private String exchangeRateApiKey;
    private String alphaVantageApiKey;
    private int apiCalls = 0;
    private int apiCallsDay;

    public void setStockFilePath(String stockFilePath, String investmentFilePath) {
        Logic.stockFilePath = stockFilePath;
        Logic.investmentFilePath = investmentFilePath;
    }

    public static Logic getInstance() {
        if (instance == null) {
            instance = new Logic();
        }
        return instance;
    }

    private Logic() {
        init();
    }

    private void init() {
        try {
            Path stockFile = Path.of(Constants.stockFilePath);
            if (!Files.exists(stockFile)) {
                Files.createDirectories(stockFile.getParent());
                Files.createFile(stockFile);
            }
            getApiKeys();
            loadExchangeRate();
            loadApiCals();
        } catch (IOException e) {
            showPopUpError(e.getMessage());
            throw new RuntimeException(e);
        }

    }

    private void getApiKeys() {
        try {
            File apiKeyFile = new File(Constants.apiKey);
            exchangeRateApiKey = FileUtils.readLines(apiKeyFile).get(0);
            newsApiKey = FileUtils.readLines(apiKeyFile).get(1);
            alphaVantageApiKey = FileUtils.readLines(apiKeyFile).get(2);
        } catch (IOException e) {
            showPopUpError(e.getMessage());
            throw new RuntimeException(e);
        }
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

    public void getStock_yahoo(String name) {
        try {
            Stock stock = YahooFinance.get(name);
            Stock_blob stockBlob = new Stock_blob();
            stockBlob.setName(stock.getName());
            stockBlob.setSymbol(stock.getSymbol());
            stockBlob.setValue(stock.getQuote(true).getPrice());

            if (!stockList.containsKey(stockBlob.getSymbol().toUpperCase())) {
                stockList.put(stockBlob.getSymbol(), stockBlob);
                saveStock();
            }

            loadStockData(stockFilePath);
        } catch (RuntimeException e) {
            showPopUpError(e.getMessage());
            Utils.Log(e.getMessage());
        } catch (IOException e) {
            Utils.Log(e.getMessage());
            Utils.Log("Yahoo server connection failed !!!!");
            Utils.Log("Switching to alphaVantageApi.");
            getStock(name);
        }
    }

    public void getStock(String name){
        String apiKey = alphaVantageApiKey;
        String symbol = name;
        String apiUrl = "https://www.alphavantage.co/query?function=GLOBAL_QUOTE&symbol=" + symbol + "&apikey=" + apiKey;
        checkApiCalls();

        try {
            URL url = new URL(apiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String line;
                StringBuilder response = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                conn.disconnect();

                Gson gson = new Gson();
                JsonObject jsonResponse = gson.fromJson(response.toString(), JsonObject.class);
                JsonObject globalQuote = jsonResponse.getAsJsonObject("Global Quote");

                // Process the response
                Stock_blob stockBlob = new Stock_blob();
                stockBlob.setSymbol(globalQuote.get("01. symbol").getAsString());
                stockBlob.setValue(BigDecimal.valueOf(globalQuote.get("05. price").getAsDouble()));

                Utils.Log("Adding stock "+stockBlob.getSymbol());

                if (!stockList.containsKey(stockBlob.getSymbol().toUpperCase())) {
                    stockList.put(stockBlob.getSymbol(), stockBlob);
                    saveStock();
                }

                loadStockData(stockFilePath);

            } else {
                // Handle the error case
                System.out.println("Error: " + responseCode);
            }
        } catch (IOException e) {
            showPopUpError(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings(value = "unchecked")
    public HashMap<String, Stock_blob> loadStockData(String stockFilePath) {
        HashMap<String, Stock_blob> stockBlobs = new HashMap<>();
        try {
            FileInputStream fi = new FileInputStream(stockFilePath);
            ObjectInputStream oi = new ObjectInputStream(fi);

            stockList = (HashMap<String, Object>) oi.readObject();
            for (String key : stockList.keySet()) {
                Stock_blob stock = (Stock_blob) stockList.get(key);
                stockBlobs.put(key, stock);
            }

            oi.close();
            fi.close();

        } catch (IOException | ClassNotFoundException e) {
            showPopUpError(e.getMessage());
            Utils.Log("Something went wrong with FileInputStream variable.");
        }
        return stockBlobs;
    }

    @SuppressWarnings(value = "unchecked")
    public HashMap<String, Investment_blob> loadInvestmentData(String investmentFilePath) {
        HashMap<String, Investment_blob> investmentBlob = new HashMap<>();
        try {
            FileInputStream fi = new FileInputStream(investmentFilePath);
            ObjectInputStream oi = new ObjectInputStream(fi);

            investmentList = (HashMap<String, Object>) oi.readObject();
            for (String key : investmentList.keySet()) {
                Investment_blob investment = (Investment_blob) investmentList.get(key);
                investmentBlob.put(key, investment);
            }

            oi.close();
            fi.close();

        } catch (IOException | ClassNotFoundException e) {
            showPopUpError(e.getMessage());
            Utils.Log("Something went wrong with FileInputStream variable.");
        }
        return investmentBlob;
    }

    public Stock_blob getAddedStock(String symbol) {
        if (stockList.containsKey(symbol)) {
            return (Stock_blob) stockList.get(symbol);
        } else {
            return null;
        }
    }

    public void removeStock(String symbol) {
        if (stockList.containsKey(symbol)) {
            stockList.remove(symbol);
            investmentList.remove(symbol);
            saveStock();
            saveInvestment();
        }
    }

    public void updateStock(Stock_blob stockBlob) {
        Stock_blob old_stock;
        if (stockList.get(stockBlob.getSymbol()) == null) {
            old_stock = new Stock_blob();
        } else {
            old_stock = (Stock_blob) stockList.get(stockBlob.getSymbol());
        }

        old_stock.setIndustry(stockBlob.getIndustry());
        old_stock.setDivPerQ(stockBlob.getLastDicPerQ());
        old_stock.setOwnShares(stockBlob.getOwnShares());
        old_stock.setInvestment(stockBlob.getInvestment());
        old_stock.setSector(stockBlob.getSector());
        old_stock.setPayDate(stockBlob.getPayDate());
        old_stock.setName(stockBlob.getName());

        if (stockList.get(stockBlob.getSymbol()) != null) {
            stockList.replace(stockBlob.getSymbol(), old_stock);
        } else {
            stockList.put(stockBlob.getSymbol(), old_stock);
        }

        saveStock();
    }

    public double getExchangeRate(String currency, boolean readOnly) {

        if (readOnly) {
            if (currency.equals(Constants.Euro)) return exchangeRateEUR;
            if (currency.equals(Constants.CanadianDollar)) return exchangeRateCAD;
            if (currency.equals(Constants.Pounds)) return exchangeRateGBP;
            if (currency.equals(Constants.Ron)) return exchangeRateRON;
            if (currency.equals("USD")) return 1;
        } else {
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
                showPopUpError(e.getMessage());
                throw new RuntimeException(e);
            }
        }
        return 0;
    }

    public void readXLSX(String xlsxPath) {

        try {
            FileInputStream fileInputStream = new FileInputStream(xlsxPath);
            XSSFWorkbook workBook = new XSSFWorkbook(fileInputStream);
            XSSFSheet dividendAll = workBook.getSheet(Constants.xmlDividendXMLSheetName);
            XSSFSheet qInvestmentDetail = workBook.getSheet(Constants.xmlQInvestmentDetail);

            readXLSAllShares(dividendAll);
            readXLSInvestment(qInvestmentDetail);

        } catch (IOException e) {
            showPopUpError(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void removeAllStock() {
        stockList.clear();
        saveStock();
        investmentList.clear();
        saveInvestment();
    }

    public void saveStock() {
        Utils.saveData(stockList, stockFilePath);
        Utils.Log("The stock list  was successfully written to a file");
    }

    public void saveInvestment() {
        Utils.saveData(investmentList, investmentFilePath);
        Utils.Log("The Investment list was successfully written to " + investmentFilePath);
    }

    public double getTotalInvested() {
        double totalInvested = 0;
        for (Object object : stockList.values()) {
            Stock_blob stockBlob = (Stock_blob) object;
            String shareSymbol = stockBlob.getSymbol();
            switch (shareSymbol) {
                case "TRIG.L", "BSIF.L" -> totalInvested += stockBlob.getInvestment() * getExchangeRateGBP();
                case "MC.PA" -> totalInvested += stockBlob.getInvestment() * getExchangeRateEUR();
                case "ENB" -> totalInvested += stockBlob.getInvestment() * getExchangeRateCAD();
                default -> totalInvested += stockBlob.getInvestment();
            }
        }
        return totalInvested;
    }

    public double getTotalProfit() {
        double totalProfit = 0;
        for (Object object : stockList.values()) {
            Stock_blob stockBlob = (Stock_blob) object;
            String shareSymbol = stockBlob.getSymbol();
            totalProfit += getShareProfit(shareSymbol);
        }
        Utils.Log("Total profit: " + totalProfit + " $");
        return totalProfit;
    }

    public double getTotalTax() {
        double totalTax = 0;
        for (Object object : stockList.values()) {
            Stock_blob stockBlob = (Stock_blob) object;
            String shareSymbol = stockBlob.getSymbol();
            totalTax +=getShareTax(shareSymbol);
        }
        Utils.Log("Total tax: " + totalTax + " $");
        return totalTax;
    }

    public double getInvestmentPercent(String sector) {
        double totalSectorInvestment = 0;
        for (Object object : stockList.values()) {
            Stock_blob stockBlob = (Stock_blob) object;
            String shareSector = stockBlob.getSector();
            if (sector.equals(shareSector)) {
                String shareSymbol = stockBlob.getSymbol();
                switch (shareSymbol) {
                    case "TRIG.L", "BSIF.L" ->
                            totalSectorInvestment += stockBlob.getInvestment() * getExchangeRateGBP();
                    case "MC.PA" -> totalSectorInvestment += stockBlob.getInvestment() * getExchangeRateEUR();
                    case "ENB" -> totalSectorInvestment += stockBlob.getInvestment() * getExchangeRateCAD();
                    default -> totalSectorInvestment += stockBlob.getInvestment();
                }
            }
        }
        double percent = (totalSectorInvestment * 100) / getTotalInvested();
        Utils.Log("Investment percent in " + sector + " sector: " + Constants.currencyFormat.format(percent) + " %");
        return percent;
    }

    public double getShareTax(String shareSymbol) {
        double tax;
        Stock_blob stockBlob = getAddedStock(shareSymbol);
        switch (shareSymbol) {
            case "TRIG.L", "BSIF.L" ->
                    tax = ((stockBlob.getLastDicPerQ() * stockBlob.getOwnShares() * Constants.GBIncomeTax) / 100) * getExchangeRateGBP();
            case "MC.PA" ->
                    tax = ((stockBlob.getLastDicPerQ() * stockBlob.getOwnShares() * Constants.FRIncomeTax) / 100) * getExchangeRateEUR();
            case "ENB" ->
                    tax = ((stockBlob.getLastDicPerQ() * stockBlob.getOwnShares() * Constants.USAIncomeTax) / 100) * getExchangeRateCAD();
            case "TSM" -> tax = ((stockBlob.getLastDicPerQ()) * stockBlob.getOwnShares() * Constants.TWIncomeTax) / 100;
            default -> tax = ((stockBlob.getLastDicPerQ()) * stockBlob.getOwnShares() * Constants.USAIncomeTax) / 100;
        }
        Utils.Log("Tax for " + shareSymbol + " : " + tax + " $");
        return tax;
    }

    public double getShareProfit(String shareSymbol){
        double profit;
        Stock_blob stockBlob = getAddedStock(shareSymbol);
        switch (shareSymbol) {
            case "TRIG.L", "BSIF.L" -> profit = stockBlob.getLastDicPerQ() * getExchangeRateGBP() * stockBlob.getOwnShares()/100;
            case "MC.PA" -> profit = stockBlob.getLastDicPerQ() * getExchangeRateEUR() * stockBlob.getOwnShares();
            case "ENB" -> profit = stockBlob.getLastDicPerQ() * getExchangeRateCAD() * stockBlob.getOwnShares();
            default -> profit = stockBlob.getLastDicPerQ() * stockBlob.getOwnShares();
        }
        return profit;
    }

    public String getShareLatestNews(String shareSymbol) {
        String urlString = "https://newsapi.org/v2/everything?q=" + shareSymbol + "&apiKey=" + newsApiKey;

        StringBuilder news = new StringBuilder();

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
                news.append("------------").append(article.getString("publishedAt").split("T")[0]).append("------------\n");
                news.append(article.getString("title")).append("\n\n");
                news.append(article.getString("description")).append("\n\n");
                news.append(article.getString("url")).append("\n");
                news.append("\n");
            }
        } catch (IOException | JSONException e) {
            System.out.println(e.getMessage());
            showPopUpError(e.getMessage());
            news.append(DefaultLang.noNewsInfo).append(shareSymbol);
        }
        return news.toString();
    }

    public Investment_blob getAddedInvestment(String symbol) {
        if (investmentList.containsKey(symbol)) {
            return (Investment_blob) investmentList.get(symbol);
        } else {
            return null;
        }
    }

    private void checkApiCalls() {
        if (apiCallsDay > 0) {
            if (apiCalls < 5) {
                apiCalls += 1;
                apiCallsDay -= 1;
            } else {
                try {
                    Utils.Log("Waiting 1 min because you are using a free api finance !!!");
                    TimeUnit.MINUTES.sleep(1);
                    apiCalls = 1;
                    apiCallsDay -= 1;
                } catch (InterruptedException e) {
                    showPopUpError(e.getMessage());
                    throw new RuntimeException(e);
                }
            }
            saveApiCalls();
        }else {
            Utils.Log("You are using a free api finance !!! Maxim api calls per day have been reached");
        }
    }

    private void readXLSAllShares(XSSFSheet dividendAll) {
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
                getStock_yahoo(shareSymbol);
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

    private void readXLSInvestment(XSSFSheet qInvestmentDetail) {
        int filedColumns = 1;
        HashMap<String, String> shareSymbolReplacement = Constants.shareSymbolReplacement;

        Row row_header = qInvestmentDetail.getRow(1);
        for (int i = 1; i < row_header.getLastCellNum(); i++) {
            Cell cell = row_header.getCell(i);
            if (!cell.getStringCellValue().equals("")) {
                filedColumns += 1;
            }
        }

        for (Row row : qInvestmentDetail) {
            int rowNum = row.getRowNum();
            if ((rowNum >= 2)) {
                String shareSymbol = row.getCell(0).getStringCellValue();
                for (String origSymbol : shareSymbolReplacement.keySet()) {
                    if (shareSymbol.equals(origSymbol)) {
                        shareSymbol = shareSymbolReplacement.get(origSymbol);
                    }
                }

                Investment_blob investmentBlob = getAddedInvestment(shareSymbol);
                if (investmentBlob == null) {
                    investmentBlob = new Investment_blob();
                    investmentBlob.setStockSymbol(shareSymbol);
                }

                short prompter_exchange = 2;
                short prompter_investment = 3;
                short prompter_price = 4;
                short i = 0;

                double exchange;
                double price;
                double investment;

                while (prompter_price < filedColumns - 1) {
                    exchange = row.getCell(prompter_exchange).getNumericCellValue();
                    price = row.getCell(prompter_price).getNumericCellValue();
                    investment = row.getCell(prompter_investment).getNumericCellValue();
                    investmentBlob.setInvestment(investment, exchange, price);

                    if (i < 3) {
                        prompter_price += 3;
                        prompter_exchange += 3;
                        prompter_investment += 3;
                        i += 1;
                    } else {
                        prompter_price += 4;
                        prompter_investment += 4;
                        prompter_exchange += 4;
                        i = 0;
                    }
                }
                investmentList.put(shareSymbol, investmentBlob);
            }
        }

        saveInvestment();
    }

    public double computeNecessaryInvestment(String shareSymbol) {
        double necessaryInvestment;
        Investment_blob addedInvestment = getAddedInvestment(shareSymbol);
        HashMap<Double, HashMap<Double, Double>> investment_struct = addedInvestment.getInvestment();

        double mediumSharePrice = 0;
        double mediumInvestment = 0;
        int inv_size = 0;

        for (Double investment : investment_struct.keySet()) {
            HashMap<Double, Double> hashMap = investment_struct.get(investment);
            for (Double exchangeRate : hashMap.keySet()) {
                if (hashMap.get(exchangeRate) != 0) {
                    mediumInvestment += investment * exchangeRate;
                    mediumSharePrice += hashMap.get(exchangeRate);
                    inv_size += 1;
                }
            }
        }
        mediumInvestment = mediumInvestment / inv_size;
        int iter = 1;
        if (mediumSharePrice > 0) {
            mediumSharePrice = mediumSharePrice / inv_size;
            double currentSharePrice = getAddedStock(shareSymbol).getValue();
            double percentDiff = 0;
            if (mediumSharePrice > currentSharePrice) {
                while ((percentDiff < 30) && (iter<=10)) {
                    double newMediumPrice = (mediumSharePrice + (currentSharePrice * iter)) / (iter + 1);
                    double diff = mediumSharePrice - newMediumPrice;
                    percentDiff = (diff * 100) / newMediumPrice;
                    iter += 1;
                }
            }
        }
        necessaryInvestment = mediumInvestment * iter * getExchangeRateRON();
        return necessaryInvestment;
    }

    private void saveApiCalls(){
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(Constants.apiCallsDay);
            DataOutputStream dataOutputStream = new DataOutputStream(fileOutputStream);

            dataOutputStream.writeInt(apiCallsDay);

            dataOutputStream.close();
            fileOutputStream.close();
            Utils.Log("Save finance api calls with value: "+apiCallsDay);
        } catch (IOException e) {
            showPopUpError(e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadApiCals() {
        try {
            FileInputStream fileInputStream = new FileInputStream(Constants.apiCallsDay);
            DataInputStream dataInputStream = new DataInputStream(fileInputStream);

            int readNumber = dataInputStream.readInt();

            dataInputStream.close();
            fileInputStream.close();

            apiCallsDay = readNumber;
            Utils.Log("Load finance api calls with value: "+apiCallsDay);
        } catch (IOException e) {
            showPopUpError(e.getMessage());
            e.printStackTrace();
        }
    }

    private void showPopUpError(String message){
        JOptionPane.showMessageDialog(null, message, "", JOptionPane.WARNING_MESSAGE);
    }

    public void updateExchangeRates(){
        exchangeRateRON = getExchangeRate(Constants.Ron, false);
        exchangeRateCAD = getExchangeRate(Constants.CanadianDollar, false);
        exchangeRateEUR = getExchangeRate(Constants.Euro, false);
        exchangeRateGBP = getExchangeRate(Constants.Pounds, false);
        saveExchangeRate();
    }

    private void loadExchangeRate(){
        try {
            File apiKeyFile = new File(Constants.stockExchange);
            exchangeRateEUR = Double.parseDouble(FileUtils.readLines(apiKeyFile).get(0));
            exchangeRateCAD = Double.parseDouble(FileUtils.readLines(apiKeyFile).get(1));
            exchangeRateRON = Double.parseDouble(FileUtils.readLines(apiKeyFile).get(2));
            exchangeRateGBP = Double.parseDouble(FileUtils.readLines(apiKeyFile).get(3));
        } catch (IOException e) {
            showPopUpError(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private void saveExchangeRate(){
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(Constants.stockExchange);
            DataOutputStream dataOutputStream = new DataOutputStream(fileOutputStream);

            dataOutputStream.writeBytes(exchangeRateEUR+"\n"+exchangeRateCAD+"\n"+exchangeRateRON+"\n"+exchangeRateGBP);

            dataOutputStream.close();
            fileOutputStream.close();
            Utils.Log("Save finance api calls with value: "+apiCallsDay);
        } catch (IOException e) {
            showPopUpError(e.getMessage());
            e.printStackTrace();
        }
    }

    public void importTrading212CSV(String filePath){
        List<Stock_blob> stockBlobList = new ArrayList<>();
        List<Investment_blob> investmentBlobs = new ArrayList<>();
        try {
            List<String> allLines = Files.readAllLines(Paths.get(filePath));
            for (String line : allLines) {
                String[] split = line.split(",");
                if (split[0].matches("Market [a-z]+")){
                    Stock_blob stockBlob = new Stock_blob();
                    stockBlob.setSymbol(split[3]);
                    stockBlob.setName(split[4].split("\"")[1].split("\"")[0]);
                    stockBlob.setOwnShares(Double.parseDouble(split[5]));
                    Double interimInvestment = Double.parseDouble(split[10])*Double.parseDouble(split[8]);
                    switch (split[7]){
                        case "EUR" ->
                                stockBlob.setInvestment(interimInvestment*getExchangeRateEUR());
                        case "GBX" ->
                                stockBlob.setInvestment(interimInvestment*getExchangeRateGBP()/100);
                        default ->
                            stockBlob.setInvestment(interimInvestment);
                    }

                    stockBlobList.add(stockBlob);

                    Investment_blob investmentBlob = new Investment_blob();
                    investmentBlob.setStockSymbol(split[3]);
                    investmentBlob.setInvestment(stockBlob.getInvestment(), Double.parseDouble(split[8]),
                            Double.parseDouble(split[6]));

                    investmentBlobs.add(investmentBlob);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

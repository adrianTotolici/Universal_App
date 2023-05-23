package org.homemade.stockmanager;

import org.apache.commons.io.FileUtils;
import org.homemade.stockmanager.blobs.Investment_blob;
import org.homemade.stockmanager.blobs.Stock_blob;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static org.homemade.stockmanager.Constants.USAIncomeTax;

public class LogicTest {

    public static String TEST_STOCK = "AMD";
    public static String TEST_SECOND_STOCK = "INTC";
    public static String TEST_THIRD_STOCK = "MC";
    public static String TEST_EUR_STOCK = "MC.PA";
    public static String TEST_GBP_STOCK = "TRIG.L";
    public static String TEST_CAD_STOCK = "ENB";
    public static String INVALID_TEST_STOCK = "TEST";
    public static String TEST_INDUSTRY_VALUE = "Semiconductors";
    public static double TEST_INVESTMENT_VALUE = 23;
    public static double TEST_DIVIDEND_PERQ = 10;

    public static String TEST_STOCK_FILE_PATH = "src/test/resources/stock_file";
    public static String TEST_STOCK_FILE_DIVIDEND_PATH = "src/test/resources/Dividende_test.xlsx";

    public static String TEST_SAVE_STOCK_FILE = "src/test/resources/test_stock_file_path";
    public static String TEST_SAVE_INVESTMENT_FILE = "src/test/resources/test_investment_file_path";
    public static String TEST_NEW_DIRECTORY_LOCATION = "src/test/resources/new_location";

    public static boolean test = true;

    @Test
    public void getInstance() {
        Logic firstCallInstance = Logic.getInstance();
        Logic secondCallInstance = Logic.getInstance();

        Assertions.assertSame(firstCallInstance, secondCallInstance);
    }

    @Test
    public void getExchangeRateRon() {
        Assertions.assertEquals(Logic.getInstance().getExchangeRate(Constants.Ron, test), Logic.getInstance().getExchangeRateRON());
    }

    @Test
    public void getExchangeRateEUR() {
        Assertions.assertEquals(Logic.getInstance().getExchangeRate(Constants.Euro, test), Logic.getInstance().getExchangeRateEUR());
    }

    @Test
    public void getExchangeRateCAD() {
        Assertions.assertEquals(Logic.getInstance().getExchangeRate(Constants.CanadianDollar, test), Logic.getInstance().getExchangeRateCAD());
    }

    @Test
    public void getExchangeRateGBP() {
        Assertions.assertEquals(Logic.getInstance().getExchangeRate(Constants.Pounds, test), Logic.getInstance().getExchangeRateGBP());
    }

    @Test
    public void getStock() {
        Logic.getInstance().setStockFilePath(TEST_SAVE_STOCK_FILE, TEST_SAVE_INVESTMENT_FILE);
        Logic.getInstance().getStock(TEST_STOCK);
        Stock_blob stockBlob = Logic.getInstance().getAddedStock(TEST_STOCK);
        Assertions.assertEquals(TEST_STOCK, stockBlob.getSymbol().toUpperCase());
        Logic.getInstance().removeAllStock();
    }

    @Test
    public void loadStockData() {
        Logic.getInstance().setStockFilePath(TEST_SAVE_STOCK_FILE, TEST_SAVE_INVESTMENT_FILE);
        Logic.getInstance().getStock(TEST_STOCK);
        HashMap<String, Stock_blob> stockBlob = Logic.getInstance().loadStockData(TEST_STOCK_FILE_PATH);
        Assertions.assertNotNull(stockBlob.get(TEST_STOCK));
        Logic.getInstance().removeAllStock();
    }

    @Test
    public void getAddedStock() {
        Logic.getInstance().setStockFilePath(TEST_SAVE_STOCK_FILE, TEST_SAVE_INVESTMENT_FILE);
        Logic.getInstance().getStock(TEST_STOCK);
        Stock_blob stockBlob = Logic.getInstance().getAddedStock(TEST_STOCK);
        Assertions.assertEquals(TEST_STOCK, stockBlob.getSymbol());

        stockBlob = Logic.getInstance().getAddedStock(INVALID_TEST_STOCK);
        Assertions.assertNull(stockBlob);
        Logic.getInstance().removeAllStock();
    }

    @Test
    public void updateStock() {
        Logic.getInstance().setStockFilePath(TEST_SAVE_STOCK_FILE, TEST_SAVE_INVESTMENT_FILE);
        Logic.getInstance().getStock(TEST_STOCK);
        Stock_blob stockBlob = Logic.getInstance().getAddedStock(TEST_STOCK);
        stockBlob.setIndustry(TEST_INDUSTRY_VALUE);

        Logic.getInstance().updateStock(stockBlob);
        Stock_blob stockBlobUpdated = Logic.getInstance().getAddedStock(TEST_STOCK);

        Assertions.assertSame(stockBlob, stockBlobUpdated);
        Assertions.assertEquals(TEST_INDUSTRY_VALUE, stockBlobUpdated.getIndustry());
        Logic.getInstance().removeAllStock();
    }

    @Test
    public void removeStock() {
        Logic.getInstance().setStockFilePath(TEST_SAVE_STOCK_FILE, TEST_SAVE_INVESTMENT_FILE);
        Logic.getInstance().getAddedStock(TEST_STOCK);
        Logic.getInstance().removeStock(TEST_STOCK);

        Stock_blob stockBlob = Logic.getInstance().getAddedStock(TEST_STOCK);
        Assertions.assertNull(stockBlob);
        Logic.getInstance().removeAllStock();
    }

    @Test
    public void getExchangeRate() {
        Assertions.assertTrue(Logic.getInstance().getExchangeRate(Constants.Euro, test) > 0);
        Assertions.assertEquals(1, Logic.getInstance().getExchangeRate("USD", test));
    }

    @Test
    public void readXLSX() {
        Logic.getInstance().setStockFilePath(TEST_SAVE_STOCK_FILE, TEST_SAVE_INVESTMENT_FILE);
        Logic.getInstance().readXLSX(TEST_STOCK_FILE_DIVIDEND_PATH);
        Stock_blob stockBlob = Logic.getInstance().getAddedStock(TEST_STOCK);
        Assertions.assertEquals(stockBlob.getSymbol(), TEST_STOCK);
        Assertions.assertEquals(0.625, stockBlob.getDivPerQ());
        Logic.getInstance().removeAllStock();
    }

    @Test
    public void removeAllStock() {
        Logic.getInstance().setStockFilePath(TEST_SAVE_STOCK_FILE, TEST_SAVE_INVESTMENT_FILE);
        Logic.getInstance().getStock(TEST_STOCK);
        Stock_blob stockBlob = Logic.getInstance().getAddedStock(TEST_STOCK);
        Assertions.assertEquals(TEST_STOCK, stockBlob.getSymbol().toUpperCase());

        Logic.getInstance().removeAllStock();
        Assertions.assertNull(Logic.getInstance().getAddedStock(TEST_STOCK));
    }

    @Test
    public void saveStock() {

        File testLocation = new File(TEST_NEW_DIRECTORY_LOCATION);
        testLocation.mkdirs();
        Path path = Paths.get(TEST_NEW_DIRECTORY_LOCATION);
        Assertions.assertTrue(Files.exists(path));

        Logic.getInstance().setStockFilePath(TEST_NEW_DIRECTORY_LOCATION + "/stock_file", TEST_SAVE_INVESTMENT_FILE);
        Logic.getInstance().getStock(TEST_STOCK);
        Stock_blob stockBlob = Logic.getInstance().getAddedStock(TEST_STOCK);
        Assertions.assertEquals(TEST_STOCK, stockBlob.getSymbol());
        Logic.getInstance().removeAllStock();

        File testFileLocation = new File(TEST_NEW_DIRECTORY_LOCATION + "/stock_file");
        Path filePath = Paths.get(TEST_NEW_DIRECTORY_LOCATION + "/stock_file");
        Assertions.assertTrue(Files.exists(filePath));

        if (testFileLocation.delete()) {
            System.out.println("Deleted the file: " + testFileLocation.getName());
        } else {
            System.out.println("Failed to delete the file.");
        }

        Assertions.assertFalse(Files.exists(filePath));

        if (testLocation.delete()) {
            System.out.println("Deleted the folder: " + testLocation.getName());
        } else {
            System.out.println("Failed to delete the folder.");
        }

        Assertions.assertFalse(Files.exists(path));
    }

    @Test
    public void getTotalInvested() {
        Logic.getInstance().setStockFilePath(TEST_SAVE_STOCK_FILE, TEST_SAVE_INVESTMENT_FILE);
        Logic.getInstance().getStock(TEST_STOCK);
        Stock_blob stockBlob = Logic.getInstance().getAddedStock(TEST_STOCK);
        stockBlob.setInvestment(TEST_INVESTMENT_VALUE);
        Logic.getInstance().updateStock(stockBlob);


        Logic.getInstance().getStock(TEST_SECOND_STOCK);
        Stock_blob secondStockBlob = Logic.getInstance().getAddedStock(TEST_SECOND_STOCK);
        secondStockBlob.setInvestment(TEST_INVESTMENT_VALUE);
        Logic.getInstance().updateStock(stockBlob);

        Assertions.assertEquals(Logic.getInstance().getTotalInvested(), TEST_INVESTMENT_VALUE + TEST_INVESTMENT_VALUE);
        Logic.getInstance().removeAllStock();
    }

    @Test
    public void getTotalProfit() {
        Logic.getInstance().setStockFilePath(TEST_SAVE_STOCK_FILE, TEST_SAVE_INVESTMENT_FILE);

        Logic.getInstance().getStock(TEST_STOCK);
        Stock_blob stockBlob = Logic.getInstance().getAddedStock(TEST_STOCK);
        stockBlob.setDivPerQ(TEST_DIVIDEND_PERQ);
        Logic.getInstance().updateStock(stockBlob);

        Logic.getInstance().getStock(TEST_SECOND_STOCK);
        Stock_blob stockBlob1 = Logic.getInstance().getAddedStock(TEST_SECOND_STOCK);
        stockBlob1.setDivPerQ(TEST_DIVIDEND_PERQ);
        Logic.getInstance().updateStock(stockBlob1);

        Logic.getInstance().getStock(TEST_EUR_STOCK);
        Stock_blob stockBlob2 = Logic.getInstance().getAddedStock(TEST_EUR_STOCK);
        stockBlob2.setDivPerQ(TEST_DIVIDEND_PERQ);
        Logic.getInstance().updateStock(stockBlob2);

        Logic.getInstance().getStock(TEST_GBP_STOCK);
        Stock_blob stockBlob3 = Logic.getInstance().getAddedStock(TEST_GBP_STOCK);
        stockBlob3.setDivPerQ(TEST_DIVIDEND_PERQ);
        Logic.getInstance().updateStock(stockBlob3);

        Logic.getInstance().getStock(TEST_CAD_STOCK);
        Stock_blob stockBlob4 = Logic.getInstance().getAddedStock(TEST_CAD_STOCK);
        stockBlob4.setDivPerQ(TEST_DIVIDEND_PERQ);
        Logic.getInstance().updateStock(stockBlob4);

        double expectedProfit = (TEST_DIVIDEND_PERQ * 2) + (TEST_DIVIDEND_PERQ * Logic.getInstance().getExchangeRateEUR()) +
                (TEST_DIVIDEND_PERQ * Logic.getInstance().getExchangeRateGBP()) + (TEST_DIVIDEND_PERQ * Logic.getInstance().getExchangeRateCAD());
        Assertions.assertEquals(Logic.getInstance().getTotalProfit(), expectedProfit);
        Logic.getInstance().removeAllStock();
    }

    @Test
    public void getTotalTax() {
        Logic.getInstance().setStockFilePath(TEST_SAVE_STOCK_FILE, TEST_SAVE_INVESTMENT_FILE);

        Logic.getInstance().getStock(TEST_STOCK);
        Stock_blob stockBlob = Logic.getInstance().getAddedStock(TEST_STOCK);
        stockBlob.setDivPerQ(TEST_DIVIDEND_PERQ);
        stockBlob.setOwnShares(1);
        Logic.getInstance().updateStock(stockBlob);

        Logic.getInstance().getStock(TEST_SECOND_STOCK);
        Stock_blob stockBlob1 = Logic.getInstance().getAddedStock(TEST_SECOND_STOCK);
        stockBlob1.setDivPerQ(TEST_DIVIDEND_PERQ);
        stockBlob1.setOwnShares(1);
        Logic.getInstance().updateStock(stockBlob1);

        Logic.getInstance().getStock(TEST_EUR_STOCK);
        Stock_blob stockBlob2 = Logic.getInstance().getAddedStock(TEST_EUR_STOCK);
        stockBlob2.setDivPerQ(TEST_DIVIDEND_PERQ);
        stockBlob2.setOwnShares(1);
        Logic.getInstance().updateStock(stockBlob2);

        Logic.getInstance().getStock(TEST_GBP_STOCK);
        Stock_blob stockBlob3 = Logic.getInstance().getAddedStock(TEST_GBP_STOCK);
        stockBlob3.setDivPerQ(TEST_DIVIDEND_PERQ);
        stockBlob3.setOwnShares(1);
        Logic.getInstance().updateStock(stockBlob3);

        Logic.getInstance().getStock(TEST_CAD_STOCK);
        Stock_blob stockBlob4 = Logic.getInstance().getAddedStock(TEST_CAD_STOCK);
        stockBlob4.setDivPerQ(TEST_DIVIDEND_PERQ);
        stockBlob4.setOwnShares(1);
        Logic.getInstance().updateStock(stockBlob4);

        double expectedTax = ((TEST_DIVIDEND_PERQ * 2) * USAIncomeTax) / 100;
        expectedTax += ((TEST_DIVIDEND_PERQ * Constants.FRIncomeTax) / 100) * Logic.getInstance().getExchangeRateEUR();
        expectedTax += ((TEST_DIVIDEND_PERQ * Constants.GBIncomeTax) / 100) * Logic.getInstance().getExchangeRateGBP();
        expectedTax += ((TEST_DIVIDEND_PERQ * USAIncomeTax) / 100) * Logic.getInstance().getExchangeRateCAD();

        Assertions.assertEquals(Logic.getInstance().getTotalTax(), expectedTax);
        Logic.getInstance().removeAllStock();
    }

    @Test
    public void getInvestmentPercent() {
        Logic.getInstance().setStockFilePath(TEST_SAVE_STOCK_FILE, TEST_SAVE_INVESTMENT_FILE);

        Logic.getInstance().getStock(TEST_STOCK);
        Stock_blob stockBlob = Logic.getInstance().getAddedStock(TEST_STOCK);
        stockBlob.setInvestment(TEST_INVESTMENT_VALUE*2);
        stockBlob.setSector(Constants.sectorComboBoxList[0]);
        Logic.getInstance().updateStock(stockBlob);

        Logic.getInstance().getStock(TEST_SECOND_STOCK);
        Stock_blob secondStockBlob = Logic.getInstance().getAddedStock(TEST_SECOND_STOCK);
        secondStockBlob.setInvestment(TEST_INVESTMENT_VALUE);
        secondStockBlob.setSector(Constants.sectorComboBoxList[1]);
        Logic.getInstance().updateStock(secondStockBlob);

        Logic.getInstance().getStock(TEST_THIRD_STOCK);
        Stock_blob stockBlob1 = Logic.getInstance().getAddedStock(TEST_THIRD_STOCK);
        stockBlob1.setInvestment(TEST_INVESTMENT_VALUE);
        stockBlob1.setSector(Constants.sectorComboBoxList[2]);
        Logic.getInstance().updateStock(stockBlob1);

        Assertions.assertEquals(Logic.getInstance().getInvestmentPercent(Constants.sectorComboBoxList[0]), 50);
        Assertions.assertEquals(Logic.getInstance().getInvestmentPercent(Constants.sectorComboBoxList[1]), 25);
        Assertions.assertEquals(Logic.getInstance().getInvestmentPercent(Constants.sectorComboBoxList[2]), 25);

        Logic.getInstance().removeAllStock();
    }

    @Test
    public void getShareTax(){
        Logic.getInstance().setStockFilePath(TEST_SAVE_STOCK_FILE, TEST_SAVE_INVESTMENT_FILE);
        Logic.getInstance().getStock(TEST_STOCK);
        Stock_blob stockBlob = Logic.getInstance().getAddedStock(TEST_STOCK);
        stockBlob.setDivPerQ(10);
        stockBlob.setOwnShares(1);
        Logic.getInstance().updateStock(stockBlob);

        Assertions.assertEquals(Logic.getInstance().getShareTax(TEST_STOCK), 1);
    }

    @Test
    public void getShareLatestNews(){
        Logic.getInstance().setStockFilePath(TEST_SAVE_STOCK_FILE, TEST_SAVE_INVESTMENT_FILE);
        Logic.getInstance().getStock(TEST_STOCK);
        Stock_blob stockBlob = Logic.getInstance().getAddedStock(TEST_STOCK);
        Assertions.assertNotEquals("", Logic.getInstance().getShareLatestNews(stockBlob.getName()));
        Assertions.assertEquals(Logic.getInstance().getShareLatestNews(""), DefaultLang.noNewsInfo);
        Logic.getInstance().removeAllStock();
    }

    @Test
    public void loadInvestmentData() {
        double[] testSharePrice = new double[]{0.0, 40, 53.98, 34.98};
        Logic.getInstance().setStockFilePath(TEST_SAVE_STOCK_FILE, TEST_SAVE_INVESTMENT_FILE);
        Logic.getInstance().readXLSX(TEST_STOCK_FILE_DIVIDEND_PATH);
        HashMap<String, Investment_blob> investmentData = Logic.getInstance().loadInvestmentData(TEST_SAVE_INVESTMENT_FILE);
        HashMap<Double, HashMap<Double, Double>> amd = investmentData.get(TEST_STOCK).getInvestment();
        int i = 0;
        for (Map.Entry<Double, HashMap<Double, Double>> entry : amd.entrySet()) {
            Double key = entry.getKey();
            Assertions.assertTrue(key>=testSharePrice[i] && key<(testSharePrice[i]+0.1));
            i++;
        }

        double[] testNegativeSharePrice = new double[]{2.0, 43, 52.98, 36.98};
        i = 0;
        for (Map.Entry<Double, HashMap<Double, Double>> entry : amd.entrySet()) {
            Double key = entry.getKey();
            Assertions.assertFalse(key >= testNegativeSharePrice[i] && key < testNegativeSharePrice[i] + 0.1);
            i++;
        }
        Logic.getInstance().removeAllStock();
    }

    @Test
    public void saveInvestment() {

        File testLocation = new File(TEST_NEW_DIRECTORY_LOCATION);
        testLocation.mkdirs();
        Path path = Paths.get(TEST_NEW_DIRECTORY_LOCATION);
        Assertions.assertTrue(Files.exists(path));

        Logic.getInstance().setStockFilePath(TEST_NEW_DIRECTORY_LOCATION + "/investment_file", TEST_SAVE_INVESTMENT_FILE);
        Logic.getInstance().readXLSX(TEST_STOCK_FILE_DIVIDEND_PATH);
        Logic.getInstance().getAddedInvestment(TEST_STOCK);
        Investment_blob stockBlob = Logic.getInstance().getAddedInvestment(TEST_STOCK);
        Assertions.assertEquals(TEST_STOCK, stockBlob.getStockSymbol());
        Logic.getInstance().removeAllStock();

        Path filePath = Paths.get(TEST_NEW_DIRECTORY_LOCATION + "/investment_file");
        Assertions.assertTrue(Files.exists(filePath));

        try {
            FileUtils.cleanDirectory(testLocation.getAbsoluteFile());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (testLocation.delete()) {
            System.out.println("Deleted the folder: " + testLocation.getName());
        } else {
            System.out.println("Failed to delete the folder.");
        }

        Assertions.assertFalse(Files.exists(path));
    }

    @Test
    public void getAddedInvestment(){
        Logic.getInstance().setStockFilePath(TEST_SAVE_STOCK_FILE, TEST_SAVE_INVESTMENT_FILE);
        Logic.getInstance().readXLSX(TEST_STOCK_FILE_DIVIDEND_PATH);
        HashMap<String, Investment_blob> investmentData = Logic.getInstance().loadInvestmentData(TEST_SAVE_INVESTMENT_FILE);
        Assertions.assertNotNull(investmentData);

        Logic.getInstance().removeAllStock();
    }

    @Test
    public void computeNecessaryInvestment(){
        Logic.getInstance().setStockFilePath(TEST_SAVE_STOCK_FILE, TEST_SAVE_INVESTMENT_FILE);
        Logic.getInstance().readXLSX(TEST_STOCK_FILE_DIVIDEND_PATH);
        double necessaryInvestment = Logic.getInstance().computeNecessaryInvestment(TEST_STOCK);
        Assertions.assertTrue(necessaryInvestment>=40.78 && necessaryInvestment<40.88);
        Assertions.assertFalse(necessaryInvestment>=56 && necessaryInvestment<67);

        Logic.getInstance().removeAllStock();
    }
}

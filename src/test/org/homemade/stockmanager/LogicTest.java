package org.homemade.stockmanager;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.homemade.stockmanager.blobs.Stock_blob;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

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
    public static String TEST_NEW_DIRECTORY_LOCATION = "src/test/resources/new_location";

    @Test
    public void getInstance() {
        Logic firstCallInstance = Logic.getInstance();
        Logic secondCallInstance = Logic.getInstance();

        Assertions.assertSame(firstCallInstance, secondCallInstance);
    }

    @Test
    public void getExchangeRateRon() {
        Assertions.assertEquals(Logic.getInstance().getExchangeRate(Constants.Ron), Logic.getInstance().getExchangeRateRON());
    }

    @Test
    public void getExchangeRateEUR() {
        Assertions.assertEquals(Logic.getInstance().getExchangeRate(Constants.Euro), Logic.getInstance().getExchangeRateEUR());
    }

    @Test
    public void getExchangeRateCAD() {
        Assertions.assertEquals(Logic.getInstance().getExchangeRate(Constants.CanadianDollar), Logic.getInstance().getExchangeRateCAD());
    }

    @Test
    public void getExchangeRateGBP() {
        Assertions.assertEquals(Logic.getInstance().getExchangeRate(Constants.Pounds), Logic.getInstance().getExchangeRateGBP());
    }

    @Test
    public void getStock() {
        Logic.getInstance().setStockFilePath(TEST_SAVE_STOCK_FILE);
        Logic.getInstance().getStock(TEST_STOCK);
        Stock_blob stockBlob = Logic.getInstance().getAddedStock(TEST_STOCK);
        Assertions.assertEquals(TEST_STOCK, stockBlob.getSymbol().toUpperCase());
        Logic.getInstance().removeAllStock();
    }

    @Test
    public void loadStockData() {
        Logic.getInstance().setStockFilePath(TEST_SAVE_STOCK_FILE);
        Logic.getInstance().getStock(TEST_STOCK);
        HashMap<String, Stock_blob> stockBlob = Logic.getInstance().loadStockData(TEST_STOCK_FILE_PATH);
        Assertions.assertNotNull(stockBlob.get(TEST_STOCK));
        Logic.getInstance().removeAllStock();
    }

    @Test
    public void getAddedStock() {
        Logic.getInstance().setStockFilePath(TEST_SAVE_STOCK_FILE);
        Logic.getInstance().getStock(TEST_STOCK);
        Stock_blob stockBlob = Logic.getInstance().getAddedStock(TEST_STOCK);
        Assertions.assertEquals(TEST_STOCK, stockBlob.getSymbol());

        stockBlob = Logic.getInstance().getAddedStock(INVALID_TEST_STOCK);
        Assertions.assertNull(stockBlob);
        Logic.getInstance().removeAllStock();
    }

    @Test
    public void updateStock() {
        Logic.getInstance().setStockFilePath(TEST_SAVE_STOCK_FILE);
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
        Logic.getInstance().setStockFilePath(TEST_SAVE_STOCK_FILE);
        Logic.getInstance().getAddedStock(TEST_STOCK);
        Logic.getInstance().removeStock(TEST_STOCK);

        Stock_blob stockBlob = Logic.getInstance().getAddedStock(TEST_STOCK);
        Assertions.assertNull(stockBlob);
        Logic.getInstance().removeAllStock();
    }

    @Test
    public void getExchangeRate() {
        Assertions.assertTrue(Logic.getInstance().getExchangeRate(Constants.Euro) > 0);
        Assertions.assertEquals(1, Logic.getInstance().getExchangeRate("USD"));
    }

    @Test
    public void readXLSX() {
        Logic.getInstance().setStockFilePath(TEST_SAVE_STOCK_FILE);
        Logic.getInstance().readXLSX(TEST_STOCK_FILE_DIVIDEND_PATH);
        Stock_blob stockBlob = Logic.getInstance().getAddedStock(TEST_STOCK);
        Assertions.assertEquals(stockBlob.getSymbol(), TEST_STOCK);
        Assertions.assertEquals(0.625, stockBlob.getDivPerQ());
        Logic.getInstance().removeAllStock();
    }

    @Test
    public void removeAllStock() {
        Logic.getInstance().setStockFilePath(TEST_SAVE_STOCK_FILE);
        Logic.getInstance().getStock(TEST_STOCK);
        Stock_blob stockBlob = Logic.getInstance().getAddedStock(TEST_STOCK);
        Assertions.assertEquals(TEST_STOCK, stockBlob.getSymbol().toUpperCase());

        Logic.getInstance().removeAllStock();
        Assertions.assertNull(Logic.getInstance().getAddedStock(TEST_STOCK));
    }

    @Test
    public void getExchangeRates() {
        Logic.getInstance().getExchangeRates();
        Assertions.assertTrue(0 < Logic.getInstance().getExchangeRateRON());
        Assertions.assertTrue(0 < Logic.getInstance().getExchangeRateCAD());
        Assertions.assertTrue(0 < Logic.getInstance().getExchangeRateEUR());
        Assertions.assertTrue(0 < Logic.getInstance().getExchangeRateGBP());
    }

    @Test
    public void saveStock() {

        File testLocation = new File(TEST_NEW_DIRECTORY_LOCATION);
        testLocation.mkdirs();
        Path path = Paths.get(TEST_NEW_DIRECTORY_LOCATION);
        Assertions.assertTrue(Files.exists(path));

        Logic.getInstance().setStockFilePath(TEST_NEW_DIRECTORY_LOCATION + "/stock_file");
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
        Logic.getInstance().setStockFilePath(TEST_SAVE_STOCK_FILE);
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
        Logic.getInstance().setStockFilePath(TEST_SAVE_STOCK_FILE);

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
        Logic.getInstance().setStockFilePath(TEST_SAVE_STOCK_FILE);

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

        double expectedTax = ((TEST_DIVIDEND_PERQ * 2) * Constants.USAIncomeTax) / 100;
        expectedTax += ((TEST_DIVIDEND_PERQ * Constants.FRIncomeTax) / 100) * Logic.getInstance().getExchangeRateEUR();
        expectedTax += ((TEST_DIVIDEND_PERQ * Constants.GBIncomeTax) / 100) * Logic.getInstance().getExchangeRateGBP();
        expectedTax += ((TEST_DIVIDEND_PERQ * Constants.USAIncomeTax) / 100) * Logic.getInstance().getExchangeRateCAD();

        Assertions.assertEquals(Logic.getInstance().getTotalTax(), expectedTax);
        Logic.getInstance().removeAllStock();
    }

    @Test
    public void getInvestmentPercent() {
        Logic.getInstance().setStockFilePath(TEST_SAVE_STOCK_FILE);

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
}

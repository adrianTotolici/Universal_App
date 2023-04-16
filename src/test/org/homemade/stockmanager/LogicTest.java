package org.homemade.stockmanager;

import org.homemade.stockmanager.blobs.Stock_blob;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

public class LogicTest {

    public static String TEST_STOCK = "AMD";
    public static String INVALID_TEST_STOCK = "TEST";
    public static String TEST_INDUSTRY_VALUE = "Semiconductors";

    public static String TEST_STOCK_FILE_PATH = "src/test/resources/stock_file";
    public static String TEST_STOCK_FILE_DIVIDEND_PATH = "src/test/resources/Dividende_test.xlsx";

    public static String TEST_SAVE_STOCK_FILE = "src/test/resources/test_stock_file_path";

    @Test
    public void getInstance(){
        Logic firstCallInstance = Logic.getInstance();
        Logic secondCallInstance = Logic.getInstance();

        Assertions.assertSame(firstCallInstance, secondCallInstance);
    }

    @Test
    public void getFirstInstance(){
        Logic firstCallInstance = Logic.getFirstInstance(TEST_SAVE_STOCK_FILE);
        Logic secondCallInstance = Logic.getInstance();

        Assertions.assertSame(firstCallInstance, secondCallInstance);
    }

    @Test
    public void getExchangeRateRon(){
        Assertions.assertEquals(Logic.getFirstInstance(TEST_SAVE_STOCK_FILE).getExchangeRate(Constants.Ron), Logic.getInstance().getExchangeRateRON());
    }

    @Test
    public void getExchangeRateEUR(){
        Assertions.assertEquals(Logic.getFirstInstance(TEST_SAVE_STOCK_FILE).getExchangeRate(Constants.Euro), Logic.getInstance().getExchangeRateEUR());
    }

    @Test
    public void getExchangeRateCAD(){
        Assertions.assertEquals(Logic.getFirstInstance(TEST_SAVE_STOCK_FILE).getExchangeRate(Constants.CanadianDollar), Logic.getInstance().getExchangeRateCAD());
    }

    @Test
    public void getExchangeRateGBP(){
        Assertions.assertEquals(Logic.getFirstInstance(TEST_SAVE_STOCK_FILE).getExchangeRate(Constants.Pounds), Logic.getInstance().getExchangeRateGBP());
    }

    @Test
    public void getStock(){
        Logic.getFirstInstance(TEST_SAVE_STOCK_FILE).getStock(TEST_STOCK);
        Stock_blob stockBlob = Logic.getFirstInstance(TEST_SAVE_STOCK_FILE).getAddedStock(TEST_STOCK);
        Assertions.assertEquals(TEST_STOCK, stockBlob.getSymbol().toUpperCase());
    }

    @Test
    public void loadStockData(){
        HashMap<String, Stock_blob> stockBlob = Logic.getFirstInstance(TEST_SAVE_STOCK_FILE).loadStockData(TEST_STOCK_FILE_PATH);
        Assertions.assertNotNull(stockBlob.get(TEST_STOCK));
    }

    @Test
    public void getAddedStock(){
        Logic.getFirstInstance(TEST_SAVE_STOCK_FILE).getStock(TEST_STOCK);
        Stock_blob stockBlob = Logic.getFirstInstance(TEST_SAVE_STOCK_FILE).getAddedStock(TEST_STOCK);
        Assertions.assertEquals(TEST_STOCK, stockBlob.getSymbol());

        stockBlob = Logic.getFirstInstance(TEST_SAVE_STOCK_FILE).getAddedStock(INVALID_TEST_STOCK);
        Assertions.assertNull(stockBlob);
    }

    @Test
    public void updateStock(){
        Logic.getFirstInstance(TEST_SAVE_STOCK_FILE).getStock(TEST_STOCK);
        Stock_blob stockBlob = Logic.getFirstInstance(TEST_SAVE_STOCK_FILE).getAddedStock(TEST_STOCK);
        stockBlob.setIndustry(TEST_INDUSTRY_VALUE);

        Logic.getFirstInstance(TEST_SAVE_STOCK_FILE).updateStock(stockBlob);
        Stock_blob stockBlobUpdated = Logic.getFirstInstance(TEST_SAVE_STOCK_FILE).getAddedStock(TEST_STOCK);

        Assertions.assertSame(stockBlob, stockBlobUpdated);
        Assertions.assertEquals(TEST_INDUSTRY_VALUE, stockBlobUpdated.getIndustry());
    }

    @Test
    public void removeStock(){
        Logic.getFirstInstance(TEST_SAVE_STOCK_FILE).getAddedStock(TEST_STOCK);
        Logic.getFirstInstance(TEST_SAVE_STOCK_FILE).removeStock(TEST_STOCK);

        Stock_blob stockBlob = Logic.getFirstInstance(TEST_SAVE_STOCK_FILE).getAddedStock(TEST_STOCK);
        Assertions.assertNull(stockBlob);
    }

    @Test
    public void getExchangeRate(){
        Assertions.assertTrue(Logic.getFirstInstance(TEST_SAVE_STOCK_FILE).getExchangeRate(Constants.Euro)>0);
        Assertions.assertEquals(1, Logic.getFirstInstance(TEST_SAVE_STOCK_FILE).getExchangeRate("USD"));
    }

    @Test
    public void readXLSX(){
        Logic.getFirstInstance(TEST_SAVE_STOCK_FILE).readXLSX(TEST_STOCK_FILE_DIVIDEND_PATH);
        Stock_blob stockBlob = Logic.getFirstInstance(TEST_SAVE_STOCK_FILE).getAddedStock(TEST_STOCK);
        Assertions.assertEquals(stockBlob.getSymbol(), TEST_STOCK);
        Assertions.assertEquals(0.625, stockBlob.getDivPerQ());
    }

    @Test
    public void removeAllStock(){
        Logic.getFirstInstance(TEST_SAVE_STOCK_FILE).getStock(TEST_STOCK);
        Stock_blob stockBlob = Logic.getFirstInstance(TEST_SAVE_STOCK_FILE).getAddedStock(TEST_STOCK);
        Assertions.assertEquals(TEST_STOCK, stockBlob.getSymbol().toUpperCase());

        Logic.getFirstInstance(TEST_SAVE_STOCK_FILE).removeAllStock();
        Assertions.assertNull(Logic.getFirstInstance(TEST_SAVE_STOCK_FILE).getAddedStock(TEST_STOCK));
    }

    @Test
    public void getExchangeRates(){
        Logic.getFirstInstance(TEST_SAVE_STOCK_FILE).getExchangeRates();
        Assertions.assertTrue(0 < Logic.getFirstInstance(TEST_SAVE_STOCK_FILE).getExchangeRateRON());
        Assertions.assertTrue(0 < Logic.getFirstInstance(TEST_SAVE_STOCK_FILE).getExchangeRateCAD());
        Assertions.assertTrue(0 < Logic.getFirstInstance(TEST_SAVE_STOCK_FILE).getExchangeRateEUR());
        Assertions.assertTrue(0 < Logic.getFirstInstance(TEST_SAVE_STOCK_FILE).getExchangeRateGBP());
    }
}

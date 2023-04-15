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

    @Test
    public void getInstance(){
        Logic firstCallInstance = Logic.getInstance();
        Logic secondCallInstance = Logic.getInstance();

        Assertions.assertSame(firstCallInstance, secondCallInstance);
    }

    @Test
    public void getExchangeRateRon(){
        Assertions.assertEquals(Logic.getInstance().getExchangeRate(Constants.Ron), Logic.getInstance().getExchangeRateRON());
    }

    @Test
    public void getExchangeRateEUR(){
        Assertions.assertEquals(Logic.getInstance().getExchangeRate(Constants.Euro), Logic.getInstance().getExchangeRateEUR());
    }

    @Test
    public void getExchangeRateCAD(){
        Assertions.assertEquals(Logic.getInstance().getExchangeRate(Constants.CanadianDollar), Logic.getInstance().getExchangeRateCAD());
    }

    @Test
    public void getExchangeRateGBP(){
        Assertions.assertEquals(Logic.getInstance().getExchangeRate(Constants.Pounds), Logic.getInstance().getExchangeRateGBP());
    }

    @Test
    public void getStock(){
        Logic.getInstance().getStock(TEST_STOCK);
        Stock_blob stockBlob = Logic.getInstance().getAddedStock(TEST_STOCK);
        Assertions.assertEquals(TEST_STOCK, stockBlob.getSymbol().toUpperCase());
    }

    @Test
    public void loadStockData(){
        HashMap<String, Stock_blob> stockBlob = Logic.getInstance().loadStockData(TEST_STOCK_FILE_PATH);
        Assertions.assertNotNull(stockBlob.get(TEST_STOCK));
    }

    @Test
    public void getAddedStock(){
        Logic.getInstance().getStock(TEST_STOCK);
        Stock_blob stockBlob = Logic.getInstance().getAddedStock(TEST_STOCK);
        Assertions.assertEquals(TEST_STOCK, stockBlob.getSymbol());

        stockBlob = Logic.getInstance().getAddedStock(INVALID_TEST_STOCK);
        Assertions.assertNull(stockBlob);
    }

    @Test
    public void updateStock(){
        Logic.getInstance().getStock(TEST_STOCK);
        Stock_blob stockBlob = Logic.getInstance().getAddedStock(TEST_STOCK);
        stockBlob.setIndustry(TEST_INDUSTRY_VALUE);

        Logic.getInstance().updateStock(stockBlob);
        Stock_blob stockBlobUpdated = Logic.getInstance().getAddedStock(TEST_STOCK);

        Assertions.assertSame(stockBlob, stockBlobUpdated);
        Assertions.assertEquals(TEST_INDUSTRY_VALUE, stockBlobUpdated.getIndustry());
    }

    @Test
    public void removeStock(){
        Logic.getInstance().getAddedStock(TEST_STOCK);
        Logic.getInstance().removeStock(TEST_STOCK);

        Stock_blob stockBlob = Logic.getInstance().getAddedStock(TEST_STOCK);
        Assertions.assertNull(stockBlob);
    }

    @Test
    public void getExchangeRate(){
        Assertions.assertTrue(Logic.getInstance().getExchangeRate(Constants.Euro)>0);
        Assertions.assertEquals(1, Logic.getInstance().getExchangeRate("USD"));
    }

    @Test
    public void readXLSX(){

    }

    @Test
    public void removeAllStock(){
        
    }
}

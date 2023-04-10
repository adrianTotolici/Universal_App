import org.homemade.stockmanager.Logic;
import org.homemade.stockmanager.blobs.Stock_blob;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class StockManagerTests {

    @Test
    public void getInstance(){
        Logic firstCallInstance = Logic.getInstance();
        Logic secondCallInstance = Logic.getInstance();

        Assertions.assertSame(firstCallInstance, secondCallInstance);
    }

    @Test
    public void getExchangeRateRon(){
        Assertions.assertTrue(Logic.getInstance().getExchangeRateRon() > 0);
    }

    @Test
    public void getStock(){
        Logic.getInstance().getStock("AMD");
        Stock_blob stockBlob = Logic.getInstance().getAddedStock("AMD");
        Assertions.assertEquals("AMD", stockBlob.getSymbol().toUpperCase());
    }

    @Test
    public void updateStock(){
        Logic.getInstance().getStock("AMD");
        Stock_blob stockBlob = Logic.getInstance().getAddedStock("AMD");
        stockBlob.setIndustry("Semiconductors");

        Logic.getInstance().updateStock(stockBlob);
        Stock_blob stockBlobUpdated = Logic.getInstance().getAddedStock("AMD");

        Assertions.assertSame(stockBlob, stockBlobUpdated);
        Assertions.assertEquals("Semiconductors", stockBlobUpdated.getIndustry());
    }

    @Test
    public void removeStock(){
        Logic.getInstance().getAddedStock("AMD");
        Logic.getInstance().removeStock("AMD");

        Stock_blob stockBlob = Logic.getInstance().getAddedStock("AMD");
        Assertions.assertNull(stockBlob);
    }

    @Test
    public void getExchangeRate(){
        Assertions.assertTrue(Logic.getInstance().getExchangeRate("EUR")>0);
        Assertions.assertEquals(1, Logic.getInstance().getExchangeRate("USD"));
    }
}

import org.homemade.stockmanager.Logic;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class StockManagerTests {

    @Test
    public void getInstance(){
        Logic firstCallInstance = Logic.getInstance();
        Logic secondCallInstance = Logic.getInstance();

        Assertions.assertSame(firstCallInstance, secondCallInstance);
    }

}

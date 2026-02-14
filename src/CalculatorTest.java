import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CalculatorTest {

    @Test
    public void testAdd() {
        // Create the calculator right here where I need it
        Calculator myCalc = new Calculator();

        // I'm testing -1 + 8 = 7
        int result = myCalc.add(-1, 8);

        assertEquals(7, result);
    }

    @Test
    public void testSub() {
        Calculator myCalc = new Calculator();

        // I'm testing -1 - (-1) = 0
        int result = myCalc.subtract(-1, -1);

        assertEquals(0, result);
    }
}
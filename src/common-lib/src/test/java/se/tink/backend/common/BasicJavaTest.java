package se.tink.backend.common;

import org.junit.Assert;
import org.junit.Test;

public class BasicJavaTest {

    @Test
    public void testNegativeDoubleToIntCast() {
        System.out.println((int) -1.1);
        System.out.println((int) -1.9);
    }

    @Test
    public void testRecreatingIncorrectDoubleComparison() {
        double a = 0;
        double b = -0.4;
        double c = -1.1;

        int compA = (int) (a - b);
        int compB = (int) (b - c);
        int compC = (int) (a - c);

        Assert.assertEquals(compA, 0);
        Assert.assertEquals(compB, 0);
        Assert.assertEquals(compC, 1); // This is wrong. Triangle equality would expect a==c since a==b and b==c.

    }

}

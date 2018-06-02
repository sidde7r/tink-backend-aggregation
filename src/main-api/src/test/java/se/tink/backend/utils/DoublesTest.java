package se.tink.backend.utils;

import org.junit.Assert;
import org.junit.Test;

public class DoublesTest {
    @Test
    public void testFuzzyEquals() {
        Assert.assertTrue(Doubles.fuzzyEquals(null, null, 0.1));
        Assert.assertFalse(Doubles.fuzzyEquals(1.0, null, 0.1));
        Assert.assertFalse(Doubles.fuzzyEquals(null, 1.0, 0.1));
        
        Assert.assertTrue(Doubles.fuzzyEquals(1.0, 1.0, 0));
        Assert.assertTrue(Doubles.fuzzyEquals(1.1, 1.0, 0.2));
        Assert.assertTrue(Doubles.fuzzyEquals(0.0, 0.0, 0));
        Assert.assertTrue(Doubles.fuzzyEquals(0.0, 0.0, 0.1));
        
        Assert.assertTrue(Doubles.fuzzyEquals(3.141592654, 3.141596535, 0.001));
        Assert.assertFalse(Doubles.fuzzyEquals(3.141592654, 4.141596535, 0.001));
    }
}

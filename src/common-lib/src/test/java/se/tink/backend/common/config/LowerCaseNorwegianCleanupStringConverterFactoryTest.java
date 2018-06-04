package se.tink.backend.common.config;

import java.util.function.Function;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class LowerCaseNorwegianCleanupStringConverterFactoryTest {
    private static Function<String, String> preprocessor;

    @BeforeClass
    public static void initialize() {
        preprocessor = new LowerCaseNorwegianCleanupStringConverterFactory().build();
    }

    @Test
    public void largeNumberTest() {
        Assert.assertEquals("test", preprocessor.apply("Test 123456"));
    }

    @Test
    public void characterPattern() {
        Assert.assertEquals("paypal asdf", preprocessor.apply("paypal* asdf"));
    }

    @Test
    public void cleanupTest() {
        Assert.assertEquals("test test", preprocessor.apply("   test    test   "));
    }

    @Test
    public void bankNumberTest() {
        Assert.assertEquals("- hyra", preprocessor.apply("Til: 1234.56.78901 - Hyra"));
    }

    @Test
    public void dateTest() {
        Assert.assertEquals("bergen betal dato", preprocessor.apply("BERGEN betal dato 2016-01-11"));
    }

    @Test
    public void tilTest() {
        Assert.assertEquals("a", preprocessor.apply("til: a"));
    }

    @Test
    public void additionalDateTest() {
        Assert.assertEquals("betalt:", preprocessor.apply("til: betalt: 12 01 18"));
    }

    @Test
    public void noChangeTest() {
        String testString = "abcdefghijkmnoprqstuvwv";
        Assert.assertEquals(testString, preprocessor.apply(testString));
    }
}


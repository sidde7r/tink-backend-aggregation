package se.tink.backend.common.config;

import java.util.function.Function;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class LowerCaseOnlyAZStringConverterFactoryTest {
    private static Function<String, String> preprocessor;

    @BeforeClass
    public static void initialize() {
        preprocessor = new LowerCaseOnlyAZStringConverterFactory().build();
    }

    @Test
    public void swedishUmlautsTest() {
        Assert.assertEquals("test aaoaao", preprocessor.apply("Test åäöÅÄÖ"));
    }

    @Test
    public void onlyAZTest() {
        Assert.assertEquals("qwertyuiopaasdfghjklozxcvbnm",
                preprocessor.apply("QWERTYUIOPÅasdfghjklözxcvbnm,.-1234567890+"));
    }

    @Test
    public void noChangeTest() {
        String testString = "abcdefghijkmnoprqstuvwv";
        Assert.assertEquals(testString, preprocessor.apply(testString));
    }
}

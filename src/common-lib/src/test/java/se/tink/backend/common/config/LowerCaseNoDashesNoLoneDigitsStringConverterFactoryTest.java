package se.tink.backend.common.config;

import java.util.function.Function;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class LowerCaseNoDashesNoLoneDigitsStringConverterFactoryTest {
    private static Function<String, String> preprocessor;

    @BeforeClass
    public static void initialize() {
        preprocessor = new LowerCaseNoDashesNoLoneDigitsStringConverterFactory().build();
    }

    @Test
    public void swedishUmlautsTest() {
        Assert.assertEquals("test åäöåäö", preprocessor.apply("Test åäöÅÄÖ"));
    }

    @Test
    public void noDashesTest() {
        Assert.assertEquals("sushi bar ki mama", preprocessor.apply("Sushi-bar Ki-mama"));
    }

    @Test
    public void noLoneStartingDigitsTest() {
        Assert.assertEquals("pressbyrån", preprocessor.apply("Pressbyrån 123"));
    }

    @Test
    public void noLoneDigitsTest() {
        Assert.assertEquals("pressbyrån  abc", preprocessor.apply("Pressbyrån 123 Abc"));
    }

    @Test
    public void noLoneEndingDigitsTest() {
        Assert.assertEquals("pressbyrån", preprocessor.apply("123 Pressbyrån"));
    }

    @Test
    public void combinedDashDigitTest() {
        Assert.assertEquals("7 eleven", preprocessor.apply("7-eleven"));
    }

    @Test
    public void combinedDigitLetterTest() {
        Assert.assertEquals("hejsan57", preprocessor.apply("hejsan57"));
    }
}

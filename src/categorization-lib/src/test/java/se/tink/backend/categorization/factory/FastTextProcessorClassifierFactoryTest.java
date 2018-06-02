package se.tink.backend.categorization.factory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.Assert;
import org.junit.Test;

public class FastTextProcessorClassifierFactoryTest {

    @Test
    public void testUrlPatterns() throws Exception {
        assertUrlPatternMatches(FastTextProcessorCategorizerFactory.urlPattern,
                "resource:///categorization-lib/minimal_model.bin",
                "resource", "", "categorization-lib/minimal_model.bin");
        assertUrlPatternMatches(FastTextProcessorCategorizerFactory.urlPattern,
                "s3://tink-categorization/all_transactions.bin",
                "s3", "tink-categorization", "all_transactions.bin");
    }

    private static void assertUrlPatternMatches(Pattern pattern, String input, String resource, String host,
            String path) {
        Matcher matcher = pattern.matcher(input);
        Assert.assertTrue("No match for: " + path, matcher.matches());
        Assert.assertEquals(matcher.group("protocol"), resource);
        Assert.assertEquals(matcher.group("host"), host);
        Assert.assertEquals(matcher.group("path"), path);
    }
}

package se.tink.backend.aggregation.agents.framework.wiremock.utils.parsingrules.predicates.test;

import java.util.function.Predicate;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.wiremock.utils.parsingrules.predicates.HasPrefix;

public final class HasPrefixTest {

    @Test
    public void bodyFormat_matchLinesWithPrefix_format_0() {

        Predicate<String> predicate = new HasPrefix();
        String someTextWithPrefix = "1234 > SomeText";

        boolean isMatch = predicate.test(someTextWithPrefix);

        Assert.assertTrue(isMatch);
    }

    @Test
    public void bodyFormat_matchLinesWithPrefix_1() {

        Predicate<String> predicate = new HasPrefix();
        String someTextWithPrefix = "1234 < SomeText";

        boolean isMatch = predicate.test(someTextWithPrefix);

        Assert.assertTrue(isMatch);
    }

    @Test
    public void bodyFormat_matchLinesWithPrefix_2() {

        Predicate<String> predicate = new HasPrefix();
        String someTextWithPrefix = "1234 * SomeText";

        boolean isMatch = predicate.test(someTextWithPrefix);

        Assert.assertTrue(isMatch);
    }

    @Test
    public void bodyFormat_matchLinesWithPrefix_short() {

        Predicate<String> predicate = new HasPrefix();
        String someTextWithPrefix = "2 * SomeText";

        boolean isMatch = predicate.test(someTextWithPrefix);

        Assert.assertTrue(isMatch);
    }

    @Test
    public void bodyFormat_matchLinesWithPrefix_long() {

        Predicate<String> predicate = new HasPrefix();
        String someTextWithPrefix = "123456789 * SomeText";

        boolean isMatch = predicate.test(someTextWithPrefix);

        Assert.assertTrue(isMatch);
    }

    @Test
    public void bodyFormat_doesNot_matchLinesWithNoPrefix() {

        Predicate<String> predicate = new HasPrefix();
        String someTextWithNoPrefix = "SomeText";

        boolean isMatch = predicate.test(someTextWithNoPrefix);

        Assert.assertFalse(isMatch);
    }
}

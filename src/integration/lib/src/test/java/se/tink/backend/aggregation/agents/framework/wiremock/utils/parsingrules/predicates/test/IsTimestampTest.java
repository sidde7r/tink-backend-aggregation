package se.tink.backend.aggregation.agents.framework.wiremock.utils.parsingrules.predicates.test;

import java.util.function.Predicate;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.wiremock.utils.parsingrules.predicates.IsTimestamp;

public final class IsTimestampTest {

    @Test
    public void isTimestamp_matchesTimestampWithPrefix() {

        Predicate<String> rule = new IsTimestamp();
        String timestampLine = "1 * 2020-01-30--13:43:24.579";

        boolean isMatch = rule.test(timestampLine);

        Assert.assertTrue(isMatch);
    }

    @Test
    public void isTimestamp_matchesTimestampWithPrefix_longPrefix() {

        Predicate<String> rule = new IsTimestamp();
        String timestampLine = "12345789 * 2020-01-30--13:43:24.579";

        boolean isMatch = rule.test(timestampLine);

        Assert.assertTrue(isMatch);
    }

    @Test
    public void isTimestamp_doesNot_matchTimestampWithNoPrefix() {

        Predicate<String> rule = new IsTimestamp();
        String timestampLine = "2020-01-30--13:43:24.579";

        boolean isMatch = rule.test(timestampLine);

        Assert.assertFalse(isMatch);
    }

    @Test
    public void isTimestamp_doesNot_matchNonTimestamp() {

        Predicate<String> rule = new IsTimestamp();
        String timestampLine = "Not a timestamp";

        boolean isMatch = rule.test(timestampLine);

        Assert.assertFalse(isMatch);
    }

    @Test
    public void isTimestamp_doesNot_matchNonTimestampWithPrefix() {

        Predicate<String> rule = new IsTimestamp();
        String timestampLine = "1 * Not a timestamp";

        boolean isMatch = rule.test(timestampLine);

        Assert.assertFalse(isMatch);
    }
}

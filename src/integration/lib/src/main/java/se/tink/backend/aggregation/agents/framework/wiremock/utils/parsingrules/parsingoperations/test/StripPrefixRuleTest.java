package se.tink.backend.aggregation.agents.framework.wiremock.utils.parsingrules.parsingoperations.test;

import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.wiremock.utils.parsingrules.parsingoperations.ParsingOperation;
import se.tink.backend.aggregation.agents.framework.wiremock.utils.parsingrules.parsingoperations.StripPrefix;

public final class StripPrefixRuleTest {

    @Test
    public void stripPrefix_removesRequestPrefix() {

        ParsingOperation rule = new StripPrefix();
        String someTextWithPrefix = "1 > SomeText";

        List<String> result = rule.performOperation(someTextWithPrefix);

        Assert.assertEquals(1, result.size());
        Assert.assertEquals("SomeText", result.get(0));
    }

    @Test
    public void stripPrefix_removesRequestPrefixWithManyDigits() {

        ParsingOperation rule = new StripPrefix();
        String someTextWithPrefix = "1234 > SomeText";

        List<String> result = rule.performOperation(someTextWithPrefix);

        Assert.assertEquals(1, result.size());
        Assert.assertEquals("SomeText", result.get(0));
    }

    @Test
    public void stripPrefix_removesResponsePrefix() {

        ParsingOperation rule = new StripPrefix();
        String someTextWithPrefix = "1 < SomeText";

        List<String> result = rule.performOperation(someTextWithPrefix);

        Assert.assertEquals(1, result.size());
        Assert.assertEquals("SomeText", result.get(0));
    }

    @Test
    public void stripPrefix_removesResponsePrefixWithManyDigits() {

        ParsingOperation rule = new StripPrefix();
        String someTextWithPrefix = "1 < SomeText";

        List<String> result = rule.performOperation(someTextWithPrefix);

        Assert.assertEquals(1, result.size());
        Assert.assertEquals("SomeText", result.get(0));
    }
}

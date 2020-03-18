package se.tink.backend.aggregation.agents.framework.wiremock.utils.parsingrules.parsingoperations.test;

import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.wiremock.utils.parsingrules.parsingoperations.ParsingOperation;
import se.tink.backend.aggregation.agents.framework.wiremock.utils.parsingrules.parsingoperations.RemoveLine;

public final class RemoveLineRuleTest {

    @Test
    public void removeLine_returnsEmptyList() {

        ParsingOperation rule = new RemoveLine();
        String aLine = "A Line";

        List<String> result = rule.performOperation(aLine);

        Assert.assertTrue(result.isEmpty());
    }
}

package se.tink.backend.aggregation.agents.framework.wiremock.utils.parsingrules.parsingoperations.test;

import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.wiremock.utils.parsingrules.parsingoperations.InsertEmptyLineBefore;
import se.tink.backend.aggregation.agents.framework.wiremock.utils.parsingrules.parsingoperations.ParsingOperation;

public final class InsertEmptyLineBeforeRuleTest {

    @Test
    public void bodyFormat_insertsNewLineBefore() {

        ParsingOperation rule = new InsertEmptyLineBefore();
        String someLine = "SomeLine";

        List<String> result = rule.performOperation(someLine);

        Assert.assertEquals(2, result.size());
        Assert.assertEquals("", result.get(0));
        Assert.assertEquals("SomeLine", result.get(1));
    }
}

package se.tink.backend.aggregation.agents.framework.wiremock.utils.parsingrules.parsingoperations.test;

import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.wiremock.utils.parsingrules.parsingoperations.ParsingOperation;
import se.tink.backend.aggregation.agents.framework.wiremock.utils.parsingrules.parsingoperations.RequestResponseSubstitution;

public final class RequestResponseSubstitutionRuleTest {

    @Test
    public void
            requestResponseSubstitution_substitutesS3FormatWithGivenTextAndIndex_withLeadingEmptyLine() {

        ParsingOperation rule = new RequestResponseSubstitution("RESPONSE");
        String request22 = "22 * Client in-bound response";

        List<String> result = rule.performOperation(request22);

        Assert.assertEquals(2, result.size());
        Assert.assertEquals("", result.get(0));
        Assert.assertEquals("RESPONSE 22", result.get(1));
    }
}

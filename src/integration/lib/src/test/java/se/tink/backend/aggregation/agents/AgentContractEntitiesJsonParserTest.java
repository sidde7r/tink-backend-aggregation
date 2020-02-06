package se.tink.backend.aggregation.agents;

import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.assertions.AgentContractEntitiesJsonParser;
import se.tink.backend.aggregation.agents.framework.assertions.entities.AgentContractEntity;
import se.tink.backend.aggregation.agents.framework.assertions.utils.ResourceFileReader;

public class AgentContractEntitiesJsonParserTest {

    private ResourceFileReader reader = new ResourceFileReader();

    @Test
    public void testContractJsonParsing() {
        String jsonContent =
                reader.read(
                        String.format(
                                "%s/%s",
                                "src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/resources",
                                "contract_test.json"));

        AgentContractEntitiesJsonParser parser = new AgentContractEntitiesJsonParser();
        AgentContractEntity entity = parser.parse(jsonContent);

        Assert.assertEquals(1, entity.getAccounts().size());
        Assert.assertEquals(4, entity.getTransactions().size());
    }
}

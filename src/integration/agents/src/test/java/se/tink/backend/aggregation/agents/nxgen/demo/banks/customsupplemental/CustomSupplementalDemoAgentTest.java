package se.tink.backend.aggregation.agents.nxgen.demo.banks.customsupplemental;

import java.nio.file.Files;
import java.nio.file.Paths;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

public class CustomSupplementalDemoAgentTest {

    private static final String TEST_DATA_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/demo/banks/customsupplemental/resources";

    @Test
    public void testRefresh() throws Exception {
        String json =
                String.join(
                        "",
                        Files.readAllLines(
                                Paths.get(TEST_DATA_PATH, "multipleScreensExample.json")));

        new AgentIntegrationTest.Builder("de", "de-test-custom-supplemental")
                .addCredentialField(Field.Key.USERNAME, json)
                .build()
                .testRefresh();
    }
}

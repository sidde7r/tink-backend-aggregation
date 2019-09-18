package se.tink.backend.aggregation.agents.nxgen.pt.openbanking.ing;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

@Ignore
public class IngAgentTest {

  private AgentIntegrationTest.Builder builder;

  @Before
  public void setup() {
    builder =
        new AgentIntegrationTest.Builder("pt", "pt-ing-ob")
            .setFinancialInstitutionId("ing")
            .setAppId("tink")
            .loadCredentialsBefore(false)
            .saveCredentialsAfter(false)
            .expectLoggedIn(false);
  }

  @Test
  public void testRefresh() throws Exception {
    builder.build().testRefresh();
  }
}

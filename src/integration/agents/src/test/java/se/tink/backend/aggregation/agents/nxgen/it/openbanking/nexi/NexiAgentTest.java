package se.tink.backend.aggregation.agents.nxgen.it.openbanking.nexi;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

@Ignore
public class NexiAgentTest {

  private AgentIntegrationTest.Builder builder;

  @Before
  public void setup() {
    builder =
        new AgentIntegrationTest.Builder("it", "it-nexi-oauth2")
            .expectLoggedIn(false)
            .loadCredentialsBefore(false)
            .saveCredentialsAfter(false)
            .setFinancialInstitutionId("nexi")
            .setAppId("tink");
  }

  @Test
  public void testRefresh() throws Exception {
    builder.build().testRefresh();
  }
}

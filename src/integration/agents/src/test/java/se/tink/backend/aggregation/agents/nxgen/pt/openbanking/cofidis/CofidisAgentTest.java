package se.tink.backend.aggregation.agents.nxgen.pt.openbanking.cofidis;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;


//@Ignore
public class CofidisAgentTest {

  private AgentIntegrationTest.Builder builder;

  @Before
  public void setup() {
    builder =
        new AgentIntegrationTest.Builder("pt", "pt-cofidis-oauth2")
            .loadCredentialsBefore(false)
            .saveCredentialsAfter(false)
            .expectLoggedIn(false);
  }

  @Test
  public void testRefresh() throws Exception {
    builder.build().testRefresh();
  }

}

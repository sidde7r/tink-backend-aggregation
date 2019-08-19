package se.tink.backend.aggregation.agents.nxgen.pt.openbanking.unicre;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

@Ignore
public class UnicreAgentTest {

  private AgentIntegrationTest.Builder builder;

  @Before
  public void setup() {
    builder =
        new AgentIntegrationTest.Builder("pt", "pt-unicre-oauth2")
            .loadCredentialsBefore(false)
            .saveCredentialsAfter(false)
            .expectLoggedIn(false);
  }

  @Test
  public void testRefresh() throws Exception {
    builder.build().testRefresh();
  }

}

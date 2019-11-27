package se.tink.sa.agent;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

@Ignore
public class GenericAgentTest {

    private AgentIntegrationTest.Builder builder;

    @Before
    public void setup() {
        builder =
                new AgentIntegrationTest.Builder("pt", "pt-standalonemillenniumbcp-oauth2")
                        .setFinancialInstitutionId("milleniumPtStandalone")
                        .setAppId("tink")
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false)
                        .expectLoggedIn(false);
    }

    @Test
    public void testRefresh() throws Exception {
        builder.build().testRefresh();
    }

    //    @Test
    //    public void businessPointlessTechnicalFlowTest() throws Exception {
    //        GenericAgent agent = new GenericAgent();
    //        SteppableAuthenticationResponse login =
    //                agent.login(SteppableAuthenticationRequest.initialRequest());
    //    }
}

package se.tink.backend.aggregation.agents.nxgen.demo.banks.dk;

import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

public final class DkDemoAgentTest {

    @Test
    public void testNordeaFlowNemId() throws Exception {
        AgentIntegrationTest.Builder builder =
                new AgentIntegrationTest.Builder("dk", "dk-demo-provider-nordea")
                        .addCredentialField(
                                Field.Key.AUTH_METHOD_SELECTOR,
                                DkAuthMethod.NEM_ID.getSupplementalInfoKeys().get(0))
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false);

        builder.build().testRefresh();
    }

    @Test
    public void testNordeaFlowMitId() throws Exception {
        AgentIntegrationTest.Builder builder =
                new AgentIntegrationTest.Builder("dk", "dk-demo-provider-nordea")
                        .addCredentialField(
                                Field.Key.AUTH_METHOD_SELECTOR,
                                DkAuthMethod.MIT_ID.getSupplementalInfoKeys().get(0))
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false);

        builder.build().testRefresh();
    }

    @Test
    public void testDanskeFlowNemId() throws Exception {
        AgentIntegrationTest.Builder builder =
                new AgentIntegrationTest.Builder("dk", "dk-demo-provider-danske")
                        .addCredentialField(
                                Field.Key.AUTH_METHOD_SELECTOR,
                                DkAuthMethod.NEM_ID.getSupplementalInfoKeys().get(1))
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false);

        builder.build().testRefresh();
    }

    @Test
    public void testDanskeFlowMitId() throws Exception {
        AgentIntegrationTest.Builder builder =
                new AgentIntegrationTest.Builder("dk", "dk-demo-provider-danske")
                        .addCredentialField(
                                Field.Key.AUTH_METHOD_SELECTOR,
                                DkAuthMethod.MIT_ID.getSupplementalInfoKeys().get(1))
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false);

        builder.build().testRefresh();
    }

    @Test
    public void testBecFlowNemId() throws Exception {
        AgentIntegrationTest.Builder builder =
                new AgentIntegrationTest.Builder("dk", "dk-demo-provider-bec")
                        .addCredentialField(
                                Field.Key.AUTH_METHOD_SELECTOR,
                                DkAuthMethod.NEM_ID.getSupplementalInfoKeys().get(0))
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false);

        builder.build().testRefresh();
    }

    @Test
    public void testBecFlowMitId() throws Exception {
        AgentIntegrationTest.Builder builder =
                new AgentIntegrationTest.Builder("dk", "dk-demo-provider-bec")
                        .addCredentialField(
                                Field.Key.AUTH_METHOD_SELECTOR,
                                DkAuthMethod.MIT_ID.getSupplementalInfoKeys().get(0))
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false);

        builder.build().testRefresh();
    }
}

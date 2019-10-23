package se.tink.backend.aggregation.agents.nxgen.dk.banks.sdc.authenticator;

import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.DualAgentIntegrationTest;
import se.tink.libraries.credentials.service.RefreshableItem;

public class SdcDkDualAgentTest {

    private final String USERNAME = "";
    private final String TEST_SSN = "";

    @Test
    public void dualTest() throws Exception {

        DualAgentIntegrationTest.of(
                        new AgentIntegrationTest.Builder("dk", "dk-banknordik-password")
                                .addCredentialField(Field.Key.USERNAME, USERNAME)
                                .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                                .addRefreshableItems(RefreshableItem.IDENTITY_DATA)
                                .loadCredentialsBefore(false)
                                .saveCredentialsAfter(false)
                                .expectLoggedIn(false)
                                .build(),
                        new AgentIntegrationTest.Builder("dk", "dk-banknordik-ob")
                                .addCredentialField(Field.Key.LOGIN_INPUT, TEST_SSN)
                                .loadCredentialsBefore(false)
                                .saveCredentialsAfter(true)
                                .expectLoggedIn(false)
                                .setAppId("tink")
                                .setFinancialInstitutionId("banknordik")
                                .build())
                .testAndCompare();
    }
}

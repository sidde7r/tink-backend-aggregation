package se.tink.backend.aggregation.agents.nxgen.se.openbanking.corporate.handelsbanken;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.CorporateIdArgumentEnum;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.UsernameArgumentEnum;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.corporate.handelsbanken.HandelsbankenSEConstants.CredentialKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseConstants.Scope;

public class HandelsbankenAgentTest {

    private final ArgumentManager<UsernameArgumentEnum> manager =
            new ArgumentManager<>(UsernameArgumentEnum.values());
    private final ArgumentManager<CorporateIdArgumentEnum> Manager =
            new ArgumentManager<>(CorporateIdArgumentEnum.values());
    private AgentIntegrationTest.Builder builder;

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }

    @Before
    public void setup() {
        manager.before();
        Manager.before();
        builder =
                new AgentIntegrationTest.Builder("se", "se-handelsbankencorporate-ob")
                        .addCredentialField(
                                Field.Key.USERNAME, manager.get(UsernameArgumentEnum.USERNAME))
                        .addCredentialField(
                                Key.CORPORATE_ID, Manager.get(CorporateIdArgumentEnum.CPI))
                        .addCredentialField(CredentialKeys.SCOPE, Scope.AIS)
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false)
                        .setFinancialInstitutionId("handelsbanken")
                        .setAppId("tink")
                        .expectLoggedIn(false);
    }

    @Test
    public void testRefresh() throws Exception {
        builder.build().testRefresh();
    }
}

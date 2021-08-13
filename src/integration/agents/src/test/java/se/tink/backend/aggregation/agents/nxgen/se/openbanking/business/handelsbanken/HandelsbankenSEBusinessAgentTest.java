package se.tink.backend.aggregation.agents.nxgen.se.openbanking.business.handelsbanken;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.BusinessIdArgumentEnum;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.UsernameArgumentEnum;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.business.handelsbanken.HandelsbankenSEBusinessConstants.CredentialKeys;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.business.handelsbanken.HandelsbankenSEBusinessConstants.Scope;

public class HandelsbankenSEBusinessAgentTest {

    private final ArgumentManager<UsernameArgumentEnum> usernameArgumentManager =
            new ArgumentManager<>(UsernameArgumentEnum.values());
    private final ArgumentManager<BusinessIdArgumentEnum>
            businessIdArgumentEnumArgumentManagerIdArgument =
                    new ArgumentManager<>(BusinessIdArgumentEnum.values());
    private AgentIntegrationTest.Builder builder;

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }

    @Before
    public void setup() {
        usernameArgumentManager.before();
        businessIdArgumentEnumArgumentManagerIdArgument.before();
        builder =
                new AgentIntegrationTest.Builder("se", "se-handelsbanken-business-ob")
                        .addCredentialField(
                                Field.Key.USERNAME,
                                usernameArgumentManager.get(UsernameArgumentEnum.USERNAME))
                        .addCredentialField(
                                Key.CORPORATE_ID,
                                businessIdArgumentEnumArgumentManagerIdArgument.get(
                                        BusinessIdArgumentEnum.CPI))
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

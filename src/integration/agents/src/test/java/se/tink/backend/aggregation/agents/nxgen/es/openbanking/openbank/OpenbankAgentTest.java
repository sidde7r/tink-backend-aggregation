package se.tink.backend.aggregation.agents.nxgen.es.openbanking.openbank;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.IbanArgumentEnum;

public class OpenbankAgentTest {
    private final ArgumentManager<IbanArgumentEnum> ibanArgumentEnumArgumentManager =
            new ArgumentManager<>(IbanArgumentEnum.values());

    private AgentIntegrationTest.Builder builder;

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }

    @Before
    public void setup() {
        ibanArgumentEnumArgumentManager.before();
        builder =
                new AgentIntegrationTest.Builder("es", "es-redsys-openbank-ob")
                        .addCredentialField(
                                Key.IBAN,
                                ibanArgumentEnumArgumentManager.get(IbanArgumentEnum.IBAN))
                        .setAppId("tink")
                        .setFinancialInstitutionId("17def709bf0b4fc9b79c690c6eb615ad")
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false)
                        .expectLoggedIn(false);
    }

    @Test
    public void testRefresh() throws Exception {
        builder.build().testRefresh();
    }
}

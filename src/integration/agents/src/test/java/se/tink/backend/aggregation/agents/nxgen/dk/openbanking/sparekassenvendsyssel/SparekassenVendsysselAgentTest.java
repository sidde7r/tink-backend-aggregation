package se.tink.backend.aggregation.agents.nxgen.dk.openbanking.sparekassenvendsyssel;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.IbanArgumentEnum;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.BankdataConstants;

@Ignore
public class SparekassenVendsysselAgentTest {

    private final ArgumentManager<IbanArgumentEnum> manager =
            new ArgumentManager<>(IbanArgumentEnum.values());
    private AgentIntegrationTest.Builder builder;

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }

    @Before
    public void setup() {
        manager.before();

        builder =
                new AgentIntegrationTest.Builder("dk", "dk-sparekassenvendsyssel-ob")
                        .addCredentialField(
                                BankdataConstants.CredentialKeys.IBAN,
                                manager.get(IbanArgumentEnum.IBAN))
                        .setFinancialInstitutionId("sparekassenvendsyssel")
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

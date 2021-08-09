package se.tink.backend.aggregation.agents.nxgen.de.openbanking.unicredit;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.tools.UserAvailabilityBuilder;

/**
 * Remember to set proper <strong>PSU Id Type</strong> in <i>./etc/development.yml</i> file. At the
 * time of writing this document allowed values are:
 *
 * <ul>
 *   <li>HVB_ONLINEBANKING
 *   <li>UCEBANKINGGLOBAL
 * </ul>
 *
 * For more details please check documentation at <a
 * href="http://developer.unicredit.eu/">http://developer.unicredit.eu/</a>.
 */
public class UnicreditAgentTest {

    private AgentIntegrationTest.Builder builder;

    @Before
    public void setup() {
        builder =
                new AgentIntegrationTest.Builder("de", "de-unicredit-ob")
                        .setFinancialInstitutionId("unicredit-de")
                        .setAppId("tink")
                        .setUserAvailability(UserAvailabilityBuilder.availableUser())
                        .loadCredentialsBefore(true)
                        .saveCredentialsAfter(false)
                        .setRequestFlagUpdate(false)
                        .expectLoggedIn(false);
    }

    @Test
    public void testRefresh() throws Exception {
        builder.build().testRefresh();
    }
}

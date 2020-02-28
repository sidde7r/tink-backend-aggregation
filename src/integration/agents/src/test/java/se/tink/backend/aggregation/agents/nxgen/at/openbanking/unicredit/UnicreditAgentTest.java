package se.tink.backend.aggregation.agents.nxgen.at.openbanking.unicredit;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

/**
 * Remember to set proper <strong>PSU Id Type</strong> in <i>./etc/development.yml</i> file. At the
 * time of writing this document allowed values are:
 *
 * <ul>
 *   <li>24YOU
 *   <li>BUSINESSNET
 *   <li>UCEBANKINGGLOBAL
 * </ul>
 *
 * For more details please check documentation at <a
 * href="http://developer.unicredit.eu/">http://developer.unicredit.eu/</a>.
 */
@Ignore
public class UnicreditAgentTest {

    private AgentIntegrationTest.Builder builder;

    @Before
    public void setup() {
        builder =
                new AgentIntegrationTest.Builder("at", "at-unicredit-ob")
                        .setFinancialInstitutionId("unicredit-at")
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

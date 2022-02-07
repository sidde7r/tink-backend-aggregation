package se.tink.backend.aggregation.agents.nxgen.de.openbanking.unicredit;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.PasswordArgumentEnum;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.UsernameArgumentEnum;
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

    private final ArgumentManager<UsernameArgumentEnum> usernameManager =
            new ArgumentManager<>(UsernameArgumentEnum.values());
    private final ArgumentManager<PasswordArgumentEnum> passwordManager =
            new ArgumentManager<>(PasswordArgumentEnum.values());
    private AgentIntegrationTest.Builder builder;

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }

    @Before
    public void setup() {
        usernameManager.before();
        passwordManager.before();
        builder =
                new AgentIntegrationTest.Builder("de", "de-unicredit-ob")
                        .addCredentialField(
                                Field.Key.USERNAME,
                                usernameManager.get(UsernameArgumentEnum.USERNAME))
                        .addCredentialField(
                                Field.Key.PASSWORD,
                                passwordManager.get(PasswordArgumentEnum.PASSWORD))
                        .setFinancialInstitutionId("unicredit-de")
                        .setAppId("tink")
                        .setUserAvailability(UserAvailabilityBuilder.availableUser())
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false)
                        .setRequestFlagUpdate(false)
                        .expectLoggedIn(false);
    }

    @Test
    public void testRefresh() throws Exception {
        builder.build().testRefresh();
    }
}

package se.tink.backend.aggregation.agents.nxgen.se.openbanking.entercard;

import java.util.Arrays;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.FeatureFlags;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.SsnArgumentEnum;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.entercard.EnterCardConstants.CredentialKeys;
import se.tink.libraries.user.rpc.User;
import se.tink.libraries.user.rpc.UserProfile;

public class EnterCardAgentTest {
    private final ArgumentManager<SsnArgumentEnum> manager =
            new ArgumentManager<>(SsnArgumentEnum.values());
    private AgentIntegrationTest.Builder builder;

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }

    private static User createUser() {
        final User user = new User();
        user.setFlags(
                Arrays.asList(FeatureFlags.TINK_EMPLOYEE, FeatureFlags.MULTI_CURRENCY_PHASE_1));
        final UserProfile profile = new UserProfile();
        profile.setLocale("sv_SE");
        user.setProfile(profile);
        return user;
    }

    @Before
    public void setup() {
        manager.before();

        builder =
                new AgentIntegrationTest.Builder("se", "se-entercard-oauth2")
                        .addCredentialField(CredentialKeys.SSN, manager.get(SsnArgumentEnum.SSN))
                        .setFinancialInstitutionId("entercard")
                        .setAppId("tink")
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false)
                        .setUser(createUser())
                        .expectLoggedIn(false);
    }

    @Test
    public void testRefresh() throws Exception {
        builder.build().testRefresh();
    }
}

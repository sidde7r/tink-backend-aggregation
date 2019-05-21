package se.tink.backend.aggregation.agents.nxgen.se.openbanking.entercard;

import java.util.Arrays;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.agents.rpc.FeatureFlags;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.libraries.user.rpc.User;
import se.tink.libraries.user.rpc.UserProfile;

@Ignore
public class EnterCardAgentTest {

    private AgentIntegrationTest.Builder builder;

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
        builder =
                new AgentIntegrationTest.Builder("se", "se-entercard-oauth2")
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

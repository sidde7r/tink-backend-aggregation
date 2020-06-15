package se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.agent;

import static com.fasterxml.jackson.annotation.JsonFormat.DEFAULT_LOCALE;

import java.util.Arrays;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.libraries.enums.FeatureFlags;
import se.tink.libraries.user.rpc.User;
import se.tink.libraries.user.rpc.UserProfile;

public class FinecoBankAgentTest {

    private AgentIntegrationTest.Builder builder;

    @Before
    public void setup() {
        UserProfile profile = new UserProfile();
        profile.setLocale(DEFAULT_LOCALE);
        User user = new User();
        user.setId("deadbeefdeadbeefdeadbeefdeadbeef");
        user.setProfile(profile);
        user.setFlags(
                Arrays.asList(FeatureFlags.TINK_EMPLOYEE, FeatureFlags.MULTI_CURRENCY_PHASE_1));
        builder =
                new AgentIntegrationTest.Builder("it", "it-finecobank-oauth2")
                        .setFinancialInstitutionId("finecobank")
                        .setAppId("tink")
                        .expectLoggedIn(false)
                        // .setUser(user)
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false);
    }

    @Test
    public void testRefresh() throws Exception {
        builder.build().testRefresh();
    }
}

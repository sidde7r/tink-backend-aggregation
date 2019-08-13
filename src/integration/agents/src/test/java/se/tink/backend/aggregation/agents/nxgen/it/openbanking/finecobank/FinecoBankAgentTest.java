package se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank;

import static com.fasterxml.jackson.annotation.JsonFormat.DEFAULT_LOCALE;

import java.util.Arrays;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.libraries.enums.FeatureFlags;
import se.tink.libraries.user.rpc.User;
import se.tink.libraries.user.rpc.UserProfile;

@Ignore
public class FinecoBankAgentTest {

    private final ArgumentManager<Arg> manager = new ArgumentManager<>(Arg.values());
    private AgentIntegrationTest.Builder builder;

    @Before
    public void setup() {

        manager.before();

        UserProfile profile = new UserProfile();
        profile.setLocale(DEFAULT_LOCALE);

        User user = new User();
        user.setId("deadbeefdeadbeefdeadbeefdeadbeef");
        user.setProfile(profile);
        user.setFlags(
                Arrays.asList(FeatureFlags.TINK_EMPLOYEE, FeatureFlags.MULTI_CURRENCY_PHASE_1));

        builder =
                new AgentIntegrationTest.Builder("it", "it-finecobank-oauth2")
                        .expectLoggedIn(false)
                        .setUser(user)
                        .loadCredentialsBefore(Boolean.parseBoolean(manager.get(Arg.LOAD_BEFORE)))
                        .saveCredentialsAfter(Boolean.parseBoolean(manager.get(Arg.SAVE_AFTER)));
    }

    @Test
    public void testRefresh() throws Exception {
        builder.build().testRefresh();
    }

    private enum Arg {
        SAVE_AFTER,
        LOAD_BEFORE
    }
}

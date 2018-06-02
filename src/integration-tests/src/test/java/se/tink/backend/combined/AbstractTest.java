package se.tink.backend.combined;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import se.tink.backend.api.CredentialsService;
import se.tink.backend.api.UserService;
import se.tink.backend.auth.AuthenticatedUser;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.CredentialsStatus;
import se.tink.backend.core.User;
import se.tink.backend.core.UserProfile;
import se.tink.libraries.auth.HttpAuthenticationMethod;
import se.tink.backend.rpc.UserLoginResponse;
import se.tink.backend.utils.LogUtils;

public abstract class AbstractTest {
    protected static final ObjectMapper objectMapper = new ObjectMapper();
    protected static final LogUtils log = new LogUtils(AbstractTest.class);
    protected static final Random RANDOM = new Random();

    /**
     * Create a random username for testing.
     */
    protected String randomUsername() {
        String userName = ("user" + Math.abs(RANDOM.nextInt()) + "@test.tink.se");

        return userName;
    }

    /**
     * Helper method to create a user without demo credentials and data.
     */
    public User registerUser(UserService userService, String username, String password, UserProfile profile) {
        User user = new User();

        user.setUsername(username);
        user.setPassword(password);
        user.setProfile(profile);

        UserLoginResponse response = userService.register(user);

        return response.getContext().getUser();
    }

    /**
     * Helper method for polling the status of the refresh-credentials request.
     * 
     * @throws InterruptedException
     */
    protected static void waitForRefresh(User sessionId, CredentialsService credentialsService)
            throws InterruptedException {
        // Waits 1m, terminating if nothing found by then.

        for (int i = 0; i < 60; i++) {
            List<Credentials> cs = credentialsService
                    .list(new AuthenticatedUser(HttpAuthenticationMethod.BASIC, sessionId));

            boolean done = true;

            for (Credentials c : cs) {
                log.info(c.getId() + " - " + c.getStatus() + " - " + c.getStatusPayload());

                if (c.getStatus().equals(CredentialsStatus.UPDATING)
                        || c.getStatus().equals(CredentialsStatus.CREATED)
                        || c.getStatus().equals(CredentialsStatus.AUTHENTICATING)
                        || c.getStatus().equals(CredentialsStatus.AWAITING_MOBILE_BANKID_AUTHENTICATION)
                        || c.getStatus().equals(CredentialsStatus.AWAITING_SUPPLEMENTAL_INFORMATION)) {
                    done = false;
                    break;
                }
            }

            if (done) {
                break;
            }

            Thread.sleep(1000);
        }
    }

    /**
     * Creates a sample user profile.
     */
    protected static UserProfile createUserProfile() {
        UserProfile profile = new UserProfile();

        profile.setMarket("SE");
        profile.setCurrency("SEK");
        profile.setLocale("sv_SE");

        return profile;
    }

    /**
     * Runs a bunch of runnables in parallel.
     */
    protected void runInParallel(List<Runnable> runnables) throws InterruptedException {
        final ExecutorService executor = Executors.newFixedThreadPool(runnables.size());

        for (Runnable runnable : runnables) {
            executor.execute(runnable);
        }

        executor.shutdown();

        if (!executor.awaitTermination(30, TimeUnit.MINUTES)) {
            executor.shutdownNow();
        }
    }
}

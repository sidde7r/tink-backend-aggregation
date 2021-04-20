package se.tink.backend.aggregation;

import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Provider;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.credentials.service.RefreshInformationRequest;
import se.tink.libraries.credentials.service.UserAvailability;
import se.tink.libraries.user.rpc.User;

public class AgentRequestTest {

    @Test
    public void testInformationRefreshRequestLockPath() {
        Credentials credentials = new Credentials();
        credentials.setFieldsSerialized("{}");
        credentials.setProviderName("jens-test-provider");

        UserAvailability userAvailability = new UserAvailability();
        userAvailability.setUserPresent(false);
        userAvailability.setUserAvailableForInteraction(false);
        userAvailability.setOriginatingUserIp("127.0.0.1");

        CredentialsRequest request =
                RefreshInformationRequest.builder()
                        .user(new User())
                        .provider(new Provider())
                        .credentials(credentials)
                        .originatingUserIp("127.0.0.1")
                        .userAvailability(userAvailability)
                        .manual(false)
                        .forceAuthenticate(false)
                        .build();

        Assert.assertTrue(
                request.constructLockPath("SALT")
                        .matches(
                                "/locks/refreshCredentials/credentials/[0-9a-f]{10}/[0-9a-f]{10}"));
    }
}

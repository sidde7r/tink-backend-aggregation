package se.tink.backend.aggregation;

import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Provider;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.backend.aggregation.rpc.RefreshInformationRequest;
import se.tink.libraries.user.rpc.User;

public class AgentRequestTest {

    @Test
    public void testInformationRefreshRequestLockPath() {
        Credentials credentials = new Credentials();
        credentials.setFieldsSerialized("{}");
        credentials.setProviderName("jens-test-provider");

        CredentialsRequest request = new RefreshInformationRequest(new User(), new Provider(), credentials, false);
        
        Assert.assertTrue(request.constructLockPath("SALT").matches("/locks/refreshCredentials/credentials/[0-9a-f]{10}/[0-9a-f]{10}"));
    }
    
}

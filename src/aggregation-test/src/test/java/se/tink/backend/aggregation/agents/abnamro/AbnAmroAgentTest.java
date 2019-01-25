package se.tink.backend.aggregation.agents.abnamro;

import org.junit.Test;
import se.tink.backend.aggregation.agents.AbstractAgentTest;
import se.tink.backend.aggregation.rpc.Credentials;
import se.tink.backend.aggregation.rpc.CredentialsTypes;
import se.tink.backend.agents.rpc.User;
import se.tink.libraries.uuid.UUIDUtils;

public class AbnAmroAgentTest extends AbstractAgentTest<AbnAmroAgent> {

    public AbnAmroAgentTest() {
        super(AbnAmroAgent.class);
    }

    @Test
    public void testEnrollment() throws Exception {
        Credentials credentials = new Credentials();

        credentials.setId(UUIDUtils.generateUUID());
        credentials.setUserId(UUIDUtils.generateUUID());
        credentials.setType(CredentialsTypes.PASSWORD);
        credentials.setProviderName("nl-abnamro");

        testAgent(credentials);

        int i = 10;
    }

    @Override
    protected User createUser(Credentials credentials) {
        User user = super.createUser(credentials);

        // Need a proper phone number on the user to be able to start enrollment at ABN AMRO.
        user.setUsername("+46709202541");

        return user;
    }
}

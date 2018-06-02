package se.tink.backend.aggregation.agents.banks.uk;

import org.junit.Test;
import se.tink.backend.aggregation.agents.AbstractAgentTest;
import se.tink.backend.aggregation.agents.banks.uk.barclays.BarclaysAgent;
import se.tink.backend.aggregation.rpc.Credentials;
import se.tink.backend.aggregation.rpc.CredentialsTypes;

public class BarclaysAgentTest extends AbstractAgentTest<BarclaysAgent> {
    public BarclaysAgentTest() {
        super(BarclaysAgent.class);
    }

    @Test
    public void testUser1PasswordActivate() throws Exception {
        Credentials credentials = new Credentials();
        credentials.setField("firstName", "-");
        credentials.setField("lastName", "-");
        credentials.setField("sortCode", "-");
        credentials.setField("accountNumber", "-");
        credentials.setField("phoneNumber", "-");
        credentials.setType(CredentialsTypes.PASSWORD);

        testAgent(credentials);
    }
}

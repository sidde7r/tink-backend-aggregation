package se.tink.backend.aggregation.agents.creditcards.okq8;

import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.legacy.AbstractAgentTest;
import se.tink.libraries.social.security.ssn.TestSSN;

public class OKQ8BankAgentTest extends AbstractAgentTest<OKQ8BankAgent> {
    public OKQ8BankAgentTest() {
        super(OKQ8BankAgent.class);
    }

    @Test
    @Ignore("Broken test")
    public void testUser1AuthenticationError() throws Exception {
        testAgentAuthenticationError(TestSSN.FH, "testtest");
    }
}

package se.tink.backend.aggregation.agents.creditcards.okq8;

import org.junit.Test;
import se.tink.backend.aggregation.agents.AbstractAgentTest;
import se.tink.backend.common.utils.TestSSN;

public class OKQ8BankAgentTest extends AbstractAgentTest<OKQ8BankAgent> {
    public OKQ8BankAgentTest() {
        super(OKQ8BankAgent.class);
    }

    @Test
    public void testUser1AuthenticationError() throws Exception {
        testAgentAuthenticationError(TestSSN.FH, "testtest");
    }
}

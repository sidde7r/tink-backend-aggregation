package se.tink.backend.aggregation.agents.banks.marginalenbank;

import org.junit.Test;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.aggregation.agents.banks.se.marginalenbank.MarginalenBankAgent;
import se.tink.backend.aggregation.agents.framework.AbstractAgentTest;

public class MarginalenBankAgentTest extends AbstractAgentTest<MarginalenBankAgent> {

    public MarginalenBankAgentTest() {
        super(MarginalenBankAgent.class);
    }

    @Test
    public void newUser() throws Exception {
        testAgent(null, null, CredentialsTypes.MOBILE_BANKID);
    }
}

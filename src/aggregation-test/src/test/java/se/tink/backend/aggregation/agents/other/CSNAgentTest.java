package se.tink.backend.aggregation.agents.other;

import org.junit.Test;
import se.tink.backend.aggregation.agents.AbstractAgentTest;
import se.tink.backend.agents.rpc.CredentialsTypes;

public class CSNAgentTest extends AbstractAgentTest<CSNAgent> {
	public CSNAgentTest() {
		super(CSNAgent.class);
	}

	@Test
	public void testUser1() throws Exception {
		testAgent("198203300382", "6110", CredentialsTypes.PASSWORD, false);
	}

	@Test
	public void testUser1AuthenticationError() throws Exception {
		testAgentAuthenticationError("198203300382", "6111");
	}
}

package se.tink.backend.aggregation.agents.demo;

import org.junit.Test;
import se.tink.backend.aggregation.agents.AbstractAgentTest;
import se.tink.credentials.demo.DemoCredentials;

public class DemoAgentTest extends AbstractAgentTest<DemoAgent> {
    public DemoAgentTest() {
        super(DemoAgent.class);
    }

    @Test
    public void testUserEng() throws Exception {
        testAgent(DemoCredentials.USER3.getUsername(), "demo");
    }

    @Test
    public void testUserSwe() throws Exception {
        testAgent(DemoCredentials.USER4.getUsername(), "demo");
    }
    
    @Test
    public void testUserSupplementalInformation() throws Exception {
        testAgent(DemoCredentials.USER8.getUsername(), "demo");
    } 
    
    @Test
    public void testUserBankId() throws Exception {
        testAgent(DemoCredentials.USER9.getUsername(), "demo");
    }
}

package se.tink.backend.aggregation.agents.demo;

import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.legacy.AbstractAgentTest;
import se.tink.libraries.credentials.demo.DemoCredentials;

public class DemoAgentTest extends AbstractAgentTest<DemoAgent> {
    public DemoAgentTest() {
        super(DemoAgent.class);
    }

    @Test
    @Ignore("Broken test")
    public void testUserEng() throws Exception {
        testAgent(DemoCredentials.USER3.getUsername(), "demo");
    }

    @Test
    @Ignore("Broken test")
    public void testUserSwe() throws Exception {
        testAgent(DemoCredentials.USER4.getUsername(), "demo");
    }

    @Test
    @Ignore("Broken test")
    public void testUserSupplementalInformation() throws Exception {
        testAgent(DemoCredentials.USER8.getUsername(), "demo");
    }

    @Test
    @Ignore("Broken test")
    public void testUserBankId() throws Exception {
        testAgent(DemoCredentials.USER9.getUsername(), "demo");
    }
}

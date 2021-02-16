package se.tink.backend.aggregation.agents.creditcards.rikskortet;

import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.legacy.AbstractAgentTest;
import se.tink.libraries.social.security.ssn.TestSSN;

public class RikskortetAgentTest extends AbstractAgentTest<RikskortetAgent> {
    public RikskortetAgentTest() {
        super(RikskortetAgent.class);

        System.setProperty("com.sun.xml.ws.transport.http.client.HttpTransportPipe.dump", "true");
        System.setProperty(
                "com.sun.xml.internal.ws.transport.http.client.HttpTransportPipe.dump", "true");
        System.setProperty("com.sun.xml.ws.transport.http.HttpAdapter.dump", "true");
        System.setProperty("com.sun.xml.internal.ws.transport.http.HttpAdapter.dump", "true");
    }

    @Test
    @Ignore("Broken test")
    public void testUser1() throws Exception {
        testAgent("8404162524", "A7MydzwA");
    }

    @Test
    @Ignore("Broken test")
    public void testUser1AuthenticationError() throws Exception {
        testAgentAuthenticationError("198404162524", "testtest");
    }

    @Test
    @Ignore("Broken test")
    public void testUser2AuthenticationError() throws Exception {
        testAgentAuthenticationError(TestSSN.FH, "testtest");
    }
}

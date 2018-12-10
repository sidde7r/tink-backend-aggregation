package se.tink.backend.aggregation.agents.creditcards.rikskortet;

import org.junit.Test;
import se.tink.backend.aggregation.agents.AbstractAgentTest;
import se.tink.libraries.social.security.TestSSN;

public class RikskortetAgentTest extends AbstractAgentTest<RikskortetAgent> {
    public RikskortetAgentTest() {
        super(RikskortetAgent.class);

        System.setProperty("com.sun.xml.ws.transport.http.client.HttpTransportPipe.dump", "true");
        System.setProperty("com.sun.xml.internal.ws.transport.http.client.HttpTransportPipe.dump", "true");
        System.setProperty("com.sun.xml.ws.transport.http.HttpAdapter.dump", "true");
        System.setProperty("com.sun.xml.internal.ws.transport.http.HttpAdapter.dump", "true");
    }

    @Test
    public void testUser1() throws Exception {
        testAgent("8404162524", "A7MydzwA");
    }

    @Test
    public void testUser1AuthenticationError() throws Exception {
        testAgentAuthenticationError("198404162524", "testtest");
    }

    @Test
    public void testUser2AuthenticationError() throws Exception {
        testAgentAuthenticationError(TestSSN.FH, "testtest");
    }
}

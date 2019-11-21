package se.tink.sa.agent;

import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.aggregation.nxgen.controllers.authentication.SteppableAuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.SteppableAuthenticationResponse;

@Ignore
public class GenericAgentTest {

    @Test
    public void businessPointlessTechnicalFlowTest() throws Exception {
        GenericAgent agent = new GenericAgent();
        SteppableAuthenticationResponse login =
                agent.login(SteppableAuthenticationRequest.initialRequest());
    }
}

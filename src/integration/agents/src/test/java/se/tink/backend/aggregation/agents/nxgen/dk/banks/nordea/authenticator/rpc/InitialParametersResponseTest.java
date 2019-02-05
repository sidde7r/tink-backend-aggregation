package se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.authenticator.rpc;

import org.junit.Assert;
import org.junit.Test;
import static org.junit.Assert.assertNotNull;

public class InitialParametersResponseTest {

    @Test
    public void initialParametersResponse() throws Exception {
        InitialParametersResponse response = InitialParametersResponseTestData.getTestData();
        assertNotNull(response.getInitialParametersResponse());
        assertNotNull(response.getInitialParametersResponse().getInitialParameters());

        Assert.assertEquals("js", response.getInitialParametersResponse().getInitialParameters().getParamTable().getKey());
        Assert.assertNotNull(response.getInitialParametersResponse().getInitialParameters().getParamTable().getVal());
        Assert.assertEquals("DK-2017-12-13T12:37:33.192Z-9574a", response.getInitialParametersResponse().getSessionId());
    }
}

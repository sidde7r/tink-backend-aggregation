package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.authenticator.rpc;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno.SdcNoConfiguration;

public class SdcErrorMessagesTest {
    private SdcNoConfiguration agentConfiguration;

    @Before
    public void setUp() throws Exception {
        Provider provider = new Provider();
        provider.setPayload("1234");

        agentConfiguration = new SdcNoConfiguration(provider);
    }

    @Test
    public void noCustomerErrorMessage() {
        String errorMessage = "You have no agreement";
        Assert.assertTrue(agentConfiguration.isNotCustomer(errorMessage));
    }

    @Test
    public void notNoCustomerErrorMessage() {
        String errorMessage = "I am a little teapot";
        Assert.assertFalse(agentConfiguration.isNotCustomer(errorMessage));
    }
}

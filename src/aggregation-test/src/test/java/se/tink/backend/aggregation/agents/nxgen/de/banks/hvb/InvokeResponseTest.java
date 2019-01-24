package se.tink.backend.aggregation.agents.nxgen.de.banks.hvb;

import java.util.Optional;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.worklight.InvokeResponse;

public final class InvokeResponseTest {

    @Test
    public void testGetMessages() {
        final InvokeResponse invokeResponse = new InvokeResponse();
        // None of the members are set during initialization -> assert that there is no message
        Assert.assertEquals(invokeResponse.getMessages(), Optional.empty());
    }
}

package se.tink.backend.aggregation.aggregationcontroller;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateCredentialsStatusRequest;
import se.tink.backend.aggregationcontroller.v1.rpc.enums.CredentialsRequestType;

public class UpdateCredentialsStatusRequestTest {

    private CredentialsRequestType[] aggregationServiceRequestTypes;
    private UpdateCredentialsStatusRequest updateRequest;

    @Before
    public void setUp() {
        aggregationServiceRequestTypes = CredentialsRequestType.values();
        updateRequest = new UpdateCredentialsStatusRequest();
    }

    @Test
    public void ensureAllRequestTypesCanTranslate() {
        for (CredentialsRequestType rt : aggregationServiceRequestTypes) {
            updateRequest.setRequestType(rt);
            Assert.assertNotNull(updateRequest.getRequestType());
        }
        Assert.assertNotNull(updateRequest.getRequestType());
    }

    @Test
    public void ensureNullDoesNotTranslate() {
        updateRequest.setRequestType(null);
        Assert.assertNull(updateRequest.getRequestType());
    }
}

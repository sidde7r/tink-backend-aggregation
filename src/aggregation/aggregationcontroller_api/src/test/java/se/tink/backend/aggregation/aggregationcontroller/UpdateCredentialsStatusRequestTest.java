package se.tink.backend.aggregation.aggregationcontroller;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateCredentialsStatusRequest;
import se.tink.libraries.credentials.service.CredentialsRequestType;

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
            updateRequest.setCredentialsRequestType(rt);
            Assert.assertNotNull(updateRequest.getCredentialsRequestType());
        }
        Assert.assertNotNull(updateRequest.getCredentialsRequestType());
    }

    @Test
    public void ensureNullDoesNotTranslate() {
        updateRequest.setCredentialsRequestType(null);
        Assert.assertNull(updateRequest.getCredentialsRequestType());
    }
}

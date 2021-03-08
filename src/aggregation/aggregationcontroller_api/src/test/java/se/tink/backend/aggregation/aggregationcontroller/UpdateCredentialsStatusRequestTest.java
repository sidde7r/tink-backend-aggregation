package se.tink.backend.aggregation.aggregationcontroller;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateCredentialsStatusRequest;
import se.tink.backend.aggregationcontroller.v1.rpc.enums.CredentialsRequestType;
import se.tink.connectivity.errors.ConnectivityError;
import se.tink.connectivity.errors.ConnectivityErrorDetails;
import se.tink.connectivity.errors.ConnectivityErrorType;

public class UpdateCredentialsStatusRequestTest {

    private static final ObjectMapper mapper = new ObjectMapper();
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

    @Test
    public void ensureRequestObjectWithErrorSerializesAndDeserializes() throws IOException {
        // given
        updateRequest.setDetailedError(
                ConnectivityError.newBuilder()
                        .setType(ConnectivityErrorType.ERROR_AUTH_DYNAMIC_FLOW_CANCELLED)
                        .setDetails(
                                ConnectivityErrorDetails.newBuilder().setRetryable(true).build())
                        .build());

        // when
        String json = mapper.writeValueAsString(updateRequest);
        UpdateCredentialsStatusRequest result =
                mapper.readValue(json, UpdateCredentialsStatusRequest.class);

        // then
        Assert.assertEquals(
                ConnectivityErrorType.ERROR_AUTH_DYNAMIC_FLOW_CANCELLED,
                result.getDetailedError().getType());
        Assert.assertTrue(result.getDetailedError().getDetails().getRetryable());
    }

    @Test
    public void ensureRequestObjectWithNullErrorSerializesAndDeserializes() throws IOException {
        // given
        String json = mapper.writeValueAsString(updateRequest);

        // when
        UpdateCredentialsStatusRequest result =
                mapper.readValue(json, UpdateCredentialsStatusRequest.class);

        // then
        Assert.assertNull(result.getDetailedError());
    }

    @Test
    public void ensureRequestObjectWithUnknownErrorSerializesAndDeserializes() throws IOException {
        // given
        updateRequest.setDetailedError(
                ConnectivityError.newBuilder()
                        .setType(ConnectivityErrorType.UNKNOWN_ERROR)
                        .build());

        // when
        String json = mapper.writeValueAsString(updateRequest);
        UpdateCredentialsStatusRequest result =
                mapper.readValue(json, UpdateCredentialsStatusRequest.class);

        // then
        Assert.assertEquals(
                ConnectivityErrorType.UNKNOWN_ERROR, result.getDetailedError().getType());
    }
}

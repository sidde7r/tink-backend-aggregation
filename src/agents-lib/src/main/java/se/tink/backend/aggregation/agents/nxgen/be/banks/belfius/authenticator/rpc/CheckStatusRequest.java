package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.rpc;

import com.google.common.collect.ImmutableMap;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.BelfiusConstants;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc.ExecuteMethodRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc.BelfiusRequest;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CheckStatusRequest extends BelfiusRequest {

    public static Builder create(String panNumber, String deviceTokenHash) {
        return BelfiusRequest.builder()
                .setApplicationId(BelfiusConstants.Request.CHECK_STATUS_APPLICATION_ID)
                .setExecutionMode(BelfiusConstants.Request.CHECK_STATUS_EXECUTION_MODE)
                .setRequests(ExecuteMethodRequest.builder()
                        .setApplicationId(BelfiusConstants.Request.CHECK_STATUS_APPLICATION_ID)
                        .setMethodId(BelfiusConstants.Request.CHECK_STATUS_METHOD_ID)
                        .setServiceName(BelfiusConstants.Request.CHECK_STATUS_SERVICE_NAME)
                        .setInputs(ImmutableMap.<String, Object>builder()
                                .put("PanNumber", panNumber)
                                .put("DeviceToken", deviceTokenHash).build()).build());
    }
}

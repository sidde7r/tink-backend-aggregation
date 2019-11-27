package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.BelfiusConstants;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc.BelfiusRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc.ExecuteMethodRequest;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FeedStructureRequest extends BelfiusRequest {

    public static Builder create() {
        return BelfiusRequest.builder()
                .setApplicationId(BelfiusConstants.Request.CHECK_STATUS_APPLICATION_ID)
                .setExecutionMode(BelfiusConstants.Request.CHECK_STATUS_EXECUTION_MODE)
                .setRequests(
                        ExecuteMethodRequest.builder()
                                .setApplicationId(
                                        BelfiusConstants.Request.CHECK_STATUS_APPLICATION_ID)
                                .setMethodId("getFeedStructure")
                                .setServiceName("gef0.gef1.gemd.FeedStructure.diamlservice")
                                .build());
    }

    public static Builder createHybridMapping() {
        return BelfiusRequest.builder()
                .setApplicationId(BelfiusConstants.Request.CHECK_STATUS_APPLICATION_ID)
                .setExecutionMode(BelfiusConstants.Request.CHECK_STATUS_EXECUTION_MODE)
                .setRequests(
                        ExecuteMethodRequest.builder()
                                .setApplicationId(
                                        BelfiusConstants.Request.CHECK_STATUS_APPLICATION_ID)
                                .setMethodId("hybridMapping")
                                .setServiceName("gef0.gef1.gemd.HybridMapping.diamlservice")
                                .build());
    }

    public static Builder createBacProductList() {
        return BelfiusRequest.builder()
                .setApplicationId(BelfiusConstants.Request.CHECK_STATUS_APPLICATION_ID)
                .setExecutionMode(BelfiusConstants.Request.CHECK_STATUS_EXECUTION_MODE)
                .setRequests(
                        ExecuteMethodRequest.builder()
                                .setApplicationId(
                                        BelfiusConstants.Request.CHECK_STATUS_APPLICATION_ID)
                                .setMethodId("BAC_ProductList")
                                .setServiceName("gef0.gef1.gemd.BecomingACustomer.diamlservice")
                                .build());
    }
}

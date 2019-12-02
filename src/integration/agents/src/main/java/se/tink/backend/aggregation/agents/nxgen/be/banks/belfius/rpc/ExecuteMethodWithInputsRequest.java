package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ExecuteMethodWithInputsRequest extends RequestEntity {

    private String applicationId;
    private String methodId;
    private String serviceName;
    private Map<String, Object> inputs;

    public static ExecuteMethodWithInputsRequest.Builder builder() {
        return new ExecuteMethodWithInputsRequest.Builder();
    }

    public static RequestEntity createGetAppMessageText() {
        return builder()
                .setApplicationId("services")
                .setMethodId("List")
                .setInputs(
                        ImmutableMap.<String, Object>builder()
                                .put("AppRelease", "09310")
                                .put("Application", "BDM")
                                .put("Language", "NL")
                                .put("Platform", "I")
                                .put("TypeDevice", "PHONE")
                                .build())
                .build();
    }

    public static class Builder {
        private ExecuteMethodWithInputsRequest method;

        public Builder() {
            method = new ExecuteMethodWithInputsRequest();
        }

        public ExecuteMethodWithInputsRequest.Builder setApplicationId(String applicationId) {
            method.applicationId = applicationId;
            return this;
        }

        public ExecuteMethodWithInputsRequest.Builder setMethodId(String methodId) {
            method.methodId = methodId;
            return this;
        }

        public ExecuteMethodWithInputsRequest.Builder setServiceName(String serviceName) {
            method.serviceName = serviceName;
            return this;
        }

        public ExecuteMethodWithInputsRequest.Builder setInputs(Map<String, Object> inputs) {
            method.inputs = inputs;
            return this;
        }

        public ExecuteMethodWithInputsRequest build() {
            return method;
        }
    }
}

package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.BelfiusConstants.Request;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ExecuteMethodGetAppMessageTextRequest extends RequestEntity {

    private String applicationId;
    private String methodId;
    private String serviceName;
    private Map<String, Object> inputs;

    public static ExecuteMethodGetAppMessageTextRequest.Builder builder() {
        return new ExecuteMethodGetAppMessageTextRequest.Builder();
    }

    public static RequestEntity create() {
        return builder()
                .setApplicationId("services")
                .setMethodId("List")
                .setServiceName(Request.GET_APP_MESSAGE_TEXT_NAME)
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
        private ExecuteMethodGetAppMessageTextRequest method;

        public Builder() {
            method = new ExecuteMethodGetAppMessageTextRequest();
        }

        public ExecuteMethodGetAppMessageTextRequest.Builder setApplicationId(
                String applicationId) {
            method.applicationId = applicationId;
            return this;
        }

        public ExecuteMethodGetAppMessageTextRequest.Builder setMethodId(String methodId) {
            method.methodId = methodId;
            return this;
        }

        public ExecuteMethodGetAppMessageTextRequest.Builder setServiceName(String serviceName) {
            method.serviceName = serviceName;
            return this;
        }

        @JsonIgnore
        public ExecuteMethodGetAppMessageTextRequest.Builder setInputs(Map<String, Object> inputs) {
            method.inputs = inputs;
            return this;
        }

        public ExecuteMethodGetAppMessageTextRequest build() {
            return method;
        }
    }
}

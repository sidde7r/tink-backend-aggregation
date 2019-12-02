package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.BelfiusConstants.Request;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ExecuteMethodContractRequest extends RequestEntity {

    private String applicationId;
    private String methodId;
    private String serviceName;
    private Map<String, Object> inputs;

    public static ExecuteMethodContractRequest.Builder builder() {
        return new ExecuteMethodContractRequest.Builder();
    }

    public static RequestEntity create() {
        return builder()
                .setApplicationId("services")
                .setMethodId("CheckStatus")
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
        private ExecuteMethodContractRequest method;

        public Builder() {
            method = new ExecuteMethodContractRequest();
        }

        public ExecuteMethodContractRequest.Builder setApplicationId(String applicationId) {
            method.applicationId = applicationId;
            return this;
        }

        public ExecuteMethodContractRequest.Builder setMethodId(String methodId) {
            method.methodId = methodId;
            return this;
        }

        public ExecuteMethodContractRequest.Builder setServiceName(String serviceName) {
            method.serviceName = serviceName;
            return this;
        }

        @JsonIgnore
        public ExecuteMethodContractRequest.Builder setInputs(Map<String, Object> inputs) {
            method.inputs = inputs;
            return this;
        }

        public ExecuteMethodContractRequest build() {
            return method;
        }
    }
}

package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc;

import java.util.Map;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ExecuteMethodRequest extends RequestEntity {

    private String applicationId;
    private String methodId;
    private String serviceName;
    private Map<String, Object> inputs;

    public static ExecuteMethodRequest.Builder builder() {
        return new ExecuteMethodRequest.Builder();
    }

    public static class Builder {
        private ExecuteMethodRequest method;

        public Builder() {
            method = new ExecuteMethodRequest();
        }

        public ExecuteMethodRequest.Builder setApplicationId(String applicationId) {
            method.applicationId = applicationId;
            return this;
        }

        public ExecuteMethodRequest.Builder setMethodId(String methodId) {
            method.methodId = methodId;
            return this;
        }

        public ExecuteMethodRequest.Builder setServiceName(String serviceName) {
            method.serviceName = serviceName;
            return this;
        }

        public ExecuteMethodRequest.Builder setInputs(Map<String, Object> inputs) {
            method.inputs = inputs;
            return this;
        }

        public ExecuteMethodRequest build() {
            return method;
        }
    }
}

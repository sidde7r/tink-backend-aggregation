package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.google.common.collect.Lists;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.BelfiusConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.serialization.utils.SerializationUtils;

@JsonObject
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BelfiusRequest {

    private String executionMode;
    private String requestCounter;
    private String applicationId;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean retry;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String sessionId;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String transactionId;

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.WRAPPER_OBJECT)
    @JsonSubTypes({
        @JsonSubTypes.Type(
                value = OpenSessionRequest.class,
                name = BelfiusConstants.Response.Attribute.OPEN_SESSION),
        @JsonSubTypes.Type(
                value = WidgetEventsRequest.class,
                name = BelfiusConstants.Response.Attribute.WIDGET_EVENTS),
        @JsonSubTypes.Type(
                value = StartFlowRequest.class,
                name = BelfiusConstants.Response.Attribute.START_FLOW),
        @JsonSubTypes.Type(
                value = ExecuteMethodRequest.class,
                name = BelfiusConstants.Response.Attribute.EXECUTE_METHOD),
        @JsonSubTypes.Type(
                value = ExecuteMethodGetAppMessageTextRequest.class,
                name = BelfiusConstants.Response.Attribute.EXECUTE_METHOD),
        @JsonSubTypes.Type(
                value = ExecuteMethodContractRequest.class,
                name = BelfiusConstants.Response.Attribute.EXECUTE_METHOD),
        @JsonSubTypes.Type(
                value = ExecuteMethodWithInputsRequest.class,
                name = BelfiusConstants.Response.Attribute.EXECUTE_METHOD),
    })
    private List<RequestEntity> requests;

    public String getExecutionMode() {
        return this.executionMode;
    }

    public String getRequestCounter() {
        return this.requestCounter;
    }

    public String getApplicationId() {
        return this.applicationId;
    }

    public Boolean getRetry() {
        return this.retry;
    }

    public String getSessionId() {
        return this.sessionId;
    }

    public String getTransactionId() {
        return this.transactionId;
    }

    public List<RequestEntity> getRequests() {
        return this.requests;
    }

    @JsonIgnore
    public String urlEncode() {
        return "request="
                + urlEncode(SerializationUtils.serializeToString(this)).replace("+", "%20");
    }

    private static String urlEncode(final String string) {
        try {
            return URLEncoder.encode(string, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }

    public static BelfiusRequest.Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final BelfiusRequest request;

        public Builder() {
            this.request = new BelfiusRequest();
            this.request.applicationId = BelfiusConstants.Request.APPLICATION_ID;
            this.request.executionMode = BelfiusConstants.Request.AGGREGATED_EXECUTION_MODE;
            this.request.requestCounter = "1";
        }

        public Builder setApplicationId(String applicationId) {
            this.request.applicationId = applicationId;
            return this;
        }

        public Builder setExecutionMode(String executionMode) {
            this.request.executionMode = executionMode;
            return this;
        }

        public Builder setSessionId(String sessionId) {
            this.request.sessionId = sessionId;
            return this;
        }

        public Builder setRequestCounter(String requestCounter) {
            this.request.requestCounter = requestCounter;
            return this;
        }

        public Builder setRetry(Boolean retry) {
            this.request.retry = retry;
            return this;
        }

        public Builder setTransactionId(String transactionId) {
            this.request.transactionId = transactionId;
            return this;
        }

        public Builder setRequests(RequestEntity... requests) {
            this.request.requests = Lists.newArrayList(requests);
            return this;
        }

        public BelfiusRequest build() {
            return this.request;
        }
    }
}

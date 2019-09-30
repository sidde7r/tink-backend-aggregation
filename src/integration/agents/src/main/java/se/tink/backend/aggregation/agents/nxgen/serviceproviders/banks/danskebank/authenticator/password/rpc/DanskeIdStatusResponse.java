package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.authenticator.password.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class DanskeIdStatusResponse {
    @JsonProperty("EupToken")
    private Object eupToken;

    @JsonProperty("OtpRequestId")
    private String otpRequestId;

    @JsonProperty("ResponseCode")
    private Integer responseCode;

    @JsonProperty("ResponseMessage")
    private Object responseMessage;

    @JsonProperty("Status")
    private String status;

    @JsonProperty("StatusCode")
    private Integer statusCode;

    @JsonProperty("TraceId")
    private Object traceId;

    public Object getEupToken() {
        return eupToken;
    }

    public String getOtpRequestId() {
        return otpRequestId;
    }

    public Integer getResponseCode() {
        return responseCode;
    }

    public Object getResponseMessage() {
        return responseMessage;
    }

    public String getStatus() {
        return status;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public Object getTraceId() {
        return traceId;
    }
}

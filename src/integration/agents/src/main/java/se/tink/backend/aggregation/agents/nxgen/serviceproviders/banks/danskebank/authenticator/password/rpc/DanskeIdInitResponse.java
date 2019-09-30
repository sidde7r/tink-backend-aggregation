package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.authenticator.password.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class DanskeIdInitResponse {
    @JsonProperty("EupToken")
    private Object eupToken;

    @JsonProperty("OtpRequestId")
    private Integer otpRequestId;

    @JsonProperty("ResponseCode")
    private Integer responseCode;

    @JsonProperty("ResponseMessage")
    private Object responseMessage;

    @JsonProperty("StatusCode")
    private Integer statusCode;

    @JsonProperty("TraceId")
    private Object traceId;

    public Object getEupToken() {
        return eupToken;
    }

    public Integer getOtpRequestId() {
        return otpRequestId;
    }

    public Integer getResponseCode() {
        return responseCode;
    }

    public Object getResponseMessage() {
        return responseMessage;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public Object getTraceId() {
        return traceId;
    }
}

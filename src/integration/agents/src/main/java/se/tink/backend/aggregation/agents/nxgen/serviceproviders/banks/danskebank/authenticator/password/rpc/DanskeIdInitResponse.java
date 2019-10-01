package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.authenticator.password.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class DanskeIdInitResponse {
    @JsonProperty("EupToken")
    private String eupToken;

    @JsonProperty("OtpRequestId")
    private Integer otpRequestId;

    @JsonProperty("ResponseCode")
    private Integer responseCode;

    @JsonProperty("ResponseMessage")
    private String responseMessage;

    @JsonProperty("StatusCode")
    private Integer statusCode;

    @JsonProperty("TraceId")
    private String traceId;

    public String getEupToken() {
        return eupToken;
    }

    public Integer getOtpRequestId() {
        return otpRequestId;
    }

    public Integer getResponseCode() {
        return responseCode;
    }

    public String getResponseMessage() {
        return responseMessage;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public String getTraceId() {
        return traceId;
    }
}

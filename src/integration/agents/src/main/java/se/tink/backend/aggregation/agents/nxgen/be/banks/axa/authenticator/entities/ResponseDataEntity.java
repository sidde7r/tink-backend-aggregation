package se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonInclude(Include.NON_NULL)
public class ResponseDataEntity {

    private String errors;
    private String language;

    @JsonProperty("ns2:authenticationParameterMap")
    private AuthParameterMapEntity authenticationParameterMap;

    @JsonProperty("otp_format")
    private OtpFormatEntity otpFormat;

    @JsonProperty("otpLength")
    private String otpLength;

    private PropertiesEntity properties;
    private String requestId;
    private String sessionId;
    private String user;

    @JsonProperty("json_data")
    private JsonDataEntity jsonData;

    public String getErrors() {
        return errors;
    }

    public String getLanguage() {
        return language;
    }

    public AuthParameterMapEntity getAuthenticationParameterMap() {
        return authenticationParameterMap;
    }

    public OtpFormatEntity getOtpFormat() {
        return otpFormat;
    }

    public String getOtpLength() {
        return otpLength;
    }

    public PropertiesEntity getProperties() {
        return properties;
    }

    public String getRequestId() {
        return requestId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getUser() {
        return user;
    }

    public JsonDataEntity getJsonData() {
        return jsonData;
    }
}

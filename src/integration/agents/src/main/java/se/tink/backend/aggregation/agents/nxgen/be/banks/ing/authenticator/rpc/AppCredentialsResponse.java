package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AppCredentialsResponse {
    @JsonProperty("X-ENROLMENT-PROTOCOL-VERSION")
    private String xEnrolmentProtocolVersion;

    @JsonProperty("X-ENROLMENT-MESSAGE")
    private String xEnrolmentMessage;

    @JsonProperty("X-ENROLMENT-STATUS-CODE")
    private String xEnrolmentStatusCode;

    @JsonProperty("X-ENROLMENT-ENC-CREDENTIALS")
    private String xEnrolmentEncCredentials;

    public String getxEnrolmentProtocolVersion() {
        return xEnrolmentProtocolVersion;
    }

    public String getxEnrolmentMessage() {
        return xEnrolmentMessage;
    }

    public String getxEnrolmentStatusCode() {
        return xEnrolmentStatusCode;
    }

    public String getxEnrolmentEncCredentials() {
        return Preconditions.checkNotNull(xEnrolmentEncCredentials);
    }
}

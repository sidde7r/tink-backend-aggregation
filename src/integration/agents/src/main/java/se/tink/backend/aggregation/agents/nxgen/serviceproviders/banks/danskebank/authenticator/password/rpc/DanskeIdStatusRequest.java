package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.authenticator.password.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankConstants;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class DanskeIdStatusRequest {
    @JsonProperty("ExternalUserId")
    private String externalUserId;

    @JsonProperty("ExternalUserIdType")
    private String externalUserIdType;

    @JsonProperty("LastCheck")
    private String lastCheck;

    @JsonProperty("OtpRequestId")
    private Integer otpRequestId;

    public DanskeIdStatusRequest(String externalUserId, Integer otpRequestId) {
        this.externalUserId = externalUserId;
        this.externalUserIdType = DanskeBankConstants.DanskeIdFormValues.EXTERNALUSERIDTYPE;
        this.lastCheck = DanskeBankConstants.DanskeIdFormValues.LASTCHECK;
        this.otpRequestId = otpRequestId;
    }
}

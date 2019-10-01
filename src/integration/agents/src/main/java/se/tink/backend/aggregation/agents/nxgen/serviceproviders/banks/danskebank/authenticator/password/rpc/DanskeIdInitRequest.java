package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.authenticator.password.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class DanskeIdInitRequest {
    @JsonProperty("ExternalRef")
    private String externalRef;

    @JsonProperty("ExternalText")
    private String externalText;

    @JsonProperty("ExternalUserId")
    private String externalUserId;

    @JsonProperty("ExternalUserIdType")
    private String externalUserIdType;

    @JsonProperty("MessageTemplateID")
    private String messageTemplateID;

    @JsonProperty("OtpAppType")
    private String otpAppType;

    @JsonProperty("OtpRequestType")
    private String otpRequestType;

    @JsonProperty("Product")
    private String product;

    public DanskeIdInitRequest(
            String externalRef,
            String externalText,
            String externalUserId,
            String externalUserIdType,
            String messageTemplateID,
            String otpAppType,
            String otpRequestType,
            String product) {
        this.externalRef = externalRef;
        this.externalText = externalText;
        this.externalUserId = externalUserId;
        this.externalUserIdType = externalUserIdType;
        this.messageTemplateID = messageTemplateID;
        this.otpAppType = otpAppType;
        this.otpRequestType = otpRequestType;
        this.product = product;
    }
}

package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.authenticator.password.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankConstants;
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

    public DanskeIdInitRequest(String externalUserId) {
        this.externalRef = DanskeBankConstants.DanskeIdFormValues.externalRef;
        this.externalText = DanskeBankConstants.DanskeIdFormValues.externalText;
        this.externalUserId = externalUserId;
        this.externalUserIdType = DanskeBankConstants.DanskeIdFormValues.externalUserIdType;
        this.messageTemplateID = DanskeBankConstants.DanskeIdFormValues.messageTemplateID;
        this.otpAppType = DanskeBankConstants.DanskeIdFormValues.otpAppType;
        this.otpRequestType = DanskeBankConstants.DanskeIdFormValues.otpRequestType;
        this.product = DanskeBankConstants.DanskeIdFormValues.product;
    }
}

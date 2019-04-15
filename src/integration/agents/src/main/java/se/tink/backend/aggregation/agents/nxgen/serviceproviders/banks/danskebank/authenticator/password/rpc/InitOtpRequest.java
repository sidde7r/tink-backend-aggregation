package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.authenticator.password.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankConstants;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InitOtpRequest {
    @JsonProperty("UserIdType")
    private String userIdType;
    @JsonProperty("DeviceType")
    private String deviceType;
    @JsonProperty("DeviceSerialNo")
    private String deviceSerialNo;
    @JsonProperty("TransactionContext")
    private String transactionContext;
    @JsonProperty("SuppressPush")
    private String suppressPush;
    @JsonProperty("ISOLanguageCode")
    private String iSOLanguageCode;

    public InitOtpRequest(String deviceType, String deviceSerialNo) {
        this.userIdType = DanskeBankConstants.Device.USER_ID_TYPE;
        this.deviceType = deviceType;
        this.deviceSerialNo = deviceSerialNo;
        this.transactionContext = DanskeBankConstants.Device.REGISTER_TRANSACTION_TEXT;
        this.suppressPush = DanskeBankConstants.Device.SUPPRESS_PUSH;
        this.iSOLanguageCode = DanskeBankConstants.Device.LANGUAGE_CODE;
    }
}

package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.authenticator.password.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.i18n.Catalog;
import src.integration.nemid.NemIdSupportedLanguageCode;

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

    public InitOtpRequest(String deviceType, String deviceSerialNo, Catalog catalog) {
        this.userIdType = DanskeBankConstants.Device.USER_ID_TYPE;
        this.deviceType = deviceType;
        this.deviceSerialNo = deviceSerialNo;
        this.transactionContext =
                catalog.getString(DanskeBankConstants.Device.REGISTER_TRANSACTION_TEXT);
        this.suppressPush = DanskeBankConstants.Device.SUPPRESS_PUSH;
        // note: this value affects the language of nemID notification on user's device
        this.iSOLanguageCode =
                NemIdSupportedLanguageCode.getFromCatalogOrDefault(catalog).getIsoLanguageCode();
    }
}

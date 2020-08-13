package se.tink.backend.aggregation.agents.nxgen.be.banks.axa.entities;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.entities.DeviceInfoEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@Setter
@JsonObject
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class InputEntity {
    private DeviceInfoEntity deviceInfo;
    private String applCd;
    private String challenge;
    private String clientInitialVector;
    private String deviceBrand;
    private String deviceModel;
    private String encryptedClientPublicKeyAndNonce;
    private String language;
    private String panNumberFull;
    private String response;
    private String serialNo;
    private String derivationCd;
    private Integer customerId;

    @JsonProperty("UCRid")
    private String uCRid;

    private String encryptedServerNonce;

    private String accountReferenceNumber;

    private Boolean includeStandingOrders;
    private Boolean includeSavingOrders;
    private Boolean includeTransactionsInExecution;
    private Boolean includeRefusedTransfers;
    private Boolean includePendingOrders;

    private AccountHistoryParameters accountHistoryParameters;
}

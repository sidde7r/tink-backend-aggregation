package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.executors.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.api.client.repackaged.com.google.common.base.Strings;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.executors.entities.PermissionsEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.rpc.ErrorResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class BankPaymentResponse extends ErrorResponse {
    @JsonProperty private String id;
    @JsonProperty private String from;

    @JsonProperty("from_currency")
    private String fromCurrency;

    @JsonProperty private String to;

    @JsonProperty("to_account_number_type")
    private String toAccountNumberType;

    @JsonProperty("recipient_name")
    private String recipientName;

    @JsonProperty("bank_name")
    private String bankName;

    @JsonProperty private String message;

    @JsonProperty("own_message")
    private String ownMessage;

    @JsonProperty private String reference;
    @JsonProperty private double amount;
    @JsonProperty private String status;
    @JsonProperty private String type;
    @JsonProperty private String due;
    @JsonProperty private String currency;
    @JsonProperty private PermissionsEntity permissions;

    @JsonIgnore
    public String getApiIdentifier() {
        return Strings.isNullOrEmpty(id) ? reference : id;
    }
}

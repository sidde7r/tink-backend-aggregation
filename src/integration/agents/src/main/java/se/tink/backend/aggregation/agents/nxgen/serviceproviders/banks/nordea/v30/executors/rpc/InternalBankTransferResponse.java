package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.executors.rpc;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.NordeaBaseConstants.PaymentStatus;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.rpc.ErrorResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InternalBankTransferResponse extends ErrorResponse {
    @JsonProperty private String id;
    @JsonProperty private String from;

    @JsonProperty("from_account_number_type")
    private String fromAccountNumberType;

    @JsonProperty("from_currency")
    private String fromCurrency;

    @JsonProperty private String to;

    @JsonProperty("bank_name")
    private String bankName;

    @JsonProperty("own_message")
    private String message;

    @JsonProperty private double amount;
    @JsonProperty private String status;
    @JsonProperty private String type;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @JsonProperty
    private Date due;

    @JsonProperty private String currency;

    @JsonIgnore
    public boolean isTransferAccepted() {
        return status.equalsIgnoreCase(PaymentStatus.PAID)
                || status.equalsIgnoreCase(PaymentStatus.CONFIRMED);
    }
}

package se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.executors.rpc;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import java.util.Optional;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InternalBankTransferResponse {
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

    @JsonProperty("error_description")
    private String errorDescription;

    @JsonIgnore
    public boolean isTransferAccepted() {
        return status.equalsIgnoreCase("paid");
    }

    @JsonIgnore
    public Optional<String> getErrorMessage() {
        return Optional.ofNullable(errorDescription);
    }
}

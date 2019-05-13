package se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.executors.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ResultsEntity {
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
}

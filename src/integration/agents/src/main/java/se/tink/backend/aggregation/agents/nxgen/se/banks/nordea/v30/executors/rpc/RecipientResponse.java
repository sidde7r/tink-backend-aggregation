package se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.executors.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class RecipientResponse {
    @JsonProperty("beneficiary_id")
    private String beneficiaryId;

    @JsonProperty("payment_type")
    private String paymentType;

    private String name;

    @JsonProperty("account_number")
    private String accountNumber;

    @JsonProperty("account_number_type")
    private String accountNumberType;

    @JsonProperty("bank_code")
    private String bankCode;

    @JsonProperty("bank_name")
    private String bankName;
}

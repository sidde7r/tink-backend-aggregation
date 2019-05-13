package se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.executors.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class RecipientRequest {
    @JsonProperty private String name;

    @JsonProperty("bank_name")
    private String bankName;

    @JsonProperty("payment_type")
    private String paymentType;

    @JsonProperty("bank_code")
    private String bankCode;

    @JsonProperty("account_number")
    private String accountNumber;

    @JsonProperty("account_number_type")
    private String accountNumberType;

    @JsonIgnore
    public void setName(String name) {
        this.name = name;
    }

    @JsonIgnore
    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    @JsonIgnore
    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }

    @JsonIgnore
    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
    }

    @JsonIgnore
    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    @JsonIgnore
    public void setAccountNumberType(String accountNumberType) {
        this.accountNumberType = accountNumberType;
    }
}

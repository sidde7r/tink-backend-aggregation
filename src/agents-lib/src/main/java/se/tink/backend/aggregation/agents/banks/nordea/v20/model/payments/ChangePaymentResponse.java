package se.tink.backend.aggregation.agents.banks.nordea.v20.model.payments;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.banks.nordea.v15.model.BankingServiceResponse;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ChangePaymentResponse {
    @JsonProperty("changePaymentOut")
    private ChangePaymentOut changePaymentOut;

    @JsonProperty("bankingServiceResponse")
    private BankingServiceResponse bankingServiceResponse;

    public ChangePaymentOut getChangePaymentOut() {
        return changePaymentOut;
    }

    public void setChangePaymentOut(ChangePaymentOut changePaymentOut) {
        this.changePaymentOut = changePaymentOut;
    }

    public BankingServiceResponse getBankingServiceResponse() {
        return bankingServiceResponse;
    }

    public void setBankingServiceResponse(BankingServiceResponse bankingServiceResponse) {
        this.bankingServiceResponse = bankingServiceResponse;
    }
}

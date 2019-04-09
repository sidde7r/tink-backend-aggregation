package se.tink.backend.aggregation.agents.banks.lansforsakringar.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentAccountsResponse {
    private List<AccountEntity> paymentAccounts;

    public List<AccountEntity> getPaymentAccounts() {
        return paymentAccounts;
    }

    public void setPaymentAccounts(List<AccountEntity> paymentAccounts) {
        this.paymentAccounts = paymentAccounts;
    }
}

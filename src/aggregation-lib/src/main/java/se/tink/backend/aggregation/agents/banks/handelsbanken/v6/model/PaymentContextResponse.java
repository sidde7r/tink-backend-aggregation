package se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentContextResponse extends AbstractResponse {
    private AccountListResponse fromAccounts;
    private List<RecipientAccountEntity> recipients;

    public AccountListResponse getFromAccounts() {
        return fromAccounts;
    }

    public void setFromAccounts(AccountListResponse fromAccounts) {
        this.fromAccounts = fromAccounts;
    }

    public List<RecipientAccountEntity> getRecipients() {
        return recipients;
    }

    public void setRecipients(List<RecipientAccountEntity> recipients) {
        this.recipients = recipients;
    }

}

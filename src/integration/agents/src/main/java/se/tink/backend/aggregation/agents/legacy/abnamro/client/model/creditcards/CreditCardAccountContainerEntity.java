package se.tink.backend.aggregation.agents.abnamro.client.model.creditcards;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CreditCardAccountContainerEntity {

    private CreditCardAccountEntity creditCardAccount;

    public CreditCardAccountEntity getCreditCardAccount() {
        return creditCardAccount;
    }

    public void setCreditCardAccount(CreditCardAccountEntity creditCardAccount) {
        this.creditCardAccount = creditCardAccount;
    }
}

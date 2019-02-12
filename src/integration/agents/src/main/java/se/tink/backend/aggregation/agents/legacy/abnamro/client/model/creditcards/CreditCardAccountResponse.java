package se.tink.backend.aggregation.agents.abnamro.client.model.creditcards;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CreditCardAccountResponse {

    private CreditCardAccountContainerContainerEntity creditCardAccountList;

    public CreditCardAccountContainerContainerEntity getCreditCardAccountList() {
        return creditCardAccountList;
    }

    public void setCreditCardAccountList(CreditCardAccountContainerContainerEntity creditCardAccountList) {
        this.creditCardAccountList = creditCardAccountList;
    }
}

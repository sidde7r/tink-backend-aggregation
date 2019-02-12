package se.tink.backend.aggregation.agents.abnamro.client.model.creditcards;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CreditCardAccountContainerContainerEntity {

    private List<CreditCardAccountContainerEntity> creditCardAccounts;

    public List<CreditCardAccountContainerEntity> getCreditCardAccounts() {
        return creditCardAccounts;
    }

    public void setCreditCardAccounts(List<CreditCardAccountContainerEntity> creditCardAccounts) {
        this.creditCardAccounts = creditCardAccounts;
    }
}

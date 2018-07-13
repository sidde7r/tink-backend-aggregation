package se.tink.backend.aggregation.agents.creditcards.coop.v2.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SummaryResultEntity {
    @JsonProperty("Accounts")
    private List<AccountEntity> accounts;

    public List<AccountEntity> getAccounts() {
        if (accounts == null) {
            return Collections.emptyList();
        }

        return accounts;
    }

    public void setAccounts(List<AccountEntity> accounts) {
        this.accounts = accounts;
    }
}

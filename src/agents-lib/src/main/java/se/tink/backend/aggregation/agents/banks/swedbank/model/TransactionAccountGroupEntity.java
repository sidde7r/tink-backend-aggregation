package se.tink.backend.aggregation.agents.banks.swedbank.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TransactionAccountGroupEntity {
    private String name;
    private List<TransactionAccountEntity> accounts;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<TransactionAccountEntity> getAccounts() {
        return accounts;
    }

    public void setAccounts(
            List<TransactionAccountEntity> accounts) {
        this.accounts = accounts;
    }
}

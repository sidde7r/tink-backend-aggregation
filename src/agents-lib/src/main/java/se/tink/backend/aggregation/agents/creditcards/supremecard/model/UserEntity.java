package se.tink.backend.aggregation.agents.creditcards.supremecard.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UserEntity {

    private List<AccountEntity> accounts;
    private int numberAccounts;

    public List<AccountEntity> getAccounts() {
        return accounts;
    }

    public int getNumberAccounts() {
        return numberAccounts;
    }

    public void setAccounts(List<AccountEntity> accounts) {
        this.accounts = accounts;
    }

    public void setNumberAccounts(int numberAccounts) {
        this.numberAccounts = numberAccounts;
    }
}

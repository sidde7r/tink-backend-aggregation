package se.tink.backend.aggregation.agents.banks.sbab.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class UserEntity {
    String id;

    @JsonProperty("__typename")
    String typeName;

    ArrayList<SavingsAccount> savingsAccounts = new ArrayList<>();

    @JsonProperty("savingsAccount")
    AccountEntity savingsAccountsDetail;

    private LoansEntity loans = new LoansEntity();

    public List<String> getAccountsIds() {
        return savingsAccounts.stream().map(SavingsAccount::getId).collect(Collectors.toList());
    }

    public AccountEntity getSavingsAccountsDetail() {
        return savingsAccountsDetail;
    }

    public LoansEntity getLoans() {
        return loans;
    }
}

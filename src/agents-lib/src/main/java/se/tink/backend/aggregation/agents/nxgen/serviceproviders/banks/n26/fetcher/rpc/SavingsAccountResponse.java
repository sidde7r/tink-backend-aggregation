package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.fetcher.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.fetcher.entities.SavingsAccountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.core.Amount;

@JsonObject
public class SavingsAccountResponse {
    private double totalBalance;
    @JsonProperty("accounts")
    private List<SavingsAccountEntity> savingsAccountList;

    private Amount getAmount(){
        return Amount.inEUR(totalBalance);
    }

    private String getName(){
        return savingsAccountList.get(0).getName();
    }

    private String getAccountNumber(){
        return savingsAccountList.get(0).getId();
    }

    public String getUniqueIdentifier(){
        return savingsAccountList.get(0).getId();
    }

    public boolean isEmpty(){
        return savingsAccountList.isEmpty();
    }

    public TransactionalAccount toSavingsAccount() {
        return se.tink.backend.aggregation.nxgen.core.account.SavingsAccount
                .builder(getUniqueIdentifier(), getAmount())
                .setAccountNumber(getAccountNumber())
                .setName(getName())
                .setBankIdentifier(getUniqueIdentifier())
                .build();
    }
}

package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.fetcher.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.fetcher.entities.SavingsAccountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;

@JsonObject
public class SavingsAccountResponse {
    private double totalBalance;
    @JsonProperty("accounts")
    private List<SavingsAccountEntity> savingsAccountList;

    public boolean isEmpty() {
        return savingsAccountList == null || savingsAccountList.isEmpty();
    }

    public List<TransactionalAccount> toSavingsAccounts() {
        return savingsAccountList.stream()
                .filter(savingsAccountEntity -> savingsAccountEntity.isValid())
                .map(SavingsAccountEntity::toSavingsAccount)
                .collect(Collectors.toList());
    }
}

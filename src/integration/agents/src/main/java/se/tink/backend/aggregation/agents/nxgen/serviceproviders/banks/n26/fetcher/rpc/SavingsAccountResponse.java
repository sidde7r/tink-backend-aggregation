package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.fetcher.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.fetcher.entities.SavingsAccountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

@JsonObject
public class SavingsAccountResponse {
    private double totalBalance;

    @JsonProperty("accounts")
    private List<SavingsAccountEntity> savingsAccountList;

    private boolean isEmpty() {
        return savingsAccountList == null || savingsAccountList.isEmpty();
    }

    public List<TransactionalAccount> toSavingsAccounts() {
        return isEmpty()
                ? Collections.emptyList()
                : savingsAccountList.stream()
                        .filter(SavingsAccountEntity::isValid)
                        .map(SavingsAccountEntity::toSavingsAccount)
                        .collect(Collectors.toList());
    }
}

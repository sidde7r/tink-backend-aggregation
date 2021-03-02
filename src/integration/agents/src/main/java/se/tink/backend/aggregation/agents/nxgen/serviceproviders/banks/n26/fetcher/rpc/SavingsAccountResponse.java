package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.fetcher.rpc;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.fetcher.entities.SavingsAccountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

@JsonObject
public class SavingsAccountResponse {
    private double totalBalance;

    private List<SavingsAccountEntity> accounts;

    private boolean isEmpty() {
        return accounts == null || accounts.isEmpty();
    }

    public List<TransactionalAccount> toSavingsAccounts() {
        return isEmpty()
                ? Collections.emptyList()
                : accounts.stream()
                        .filter(SavingsAccountEntity::isValid)
                        .map(SavingsAccountEntity::toSavingsAccount)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toList());
    }
}

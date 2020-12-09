package se.tink.backend.aggregation.agents.nxgen.se.banks.volvofinans.fetcher.transactionalaccounts.rpc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.se.banks.volvofinans.fetcher.transactionalaccounts.entities.SavingsAccountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

@JsonObject
public class SavingsAccountsResponse extends ArrayList<SavingsAccountEntity> {

    public Collection<TransactionalAccount> getTinkAccounts() {
        return stream()
                // only fetch accounts where user is account holder/main applicant (HUVUDSOKANDE)
                .filter(SavingsAccountEntity::isAccountHolder)
                .map(SavingsAccountEntity::toTinkAccount)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }
}

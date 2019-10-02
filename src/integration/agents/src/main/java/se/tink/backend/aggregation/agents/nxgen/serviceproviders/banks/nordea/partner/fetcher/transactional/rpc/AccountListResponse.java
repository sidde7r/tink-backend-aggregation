package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.fetcher.transactional.rpc;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.fetcher.transactional.entity.AccountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

@JsonObject
public class AccountListResponse {
    private List<AccountEntity> result;

    public Collection<TransactionalAccount> toTinkTransactionalAccounts() {
        return Optional.ofNullable(result).orElse(Collections.emptyList()).stream()
                .filter(AccountEntity::isTransactionalAccount)
                .map(AccountEntity::toTinkAccount)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .peek(System.out::println)
                .collect(Collectors.toList());
    }
}

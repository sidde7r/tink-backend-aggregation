package se.tink.backend.aggregation.agents.nxgen.at.openbanking.erstebank.authenticator.rpc;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.erstebank.authenticator.entities.ErsteAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.entities.BerlinGroupAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.rpc.BerlinGroupAccountResponse;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

@JsonObject
public class ErsteAccountsResponse implements BerlinGroupAccountResponse {

    protected List<ErsteAccountEntity> accounts;

    public ErsteAccountsResponse() {}

    public ErsteAccountsResponse(List<ErsteAccountEntity> accounts) {
        this.accounts = accounts;
    }

    @Override
    public Collection<TransactionalAccount> toTinkAccounts() {
        return Optional.ofNullable(accounts).orElse(Collections.emptyList()).stream()
                .map(BerlinGroupAccountEntity::toTinkAccount)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    @Override
    public List<ErsteAccountEntity> getAccounts() {
        return accounts;
    }
}

package se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.rpc;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.entity.AccountEntityResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.rpc.AccountsBaseResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class AccountsResponse extends AccountsBaseResponse {
    protected List<AccountEntityResponse> accounts;

    public AccountsResponse() {}

    public AccountsResponse(final List<AccountEntityResponse> accounts) {
        this.accounts = accounts;
    }

    public List<AccountEntityResponse> getAccountEntities() {
        return accounts;
    }

    public Collection<TransactionalAccount> toTinkAccounts() {
        return Optional.ofNullable(accounts).orElse(Collections.emptyList()).stream()
                .map(AccountEntityResponse::toTinkAccount)
                .collect(Collectors.toList());
    }
}

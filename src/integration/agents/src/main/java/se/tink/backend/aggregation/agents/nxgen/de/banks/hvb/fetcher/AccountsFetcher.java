package se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.fetcher;

import static java.util.Collections.emptyList;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.HVBStorage;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.scaffold.ExternalApiCallResult;
import se.tink.backend.aggregation.nxgen.scaffold.SimpleExternalApiCall;

public final class AccountsFetcher implements AccountFetcher<TransactionalAccount> {

    private final HVBStorage storage;
    private final AccountsCall accountsCall;
    private final AccountsMapper mapper;

    public AccountsFetcher(HVBStorage storage, AccountsCall accountsCall, AccountsMapper mapper) {
        this.storage = storage;
        this.accountsCall = accountsCall;
        this.mapper = mapper;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        AccountsResponse accountsResponse =
                executeCall(accountsCall, storage.getDirectBankingNumber());
        return getTransactionalAccounts(accountsResponse);
    }

    private List<TransactionalAccount> getTransactionalAccounts(AccountsResponse accountsResponse) {
        return Optional.ofNullable(accountsResponse)
                .map(mapper::toTransactionalAccounts)
                .orElse(emptyList());
    }

    private <T, R> R executeCall(SimpleExternalApiCall<T, R> call, T arg) {
        return Optional.ofNullable(call.execute(arg))
                .filter(ExternalApiCallResult::isSuccess)
                .map(ExternalApiCallResult::getResult)
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        "There was an error while executing call"));
    }
}

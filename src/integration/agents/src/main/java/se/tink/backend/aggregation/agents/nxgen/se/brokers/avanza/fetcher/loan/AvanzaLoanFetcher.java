package se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.fetcher.loan;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.AvanzaApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.AvanzaAuthSessionStorage;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.AvanzaConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.storage.TemporaryStorage;

public class AvanzaLoanFetcher implements AccountFetcher<LoanAccount> {

    private final AvanzaApiClient apiClient;
    private final AvanzaAuthSessionStorage authSessionStorage;
    private final TemporaryStorage temporaryStorage;

    public AvanzaLoanFetcher(
            AvanzaApiClient apiClient,
            AvanzaAuthSessionStorage authSessionStorage,
            TemporaryStorage temporaryStorage) {
        this.apiClient = apiClient;
        this.authSessionStorage = authSessionStorage;
        this.temporaryStorage = temporaryStorage;
    }

    @Override
    public Collection<LoanAccount> fetchAccounts() {
        String holderName = temporaryStorage.getOrDefault(StorageKeys.HOLDER_NAME, null);

        return authSessionStorage.keySet().stream()
                .flatMap(getAccounts(holderName))
                .collect(Collectors.toList());
    }

    private Function<String, Stream<? extends LoanAccount>> getAccounts(String holderName) {
        return authSession ->
                apiClient.fetchAccounts(authSession).getAccounts().stream()
                        .filter(AccountEntity::isLoanAccount)
                        .map(AccountEntity::getAccountId)
                        .map(accId -> apiClient.fetchAccountDetails(accId, authSession))
                        .map(account -> account.toLoanAccount(holderName))
                        .filter(Optional::isPresent)
                        .map(Optional::get);
    }
}

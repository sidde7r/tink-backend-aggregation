package se.tink.backend.aggregation.agents.nxgen.de.openbanking.consorsbank.fetcher;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.consorsbank.ConsorsbankStorage;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.consorsbank.client.ConsorsbankFetcherApiClient;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.AccessEntity;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.AccountReferenceEntity;
import se.tink.backend.aggregation.agents.utils.berlingroup.fetcher.entities.AccountEntity;
import se.tink.backend.aggregation.agents.utils.berlingroup.fetcher.mappers.AccountMapper;
import se.tink.backend.aggregation.agents.utils.berlingroup.fetcher.rpc.FetchBalancesResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

@RequiredArgsConstructor
public class ConsorsbankAccountFetcher implements AccountFetcher<TransactionalAccount> {

    private final ConsorsbankFetcherApiClient apiClient;
    private final ConsorsbankStorage storage;
    private final AccountMapper consorsbankAccountMapper;

    public Collection<TransactionalAccount> fetchAccounts() {
        AccessEntity consentAccess = storage.getConsentAccess();

        boolean canFetchAllWithBalances =
                consentAccess.getAccounts().size() == consentAccess.getBalances().size();

        Stream<AccountEntity> accountStream =
                apiClient.fetchAccounts(storage.getConsentId(), canFetchAllWithBalances)
                        .getAccounts().stream();

        if (!canFetchAllWithBalances) {
            accountStream = accountStream.map(this::tryEnrichingWithBalance);
        }

        return accountStream
                .map(consorsbankAccountMapper::toTinkAccount)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private AccountEntity tryEnrichingWithBalance(AccountEntity accountEntity) {
        if (canFetchBalancesForAccount(accountEntity)) {
            FetchBalancesResponse fetchBalancesResponse =
                    apiClient.fetchBalances(
                            storage.getConsentId(),
                            accountEntity.getLinks().getBalances().getHref());

            accountEntity.setBalances(fetchBalancesResponse.getBalances());
        }
        return accountEntity;
    }

    private boolean canFetchBalancesForAccount(AccountEntity accountEntity) {
        return storage.getConsentAccess().getBalances().stream()
                .map(AccountReferenceEntity::getIban)
                .filter(Objects::nonNull)
                .anyMatch(x -> x.equals(accountEntity.getIban()));
    }
}

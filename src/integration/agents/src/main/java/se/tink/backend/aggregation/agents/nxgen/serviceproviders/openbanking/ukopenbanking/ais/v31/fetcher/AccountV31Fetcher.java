package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.fetcher;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.UkOpenBankingApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.AccountBalanceEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.AccountOwnershipType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.PartyV31Entity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.interfaces.PartyFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.mapper.AccountMapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.mapper.AccountTypeMapper;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.instrumentation.FetcherInstrumentationRegistry;

@Slf4j
@RequiredArgsConstructor
public final class AccountV31Fetcher<T extends Account> implements AccountFetcher<T> {

    private final UkOpenBankingApiClient apiClient;
    private final PartyFetcher partyFetcher;
    private final AccountMapper<T> accountMapper;
    private final FetcherInstrumentationRegistry instrumentation;

    @Override
    public Collection<T> fetchAccounts() {
        List<AccountEntity> allAccountEntities = apiClient.fetchV31Accounts();
        instrument(allAccountEntities);
        log.info(
                "Available accounts: {}",
                allAccountEntities.stream()
                        .map(
                                accountEntity ->
                                        "type: "
                                                + accountEntity.getRawAccountType()
                                                + " and subtype: "
                                                + accountEntity.getRawAccountSubType())
                        .collect(Collectors.toList()));
        return Observable.fromIterable(allAccountEntities)
                .filter(AccountEntity::hasAccountId)
                .filter(
                        acc ->
                                accountMapper.supportsAccountType(
                                        AccountTypeMapper.getAccountType(acc)))
                .flatMapSingle(
                        account ->
                                Single.zip(
                                        fetchParties(account),
                                        fetchBalance(account),
                                        (parties, balances) -> zipper(account, balances, parties)))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList()
                .blockingGet();
    }

    private void instrument(List<AccountEntity> allAccountEntities) {
        Set<AccountTypes> distinctTypes =
                allAccountEntities.stream()
                        .map(AccountTypeMapper::getAccountType)
                        .collect(Collectors.toSet());

        for (AccountTypes type : distinctTypes) {
            long personalAccountsSeenByType =
                    allAccountEntities.stream()
                            .filter(acc -> AccountTypeMapper.getAccountType(acc) == type)
                            .filter(
                                    acc ->
                                            AccountTypeMapper.getAccountOwnershipType(acc)
                                                    == AccountOwnershipType.PERSONAL)
                            .count();
            long businessAccountsSeenByType =
                    allAccountEntities.stream()
                            .filter(acc -> AccountTypeMapper.getAccountType(acc) == type)
                            .filter(
                                    acc ->
                                            AccountTypeMapper.getAccountOwnershipType(acc)
                                                    == AccountOwnershipType.BUSINESS)
                            .count();

            instrumentation.personal(type, (int) personalAccountsSeenByType);
            instrumentation.business(type, (int) businessAccountsSeenByType);
        }
    }

    private Single<List<PartyV31Entity>> fetchParties(AccountEntity account) {
        return Single.fromCallable(() -> partyFetcher.fetchAccountParties(account))
                .subscribeOn(Schedulers.io());
    }

    private Single<List<AccountBalanceEntity>> fetchBalance(AccountEntity account) {
        return Single.fromCallable(() -> apiClient.fetchV31AccountBalances(account.getAccountId()))
                .subscribeOn(Schedulers.io());
    }

    private Optional<T> zipper(
            AccountEntity account,
            List<AccountBalanceEntity> balances,
            List<PartyV31Entity> parties) {
        if (balances.isEmpty()) {
            log.warn(
                    "[AccountV31Fetcher]: Something went wrong during balance "
                            + "fetching and balance list is empty so it could not "
                            + "be mapped to a transactional account "
                            + "- skipping account.");
            return Optional.empty();
        }
        return accountMapper.map(account, balances, parties);
    }
}

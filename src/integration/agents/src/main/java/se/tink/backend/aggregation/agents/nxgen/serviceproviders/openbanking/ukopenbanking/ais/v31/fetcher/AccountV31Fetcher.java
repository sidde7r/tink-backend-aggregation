package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.fetcher;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.UkOpenBankingApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.AccountBalanceEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.AccountOwnershipType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.IdentityDataV31Entity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.mapper.AccountMapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.mapper.AccountTypeMapper;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.instrumentation.FetcherInstrumentationRegistry;

@RequiredArgsConstructor
public final class AccountV31Fetcher<T extends Account> implements AccountFetcher<T> {

    private final UkOpenBankingApiClient apiClient;
    private final PartyDataFetcher accountPartyFetcher;
    private final AccountTypeMapper accountTypeMapper;
    private final AccountMapper<T> accountMapper;
    private final FetcherInstrumentationRegistry instrumentation;

    @Override
    public Collection<T> fetchAccounts() {
        List<AccountEntity> allAccountEntities = apiClient.fetchV31Accounts();
        instrument(allAccountEntities);

        return Observable.fromIterable(allAccountEntities)
                .filter(accountTypeMapper::supportsAccountOwnershipType)
                .filter(
                        acc ->
                                accountMapper.supportsAccountType(
                                        accountTypeMapper.getAccountType(acc)))
                .flatMapSingle(
                        account ->
                                Single.zip(
                                        fetchParties(account),
                                        fetchBalance(account),
                                        (parties, balances) ->
                                                accountMapper.map(account, balances, parties)))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList()
                .blockingGet();
    }

    private void instrument(List<AccountEntity> allAccountEntities) {
        Set<AccountTypes> distinctTypes =
                allAccountEntities.stream()
                        .map(acc -> accountTypeMapper.getAccountType(acc))
                        .distinct()
                        .collect(Collectors.toSet());

        for (AccountTypes type : distinctTypes) {
            long personalAccountsSeenByType =
                    allAccountEntities.stream()
                            .filter(acc -> accountTypeMapper.getAccountType(acc) == type)
                            .filter(
                                    acc ->
                                            accountTypeMapper.getAccountOwnershipType(acc)
                                                    == AccountOwnershipType.PERSONAL)
                            .count();
            long businessAccountsSeenByType =
                    allAccountEntities.stream()
                            .filter(acc -> accountTypeMapper.getAccountType(acc) == type)
                            .filter(
                                    acc ->
                                            accountTypeMapper.getAccountOwnershipType(acc)
                                                    == AccountOwnershipType.BUSINESS)
                            .count();

            instrumentation.personal(type, (int) personalAccountsSeenByType);
            instrumentation.business(type, (int) businessAccountsSeenByType);
        }
    }

    private Single<List<IdentityDataV31Entity>> fetchParties(AccountEntity account) {
        return Single.just(Collections.emptyList());
        // Todo temporarily disabling identity fetching - since it triggers SCA, it breaks BG
        // refresh. It will be reenabled after auth rework.
        //        return Single.fromCallable(() -> accountPartyFetcher.fetchAccountParties(account))
        //                .subscribeOn(Schedulers.io());
    }

    private Single<List<AccountBalanceEntity>> fetchBalance(AccountEntity account) {
        return Single.fromCallable(() -> apiClient.fetchV31AccountBalances(account.getAccountId()))
                .subscribeOn(Schedulers.io());
    }
}

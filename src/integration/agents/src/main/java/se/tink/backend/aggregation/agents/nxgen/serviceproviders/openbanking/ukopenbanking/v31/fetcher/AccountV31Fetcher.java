package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.fetcher;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.UkOpenBankingApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.entities.AccountBalanceEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.entities.IdentityDataV31Entity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.mapper.AccountMapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.mapper.AccountTypeMapper;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.Account;

@RequiredArgsConstructor
public final class AccountV31Fetcher<T extends Account> implements AccountFetcher<T> {

    private final UkOpenBankingApiClient apiClient;
    private final PartyDataFetcher accountPartyFetcher;
    private final AccountTypeMapper accountTypeMapper;
    private final AccountMapper<T> accountMapper;

    @Override
    public Collection<T> fetchAccounts() {
        return Observable.fromIterable(apiClient.fetchV31Accounts())
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

package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.fetcher;

import com.google.common.collect.ImmutableMap;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.functions.BiFunction;
import io.reactivex.rxjava3.schedulers.Schedulers;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.UkOpenBankingApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.entities.AccountBalanceEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.entities.IdentityDataV31Entity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.mapper.transactionalaccounts.TransactionalAccountMapper;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;

@RequiredArgsConstructor
public class TransactionalAccountV31Fetcher implements AccountFetcher<TransactionalAccount> {

    private final UkOpenBankingApiClient apiClient;
    private final TransactionalAccountMapper accountMapper;
    private final PartyDataV31Fetcher identityFetcher;

    private static final Map<String, TransactionalAccountType> HANDLED_ACCOUNT_TYPES =
            ImmutableMap.of(
                    "CurrentAccount", TransactionalAccountType.CHECKING,
                    "Savings", TransactionalAccountType.SAVINGS);

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        return Observable.fromIterable(apiClient.fetchV31Accounts())
                .filter(acc -> HANDLED_ACCOUNT_TYPES.containsKey(acc.getRawAccountSubType()))
                .flatMapSingle(
                        account ->
                                Single.zip(
                                        fetchParties(account),
                                        fetchBalance(account),
                                        combineFetchedData(account)))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList()
                .blockingGet();
    }

    private BiFunction<
                    List<IdentityDataV31Entity>,
                    List<AccountBalanceEntity>,
                    Optional<TransactionalAccount>>
            combineFetchedData(AccountEntity account) {
        return (parties, balances) ->
                accountMapper.map(
                        account,
                        HANDLED_ACCOUNT_TYPES.get(account.getRawAccountSubType()),
                        balances,
                        parties);
    }

    private Single<List<IdentityDataV31Entity>> fetchParties(AccountEntity account) {
        return Single.fromCallable(
                        () -> identityFetcher.fetchAccountParties(account.getBankIdentifier()))
                .subscribeOn(Schedulers.io());
    }

    private Single<List<AccountBalanceEntity>> fetchBalance(AccountEntity account) {
        return Single.fromCallable(
                        () -> apiClient.fetchV31AccountBalances(account.getBankIdentifier()))
                .subscribeOn(Schedulers.io());
    }
}

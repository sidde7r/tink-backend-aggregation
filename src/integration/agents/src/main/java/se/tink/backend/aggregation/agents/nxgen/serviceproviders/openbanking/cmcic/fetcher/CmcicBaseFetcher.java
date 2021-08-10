package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.apiclient.CmcicApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.converter.CmcicAccountBaseConverter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.dto.AccountResourceDto;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public abstract class CmcicBaseFetcher<T extends Account>
        implements AccountFetcher<T>, TransactionKeyPaginator<T, URL> {

    protected final CmcicApiClient cmcicApiClient;
    protected final CmcicAccountBaseConverter<T> converter;

    protected CmcicBaseFetcher(
            CmcicApiClient cmcicApiClient, CmcicAccountBaseConverter<T> converter) {
        this.cmcicApiClient = cmcicApiClient;
        this.converter = converter;
    }

    public abstract boolean predicate(AccountResourceDto account);

    @Override
    public Collection<T> fetchAccounts() {
        return cmcicApiClient.fetchAccounts().getAccounts().stream()
                .filter(this::predicate)
                .map(converter::convertToAccount)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    @Override
    public TransactionKeyPaginatorResponse<URL> getTransactionsFor(T account, URL key) {
        return cmcicApiClient.fetchTransactions(account, key);
    }
}

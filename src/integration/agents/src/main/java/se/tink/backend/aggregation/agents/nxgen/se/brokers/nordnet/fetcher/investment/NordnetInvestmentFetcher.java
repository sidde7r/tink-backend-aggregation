package se.tink.backend.aggregation.agents.nxgen.se.brokers.nordnet.fetcher.investment;

import com.google.api.client.http.HttpStatusCodes;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.nordnet.NordnetApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.nordnet.NordnetConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.nordnet.fetcher.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.nordnet.fetcher.rpc.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.nordnet.fetcher.rpc.entities.AccountInfoEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.instrument.InstrumentModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.portfolio.PortfolioModule;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class NordnetInvestmentFetcher
        implements AccountFetcher<InvestmentAccount>, TransactionDatePaginator<InvestmentAccount> {

    private final NordnetApiClient apiClient;
    private final SessionStorage sessionStorage;

    public NordnetInvestmentFetcher(NordnetApiClient apiClient, SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public Collection<InvestmentAccount> fetchAccounts() {
        AccountsResponse accounts =
                sessionStorage
                        .get(StorageKeys.ACCOUNTS, AccountsResponse.class)
                        .orElseGet(apiClient::fetchAccounts);

        return accounts.stream()
                .filter(AccountEntity::isInvestmentAccount)
                .map(this::toTinkInvestmentAccount)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private Optional<InvestmentAccount> toTinkInvestmentAccount(AccountEntity account) {

        AccountInfoEntity accountInfoEntity = fetchAccountInfo(account.getAccountId());
        List<InstrumentModule> instruments = getInstruments(account.getAccountId());
        PortfolioModule portfolio = account.toPortfolioModule(accountInfoEntity, instruments);

        return account.toInvestmentAccount(accountInfoEntity, portfolio);
    }

    private List<InstrumentModule> getInstruments(String accountId) {
        try {
            return apiClient.getPositions(accountId).toInstruments();
        } catch (HttpResponseException e) {
            if (e.getResponse().getStatus() != HttpStatusCodes.STATUS_CODE_NO_CONTENT) {
                // re-throw unknown exception
                throw new IllegalStateException("Error when fetching positions");
            }
        }
        return Collections.emptyList();
    }

    private AccountInfoEntity fetchAccountInfo(String accid) {
        return apiClient.fetchAccountInfo(accid).stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Could not fetch account info"));
    }

    @Override
    public PaginatorResponse getTransactionsFor(
            InvestmentAccount account, Date fromDate, Date toDate) {
        return PaginatorResponseImpl.createEmpty();
    }
}

package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.fetcher.transactional;

import java.time.LocalDate;
import java.util.Collection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.NordeaPartnerApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.fetcher.mapper.NordeaPartnerAccountMapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.fetcher.transactional.model.NordeaPartnerTransactionalPaginatorResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.fetcher.transactional.rpc.AccountTransactionsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

@RequiredArgsConstructor
@Slf4j
public class NordeaPartnerTransactionalAccountFetcher
        implements AccountFetcher<TransactionalAccount>,
                TransactionKeyPaginator<TransactionalAccount, String> {

    private final NordeaPartnerApiClient apiClient;
    private final NordeaPartnerAccountMapper accountMapper;
    private final LocalDate dateLimit;

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        return apiClient.fetchAccounts().toTinkTransactionalAccounts(accountMapper);
    }

    @Override
    public TransactionKeyPaginatorResponse<String> getTransactionsFor(
            TransactionalAccount account, String key) {
        try {
            final AccountTransactionsResponse response =
                    apiClient.fetchAccountTransaction(account.getApiIdentifier(), key);

            return new NordeaPartnerTransactionalPaginatorResponse(
                    response.getTinkTransactions(),
                    response.getContinuationKey(),
                    response.canFetchMore(dateLimit));
        } catch (BankServiceException e) {
            // don't raise non-actionable alert for frequent 502 and 503 errors
            log.warn("Ignoring bank service error when fetching transactions: " + e.getMessage());
            return NordeaPartnerTransactionalPaginatorResponse.createEmpty();
        }
    }
}

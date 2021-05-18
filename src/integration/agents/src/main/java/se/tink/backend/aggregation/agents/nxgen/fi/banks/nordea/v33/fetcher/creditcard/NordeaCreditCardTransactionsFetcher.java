package se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.fetcher.creditcard;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.util.Strings;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.NordeaFIApiClient;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.NordeaFIConstants;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginator;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

@RequiredArgsConstructor
@Slf4j
public class NordeaCreditCardTransactionsFetcher
        implements TransactionPagePaginator<CreditCardAccount> {
    private final NordeaFIApiClient apiClient;

    @Override
    public PaginatorResponse getTransactionsFor(CreditCardAccount account, int page) {
        String accountId = account.getApiIdentifier();

        if (Strings.isNullOrEmpty(accountId)) {
            return PaginatorResponseImpl.createEmpty(false);
        }

        try {
            return apiClient.fetchCardTransactions(page, accountId);
        } catch (HttpResponseException e) {
            log.error(NordeaFIConstants.LogTags.CREDIT_TRANSACTIONS_ERROR.toString(), e);
            return PaginatorResponseImpl.createEmpty(false);
        }
    }
}

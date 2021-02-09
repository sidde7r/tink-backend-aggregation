package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.fetcher.creditcard;

import java.time.LocalDate;
import java.util.Collection;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.NordeaPartnerApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.fetcher.creditcard.model.NordeaPartnerCreditCardPaginatorResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.fetcher.creditcard.rpc.CardTransactionListResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginator;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;

@RequiredArgsConstructor
public class NordeaPartnerCreditCardAccountFetcher
        implements AccountFetcher<CreditCardAccount>, TransactionPagePaginator<CreditCardAccount> {

    private static final int PAGE_SIZE = 50;
    private final NordeaPartnerApiClient apiClient;
    private final LocalDate dateLimit;

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        return apiClient.fetchCreditCards().toTinkCreditCardAccounts();
    }

    @Override
    public PaginatorResponse getTransactionsFor(CreditCardAccount account, int page) {
        final CardTransactionListResponse response =
                apiClient.fetchCreditCardTransactions(account.getApiIdentifier(), page, PAGE_SIZE);
        return new NordeaPartnerCreditCardPaginatorResponse(
                response.getTinkTransactions(), response.canFetchMore(dateLimit));
    }
}

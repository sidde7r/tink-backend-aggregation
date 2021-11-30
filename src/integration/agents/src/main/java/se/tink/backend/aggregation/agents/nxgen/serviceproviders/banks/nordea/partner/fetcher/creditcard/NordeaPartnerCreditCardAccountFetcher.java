package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.fetcher.creditcard;

import java.util.Collection;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.NordeaPartnerApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.NordeaPartnerMarketUtil;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.fetcher.creditcard.model.NordeaPartnerCreditCardPaginatorResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.fetcher.creditcard.rpc.CardTransactionListResponse;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginator;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.libraries.credentials.service.CredentialsRequest;

@RequiredArgsConstructor
public class NordeaPartnerCreditCardAccountFetcher
        implements AccountFetcher<CreditCardAccount>, TransactionPagePaginator<CreditCardAccount> {

    private static final int PAGE_SIZE = 50;
    private final NordeaPartnerApiClient apiClient;
    private final AgentComponentProvider componentProvider;
    private final CredentialsRequest request;

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        return apiClient.fetchCreditCards().toTinkCreditCardAccounts();
    }

    @Override
    public PaginatorResponse getTransactionsFor(CreditCardAccount account, int page) {
        final CardTransactionListResponse response =
                apiClient.fetchCreditCardTransactions(account.getApiIdentifier(), page, PAGE_SIZE);
        return new NordeaPartnerCreditCardPaginatorResponse(
                response.getTinkTransactions(apiClient.getMarket()),
                response.canFetchMore(
                        NordeaPartnerMarketUtil.getPaginationStartDate(
                                account, request, componentProvider)));
    }
}

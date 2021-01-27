package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.creditcard;

import java.util.Collection;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.apiclient.CmcicApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.converter.CmcicCreditCardConverter;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class CmcicCreditCardFetcher
        implements AccountFetcher<CreditCardAccount>,
                TransactionKeyPaginator<CreditCardAccount, URL> {

    private final CmcicApiClient cmcicApiClient;
    private final CmcicCreditCardConverter cmcicCreditCardConverter;

    public CmcicCreditCardFetcher(
            CmcicApiClient cmcicApiClient, CmcicCreditCardConverter cmcicCreditCardConverter) {
        this.cmcicApiClient = cmcicApiClient;
        this.cmcicCreditCardConverter = cmcicCreditCardConverter;
    }

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        return cmcicApiClient.fetchAccounts().getAccounts().stream()
                .filter(cmcicCreditCardConverter::isCreditCard)
                .map(cmcicCreditCardConverter::convertToCreditCard)
                .collect(Collectors.toList());
    }

    @Override
    public TransactionKeyPaginatorResponse<URL> getTransactionsFor(
            CreditCardAccount account, URL key) {
        return cmcicApiClient.fetchTransactions(account, key);
    }
}

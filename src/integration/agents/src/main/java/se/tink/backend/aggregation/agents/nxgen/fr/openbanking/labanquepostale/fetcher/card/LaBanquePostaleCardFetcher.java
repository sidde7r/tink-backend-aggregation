package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.fetcher.card;

import com.google.common.base.Strings;
import java.util.Collection;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.LaBanquePostaleApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.LaBanquePostaleConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.fetcher.transactionalaccount.converter.LaBanquePostaleCreditCardConverter;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;

public class LaBanquePostaleCardFetcher
        implements AccountFetcher<CreditCardAccount>,
                TransactionKeyPaginator<CreditCardAccount, String> {

    private final LaBanquePostaleApiClient laBanquePostaleApiClient;

    public LaBanquePostaleCardFetcher(LaBanquePostaleApiClient laBanquePostaleApiClient) {
        this.laBanquePostaleApiClient = laBanquePostaleApiClient;
    }

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        return laBanquePostaleApiClient.fetchAccounts().getAccounts().stream()
                .filter(LaBanquePostaleCreditCardConverter::isCreditCard)
                .map(LaBanquePostaleCreditCardConverter::toTinkCreditCard)
                .collect(Collectors.toList());
    }

    @Override
    public TransactionKeyPaginatorResponse<String> getTransactionsFor(
            CreditCardAccount account, String key) {
        String url =
                Strings.isNullOrEmpty(key)
                        ? String.format(Urls.FETCH_TRANSACTIONS, account.getApiIdentifier())
                        : key;
        return laBanquePostaleApiClient.fetchTransactionsLaBanquePostal(url);
    }
}

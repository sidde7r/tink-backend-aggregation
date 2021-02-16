package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.fetcher.card;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.apiclient.SocieteGeneraleApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.fetcher.transactionalaccount.entities.AccountsItemEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.fetcher.transactionalaccount.rpc.AccountsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@RequiredArgsConstructor
@Slf4j
public class SocieteGeneraleCreditCardFetcher
        implements AccountFetcher<CreditCardAccount>,
                TransactionKeyPaginator<CreditCardAccount, URL> {

    private final SocieteGeneraleApiClient societeGeneraleApiClient;

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        return Optional.ofNullable(societeGeneraleApiClient.fetchAccounts())
                .map(AccountsResponse::getAccounts).orElseGet(Collections::emptyList).stream()
                .filter(AccountsItemEntity::isCreditCard)
                .map(AccountsItemEntity::toTinkCreditCard)
                .collect(Collectors.toList());
    }

    @Override
    public TransactionKeyPaginatorResponse<URL> getTransactionsFor(
            CreditCardAccount account, URL key) {
        return societeGeneraleApiClient.getTransactions(account.getApiIdentifier(), key);
    }

    private CreditCardAccount toTinkCreditCard(AccountsItemEntity entity) {
        // temporary call to discover if balances are available for credit card
        log.debug(
                "Credit card balance response: "
                        + societeGeneraleApiClient.getCreditCardBalances(
                                URL.of(entity.getLinks().getBalancesEntity().getHref())));
        return entity.toTinkCreditCard();
    }
}

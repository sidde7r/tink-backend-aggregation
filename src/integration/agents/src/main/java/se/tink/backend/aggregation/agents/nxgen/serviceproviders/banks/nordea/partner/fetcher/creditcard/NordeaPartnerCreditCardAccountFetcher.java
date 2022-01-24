package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.fetcher.creditcard;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.NordeaPartnerApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.entity.CardEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.fetcher.creditcard.entity.CardTransaction;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.fetcher.creditcard.model.NordeaPartnerCreditCardPaginatorResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.fetcher.transactional.rpc.AccountListResponse;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginator;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.credentials.service.CredentialsRequest;

@RequiredArgsConstructor
public class NordeaPartnerCreditCardAccountFetcher
        implements AccountFetcher<CreditCardAccount>, TransactionPagePaginator<CreditCardAccount> {

    private static final int PAGE_SIZE = 50;
    private final NordeaPartnerApiClient apiClient;
    private final LocalDateTimeSource dateTimeSource;
    private final CredentialsRequest request;

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        return apiClient.getAllData().toTinkCreditCardAccounts();
    }

    @Override
    public PaginatorResponse getTransactionsFor(CreditCardAccount account, int page) {
        AccountListResponse accounts = apiClient.getAllData();
        List<CardTransaction> transactions =
                accounts.getCards().stream()
                        .filter(a -> a.getCardId().equalsIgnoreCase(account.getApiIdentifier()))
                        .map(CardEntity::getTransactions)
                        .findFirst()
                        .orElse(Collections.emptyList());
        return new NordeaPartnerCreditCardPaginatorResponse(
                getTinkTransactions(transactions, apiClient.getMarket()), Optional.of(false));
    }

    @JsonIgnore
    public Collection<Transaction> getTinkTransactions(
            List<CardTransaction> transactions, String market) {
        return Optional.ofNullable(transactions).orElse(Collections.emptyList()).stream()
                .map(cardTransaction -> cardTransaction.toTinkTransaction(market))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }
}

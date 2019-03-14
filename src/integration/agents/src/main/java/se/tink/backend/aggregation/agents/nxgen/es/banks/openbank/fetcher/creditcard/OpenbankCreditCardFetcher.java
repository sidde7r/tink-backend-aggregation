package se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.fetcher.creditcard;

import java.util.Collection;
import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.OpenbankApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.OpenbankConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.fetcher.creditcard.entities.CardTransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.fetcher.creditcard.rpc.CardTransactionsRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.fetcher.entities.CardEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

public class OpenbankCreditCardFetcher
        implements AccountFetcher<CreditCardAccount>, TransactionDatePaginator<CreditCardAccount> {
    private final OpenbankApiClient apiClient;

    public OpenbankCreditCardFetcher(OpenbankApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        return apiClient
                .fetchCards()
                .filter(CardEntity::isCreditCardAccount)
                .map(CardEntity::toTinkAccount)
                .toJavaList();
    }

    @Override
    public PaginatorResponse getTransactionsFor(
            CreditCardAccount account, Date fromDate, Date toDate) {
        final CardTransactionsRequest request = new CardTransactionsRequest();
        request.setCardNumber(
                account.getFromTemporaryStorage(OpenbankConstants.Storage.CARD_NUMBER));
        request.setProductCode(
                account.getFromTemporaryStorage(OpenbankConstants.Storage.PRODUCT_CODE_NEW));
        request.setContractNumber(
                account.getFromTemporaryStorage(OpenbankConstants.Storage.CONTRACT_NUMBER_NEW));
        request.setFromDate(fromDate);
        request.setToDate(toDate);

        final Collection<? extends Transaction> transactions =
                apiClient
                        .fetchCardTransactions(request)
                        .getCardTransactions()
                        .map(CardTransactionEntity::toTinkTransaction)
                        .toJavaList();

        return PaginatorResponseImpl.create(transactions);
    }
}

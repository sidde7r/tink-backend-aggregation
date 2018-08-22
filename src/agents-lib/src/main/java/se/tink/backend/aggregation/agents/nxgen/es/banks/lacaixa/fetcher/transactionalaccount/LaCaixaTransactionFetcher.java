package se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.transactionalaccount;

import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.LaCaixaApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.LaCaixaConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.transactionalaccount.entities.TransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.transactionalaccount.rpc.AccountTransactionResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.transactionalaccount.rpc.TransactionDetailsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginator;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;

public class LaCaixaTransactionFetcher implements TransactionPagePaginator<TransactionalAccount> {

    private final LaCaixaApiClient bankClient;

    public LaCaixaTransactionFetcher(LaCaixaApiClient bankClient) {
        this.bankClient = bankClient;
    }

    @Override
    public PaginatorResponse getTransactionsFor(TransactionalAccount account, int page) {

        String accountReference = account.getTemporaryStorage(LaCaixaConstants.TemporaryStorage.ACCOUNT_REFERENCE,
                String.class);

        AccountTransactionResponse response = bankClient.fetchNextAccountTransactions(accountReference,page == 0);

        // Update descriptions, if available.
        response.getTransactions().forEach(transaction -> {
            Optional<String> transferMessage = getTransferMessage(accountReference, transaction);
            transferMessage.ifPresent(message -> transaction.setDescription(
                    String.format("%s: %s", transaction.getDescription(), message)));
        });

        return response;
    }

    private Optional<String> getTransferMessage(String accountReference, TransactionEntity transaction) {
        if (!LaCaixaConstants.TransactionDescriptions.TRANSFER.equalsIgnoreCase(transaction.getDescription())) {
            return Optional.empty();
        }

        TransactionDetailsResponse details = bankClient.fetchTransactionDetails(accountReference, transaction);
        return details.getDetailedDescription(LaCaixaConstants.TransactionDetailsInfoKeys.TRANSFER_MESSAGE);
    }
}

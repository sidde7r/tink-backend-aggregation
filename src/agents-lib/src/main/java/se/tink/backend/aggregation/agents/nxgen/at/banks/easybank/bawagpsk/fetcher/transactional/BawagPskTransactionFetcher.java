package se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.fetcher.transactional;

import java.time.ZoneId;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;
import javax.xml.bind.JAXBException;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.BawagPskApiClient;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.BawagPskConstants;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.entities.ProductID;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.rpc.GetAccountStatementItemsRequest;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.rpc.GetAccountStatementItemsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

public class BawagPskTransactionFetcher implements TransactionDatePaginator<TransactionalAccount> {

    private final BawagPskApiClient apiClient;

    public BawagPskTransactionFetcher(BawagPskApiClient bawagPskApiClient) {
        this.apiClient = bawagPskApiClient;
    }

    @Override
    public PaginatorResponse getTransactionsFor(
            final TransactionalAccount account,
            final Date fromDate,
            final Date toDate) {

        final ProductID productID = apiClient.getLoginResponse()
                .orElseThrow(() -> new IllegalStateException("Login response not found."))
                .getProductId(account.getAccountNumber());

        final String serverSessionId = apiClient.getFromStorage(
                BawagPskConstants.Storage.SERVER_SESSION_ID.name())
                .orElseThrow(IllegalStateException::new);
        final String qid = apiClient.getFromStorage(
                BawagPskConstants.Storage.QID.name())
                .orElseThrow(IllegalStateException::new);

        final GetAccountStatementItemsRequest request = new GetAccountStatementItemsRequest(
                serverSessionId,
                qid,
                productID,
                fromDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime(),
                toDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
        );

        final String requestString;
        try {
            requestString = request.getXml();
        } catch (JAXBException e) {
            throw new IllegalStateException("Unable to marshal JAXB ", e);
        }
        final GetAccountStatementItemsResponse response = apiClient
                .getGetAccountStatementItemsResponse(requestString);

        // Get transactions, filter zero amounts since they are not shown in the app
        final Collection<? extends Transaction> transactions = response.getTransactions().stream()
                .filter(transaction -> transaction.getAmount().isPositive())
                .collect(Collectors.toSet());

        return PaginatorResponseImpl.create(transactions);
    }
}

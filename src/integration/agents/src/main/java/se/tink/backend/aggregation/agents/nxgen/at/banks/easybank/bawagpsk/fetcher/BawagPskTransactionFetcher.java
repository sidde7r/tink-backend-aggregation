package se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.fetcher;

import java.time.ZoneId;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.BawagPskApiClient;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.BawagPskConstants;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.BawagPskUtils;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.entities.ProductID;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.entities.Products;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.rpc.GetAccountStatementItemsRequest;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.rpc.GetAccountStatementItemsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

public final class BawagPskTransactionFetcher<AcccountT extends Account>
        implements TransactionDatePaginator<AcccountT> {

    private final BawagPskApiClient apiClient;

    public BawagPskTransactionFetcher(BawagPskApiClient bawagPskApiClient) {
        this.apiClient = bawagPskApiClient;
    }

    @Override
    public PaginatorResponse getTransactionsFor(
            final AcccountT account, final Date fromDate, final Date toDate) {

        final String errorMsg =
                "Could not find products from session storage necessary for fetching transactions";
        final Products products =
                BawagPskUtils.xmlToEntity(
                        apiClient
                                .getFromSessionStorage(BawagPskConstants.Storage.PRODUCTS.name())
                                .orElseThrow(() -> new IllegalStateException(errorMsg)),
                        Products.class);

        final ProductID productID =
                products.getProductIDByAccountNumber(account.getAccountNumber())
                        .orElseThrow(IllegalStateException::new);
        final String sessionID =
                apiClient
                        .getFromSessionStorage(BawagPskConstants.Storage.SERVER_SESSION_ID.name())
                        .orElseThrow(IllegalStateException::new);
        final String qid =
                apiClient
                        .getFromSessionStorage(BawagPskConstants.Storage.QID.name())
                        .orElseThrow(IllegalStateException::new);

        final GetAccountStatementItemsRequest request =
                new GetAccountStatementItemsRequest(
                        sessionID,
                        qid,
                        productID,
                        fromDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime(),
                        toDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());

        final String requestString;
        requestString = request.getXml();
        final GetAccountStatementItemsResponse response =
                apiClient.getGetAccountStatementItemsResponse(requestString);

        // Get transactions, filter zero amounts since they are not shown in the app
        final Collection<? extends Transaction> transactions =
                response.getTransactions().stream()
                        .filter(transaction -> !transaction.getAmount().isZero())
                        .collect(Collectors.toSet());

        return PaginatorResponseImpl.create(transactions);
    }
}

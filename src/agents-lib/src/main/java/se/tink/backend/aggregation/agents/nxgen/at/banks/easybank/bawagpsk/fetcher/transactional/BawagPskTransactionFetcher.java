package se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.fetcher.transactional;

import java.time.ZoneId;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;
import javax.xml.bind.JAXBException;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.BawagPskApiClient;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.BawagPskConstants;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.entities.AccountStatementItem;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.entities.ProductID;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.rpc.GetAccountStatementItemsRequest;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.rpc.GetAccountStatementItemsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.core.Amount;

public class BawagPskTransactionFetcher implements TransactionDatePaginator<TransactionalAccount> {

    private final BawagPskApiClient bawagPskApiClient;

    public BawagPskTransactionFetcher(BawagPskApiClient bawagPskApiClient) {
        this.bawagPskApiClient = bawagPskApiClient;
    }

    /**
     * In the bank's nomenclature, the "ValueDate" is the date of requesting the money to be transferred, whereas the
     * "BookingDate" is the date when the money hits the account. That is, ValueDate <= BookingDate.
     */
    private Date dateOfRequestingTheTransfer(AccountStatementItem transactionItem) {
        return Date.from(transactionItem.getValueDate().atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    @Override
    public Collection<? extends Transaction> getTransactionsFor(
            final TransactionalAccount account,
            final Date fromDate,
            final Date toDate) {

        final ProductID productID = bawagPskApiClient.getLoginResponse()
                .orElseThrow(() -> new IllegalStateException("Login response not found."))
                .getProductId(account.getAccountNumber());

        final String serverSessionId = bawagPskApiClient.getFromStorage(
                BawagPskConstants.Storage.SERVER_SESSION_ID.name())
                .orElseThrow(IllegalStateException::new);
        final String qid = bawagPskApiClient.getFromStorage(
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
        final GetAccountStatementItemsResponse response = bawagPskApiClient
                .getGetAccountStatementItemsResponse(requestString);

        return response.getAccountStatementItemList().stream()
                .map(statement -> Transaction.builder()
                        .setAmount(new Amount(statement.getAmountEntity().getCurrency(),
                                statement.getAmountEntity().getAmount()))
                        .setDate(dateOfRequestingTheTransfer(statement))
                        .setDescription(String.join(" ", statement.getTextLines()))
                        .build()
                ).collect(Collectors.toSet());
    }
}

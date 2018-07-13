package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.SdcApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.SdcSessionStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.authenticator.entities.SessionStorageAgreement;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.authenticator.entities.SessionStorageAgreements;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.rpc.SearchTransactionsRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.rpc.SearchTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.parser.SdcTransactionParser;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

public class SdcTransactionFetcher extends SdcAgreementFetcher implements TransactionDatePaginator<TransactionalAccount> {
    private static final int ONE_WEEK_AGO_IN_DAYS = -7;

    private final SdcTransactionParser transactionParser;

    public SdcTransactionFetcher(SdcApiClient bankClient, SdcSessionStorage sessionStorage,
            SdcTransactionParser transactionParser) {
        super(bankClient, sessionStorage);

        this.transactionParser = transactionParser;
    }

    @Override
    public Collection<Transaction> getTransactionsFor(TransactionalAccount account, Date fromDate, Date toDate) {
        SessionStorageAgreements agreements = getAgreements();
        SessionStorageAgreement agreement = agreements.findAgreementForAccountBankId(account.getBankIdentifier());

        selectAgreement(agreement, agreements);

        SearchTransactionsRequest searchTransactionsRequest = new SearchTransactionsRequest()
                .setAccountId(account.getAccountNumber())
                .setAgreementId(agreement.getAgreementId())
                .setIncludeReservations(shouldIncludeReservations(toDate))
                .setTransactionsFrom(formatDate(fromDate))
                .setTransactionsTo(formatDate(toDate));

        SearchTransactionsResponse response = bankClient.searchTransactions(searchTransactionsRequest);

        return response.getTinkTransactions(transactionParser);
    }

    // only fetch reservations when asking for most current transactions
    private boolean shouldIncludeReservations(Date toDate) {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DATE, ONE_WEEK_AGO_IN_DAYS);
        return toDate.after(c.getTime());
    }

    private String formatDate(Date aDate) {
        LocalDate date = new java.sql.Date(aDate.getTime()).toLocalDate();
        return date.format(DateTimeFormatter.ISO_DATE);
    }
}

package se.tink.backend.aggregation.agents.nxgen.at.banks.bankaustria.fetcher;

import java.util.Collection;
import java.util.Date;
import java.util.Objects;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.at.banks.bankaustria.BankAustriaApiClient;
import se.tink.backend.aggregation.agents.nxgen.at.banks.bankaustria.entities.RtaMessage;
import se.tink.backend.aggregation.agents.nxgen.at.banks.bankaustria.otml.OtmlResponseConverter;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

public class BankAustriaTransactionalAccountFetcher implements
        AccountFetcher<TransactionalAccount>,
        TransactionDatePaginator<TransactionalAccount> {

    private BankAustriaApiClient apiClient;
    private OtmlResponseConverter otmlResponseConverter;

    public BankAustriaTransactionalAccountFetcher(BankAustriaApiClient apiClient,
            OtmlResponseConverter otmlResponseConverter) {
        this.apiClient = apiClient;
        this.otmlResponseConverter = otmlResponseConverter;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        Collection<TransactionalAccount> accountsFromSettings = otmlResponseConverter
                .getAccountsFromSettings(apiClient.getAccountsFromSettings().getDataSources());

        // If we did not get any accounts we might have been shown a pop up screen instead,
        // retry loading the settings and accepting RTA message
        if (accountsFromSettings.isEmpty()) {
            otmlResponseConverter.anyRtaMessageToAccept(apiClient.getAccountsFromSettings().getDataSources())
                    .ifPresent(rtaMessage -> acceptRtaMessage(rtaMessage));

            accountsFromSettings = otmlResponseConverter
                    .getAccountsFromSettings(apiClient.getAccountsFromSettings().getDataSources());
        }

        return accountsFromSettings
                .stream()
                .map(account -> otmlResponseConverter
                        .fillAccountInformation(
                                apiClient.getAccountInformationFromAccountMovement(
                                        account).getDataSources(), account))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private void acceptRtaMessage(RtaMessage rtaMessage) {
        apiClient.acceptRtaMessage(rtaMessage.getRtaMessageID());
    }

    @Override
    public PaginatorResponse getTransactionsFor(TransactionalAccount account, Date fromDate, Date toDate) {
        Collection<? extends Transaction> transactions = otmlResponseConverter.getTransactions(
                apiClient.getTransactionsForDatePeriod(account, fromDate, toDate).getDataSources());

        return PaginatorResponseImpl.create(transactions);
    }
}

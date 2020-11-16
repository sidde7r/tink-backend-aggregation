package se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno.fetcher.account;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno.SdcNoApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.authenticator.entities.SdcAgreement;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.rpc.SearchTransactionsRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.rpc.SearchTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.parser.SdcTransactionParser;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.pair.Pair;

@Slf4j
@RequiredArgsConstructor
public class SdcNoTransactionFetcher implements TransactionFetcher<TransactionalAccount> {

    private static final int NUMBER_OF_MONTHS_AGO = -12;

    private final SdcNoApiClient bankClient;
    private final SdcTransactionParser transactionParser;
    private final Supplier<Date> dateSupplier;

    @Override
    public List<AggregationTransaction> fetchTransactionsFor(TransactionalAccount account) {
        getAccountIdData(account);
        SdcAgreement agreement = fetchSdcAgreement();
        SearchTransactionsRequest searchTransactionsRequest =
                prepareTransactionRequest(account, agreement);

        try {
            return fetchAndMapTransactions(searchTransactionsRequest);
        } catch (HttpResponseException e) {
            if (e.getResponse().getStatus() == 403) {
                log.warn("Customer does not have an access to checking / savings accounts.", e);
                return Collections.emptyList();
            } else {
                throw e;
            }
        }
    }

    private void getAccountIdData(TransactionalAccount account) {
        String webpage = bankClient.initWebPage();
        Set<Pair<String, String>> accountIdData = new AccountIdPairs(webpage).extractAll();
        accountIdData.stream()
                .filter(pair -> pair.first.equals(account.getAccountNumber()))
                .findFirst()
                .ifPresent(pair -> bankClient.postAccountNoToBank(pair.second, pair.first));
    }

    private SdcAgreement fetchSdcAgreement() {
        return bankClient.fetchAgreement();
    }

    private SearchTransactionsRequest prepareTransactionRequest(
            TransactionalAccount account, SdcAgreement agreement) {
        Date toDate = dateSupplier.get();
        Date fromDate = DateUtils.addMonths(toDate, NUMBER_OF_MONTHS_AGO);

        return new SearchTransactionsRequest()
                .setAccountId(account.getAccountNumber())
                .setAgreementId(agreement.getEntityKey().getAgreementNumber())
                .setIncludeReservations(true)
                .setTransactionsFrom(formatDate(fromDate))
                .setTransactionsTo(formatDate(toDate));
    }

    private List<AggregationTransaction> fetchAndMapTransactions(
            SearchTransactionsRequest searchTransactionsRequest) {
        SearchTransactionsResponse searchTransactionsResponse =
                bankClient.filterTransactionsFor(searchTransactionsRequest);
        Collection<Transaction> transactions =
                searchTransactionsResponse.getTinkTransactions(transactionParser);
        return new ArrayList<>(transactions);
    }

    private String formatDate(Date aDate) {
        LocalDate date = new java.sql.Date(aDate.getTime()).toLocalDate();
        return date.format(DateTimeFormatter.ISO_DATE);
    }
}

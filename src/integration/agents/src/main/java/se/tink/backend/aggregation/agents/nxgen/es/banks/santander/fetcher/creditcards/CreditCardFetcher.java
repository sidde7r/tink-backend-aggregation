package se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.creditcards;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.SantanderEsApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.SantanderEsConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.SantanderEsSessionStorage;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.creditcards.entities.CardEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.creditcards.entities.CreditCardRepositionEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.creditcards.rpc.CreditCardDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.creditcards.rpc.CreditCardTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.entities.SoapFaultErrorEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.rpc.LoginResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.utils.SantanderEsXmlUtils;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.CreditCardTransaction;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class CreditCardFetcher
        implements AccountFetcher<CreditCardAccount>, TransactionDatePaginator<CreditCardAccount> {
    private static final AggregationLogger log = new AggregationLogger(CreditCardFetcher.class);

    private final SantanderEsApiClient apiClient;
    private final SantanderEsSessionStorage santanderEsSessionStorage;

    public CreditCardFetcher(
            SantanderEsApiClient apiClient, SantanderEsSessionStorage santanderEsSessionStorage) {
        this.apiClient = apiClient;
        this.santanderEsSessionStorage = santanderEsSessionStorage;
    }

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        LoginResponse loginResponse = santanderEsSessionStorage.getLoginResponse();
        String userDataXml = SantanderEsXmlUtils.parseJsonToXmlString(loginResponse.getUserData());

        return Optional.ofNullable(loginResponse.getCards()).orElseGet(Collections::emptyList)
                .stream()
                .filter(
                        cardEntity ->
                                !SantanderEsConstants.AccountTypes.DEBIT_CARD_TYPE.equalsIgnoreCase(
                                        cardEntity.getCardType()))
                .map(
                        card -> {
                            CreditCardDetailsResponse detailsResponse =
                                    apiClient.fetchCreditCardDetails(
                                            userDataXml, card.getCardNumber());

                            return detailsResponse.toTinkCreditCard(userDataXml, card);
                        })
                .collect(Collectors.toList());
    }

    @Override
    public PaginatorResponse getTransactionsFor(
            CreditCardAccount account, Date fromDate, Date toDate) {
        LocalDate startDate = DateUtils.toJavaTimeLocalDate(fromDate);
        LocalDate endDate = DateUtils.toJavaTimeLocalDate(toDate);

        String userDataXml =
                account.getFromTemporaryStorage(SantanderEsConstants.Storage.USER_DATA_XML);
        CardEntity card =
                account.getFromTemporaryStorage(
                                SantanderEsConstants.Storage.CARD_ENTITY, CardEntity.class)
                        .orElseThrow(() -> new IllegalStateException("No card entity found"));

        return PaginatorResponseImpl.create(
                fetchAllTransactionsBetweenDates(userDataXml, card, startDate, endDate));
    }

    private List<CreditCardTransaction> fetchAllTransactionsBetweenDates(
            String userDataXml, CardEntity card, LocalDate startDate, LocalDate endDate) {
        boolean fetchMore = true;
        CreditCardRepositionEntity repositionEntity = null;
        List<CreditCardTransaction> transactions = new ArrayList<>();

        try {
            while (fetchMore) {
                CreditCardTransactionsResponse creditCardTransactionsResponse =
                        apiClient.fetchCreditCardTransactions(
                                userDataXml, card, startDate, endDate, repositionEntity);

                transactions.addAll(creditCardTransactionsResponse.getTinkTransactions());
                fetchMore = creditCardTransactionsResponse.canFetchMore().orElse(false);
                repositionEntity = creditCardTransactionsResponse.getReposition();
            }
        } catch (HttpResponseException hre) {
            // santander returns 500 internal error when no more transactions
            HttpResponse response = hre.getResponse();
            if (response.getStatus() == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
                String soapErrorMessage = response.getBody(String.class);
                Optional<SoapFaultErrorEntity> soapFaultError =
                        SoapFaultErrorEntity.parseFaultErrorFromSoapError(soapErrorMessage);
                if (soapFaultError.isPresent()
                        && soapFaultError
                                .get()
                                .matchesErrorMessage(
                                        SantanderEsConstants.SoapErrorMessages
                                                .NO_MORE_TRANSACTIONS)) {
                    // OK, this is a no more transactions for this query
                    return transactions;
                }
                // log error if we get other error than 'no more transactions' since this is string
                // matching
                if (soapFaultError.isPresent()) {
                    log.warn(
                            "ERROR: " + SerializationUtils.serializeToString(soapFaultError.get()));
                } else {
                    log.warn("Fetch transactions returned error");
                }
            }
            throw hre;
        }

        return transactions;
    }
}

package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.transactionalaccount;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.time.DateUtils;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.AmericanExpressConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.AmericanExpressUtils;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.AmexApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.dto.ErrorResponseDto;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.dto.TransactionDto;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.dto.TransactionResponseFormatted;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.dto.TransactionsResponseDto;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.transactionalaccount.storage.HmacAccountIdStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.transactionalaccount.storage.HmacAccountIds;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.authentication.HmacToken;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.TemporaryStorage;

@RequiredArgsConstructor
public class AmexCreditCardTransactionFetcher
        implements TransactionKeyPaginator<CreditCardAccount, String> {

    private final AmexApiClient amexApiClient;
    private final HmacAccountIdStorage hmacAccountIdStorage;
    private final TemporaryStorage temporaryStorage;
    private final ObjectMapper objectMapper;

    @Override
    public TransactionKeyPaginatorResponse<String> getTransactionsFor(
            CreditCardAccount account, String key) {

        try {
            final Map<Integer, Date> mapStatements =
                    getMapStatementEndDate(
                            account.getFromTemporaryStorage(
                                    AmericanExpressConstants.Storage.STATEMENTS));

            final HmacToken hmacToken = getHmacTokenForAccountId(account.getAccountNumber());

            List<TransactionsResponseDto> response;

            // Initially fetch pending transactions and follow by posted transactions
            if (key == null) {

                final Date now = new Date();
                response =
                        amexApiClient.fetchTransactions(
                                hmacToken, DateUtils.addDays(now, -30), now);

                return mapTransactionsToAccountAndReturnNewResponse(
                        account, response, getNextKey(mapStatements, null));

            } else {
                Date nextEndDate = getStatementEndDate(key);

                if (!getStoredTransactions(key).isEmpty()) {
                    return mapTransactionsToAccountAndReturnNewResponse(
                            account, getStoredTransactions(key), nextEndDate);
                } else {
                    response = amexApiClient.fetchTransactions(hmacToken, null, nextEndDate);
                    return mapTransactionsToAccountAndReturnNewResponse(
                            account, response, getNextKey(mapStatements, nextEndDate));
                }
            }
        } catch (HttpResponseException e) {
            ErrorResponseDto errorResponse = e.getResponse().getBody(ErrorResponseDto.class);
            if (errorResponse.getCode() == AmericanExpressConstants.ErrorCodes.DATE_OUT_OF_RANGE
                    && AmericanExpressConstants.ErrorMessages.DATE_OUT_OF_RANGE.equalsIgnoreCase(
                            errorResponse.getMessage())) {
                return new TransactionKeyPaginatorResponseImpl<>();
            }
            throw BankServiceError.BANK_SIDE_FAILURE.exception();
        }
    }

    private Map<Integer, LocalDate> getMapStatementEndDate(String fromTemporaryStorage) {
        objectMapper.registerModule(new JavaTimeModule());
        try {
            return new ObjectMapper()
                    .readValue(fromTemporaryStorage, new TypeReference<Map<Integer, Date>>() {});
        } catch (IOException e) {
            throw new IllegalStateException("Unable to parse json string to map", e);
        }
    }

    private Date getStatementEndDate(String key) {
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
        try {
            return dateFormatter.parse(key);
        } catch (ParseException e) {
            throw new IllegalStateException(String.format("Unable to parse %s", key), e);
        }
    }

    private Date getNextKey(Map<Integer, Date> map, Date nextEndDate) {
        if (nextEndDate == null) {
            return map.get(0);
        }
        Integer currentKey = getKeysByValue(map, nextEndDate);
        return map.get(currentKey + 1);
    }

    private static Integer getKeysByValue(Map<Integer, Date> statementMap, Date value) {
        Optional<Map<Integer, Date>> map = Optional.ofNullable(statementMap);
        if (!map.isPresent()) {
            throw new IllegalStateException("Statement map is empty.");
        }
        return map.get().entrySet().stream()
                .filter(entry -> entry.getValue().equals(value))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElseThrow(IllegalStateException::new);
    }

    /* When fetching transactions, a merged transaction-list containing transactions for both the main and sub cards will be returned.
     * This function will map each transaction to the given account and return a formatted TransactionResponse with the mapped transactions.
     */
    private TransactionResponseFormatted mapTransactionsToAccountAndReturnNewResponse(
            Account account, List<TransactionsResponseDto> transactionsResponse, Date endDate) {
        List<TransactionDto> transactions =
                transactionsResponse.stream()
                        .flatMap(a -> a.getTransactions().stream())
                        .filter(
                                t ->
                                        t.getDisplayAccountNumber()
                                                .contains(account.getAccountNumber()))
                        .collect(Collectors.toList());

        return new TransactionResponseFormatted(transactions, endDate);
    }

    /* Get the stored transactions from sessionStorage or return an empty list.
     */
    private List<TransactionsResponseDto> getStoredTransactions(String key) {
        List storedTransactions =
                temporaryStorage
                        .get(AmericanExpressUtils.createAndGetStorageString(key), List.class)
                        .orElse(Collections.emptyList());
        if (storedTransactions.isEmpty()) {
            return Collections.emptyList();
        } else {
            return objectMapper.convertValue(
                    storedTransactions, new TypeReference<List<TransactionsResponseDto>>() {});
        }
    }

    private HmacToken getHmacTokenForAccountId(String accountId) {
        final HmacAccountIds hmacAccountIds =
                hmacAccountIdStorage
                        .get()
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "No HmacAccountId found in the storage."));

        return Optional.ofNullable(hmacAccountIds.getAccountIdToHmacToken().get(accountId))
                .orElseThrow(
                        () -> new IllegalArgumentException("Hmac token not found for accountId."));
    }
}

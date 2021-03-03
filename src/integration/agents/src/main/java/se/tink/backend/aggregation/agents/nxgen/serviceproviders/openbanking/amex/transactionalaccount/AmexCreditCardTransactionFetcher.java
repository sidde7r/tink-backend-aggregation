package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.transactionalaccount;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.common.base.Preconditions;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.AmericanExpressConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.AmericanExpressUtils;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.AmexApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.dto.ErrorResponseDto;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.dto.TransactionDto;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.dto.TransactionResponseFormatted;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.dto.TransactionsResponseDto;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.transactionalaccount.storage.HmacAccountIdStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.transactionalaccount.storage.HmacAccountIds;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
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
    private final LocalDateTimeSource localDateTimeSource;
    private final String providerMarket;

    @Override
    public TransactionKeyPaginatorResponse<String> getTransactionsFor(
            CreditCardAccount account, String key) {

        try {
            final Map<Integer, LocalDate> statementMap =
                    getMapStatementEndDate(
                            account.getFromTemporaryStorage(
                                    AmericanExpressConstants.StorageKey.STATEMENTS));

            final HmacToken hmacToken = getHmacTokenForAccountId(account.getAccountNumber());

            List<TransactionsResponseDto> response;

            // Initially fetch pending transactions and follow by posted transactions
            if (key == null) {

                final LocalDate now = localDateTimeSource.now().toLocalDate();
                response =
                        amexApiClient.fetchTransactions(
                                hmacToken,
                                Optional.of(
                                        now.minusDays(
                                                AmericanExpressConstants.DAYS_TO_FETCH_PENDING)),
                                now);

                return mapTransactionsToAccountAndReturnNewResponse(
                        account, response, getNextKey(statementMap, null));

            } else {
                LocalDate endDate = LocalDate.parse(key, DateTimeFormatter.ISO_LOCAL_DATE);

                if (!getStoredTransactions(key).isEmpty()) {
                    return mapTransactionsToAccountAndReturnNewResponse(
                            account, getStoredTransactions(key), getNextKey(statementMap, endDate));
                } else {
                    response =
                            amexApiClient.fetchTransactions(hmacToken, Optional.empty(), endDate);
                    return mapTransactionsToAccountAndReturnNewResponse(
                            account, response, getNextKey(statementMap, endDate));
                }
            }
        } catch (HttpResponseException e) {
            ErrorResponseDto errorResponse = e.getResponse().getBody(ErrorResponseDto.class);
            throw new IllegalStateException(errorResponse.getMessage(), e);
        }
    }

    private Map<Integer, LocalDate> getMapStatementEndDate(String fromTemporaryStorage) {
        objectMapper.registerModule(new JavaTimeModule());
        try {
            return objectMapper.readValue(
                    fromTemporaryStorage, new TypeReference<Map<Integer, LocalDate>>() {});
        } catch (IOException e) {
            throw new IllegalStateException("Unable to parse json string to map", e);
        }
    }

    private LocalDate getNextKey(Map<Integer, LocalDate> map, LocalDate currentEndDate) {
        // The latest statement always index = 0
        if (currentEndDate == null) {
            return map.get(0);
        }
        Integer currentKey = getKeysByValue(map, currentEndDate);
        return map.get(currentKey + 1);
    }

    private static Integer getKeysByValue(Map<Integer, LocalDate> statementMap, LocalDate value) {
        Preconditions.checkNotNull(statementMap);

        return statementMap.entrySet().stream()
                .filter(entry -> entry.getValue().equals(value))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElseThrow(IllegalStateException::new);
    }

    /* When fetching transactions, a merged transaction-list containing transactions for both the main and sub cards will be returned.
     * This function will map each transaction to the given account and return a formatted TransactionResponse with the mapped transactions.
     */
    private TransactionResponseFormatted mapTransactionsToAccountAndReturnNewResponse(
            Account account,
            List<TransactionsResponseDto> transactionsResponse,
            LocalDate endDate) {
        List<TransactionDto> transactions =
                transactionsResponse.stream()
                        .flatMap(a -> a.getTransactions().stream())
                        .filter(
                                t ->
                                        t.getDisplayAccountNumber()
                                                .contains(account.getAccountNumber()))
                        .collect(Collectors.toList());

        return new TransactionResponseFormatted(transactions, endDate, providerMarket);
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

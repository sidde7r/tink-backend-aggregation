package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.transactionalaccount;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
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
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.authentication.HmacToken;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.TemporaryStorage;

@RequiredArgsConstructor
public class AmexCreditCardTransactionFetcher
        implements TransactionDatePaginator<CreditCardAccount> {

    private final AmexApiClient amexApiClient;
    private final HmacAccountIdStorage hmacAccountIdStorage;
    private final TemporaryStorage temporaryStorage;
    private final ObjectMapper objectMapper;

    @Override
    public PaginatorResponse getTransactionsFor(
            CreditCardAccount account, Date fromDate, Date toDate) {

        if (!getStoredTransactions(fromDate, toDate).isEmpty()) {
            return mapTransactionsToAccountAndReturnNewResponse(
                    account, getStoredTransactions(fromDate, toDate));
        }
        try {
            final HmacToken hmacToken = getHmacTokenForAccountId(account.getAccountNumber());
            return mapTransactionsToAccountAndReturnNewResponse(
                    account, amexApiClient.fetchTransactions(hmacToken, fromDate, toDate));
        } catch (HttpResponseException e) {
            ErrorResponseDto errorResponse = e.getResponse().getBody(ErrorResponseDto.class);
            if (errorResponse.getCode() == AmericanExpressConstants.ErrorCodes.DATE_OUT_OF_RANGE
                    && errorResponse
                            .getMessage()
                            .equalsIgnoreCase(
                                    AmericanExpressConstants.ErrorMessages.DATE_OUT_OF_RANGE)) {
                return PaginatorResponseImpl.createEmpty(false);
            }
            throw BankServiceError.BANK_SIDE_FAILURE.exception();
        }
    }

    /* When fetching transactions, a merged transaction-list containing transactions for both the main and sub cards will be returned.
     * This function will map each transaction to the given account and return a formatted TransactionResponse with the mapped transactions.
     */
    private TransactionResponseFormatted mapTransactionsToAccountAndReturnNewResponse(
            Account account, List<TransactionsResponseDto> paginatorResponse) {
        List<TransactionDto> transactions =
                paginatorResponse.stream()
                        .flatMap(a -> a.getTransactions().stream())
                        .filter(
                                t ->
                                        t.getDisplayAccountNumber()
                                                .contains(account.getAccountNumber()))
                        .collect(Collectors.toList());

        return new TransactionResponseFormatted(transactions);
    }

    /* Get the stored transactions from sessionStorage or return an empty list.
     */
    private List<TransactionsResponseDto> getStoredTransactions(Date fromDate, Date toDate) {
        List storedTransactions =
                temporaryStorage
                        .get(
                                AmericanExpressUtils.createAndGetStorageString(fromDate, toDate),
                                List.class)
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

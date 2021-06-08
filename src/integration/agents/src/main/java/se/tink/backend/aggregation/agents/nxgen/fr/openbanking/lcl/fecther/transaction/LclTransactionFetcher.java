package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.fecther.transaction;

import static java.util.stream.Collectors.collectingAndThen;

import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.apiclient.LclApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.apiclient.dto.transaction.TransactionResourceDto;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.apiclient.dto.transaction.TransactionStatus;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.apiclient.dto.transaction.TransactionsResponseDto;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.fecther.converter.LclDataConverter;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginator;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;

@RequiredArgsConstructor
public class LclTransactionFetcher implements TransactionPagePaginator<TransactionalAccount> {

    private final LclApiClient apiClient;

    @Override
    public PaginatorResponse getTransactionsFor(TransactionalAccount account, int page) {
        final TransactionsResponseDto transactionsResponseDto =
                getTransactionsResponse(account, page);
        final boolean canFetchMore = canFetchMoreTransactions(transactionsResponseDto);

        return transactionsResponseDto.getTransactions().stream()
                .map(this::mapTransaction)
                .collect(
                        collectingAndThen(
                                Collectors.toList(),
                                transactions ->
                                        PaginatorResponseImpl.create(transactions, canFetchMore)));
    }

    private TransactionsResponseDto getTransactionsResponse(
            TransactionalAccount account, int page) {
        final String resourceId = account.getApiIdentifier();

        return apiClient.getTransactionsResponse(resourceId, page);
    }

    private Transaction mapTransaction(TransactionResourceDto transaction) {
        return Transaction.builder()
                .setAmount(getTransactionAmount(transaction))
                .setDescription(
                        StringUtils.join(
                                transaction.getRemittanceInformation().getUnstructured(), ';'))
                .setDate(transaction.getBookingDate())
                .setPending(transaction.getStatus() != TransactionStatus.BOOK)
                .setRawDetails(transaction.getEntryReference())
                .build();
    }

    private ExactCurrencyAmount getTransactionAmount(TransactionResourceDto transaction) {
        return LclDataConverter.convertAmountDtoToExactCurrencyAmount(
                transaction.getTransactionAmount());
    }

    private static boolean canFetchMoreTransactions(
            TransactionsResponseDto transactionsResponseDto) {
        return Objects.nonNull(transactionsResponseDto.getLinks())
                && Objects.nonNull(transactionsResponseDto.getLinks().getNext());
    }
}

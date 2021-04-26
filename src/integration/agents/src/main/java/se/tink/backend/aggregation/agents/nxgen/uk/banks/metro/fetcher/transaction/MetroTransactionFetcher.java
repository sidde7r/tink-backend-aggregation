package se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.fetcher.transaction;

import io.vavr.control.Either;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.fetcher.AccountConstants;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.fetcher.common.model.CategoryEntity;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.fetcher.transaction.rpc.TransactionRequest;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.fetcher.transaction.rpc.TransactionResponse;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.AgentBankApiError;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;

@Slf4j
public class MetroTransactionFetcher implements TransactionFetcher<TransactionalAccount> {

    private final TransactionClient transactionClient;

    private final TransactionMapper mapper;

    public MetroTransactionFetcher(TransactionClient transactionClient, TransactionMapper mapper) {
        this.transactionClient = transactionClient;
        this.mapper = mapper;
    }

    @Override
    public List<AggregationTransaction> fetchTransactionsFor(TransactionalAccount account) {
        String currencyCode = account.getFromTemporaryStorage(AccountConstants.CURRENCY);
        TransactionRequest request =
                TransactionRequest.builder()
                        .accountId(account.getFromTemporaryStorage(AccountConstants.ACCOUNT_ID))
                        .category(
                                new CategoryEntity()
                                        .setAccountCategory(
                                                CategoryEntity.CategoryType.valueOf(
                                                        account.getFromTemporaryStorage(
                                                                AccountConstants.ACCOUNT_TYPE))))
                        .currencyCode(currencyCode)
                        .isPendingRequired(true)
                        .transactionEndDate(LocalDateTime.now())
                        .transactionStartDate(
                                LocalDate.parse(
                                                account.getFromTemporaryStorage(
                                                        AccountConstants.CREATION_DATE),
                                                DateTimeFormatter.ISO_DATE)
                                        .atStartOfDay())
                        .build();

        Either<AgentBankApiError, TransactionResponse> response =
                transactionClient.fetchTransactions(request);

        if (response.isRight()) {
            return response.get().getTransactions().stream()
                    .map(transaction -> mapper.map(transaction, currencyCode))
                    .collect(Collectors.toList());
        } else {
            AgentBankApiError apiError = response.getLeft();
            log.error(
                    "An error has been occurred during fetching transactions. {}",
                    apiError.getDetails());
            return Collections.emptyList();
        }
    }
}

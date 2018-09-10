package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.fetcher.rpc.paginated;

import com.google.api.client.util.Lists;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.EuroInformationConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.fetcher.entities.OperationEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

public class OperationSummaryPaginationResponse implements TransactionKeyPaginatorResponse<String> {
    private Optional<OperationSummaryResponse> operationSummaryResponse;

    private OperationSummaryPaginationResponse(OperationSummaryResponse operations) {
        this.operationSummaryResponse = Optional.ofNullable(operations);
    }

    public static OperationSummaryPaginationResponse create(OperationSummaryResponse operations) {
        return new OperationSummaryPaginationResponse(operations);
    }

    @Override
    public String nextKey() {
        return operationSummaryResponse.map(o ->
                o.getOperations().getRecoveryKey())
                .orElse(EuroInformationConstants.EMPTY_RECOVERY_KEY);
    }

    @Override
    public Collection<? extends Transaction> getTinkTransactions() {
        List<Transaction> transactions = Lists.newArrayList();
        operationSummaryResponse.ifPresent(transactionList ->
                transactionList.getOperations().getTransactions().stream()
                        .map(OperationEntity::toTransaction)
                        .forEach(transactions::add)
        );
        return transactions;
    }

    @Override
    public Optional<Boolean> canFetchMore() {
        return Optional.of(
                operationSummaryResponse
                        .map(o -> o.getOperations().getRecoveryKey())
                        .orElse(null)
                         != null
        );
    }
}

package se.tink.backend.aggregation.agents.nxgen.fi.banks.op.fetcher.transactionalaccounts.rpc;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.fetcher.transactionalaccounts.entity.OpBankPersonalFinancesSummaryEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.fetcher.transactionalaccounts.entity.OpBankTransactionEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class OpBankTransactionsResponse implements TransactionKeyPaginatorResponse<String> {

    private int status;
    private String encryptedPrevTransactionId;
    private List<OpBankTransactionEntity> transactions;
    private List<Object> forecasts;
    private OpBankPersonalFinancesSummaryEntity personalFinancesSummary;

    @Override
    public String nextKey() {
        return encryptedPrevTransactionId;
    }

    @Override
    public Collection<? extends Transaction> getTinkTransactions() {
        return Optional.ofNullable(transactions).orElse(Collections.emptyList()).stream()
                .map(OpBankTransactionEntity::toTransaction)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Boolean> canFetchMore() {
        return Optional.of(encryptedPrevTransactionId != null);
    }

    public int getStatus() {
        return status;
    }

    public String getEncryptedPrevTransactionId() {
        return encryptedPrevTransactionId;
    }

    public List<OpBankTransactionEntity> getTransactions() {
        return transactions;
    }

    public List<Object> getForecasts() {
        return forecasts;
    }

    public OpBankPersonalFinancesSummaryEntity getPersonalFinancesSummary() {
        return personalFinancesSummary;
    }
}

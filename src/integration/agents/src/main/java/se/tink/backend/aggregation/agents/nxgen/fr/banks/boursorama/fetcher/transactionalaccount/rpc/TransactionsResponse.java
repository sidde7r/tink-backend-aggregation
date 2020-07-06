package se.tink.backend.aggregation.agents.nxgen.fr.banks.boursorama.fetcher.transactionalaccount.rpc;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.assertj.core.util.Strings;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.boursorama.fetcher.transactionalaccount.entity.AccountActionsEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.boursorama.fetcher.transactionalaccount.entity.OperationsEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class TransactionsResponse {
    private List<AccountActionsEntity> actions;
    private List<OperationsEntity> operations;

    public List<AccountActionsEntity> getActions() {
        return actions;
    }

    public boolean canFetchMore() {
        return !Strings.isNullOrEmpty(getContinuationToken());
    }

    public String getContinuationToken() {
        return getActions().stream()
                .map(
                        accountActionsEntity ->
                                accountActionsEntity.getApi().getParams().getContinuationToken())
                .findFirst()
                .orElse("");
    }

    public Collection<Transaction> getTransactions() {
        return operations.stream()
                .map(OperationsEntity::toTinkTransaction)
                .collect(Collectors.toList());
    }
}

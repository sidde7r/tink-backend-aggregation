package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.fetcher.rpc;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.Getter;
import org.apache.commons.collections4.CollectionUtils;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.fetcher.entity.AccountTransactionEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
@Getter
public class AccountTransactionsResponse implements TransactionKeyPaginatorResponse<String> {

    private List<AccountTransactionEntity> transactions;
    private String offset;
    private String status;

    @Override
    public String nextKey() {
        return offset;
    }

    @Override
    public Collection<? extends Transaction> getTinkTransactions() {
        if (transactions == null) {
            return Collections.emptyList();
        }

        return transactions.stream()
                .map(AccountTransactionEntity::toTinkTransaction)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Boolean> canFetchMore() {
        return Optional.of(offset != null && CollectionUtils.isNotEmpty(transactions));
    }
}

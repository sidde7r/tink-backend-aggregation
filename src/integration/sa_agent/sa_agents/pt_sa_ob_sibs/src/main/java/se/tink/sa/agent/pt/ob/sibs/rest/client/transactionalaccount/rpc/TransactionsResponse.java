package se.tink.sa.agent.pt.ob.sibs.rest.client.transactionalaccount.rpc;

import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import se.tink.sa.agent.pt.ob.sibs.rest.client.transactionalaccount.entity.transaction.TransactionsEntity;
import se.tink.sa.framework.rest.model.TransactionKeyPaginatorResponse;

@Getter
@Setter
public class TransactionsResponse implements TransactionKeyPaginatorResponse<String> {

    private TransactionsEntity transactions;

    @Override
    public Optional<Boolean> canFetchMore() {
        return Optional.of(transactions.getLinks().canFetchMore());
    }

    @Override
    public String nextKey() {
        return transactions.getLinks().getNextKey();
    }
}

package se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.fetcher.transaction.entity;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@Data
@AllArgsConstructor
public class TransactionResponseEntity implements TransactionKeyPaginatorResponse<String> {

    private List<Transaction> transactions;
    private String nextKey;

    @Override
    public String nextKey() {
        return nextKey;
    }

    @Override
    public Collection<? extends Transaction> getTinkTransactions() {
        return transactions;
    }

    @Override
    public Optional<Boolean> canFetchMore() {
        return Optional.of(StringUtils.isNotBlank(nextKey));
    }
}

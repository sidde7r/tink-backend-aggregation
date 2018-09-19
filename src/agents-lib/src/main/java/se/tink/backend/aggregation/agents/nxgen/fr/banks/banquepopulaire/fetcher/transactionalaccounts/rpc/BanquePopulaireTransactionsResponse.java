package se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.fetcher.transactionalaccounts.rpc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.fetcher.transactionalaccounts.entities.AccountTransaction;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class BanquePopulaireTransactionsResponse extends ArrayList<AccountTransaction> implements
        TransactionKeyPaginatorResponse<String> {

    public static BanquePopulaireTransactionsResponse empty() {
        return new BanquePopulaireTransactionsResponse();
    }

    @Override
    public String nextKey() {
        return null;
    }

    @Override
    public Collection<? extends Transaction> getTinkTransactions() {
        return stream()
                .map(AccountTransaction::toTinkTransaction)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Boolean> canFetchMore() {
        return Optional.of(false);
    }
}

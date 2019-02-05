package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.fetcher.transactionalaccount.rpc;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.SamlinkConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.entities.LinkEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.entities.Links;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.fetcher.transactionalaccount.entities.TransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.rpc.LinksResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

public class TransactionsResponse extends LinksResponse {
    private List<TransactionEntity> transactions;

    public List<Transaction> toTinkTransactions(Function<Links, TransactionDetailsResponse>
            detailsSupplier) {
        return Optional.ofNullable(transactions)
                .map(Collection::stream)
                .map(trans -> trans
                        .map(transaction -> detailsSupplier.apply(transaction.getLinks()))
                        .map(TransactionDetailsResponse::toTinkTransaction)
                        .collect(Collectors.toList()))
                .orElseGet(Collections::emptyList);
    }

    public int size() {
        return transactions != null ? transactions.size() : 0;
    }

    public Optional<LinkEntity> getNext() {
        return getLinks().findLink(SamlinkConstants.LinkRel.NEXT);
    }
}

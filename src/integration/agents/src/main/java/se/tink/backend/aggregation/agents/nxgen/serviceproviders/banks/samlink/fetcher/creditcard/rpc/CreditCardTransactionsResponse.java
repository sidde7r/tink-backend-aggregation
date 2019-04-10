package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.fetcher.creditcard.rpc;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.SamlinkConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.entities.LinkEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.fetcher.creditcard.entities.CardTransaction;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.rpc.LinksResponse;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.CreditCardTransaction;

public class CreditCardTransactionsResponse extends LinksResponse {

    private List<CardTransaction> transactions;

    public Collection<CreditCardTransaction> toTinkTransactions(CreditCardAccount account) {
        return Optional.ofNullable(transactions)
                .map(Collection::stream)
                .map(
                        transactions ->
                                transactions
                                        .map(
                                                cardTransaction ->
                                                        cardTransaction.toTinkTransaction(account))
                                        .collect(Collectors.toList()))
                .orElseGet(Collections::emptyList);
    }

    public Optional<LinkEntity> getNext() {
        return getLinks().findLink(SamlinkConstants.LinkRel.NEXT);
    }

    public int size() {
        return transactions != null ? transactions.size() : 0;
    }
}

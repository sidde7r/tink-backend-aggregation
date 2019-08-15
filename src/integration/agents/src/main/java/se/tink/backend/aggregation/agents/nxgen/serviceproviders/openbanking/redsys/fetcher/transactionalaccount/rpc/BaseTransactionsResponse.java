package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.fetcher.transactionalaccount.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.RedsysConstants.Links;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.entities.AccountReferenceEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.entities.LinkEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.entities.TppMessageEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.fetcher.transactionalaccount.entities.AccountReportEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.fetcher.transactionalaccount.entities.BalanceEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.fetcher.transactionalaccount.entities.TransactionEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.core.transaction.UpcomingTransaction;

@JsonObject
public class BaseTransactionsResponse<T extends TransactionEntity>
        implements TransactionKeyPaginatorResponse<String> {
    @JsonProperty private AccountReferenceEntity account;
    @JsonProperty private AccountReportEntity<T> transactions;
    @JsonProperty private List<BalanceEntity> balances;

    @JsonProperty("_links")
    private Map<String, LinkEntity> links;

    @JsonProperty private String psuMessage;
    @JsonProperty private List<TppMessageEntity> tppMessages;

    @JsonIgnore
    public Optional<LinkEntity> getLink(String linkName) {
        if (links == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(links.get(linkName));
    }

    @Override
    @JsonIgnore
    public Collection<? extends Transaction> getTinkTransactions() {
        return transactions.getBookedTransactions().stream()
                .map(TransactionEntity::toBookedTransaction)
                .collect(Collectors.toList());
    }

    @JsonIgnore
    public Collection<UpcomingTransaction> getUpcomingTransactions() {
        return transactions.getPendingTransactions().stream()
                .map(TransactionEntity::toPendingTransaction)
                .collect(Collectors.toList());
    }

    @JsonIgnore
    private Optional<LinkEntity> getNextLink() {
        return transactions.getLink(Links.NEXT);
    }

    @Override
    @JsonIgnore
    public Optional<Boolean> canFetchMore() {
        return Optional.of(getNextLink().isPresent());
    }

    @Override
    @JsonIgnore
    public String nextKey() {
        final Optional<LinkEntity> nextLink = getNextLink();
        return nextLink.map(LinkEntity::getHref).orElse(null);
    }

    @JsonIgnore
    public boolean isLastPage() {
        return !getNextLink().isPresent();
    }
}

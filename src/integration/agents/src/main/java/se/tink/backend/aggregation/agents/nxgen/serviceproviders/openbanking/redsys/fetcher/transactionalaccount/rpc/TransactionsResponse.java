package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.fetcher.transactionalaccount.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.RedsysConstants.Links;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.entities.AccountReferenceEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.entities.LinkEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.entities.TppMessageEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.fetcher.transactionalaccount.entities.AccountReportEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.fetcher.transactionalaccount.entities.BalanceEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class TransactionsResponse implements TransactionKeyPaginatorResponse<String> {
    @JsonProperty private AccountReferenceEntity account;
    @JsonProperty private AccountReportEntity transactions;
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
    public Collection<? extends Transaction> getTinkTransactions() {
        return transactions.getTinkTransactions();
    }

    @Override
    public Optional<Boolean> canFetchMore() {
        final Optional<LinkEntity> nextLink = transactions.getLink(Links.NEXT);
        return Optional.of(nextLink.isPresent());
    }

    @Override
    public String nextKey() {
        final Optional<LinkEntity> nextLink = transactions.getLink(Links.NEXT);
        if (nextLink.isPresent()) {
            return nextLink.get().getHref();
        }
        return null;
    }
}

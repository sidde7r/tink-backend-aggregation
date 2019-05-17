package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.entities.LinkEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class AccountReportEntity {
    @JsonProperty("booked")
    private List<TransactionEntity> bookedTransactions;

    @JsonProperty("pending")
    private List<TransactionEntity> pendingTransactions;

    @JsonProperty("_links")
    private Map<String, LinkEntity> links;

    @JsonIgnore
    public Optional<LinkEntity> getLink(String linkName) {
        return Optional.ofNullable(links.get(linkName));
    }

    @JsonIgnore
    public List<TransactionEntity> getBookedTransactions() {
        return Optional.ofNullable(bookedTransactions).orElseGet(Collections::emptyList);
    }

    @JsonIgnore
    public List<TransactionEntity> getPendingTransactions() {
        return Optional.ofNullable(pendingTransactions).orElseGet(Collections::emptyList);
    }

    @JsonIgnore
    public Collection<? extends Transaction> getTinkTransactions() {
        return Stream.concat(
                        getBookedTransactions().stream()
                                .map(TransactionEntity::toBookedTransaction),
                        getPendingTransactions().stream()
                                .map(TransactionEntity::toPendingTransaction))
                .collect(Collectors.toList());
    }
}

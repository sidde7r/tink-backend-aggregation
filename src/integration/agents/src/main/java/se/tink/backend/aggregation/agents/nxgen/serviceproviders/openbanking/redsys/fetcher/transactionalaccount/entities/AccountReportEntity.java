package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.entities.LinkEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountReportEntity<T extends TransactionEntity> {
    @JsonProperty("booked")
    private List<T> bookedTransactions;

    @JsonProperty("pending")
    private List<T> pendingTransactions;

    @JsonProperty("_links")
    private Map<String, LinkEntity> links;

    @JsonIgnore
    public Optional<LinkEntity> getLink(String linkName) {
        if (links == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(links.get(linkName));
    }

    @JsonIgnore
    public List<T> getBookedTransactions() {
        return Optional.ofNullable(bookedTransactions).orElseGet(Collections::emptyList);
    }

    @JsonIgnore
    public List<T> getPendingTransactions() {
        return Optional.ofNullable(pendingTransactions).orElseGet(Collections::emptyList);
    }
}

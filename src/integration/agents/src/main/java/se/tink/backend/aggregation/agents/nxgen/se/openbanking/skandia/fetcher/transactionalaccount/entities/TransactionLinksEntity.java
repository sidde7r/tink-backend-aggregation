package se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.Href;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionLinksEntity {

    private Href account;
    @JsonProperty private Href next;

    @JsonIgnore
    public boolean hasMore() {
        return next != null && !Strings.isNullOrEmpty(next.getHref());
    }

    @JsonIgnore
    public String getNext() {
        return hasMore() ? next.getHref() : null;
    }
}

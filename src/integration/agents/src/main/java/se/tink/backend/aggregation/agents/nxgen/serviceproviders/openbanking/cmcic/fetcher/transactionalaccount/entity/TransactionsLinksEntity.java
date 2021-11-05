package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@Setter
@JsonObject
public class TransactionsLinksEntity {
    @JsonProperty("self")
    private GenericLinkEntity self = null;

    @JsonProperty("parent-list")
    private GenericLinkEntity parentList = null;

    @JsonProperty("balances")
    private GenericLinkEntity balances = null;

    @JsonProperty("first")
    private GenericLinkEntity first = null;

    @JsonProperty("last")
    private GenericLinkEntity last = null;

    @JsonProperty("next")
    private GenericLinkEntity next = null;

    @JsonProperty("prev")
    private GenericLinkEntity prev = null;
}

package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class EndUserIdentityLinksEntity {
    @JsonProperty("self")
    private GenericLinkEntity self = null;

    @JsonProperty("parent-list")
    private GenericLinkEntity parentList = null;

    public GenericLinkEntity getSelf() {
        return self;
    }

    public void setSelf(GenericLinkEntity self) {
        this.self = self;
    }

    public GenericLinkEntity getParentList() {
        return parentList;
    }

    public void setParentList(GenericLinkEntity parentList) {
        this.parentList = parentList;
    }
}

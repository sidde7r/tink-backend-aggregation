package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BeneficiariesLinksEntity {
    @JsonProperty("self")
    private GenericLinkEntity self = null;

    @JsonProperty("parent-list")
    private GenericLinkEntity parentList = null;

    @JsonProperty("first")
    private GenericLinkEntity first = null;

    @JsonProperty("last")
    private GenericLinkEntity last = null;

    @JsonProperty("next")
    private GenericLinkEntity next = null;

    @JsonProperty("prev")
    private GenericLinkEntity prev = null;

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

    public GenericLinkEntity getFirst() {
        return first;
    }

    public void setFirst(GenericLinkEntity first) {
        this.first = first;
    }

    public GenericLinkEntity getLast() {
        return last;
    }

    public void setLast(GenericLinkEntity last) {
        this.last = last;
    }

    public GenericLinkEntity getNext() {
        return next;
    }

    public void setNext(GenericLinkEntity next) {
        this.next = next;
    }

    public GenericLinkEntity getPrev() {
        return prev;
    }

    public void setPrev(GenericLinkEntity prev) {
        this.prev = prev;
    }
}

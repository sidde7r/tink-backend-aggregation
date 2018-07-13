package se.tink.backend.aggregation.agents.banks.swedbank.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.base.MoreObjects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LinksEntity {
    private LinkEntity edit;
    private LinkEntity next;
    private LinkEntity self;
    private LinkEntity sign;
    private LinkEntity delete;

    public LinkEntity getEdit() {
        return edit;
    }

    public LinkEntity getNext() {
        return next;
    }

    public LinkEntity getSelf() {
        return self;
    }

    public LinkEntity getSign() {
        return sign;
    }

    public void setEdit(LinkEntity edit) {
        this.edit = edit;
    }

    public void setNext(LinkEntity next) {
        this.next = next;
    }

    public void setSelf(LinkEntity self) {
        this.self = self;
    }

    public void setSign(LinkEntity sign) {
        this.sign = sign;
    }

    public LinkEntity getDelete() {
        return delete;
    }

    public void setDelete(LinkEntity delete) {
        this.delete = delete;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).omitNullValues()
                .add("edit", edit)
                .add("next", next)
                .add("self", self)
                .add("sign", sign)
                .add("delete", delete)
                .toString();
    }
}

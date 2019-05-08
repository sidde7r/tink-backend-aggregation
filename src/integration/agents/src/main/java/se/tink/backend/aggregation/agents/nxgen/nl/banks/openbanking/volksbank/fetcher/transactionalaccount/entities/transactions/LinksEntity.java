package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.entities.transactions;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LinksEntity {

    private HrefEntity next;
    private HrefEntity previous;

    public HrefEntity getNext() {
        return next;
    }

    public void setNext(HrefEntity next) {
        this.next = next;
    }

    public HrefEntity getPrevious() {
        return previous;
    }

    public void setPrevious(HrefEntity previous) {
        this.previous = previous;
    }
}

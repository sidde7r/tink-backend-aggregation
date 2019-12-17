package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.entities.transactions;

import se.tink.backend.aggregation.agents.Href;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LinksEntity {

    private Href next;
    private Href previous;

    public Href getNext() {
        return next;
    }

    public void setNext(Href next) {
        this.next = next;
    }

    public Href getPrevious() {
        return previous;
    }

    public void setPrevious(Href previous) {
        this.previous = previous;
    }
}

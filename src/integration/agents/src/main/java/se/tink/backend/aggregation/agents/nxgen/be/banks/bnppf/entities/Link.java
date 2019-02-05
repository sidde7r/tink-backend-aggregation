package se.tink.backend.aggregation.agents.nxgen.be.banks.bnppf.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class Link {
    private String self;
    private String first;
    private String last;
    private String next;
    private String previous;

    public String getSelf() {
        return self;
    }

    public String getFirst() {
        return first;
    }

    public String getLast() {
        return last;
    }

    public String getNext() {
        return next;
    }

    public String getPrevious() {
        return previous;
    }
}

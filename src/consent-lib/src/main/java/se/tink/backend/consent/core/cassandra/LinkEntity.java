package se.tink.backend.consent.core.cassandra;

public class LinkEntity {
    private String destination;
    private int start;
    private int end;

    public LinkEntity() {
    }

    public LinkEntity(String destination, int start, int end) {
        this.destination = destination;
        this.start = start;
        this.end = end;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }
}

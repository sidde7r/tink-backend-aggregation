package se.tink.backend.aggregation.nxgen.http.log;

public abstract class HttpLogEntry {
    private String agent;
    private String timestamp;

    public String getAgent() {
        return agent;
    }

    public void setAgent(String agent) {
        this.agent = agent;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public abstract String getEntryType();
}

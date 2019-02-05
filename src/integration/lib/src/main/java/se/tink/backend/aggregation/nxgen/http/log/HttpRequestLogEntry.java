package se.tink.backend.aggregation.nxgen.http.log;

public class HttpRequestLogEntry extends HttpLogEntry {
    private static final String ENTRY_TYPE = "Request";
    private String agent;
    private String maskedBody;
    private String maskedHeader;
    private String maskedMethod;
    private String maskedUri;
    private String timestamp;

    public String getAgent() {
        return agent;
    }

    public void setAgent(String agent) {
        this.agent = agent;
    }

    public String getMaskedBody() {
        return maskedBody;
    }

    public void setMaskedBody(String maskedBody) {
        this.maskedBody = maskedBody;
    }

    public String getMaskedHeader() {
        return maskedHeader;
    }

    public void setMaskedHeader(String maskedHeader) {
        this.maskedHeader = maskedHeader;
    }

    public String getMaskedMethod() {
        return maskedMethod;
    }

    public void setMethod(String maskedMethod) {
        this.maskedMethod = maskedMethod;
    }

    public String getMaskedUri() {
        return maskedUri;
    }

    public void setMaskedUri(String maskedUri) {
        this.maskedUri = maskedUri;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String getEntryType() {
        return  ENTRY_TYPE;
    }
}

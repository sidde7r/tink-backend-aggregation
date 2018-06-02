package se.tink.backend.system.document.core;

public class DocumentModeratorDispatchDetails {

    private final String toAddress;
    private final String fromAddress;
    private final String fromName;

    public DocumentModeratorDispatchDetails(String toAddress, String fromAddress,
            String fromName) {
        this.toAddress = toAddress;
        this.fromAddress = fromAddress;
        this.fromName = fromName;
    }

    public String getToAddress() {
        return toAddress;
    }

    public String getFromAddress() {
        return fromAddress;
    }

    public String getFromName() {
        return fromName;
    }
}

package se.tink.backend.aggregation.nxgen.http.log;

public class HttpResponseLogEntry extends HttpLogEntry {
    private static final String ENTRY_TYPE = "Response";
    private String maskedBody;
    private String maskedHeader;
    private String maskedMethod;
    private String maskedLocation;
    private String statusCode;
    private String statusInfo;

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

    public String getMaskedLocation() {
        return maskedLocation;
    }

    public void setMaskedLocation(String maskedLocation) {
        this.maskedLocation = maskedLocation;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    public String getStatusInfo() {
        return statusInfo;
    }

    public void setStatusInfo(String statusInfo) {
        this.statusInfo = statusInfo;
    }

    @Override
    public String getEntryType() {
        return ENTRY_TYPE;
    }
}

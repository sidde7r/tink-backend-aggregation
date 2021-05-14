package se.tink.backend.aggregation.eidasidentity;

public enum AvailableCertIds {
    DEFAULT("DEFAULT"),
    OLD("OLD_EIDAS"),
    UKOB("UKOB");

    private final String certId;

    AvailableCertIds(String certId) {
        this.certId = certId;
    }

    public String getValue() {
        return certId;
    }
}

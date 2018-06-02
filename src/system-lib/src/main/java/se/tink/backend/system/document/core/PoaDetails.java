package se.tink.backend.system.document.core;

public class PoaDetails {

    private final String signingLocation;
    private final String signingDate;
    private final String expirationDate;
    private final byte[] signature;

    public PoaDetails(String signingLocation, String signingDate, String expirationDate, byte[] signature) {
        this.signingLocation = signingLocation;
        this.signingDate = signingDate;
        this.expirationDate = expirationDate;
        this.signature = signature;
    }

    public String getSigningLocation() {
        return signingLocation;
    }

    public String getSigningDate() {
        return signingDate;
    }

    public String getExpirationDate() {
        return expirationDate;
    }

    public byte[] getSignature() {
        return signature;
    }
}

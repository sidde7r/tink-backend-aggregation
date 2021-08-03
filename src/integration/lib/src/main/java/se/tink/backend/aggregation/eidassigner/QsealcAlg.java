package se.tink.backend.aggregation.eidassigner;

public enum QsealcAlg {
    EIDAS_RSA_SHA256("/sign-rsa-sha256/"),
    EIDAS_PSS_SHA256("/sign-rsa-pss256/"),
    EIDAS_JWT_RSA_SHA256("/jwt-rsa-sha256/");

    private final String signingType;

    QsealcAlg(String signingType) {
        this.signingType = signingType;
    }

    public String getSigningType() {
        return this.signingType;
    }
}

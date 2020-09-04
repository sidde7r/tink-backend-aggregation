package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.samlink.provider;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupConstants;

public class SamlinkAuthorisationEntity {

    private final String keyId;
    private final String signature;

    public SamlinkAuthorisationEntity(final String keyId, final String signature) {
        this.keyId = keyId;
        this.signature = signature;
    }

    @Override
    public String toString() {
        return BerlinGroupConstants.Signature.KEY_ID_NAME
                + "\""
                + keyId
                + "\","
                + "algorithm=\"rsa-sha256\""
                + ","
                + "headers=\"Digest X-Request-ID TPP-Redirect-URI\""
                + ","
                + BerlinGroupConstants.Signature.SIGNATURE_NAME
                + "\""
                + signature
                + "\"";
    }
}

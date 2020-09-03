package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.samlink.provider;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupConstants.HeaderKeys;

public class SamlinkSignatureEntity {

    private final String digest;
    private final String requestId;
    private final String redirectUri;

    public SamlinkSignatureEntity(
            final String digest, final String requestId, final String redirectUri) {
        this.digest = digest;
        this.requestId = requestId;
        this.redirectUri = redirectUri;
    }

    @Override
    public String toString() {
        return HeaderKeys.DIGEST
                + ": "
                + digest
                + "\n"
                + HeaderKeys.X_REQUEST_ID
                + ": "
                + requestId
                + "\n"
                + "TPP-Redirect-URI"
                + ": "
                + redirectUri;
    }
}

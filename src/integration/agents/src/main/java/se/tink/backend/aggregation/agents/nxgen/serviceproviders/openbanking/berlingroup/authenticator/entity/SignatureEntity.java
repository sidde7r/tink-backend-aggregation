package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.entity;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupConstants.HeaderKeys;

public class SignatureEntity {

    private final String digest;
    private final String xIngRequestId;

    public SignatureEntity(String digest, String xIngRequestId) {
        this.digest = digest;
        this.xIngRequestId = xIngRequestId;
    }

    @Override
    public String toString() {
        return BerlinGroupConstants.Signature.DIGEST
                + digest
                + System.lineSeparator()
                + HeaderKeys.X_REQUEST_ID.toLowerCase()
                + ": "
                + xIngRequestId;
    }
}

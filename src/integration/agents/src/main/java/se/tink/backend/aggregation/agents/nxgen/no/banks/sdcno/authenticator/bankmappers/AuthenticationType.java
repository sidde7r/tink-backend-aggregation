package se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno.authenticator.bankmappers;

import se.tink.backend.agents.rpc.Field.Key;

public enum AuthenticationType {
    NETTBANK(Key.DATE_OF_BIRTH),
    PORTAL(Key.NATIONAL_ID_NUMBER);

    private Key credentialsAdditionalKey;

    AuthenticationType(Key idNumberType) {
        this.credentialsAdditionalKey = idNumberType;
    }

    public Key getCredentialsAdditionalKey() {
        return credentialsAdditionalKey;
    }
}

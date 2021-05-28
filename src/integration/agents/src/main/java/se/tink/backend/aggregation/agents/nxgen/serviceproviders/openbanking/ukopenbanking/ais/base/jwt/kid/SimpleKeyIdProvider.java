package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.jwt.kid;

public class SimpleKeyIdProvider implements KeyIdProvider {

    private final String keyId;

    public SimpleKeyIdProvider(String keyId) {
        this.keyId = keyId;
    }

    @Override
    public String get() {
        return keyId;
    }
}

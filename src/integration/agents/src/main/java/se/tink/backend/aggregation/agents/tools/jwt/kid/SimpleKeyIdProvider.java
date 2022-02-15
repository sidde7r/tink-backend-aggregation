package se.tink.backend.aggregation.agents.tools.jwt.kid;

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

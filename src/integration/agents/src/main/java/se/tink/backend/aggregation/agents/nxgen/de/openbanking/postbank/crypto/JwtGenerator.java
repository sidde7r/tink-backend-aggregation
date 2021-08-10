package se.tink.backend.aggregation.agents.nxgen.de.openbanking.postbank.crypto;

public interface JwtGenerator {
    String createJWT(String password);
}

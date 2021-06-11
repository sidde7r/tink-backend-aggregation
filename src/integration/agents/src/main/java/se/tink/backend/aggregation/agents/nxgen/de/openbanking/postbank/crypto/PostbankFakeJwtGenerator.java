package se.tink.backend.aggregation.agents.nxgen.de.openbanking.postbank.crypto;

public class PostbankFakeJwtGenerator implements JwtGenerator {

    @Override
    public String createJWT(String password) {
        return password;
    }
}

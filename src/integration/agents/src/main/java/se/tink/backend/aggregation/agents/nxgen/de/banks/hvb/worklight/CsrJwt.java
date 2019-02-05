package se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.worklight;

import java.math.BigInteger;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.worklight.authenticator.entities.JoseHeaderEntity;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.worklight.authenticator.entities.JpkEntity;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.worklight.authenticator.entities.JwtPayloadEntity;

public final class CsrJwt implements Jwt {

    private final String token;
    private final RSAPublicKey publicKey;
    private final String moduleName;

    public CsrJwt(final String token, final RSAPublicKey publicKey, final String moduleName) {
        this.token = token;
        this.publicKey = publicKey;
        this.moduleName = moduleName;
    }

    @Override
    public JoseHeaderEntity getJoseHeader() {
        final JoseHeaderEntity entity = new JoseHeaderEntity();
        entity.setAlg(WLConstants.ALG);
        final BigInteger n = this.publicKey.getModulus();
        final BigInteger e = this.publicKey.getPublicExponent();
        final JpkEntity jpk = new JpkEntity(
                "RSA",
                Base64.getUrlEncoder().encodeToString(n.toByteArray()),
                Base64.getUrlEncoder().encodeToString(e.toByteArray())
        );
        entity.setJpk(jpk);

        return entity;
    }

    @Override
    public JwtPayloadEntity getPayload() {
        final JwtPayloadEntity payload = new JwtPayloadEntity();
        payload.setToken(this.token);
        payload.setApplicationId(moduleName);
        payload.setDeviceId(WLConstants.DEVICE_ID);

        return payload;
    }
}

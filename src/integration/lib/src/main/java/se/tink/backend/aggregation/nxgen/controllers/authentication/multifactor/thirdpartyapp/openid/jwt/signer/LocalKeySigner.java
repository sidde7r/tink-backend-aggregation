package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.jwt.signer;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import java.security.interfaces.RSAPrivateKey;
import java.util.Map;
import net.minidev.json.JSONObject;
import se.tink.backend.aggregation.agents.utils.crypto.ps256.PS256;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.jwt.signer.iface.JwtSigner;

public class LocalKeySigner implements JwtSigner {

    private final String keyId;
    private final RSAPrivateKey signingKey;

    public LocalKeySigner(String keyId, RSAPrivateKey signingKey) {
        this.keyId = keyId;
        this.signingKey = signingKey;
    }

    @Override
    public String sign(
            Algorithm algorithm,
            Map<String, Object> headerClaims,
            Map<String, Object> payloadClaims,
            boolean detachedPayload) {

        JWSHeader header =
                new JWSHeader.Builder(JWSAlgorithm.parse(algorithm.toString()))
                        .keyID(keyId)
                        .customParams(headerClaims)
                        .build();

        JSONObject body = new JSONObject(payloadClaims);

        return PS256.sign(header, body, signingKey, detachedPayload);
    }
}

package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.authenticator.jwt;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.util.Base64URL;
import java.text.ParseException;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Predicate;
import se.tink.backend.aggregation.agents.utils.crypto.RSA;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.jwt.signer.SigningKeyIdProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.jwt.signer.TinkJwtSigner;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.jwt.signer.iface.JwtSigner;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class JwksKeySigner implements JwtSigner {
    private static final String BEGIN_PRIVATE_KEY = "-----BEGIN PRIVATE KEY-----\n";
    private static final String END_PRIVATE_KEY = "\n-----END PRIVATE KEY-----";
    private final String signingKey;
    private final URL jwksEndpoint;
    private final JwksClient jwksClient;

    public JwksKeySigner(String signingKey, URL jwksEndpoint, JwksClient jwksClient) {
        this.signingKey = signingKey;
        this.jwksEndpoint = jwksEndpoint;
        this.jwksClient = jwksClient;
    }

    @Override
    public String sign(
            Algorithm algorithm,
            Map<String, Object> headerClaims,
            Map<String, Object> payloadClaims,
            boolean detachedPayload) {
        return TinkJwtSigner.builder(signingKeyProvider())
                .withAlgorithm(algorithm)
                .withHeaderClaims(headerClaims)
                .withPayloadClaims(payloadClaims)
                .withDetachedPayload(detachedPayload)
                .build()
                .sign(RSA.getPrivateKeyFromBytes(EncodingUtils.decodeBase64String(signingKey)));
    }

    private SigningKeyIdProvider signingKeyProvider() {
        return () -> {
            try {
                JWKSet jsonWebKeySet = jwksClient.get(jwksEndpoint);
                String pem = BEGIN_PRIVATE_KEY + signingKey + END_PRIVATE_KEY;
                JWK jwkPrivateKey = JWK.parseFromPEMEncodedObjects(pem);
                Base64URL thumbprint = jwkPrivateKey.computeThumbprint();
                return jsonWebKeySet.getKeys().stream()
                        .filter(compereThumbprints(thumbprint))
                        .findFirst()
                        .map(JWK::getKeyID)
                        .orElseThrow(
                                () ->
                                        new NoSuchElementException(
                                                "The JWKS doesn't contains suitable thumbprint for given Private Key"));
            } catch (JOSEException | ParseException e) {
                throw new IllegalStateException(e);
            }
        };
    }

    private static Predicate<JWK> compereThumbprints(Base64URL thumbprint) {
        return jwk -> {
            try {
                return thumbprint.equals(jwk.computeThumbprint());
            } catch (JOSEException e) {
                throw new IllegalStateException(e);
            }
        };
    }
}

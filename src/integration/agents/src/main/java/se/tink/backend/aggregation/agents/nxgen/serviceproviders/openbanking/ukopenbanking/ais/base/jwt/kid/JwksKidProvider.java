package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.jwt.kid;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.util.Base64URL;
import io.vavr.collection.List;
import java.security.cert.X509Certificate;
import java.util.NoSuchElementException;
import java.util.function.Predicate;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.jwt.JwksClient;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@Slf4j
public class JwksKidProvider implements KidProvider {
    private final JwksClient jwksClient;
    private final URL jwksEndpoint;
    private final X509Certificate signingCertificate;

    public JwksKidProvider(
            JwksClient jwksClient, URL jwksEndpoint, X509Certificate signingCertificate) {
        this.jwksClient = jwksClient;
        this.jwksEndpoint = jwksEndpoint;
        this.signingCertificate = signingCertificate;
    }

    @Override
    @SneakyThrows
    public String get() {
        JWKSet jsonWebKeySet = jwksClient.get(jwksEndpoint);
        JWK jwkPublicKey = JWK.parse(signingCertificate);
        Base64URL thumbprint = jwkPublicKey.computeThumbprint();
        String keyId = findMatchingPublicKeyId(jsonWebKeySet, thumbprint);
        log.info("`{}` keyId has been found.", keyId);
        return keyId;
    }

    private static String findMatchingPublicKeyId(JWKSet jsonWebKeySet, Base64URL thumbprint) {
        return List.ofAll(jsonWebKeySet.getKeys())
                .find(compareThumbprints(thumbprint))
                .map(JWK::getKeyID)
                .getOrElseThrow(
                        () ->
                                new NoSuchElementException(
                                        "The JWKS doesn't contain matching thumbprint for public key of given signing certificate"));
    }

    private static Predicate<JWK> compareThumbprints(Base64URL thumbprint) {
        return jwk -> {
            try {
                return thumbprint.equals(jwk.computeThumbprint());
            } catch (JOSEException e) {
                throw new IllegalStateException(e);
            }
        };
    }
}

package se.tink.backend.aggregation.register.serviceproviders.danskebank.jwt;

import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.SignatureGenerationException;
import com.auth0.jwt.impl.ClaimsHolder;
import com.auth0.jwt.impl.PayloadSerializer;
import com.auth0.jwt.impl.PublicClaims;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.common.base.Strings;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.codec.binary.Base64;
import se.tink.backend.aggregation.configuration.EidasProxyConfiguration;
import se.tink.backend.aggregation.eidassigner.EidasIdentity;
import se.tink.backend.aggregation.eidassigner.QsealcAlg;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;

// This is a copy of auth0.JWTCreator with the addition of `Builder.withClaim(String name, Object
// value)`.
// It depends on auth0.jwt for, among other things, the Algorithm class.
public final class QsealcJwtCreator {

    private final QsealcSigner signer;
    private final String headerJson;
    private final String body;

    private QsealcJwtCreator(QsealcSigner signer, Map<String, Object> headerClaims, String body)
            throws JWTCreationException {
        this.signer = signer;
        try {
            ObjectMapper mapper = new ObjectMapper();
            SimpleModule module = new SimpleModule();
            module.addSerializer(ClaimsHolder.class, new PayloadSerializer());
            mapper.registerModule(module);
            mapper.configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true);
            headerJson = mapper.writeValueAsString(headerClaims);
            this.body = body;
        } catch (JsonProcessingException e) {
            throw new JWTCreationException(
                    "Some of the Claims couldn't be converted to a valid JSON format.", e);
        }
    }

    public static Builder create() {
        return new Builder();
    }

    private String sign() throws SignatureGenerationException {
        String header =
                Base64.encodeBase64URLSafeString(headerJson.getBytes(StandardCharsets.UTF_8));
        String content = String.format("%s.%s", header, body);
        String signature = signer.getSignatureBase64(content.getBytes(StandardCharsets.UTF_8));

        return String.format("%s.%s", content, signature);
    }

    public static class Builder {

        private String body;
        private Map<String, Object> headerClaims;

        Builder() {
            this.headerClaims = new HashMap<>();
        }

        public Builder withBody(final String base64Body) {
            body = base64Body;
            return this;
        }

        /**
         * Creates a new JWT and signs is with the given algorithm
         *
         * @param signingKeyId used to sign the JWT
         * @return a new JWT token
         * @throws IllegalArgumentException if the provided algorithm is null.
         * @throws JWTCreationException if the claims could not be converted to a valid JSON or
         *     there was a problem with the signing key.
         */
        public String sign(final String signingKeyId)
                throws IllegalArgumentException, JWTCreationException {

            if (Strings.isNullOrEmpty(body)) {
                throw new IllegalArgumentException("Body must not be null/empty.");
            }

            if (Strings.isNullOrEmpty(signingKeyId)) {
                throw new IllegalArgumentException("Signing keyId must not be null/empty.");
            }

            QsealcSigner signer =
                    QsealcSigner.build(
                            EidasProxyConfiguration.createLocal("").toInternalConfig(),
                            QsealcAlg.EIDAS_RSA_SHA256,
                            new EidasIdentity("oxford-staging", "", ""));

            headerClaims.put(PublicClaims.ALGORITHM, "RS256");
            headerClaims.put(PublicClaims.TYPE, "JWT");
            headerClaims.put(PublicClaims.KEY_ID, signingKeyId);

            return new QsealcJwtCreator(signer, headerClaims, body).sign();
        }
    }
}

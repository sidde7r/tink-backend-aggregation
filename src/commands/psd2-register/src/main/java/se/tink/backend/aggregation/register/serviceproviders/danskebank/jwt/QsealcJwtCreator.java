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
import java.util.concurrent.TimeUnit;
import org.apache.commons.codec.binary.Base64;
import se.tink.backend.aggregation.configuration.eidas.proxy.EidasProxyConfiguration;
import se.tink.backend.aggregation.eidassigner.QsealcAlg;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;
import se.tink.backend.aggregation.eidassigner.QsealcSignerImpl;
import se.tink.backend.aggregation.eidassigner.identity.EidasIdentity;
import se.tink.libraries.serialization.utils.SerializationUtils;

public final class QsealcJwtCreator {

    private static final String EIDAS_PROXY_URL =
            "https://eidas-proxy.staging.aggregation.tink.network";

    private final QsealcSigner signer;
    private final String headerJson;
    private final String bodyJson;

    private QsealcJwtCreator(
            QsealcSigner signer, Map<String, Object> headerClaims, Map<String, Object> bodyClaims)
            throws JWTCreationException {
        this.signer = signer;
        try {
            ObjectMapper mapper = new ObjectMapper();
            SimpleModule module = new SimpleModule();
            module.addSerializer(ClaimsHolder.class, new PayloadSerializer());
            mapper.registerModule(module);
            mapper.configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true);
            headerJson = mapper.writeValueAsString(headerClaims);
            bodyJson = mapper.writeValueAsString(bodyClaims);
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
        String body = Base64.encodeBase64URLSafeString(bodyJson.getBytes(StandardCharsets.UTF_8));
        String content = String.format("%s.%s", header, body);
        String signature =
                Base64.encodeBase64URLSafeString(
                        signer.getSignature(content.getBytes(StandardCharsets.UTF_8)));

        return String.format("%s.%s", content, signature);
    }

    public static class Builder {

        private String body;
        private Map<String, Object> headerClaims;
        private Map<String, Object> bodyClaims;

        Builder() {
            this.headerClaims = new HashMap<>();
        }

        public Builder withBody(final String base64Body) {
            body = base64Body;
            bodyClaims =
                    SerializationUtils.deserializeFromString(
                            new String(Base64.decodeBase64(body)), HashMap.class);
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
                    QsealcSignerImpl.build(
                            EidasProxyConfiguration.createLocal(EIDAS_PROXY_URL).toInternalConfig(),
                            QsealcAlg.EIDAS_PSS_SHA256,
                            new EidasIdentity(
                                    "oxford-staging", "5f98e87106384b2981c0354a33b51590", ""));

            headerClaims.put(PublicClaims.ALGORITHM, "PS256");
            headerClaims.put(PublicClaims.TYPE, "JWT");
            headerClaims.put(PublicClaims.KEY_ID, signingKeyId);

            long timestampNow = System.currentTimeMillis() / 1000;
            bodyClaims.put(PublicClaims.ISSUED_AT, timestampNow);
            bodyClaims.put(PublicClaims.EXPIRES_AT, timestampNow + TimeUnit.MINUTES.toSeconds(5));

            return new QsealcJwtCreator(signer, headerClaims, bodyClaims).sign();
        }
    }
}

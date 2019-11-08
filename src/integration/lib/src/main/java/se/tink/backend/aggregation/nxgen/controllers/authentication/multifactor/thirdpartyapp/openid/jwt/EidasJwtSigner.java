package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.jwt;

import com.auth0.jwt.impl.ClaimsHolder;
import com.auth0.jwt.impl.PayloadSerializer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.common.base.Preconditions;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.codec.binary.Base64;
import se.tink.backend.aggregation.configuration.EidasProxyConfiguration;
import se.tink.backend.aggregation.eidassigner.EidasIdentity;
import se.tink.backend.aggregation.eidassigner.QsealcAlg;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;

public class EidasJwtSigner implements JwtSigner {

    public static class EidasSigningKey {

        private final String id;
        private final QsealcAlg alg;

        private EidasSigningKey(String id, QsealcAlg alg) {
            this.id = id;
            this.alg = alg;
        }

        public static EidasSigningKey of(String id, QsealcAlg alg) {
            return new EidasSigningKey(id, alg);
        }

        public String getId() {
            return id;
        }

        public QsealcAlg getAlg() {
            return alg;
        }
    }

    private static final String EIDAS_PROXY_URL =
            "https://eidas-proxy.staging.aggregation.tink.network";

    private final Map<Algorithm, EidasSigningKey> signingKeyMap;
    private final ObjectMapper objectMapper;

    public EidasJwtSigner(Map<Algorithm, EidasSigningKey> signingKeyMap) {
        this.signingKeyMap = signingKeyMap;

        objectMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addSerializer(ClaimsHolder.class, new PayloadSerializer());
        objectMapper.registerModule(module);
        objectMapper.configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true);
    }

    @Override
    public String sign(
            Algorithm algorithm,
            Map<String, Object> extraHeaderClaims,
            Map<String, Object> payloadClaims,
            boolean detachedPayload) {

        final EidasSigningKey key = signingKeyMap.get(algorithm);
        Preconditions.checkNotNull(key, "EidasJwtSigner has no mapping for the given algorithm.");

        final Map<String, Object> headerClaims = new HashMap<>(extraHeaderClaims);
        headerClaims.put("kid", key.getId());
        headerClaims.put("alg", algorithm.toString());

        QsealcSigner signer =
                QsealcSigner.build(
                        EidasProxyConfiguration.createLocal(EIDAS_PROXY_URL).toInternalConfig(),
                        key.getAlg(),
                        new EidasIdentity(
                                "oxford-staging", "5f98e87106384b2981c0354a33b51590", ""));

        final String headerJson = mapToJson(headerClaims);
        final String payloadJson = mapToJson(payloadClaims);
        final String content = formatAsUnsignedJwt(headerJson, payloadJson);

        final String signature =
                Base64.encodeBase64URLSafeString(
                        signer.getSignature(content.getBytes(StandardCharsets.UTF_8)));

        return String.format("%s.%s", content, signature);
    }

    private String mapToJson(final Map<String, Object> map) {

        try {

            return objectMapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    private String formatAsUnsignedJwt(final String headerJson, final String payloadJson) {

        String header =
                Base64.encodeBase64URLSafeString(headerJson.getBytes(StandardCharsets.UTF_8));
        String body =
                Base64.encodeBase64URLSafeString(payloadJson.getBytes(StandardCharsets.UTF_8));

        return String.format("%s.%s", header, body);
    }
}

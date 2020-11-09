package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.jwt;

import com.auth0.jwt.impl.ClaimsHolder;
import com.auth0.jwt.impl.PayloadSerializer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.codec.binary.Base64;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.jwt.signer.iface.JwtSigner;
import se.tink.backend.aggregation.configuration.eidas.InternalEidasProxyConfiguration;
import se.tink.backend.aggregation.eidassigner.QsealcAlg;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;
import se.tink.backend.aggregation.eidassigner.QsealcSignerImpl;
import se.tink.backend.aggregation.eidassigner.identity.EidasIdentity;

/**
 * @deprecated please use {@link
 *     se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.jwt.signer.EidasJwtSigner}
 */
@Deprecated
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

    private final InternalEidasProxyConfiguration eidasProxyConfiguration;
    private final EidasIdentity eidasIdentity;

    private final Map<Algorithm, EidasSigningKey> signingKeyMap;
    private final ObjectMapper objectMapper;

    public EidasJwtSigner(InternalEidasProxyConfiguration configuration, EidasIdentity identity) {
        this.signingKeyMap =
                ImmutableMap.<Algorithm, EidasSigningKey>builder()
                        .put(
                                Algorithm.PS256,
                                EidasSigningKey.of("PSDSE-FINA-44059", QsealcAlg.EIDAS_PSS_SHA256))
                        .put(
                                Algorithm.RS256,
                                EidasSigningKey.of(
                                        "PSDSE-FINA-44059-RSA", QsealcAlg.EIDAS_RSA_SHA256))
                        .build();

        this.eidasProxyConfiguration = configuration;
        this.eidasIdentity = identity;

        this.objectMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addSerializer(ClaimsHolder.class, new PayloadSerializer());
        this.objectMapper.registerModule(module);
        this.objectMapper.configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true);
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
                QsealcSignerImpl.build(eidasProxyConfiguration, key.getAlg(), eidasIdentity);

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

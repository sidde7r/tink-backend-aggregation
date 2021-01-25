package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.jwt.signer;

import com.nimbusds.jose.crypto.RSASSASigner;
import java.lang.invoke.MethodHandles;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.configuration.UkOpenBankingConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.jwt.signer.iface.JwtSigner;
import se.tink.backend.aggregation.agents.utils.crypto.RSA;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;

@RequiredArgsConstructor
public class SecretServiceJwtSigner implements JwtSigner {
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final UkOpenBankingConfiguration configuration;

    @Override
    public String sign(
            Algorithm algorithm,
            Map<String, Object> headerClaims,
            Map<String, Object> payloadClaims,
            boolean detachedPayload) {
        RSASSASigner signer =
                new RSASSASigner(
                        RSA.getPrivateKeyFromBytes(
                                EncodingUtils.decodeBase64String(configuration.getSigningKey())));
        printVersion(RSASSASigner.class);
        return TinkJwtSigner.builder(configuration::getSigningKeyId, signer)
                .withAlgorithm(algorithm)
                .withHeaderClaims(headerClaims)
                .withPayloadClaims(payloadClaims)
                .withDetachedPayload(detachedPayload)
                .build()
                .sign();
    }

    private static void printVersion(Class<?> clazz) {
        Package p = clazz.getPackage();
        logger.info(
                "{} Title={} Version={} Vendor={}",
                clazz.getName(),
                p.getImplementationTitle(),
                p.getImplementationVersion(),
                p.getImplementationVendor());
    }
}

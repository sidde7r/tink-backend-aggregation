package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.signature;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.PolishApiConstants.Logs.LOG_TAG;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.util.Base64URL;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateException;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.apache.commons.codec.binary.Base64;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.PolishApiConfiguration;
import se.tink.backend.aggregation.agents.utils.crypto.hash.Hash;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.configuration.agents.utils.CertificateUtils;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;
import se.tink.libraries.serialization.utils.SerializationUtils;

@UtilityClass
public class PolishApiJwsSignatureProvider {

    @SneakyThrows
    public static String createJwsHeader(
            QsealcSigner signer,
            AgentConfiguration<PolishApiConfiguration> configuration,
            Object toSign) {
        JWSHeader jwsHeader =
                new JWSHeader.Builder(JWSAlgorithm.RS256)
                        .keyID(getKeyId(configuration))
                        .x509CertURL(
                                new URI(
                                        configuration
                                                .getProviderSpecificConfiguration()
                                                .getPemEndpoint()))
                        .x509CertSHA256Thumbprint(
                                Base64URL.encode(
                                        Hash.sha256(getDerEncodedX509Certificate(configuration))))
                        .build();

        String serializedToJsonHeader =
                SerializationUtils.serializeToString(jwsHeader.toJSONObject());
        String serializedToJsonPayload = SerializationUtils.serializeToString(toSign);

        String jwsHeaderEncoded =
                Base64.encodeBase64URLSafeString(
                        serializedToJsonHeader.getBytes(StandardCharsets.UTF_8));

        String payloadEncoded =
                Base64.encodeBase64URLSafeString(
                        serializedToJsonPayload.getBytes(StandardCharsets.UTF_8));

        String jwsHeaderAndPayloadToSign = String.format("%s.%s", jwsHeaderEncoded, payloadEncoded);

        String signedAndEncodedJwsAndPayload =
                Base64.encodeBase64URLSafeString(
                        signer.getSignature(
                                jwsHeaderAndPayloadToSign.getBytes(StandardCharsets.UTF_8)));

        return String.format("%s..%s", jwsHeaderEncoded, signedAndEncodedJwsAndPayload);
    }

    private static String getKeyId(AgentConfiguration<PolishApiConfiguration> configuration) {
        try {
            return CertificateUtils.getOrganizationIdentifier(configuration.getQsealc());
        } catch (CertificateException ce) {
            throw new SecurityException("Certificate error", ce);
        }
    }

    @SneakyThrows
    private static byte[] getDerEncodedX509Certificate(
            AgentConfiguration<PolishApiConfiguration> configuration) {
        try {
            return Base64.decodeBase64(
                    CertificateUtils.getDerEncodedCertFromBase64EncodedCertificate(
                            getQsealc(configuration)));
        } catch (CertificateException ce) {
            throw new SecurityException(LOG_TAG + " Unknown Certificate error", ce);
        }
    }

    private static String getQsealc(AgentConfiguration<PolishApiConfiguration> configuration) {
        return configuration.getQsealc();
    }
}

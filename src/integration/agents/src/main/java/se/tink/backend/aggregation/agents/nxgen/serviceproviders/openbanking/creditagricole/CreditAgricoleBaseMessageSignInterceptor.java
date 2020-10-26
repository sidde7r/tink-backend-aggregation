package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole;

import com.google.common.collect.ImmutableList;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.configuration.CreditAgricoleBaseConfiguration;
import se.tink.backend.aggregation.agents.utils.crypto.hash.Hash;
import se.tink.backend.aggregation.agents.utils.jersey.interceptor.MessageSignInterceptor;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.configuration.agents.utils.CertificateUtils;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;
import se.tink.backend.aggregation.nxgen.http.filter.engine.FilterOrder;
import se.tink.backend.aggregation.nxgen.http.filter.engine.FilterPhases;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.libraries.serialization.utils.SerializationUtils;

@FilterOrder(category = FilterPhases.SECURITY, order = 0)
@RequiredArgsConstructor
public class CreditAgricoleBaseMessageSignInterceptor extends MessageSignInterceptor {

    private static final String NEW_LINE = "\n";
    private static final String COLON_SPACE = ": ";

    private static final List<String> SIGNATURE_HEADERS =
            ImmutableList.of(
                    CreditAgricoleBaseConstants.HeaderKeys.DIGEST,
                    CreditAgricoleBaseConstants.HeaderKeys.AUTHORIZATION,
                    CreditAgricoleBaseConstants.HeaderKeys.X_REQUEST_ID);

    private final AgentConfiguration<CreditAgricoleBaseConfiguration> configuration;
    private final QsealcSigner qsealcSigner;

    @Override
    protected void appendAdditionalHeaders(HttpRequest request) {
        request.getHeaders()
                .add(
                        CreditAgricoleBaseConstants.HeaderKeys.X_REQUEST_ID,
                        UUID.randomUUID().toString());
    }

    @SneakyThrows
    @Override
    protected void getSignatureAndAddAsHeader(HttpRequest request) {
        List<String> serializedHeaders = new ArrayList<>();
        List<String> headersIncludedInSignature = new ArrayList<>();

        for (String key : SIGNATURE_HEADERS) {
            if (request.getHeaders().get(key) != null) {
                headersIncludedInSignature.add(key);
                if (request.getHeaders().get(key).size() > 1) {
                    throw new IllegalArgumentException(
                            "Unable to provide more than one value in signature");
                }
                serializedHeaders.add(
                        serializedHeader(key, request.getHeaders().get(key).get(0).toString()));
            }
        }

        String headersIncludedInSignatureString =
                StringUtils.join(headersIncludedInSignature, StringUtils.SPACE);
        String serializedHeadersString = StringUtils.join(serializedHeaders, NEW_LINE);
        String signatureBase64Sha = signMessage(serializedHeadersString);
        String signature = formSignature(signatureBase64Sha, headersIncludedInSignatureString);
        request.getHeaders().add(CreditAgricoleBaseConstants.HeaderKeys.SIGNATURE, signature);
    }

    private String formSignature(String signatureBase64Sha, String headers)
            throws CertificateException {
        return String.format(
                CreditAgricoleBaseConstants.Formats.SIGNATURE_STRING_FORMAT,
                CertificateUtils.getSerialNumber(configuration.getQsealc(), 16),
                CreditAgricoleBaseConstants.SignatureValues.RSA_SHA256,
                headers,
                signatureBase64Sha);
    }

    @Override
    protected void prepareDigestAndAddAsHeader(HttpRequest request) {
        if (request.getBody() != null) {
            String digest = getDigest(request.getBody());
            request.getHeaders()
                    .add(
                            CreditAgricoleBaseConstants.HeaderKeys.DIGEST,
                            CreditAgricoleBaseConstants.HeaderValues.DIGEST_PREFIX + digest);
        }
    }

    private String serializedHeader(String name, String value) {
        return name.toLowerCase() + COLON_SPACE + value;
    }

    private String signMessage(String toSignString) {
        return qsealcSigner.getSignatureBase64(toSignString.getBytes());
    }

    private String getDigest(Object body) {
        byte[] bytes =
                SerializationUtils.serializeToString(body).getBytes(StandardCharsets.US_ASCII);
        return Hash.sha256Base64(bytes);
    }
}

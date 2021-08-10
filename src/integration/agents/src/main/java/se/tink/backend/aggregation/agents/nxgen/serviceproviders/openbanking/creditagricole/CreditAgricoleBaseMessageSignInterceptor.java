package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole;

import com.google.common.collect.ImmutableList;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.common.signature.CertificateSerialNumberType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.common.signature.QSealSignatureProvider;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.common.signature.QSealSignatureProviderInput;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.configuration.CreditAgricoleBaseConfiguration;
import se.tink.backend.aggregation.agents.utils.crypto.hash.Hash;
import se.tink.backend.aggregation.agents.utils.jersey.interceptor.MessageSignInterceptor;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.nxgen.http.filter.engine.FilterOrder;
import se.tink.backend.aggregation.nxgen.http.filter.engine.FilterPhases;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.libraries.serialization.utils.SerializationUtils;

@FilterOrder(category = FilterPhases.SECURITY, order = 0)
@RequiredArgsConstructor
public class CreditAgricoleBaseMessageSignInterceptor extends MessageSignInterceptor {

    private static final List<String> SIGNATURE_HEADERS =
            ImmutableList.of(
                    CreditAgricoleBaseConstants.HeaderKeys.DIGEST,
                    CreditAgricoleBaseConstants.HeaderKeys.AUTHORIZATION,
                    CreditAgricoleBaseConstants.HeaderKeys.X_REQUEST_ID);

    private final AgentConfiguration<CreditAgricoleBaseConfiguration> configuration;
    private final QSealSignatureProvider qSealSignatureProvider;

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
        QSealSignatureProviderInput signatureProviderInput =
                QSealSignatureProviderInput.builder()
                        .certificateSerialNumberType(CertificateSerialNumberType.HEX)
                        .qseal(configuration.getQsealc())
                        .request(request)
                        .signatureHeaders(SIGNATURE_HEADERS)
                        .build();

        String signature = qSealSignatureProvider.provideSignature(signatureProviderInput);
        request.getHeaders().add(CreditAgricoleBaseConstants.HeaderKeys.SIGNATURE, signature);
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

    private String getDigest(Object body) {
        byte[] bytes =
                SerializationUtils.serializeToString(body).getBytes(StandardCharsets.US_ASCII);
        return Hash.sha256Base64(bytes);
    }
}

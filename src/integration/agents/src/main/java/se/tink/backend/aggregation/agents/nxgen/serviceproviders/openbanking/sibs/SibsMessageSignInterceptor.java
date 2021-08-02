package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs;

import com.google.common.collect.ImmutableList;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import lombok.SneakyThrows;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.common.signature.CertificateSerialNumberType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.common.signature.QSealSignatureProvider;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.common.signature.QSealSignatureProviderInput;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsConstants.Formats;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.configuration.SibsConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.utils.SibsUtils;
import se.tink.backend.aggregation.agents.utils.jersey.interceptor.MessageSignInterceptor;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.configuration.agents.utils.CertificateUtils;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;

public class SibsMessageSignInterceptor extends MessageSignInterceptor {

    private static final List<String> SIGNATURE_HEADERS =
            ImmutableList.of(
                    HeaderKeys.DIGEST,
                    HeaderKeys.TPP_TRANSACTION_ID,
                    HeaderKeys.TPP_REQUEST_ID,
                    HeaderKeys.DATE);

    private final SibsConfiguration sibsConfiguration;
    private final QSealSignatureProvider qSealSignatureProvider;
    private final String qseal;

    public SibsMessageSignInterceptor(
            AgentConfiguration<SibsConfiguration> configuration,
            QSealSignatureProvider qSealSignatureProvider) {
        this.sibsConfiguration = configuration.getProviderSpecificConfiguration();
        this.qseal = configuration.getQsealc();
        this.qSealSignatureProvider = qSealSignatureProvider;
    }

    @Override
    protected void appendAdditionalHeaders(HttpRequest request) {
        if (request.getHeaders().get(HeaderKeys.DATE) == null) {
            String requestTimestamp =
                    new SimpleDateFormat(Formats.CONSENT_BODY_DATE_FORMAT, Locale.ENGLISH)
                            .format(new Date());
            request.getHeaders().add(HeaderKeys.DATE, requestTimestamp);
        }
        request.getHeaders()
                .add(SibsConstants.HeaderKeys.X_IBM_CLIENT_ID, sibsConfiguration.getClientId());
        request.getHeaders()
                .add(SibsConstants.HeaderKeys.TPP_CERTIFICATE, getClientSigningCertificate());
        request.getHeaders()
                .add(SibsConstants.HeaderKeys.TPP_TRANSACTION_ID, SibsUtils.getRequestId());
        request.getHeaders().add(SibsConstants.HeaderKeys.TPP_REQUEST_ID, SibsUtils.getRequestId());
    }

    @Override
    protected void getSignatureAndAddAsHeader(HttpRequest request) {
        QSealSignatureProviderInput signatureProviderInput =
                QSealSignatureProviderInput.builder()
                        .certificateSerialNumberType(CertificateSerialNumberType.HEX)
                        .qseal(qseal)
                        .request(request)
                        .signatureHeaders(SIGNATURE_HEADERS)
                        .build();

        String signature = qSealSignatureProvider.provideSignature(signatureProviderInput);
        request.getHeaders().add(HeaderKeys.SIGNATURE, signature);
    }

    @Override
    protected void prepareDigestAndAddAsHeader(HttpRequest request) {
        if (request.getBody() != null) {
            String digest = SibsUtils.getDigest(request.getBody());

            request.getHeaders()
                    .add(
                            SibsConstants.HeaderKeys.DIGEST,
                            SibsConstants.HeaderValues.DIGEST_PREFIX + digest);
        }
    }

    @SneakyThrows
    private String getClientSigningCertificate() {
        return CertificateUtils.getDerEncodedCertFromBase64EncodedCertificate(qseal);
    }
}

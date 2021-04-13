package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs;

import com.google.common.collect.ImmutableList;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsConstants.Formats;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsConstants.SignatureValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.configuration.SibsConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.utils.SibsUtils;
import se.tink.backend.aggregation.agents.utils.jersey.interceptor.MessageSignInterceptor;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;

public class SibsMessageSignInterceptor extends MessageSignInterceptor {

    private static final String NEW_LINE = "\n";
    private static final String COLON_SPACE = ": ";

    private static final List<String> SIGNATURE_HEADERS =
            ImmutableList.of(
                    HeaderKeys.DIGEST,
                    HeaderKeys.TPP_TRANSACTION_ID,
                    HeaderKeys.TPP_REQUEST_ID,
                    HeaderKeys.DATE);

    private SibsConfiguration configuration;
    private final QsealcSigner qsealcSigner;

    public SibsMessageSignInterceptor(SibsConfiguration configuration, QsealcSigner qsealcSigner) {
        this.configuration = configuration;
        this.qsealcSigner = qsealcSigner;
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
                .add(SibsConstants.HeaderKeys.X_IBM_CLIENT_ID, configuration.getClientId());
        request.getHeaders()
                .add(
                        SibsConstants.HeaderKeys.TPP_CERTIFICATE,
                        configuration.getClientSigningCertificate());
        request.getHeaders()
                .add(SibsConstants.HeaderKeys.TPP_TRANSACTION_ID, SibsUtils.getRequestId());
        request.getHeaders().add(SibsConstants.HeaderKeys.TPP_REQUEST_ID, SibsUtils.getRequestId());
    }

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
        request.getHeaders().add(HeaderKeys.SIGNATURE, signature);
    }

    private String formSignature(String signatureBase64Sha, String headers) {
        return String.format(
                Formats.SIGNATURE_STRING_FORMAT,
                configuration.getClientSigningCertificateSerialNumber(),
                SignatureValues.RSA_SHA256,
                headers,
                signatureBase64Sha);
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

    private String serializedHeader(String name, String value) {
        return name.toLowerCase() + COLON_SPACE + value;
    }

    private String signMessage(String toSignString) {
        return qsealcSigner.getSignatureBase64(toSignString.getBytes());
    }
}

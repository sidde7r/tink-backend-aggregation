package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.creditagricole;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.creditagricole.configuration.CreditAgricoleConfiguration;
import se.tink.backend.aggregation.agents.utils.jersey.MessageSignInterceptor;
import se.tink.backend.aggregation.configuration.EidasProxyConfiguration;
import se.tink.backend.aggregation.eidassigner.EidasIdentity;
import se.tink.backend.aggregation.eidassigner.QsealcAlg;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;
import se.tink.backend.aggregation.nxgen.http.filter.engine.FilterOrder;
import se.tink.backend.aggregation.nxgen.http.filter.engine.FilterPhases;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;

@FilterOrder(category = FilterPhases.SECURITY, order = 0)
public class CreditAgricoleMessageSignInterceptor extends MessageSignInterceptor {

    private static final String NEW_LINE = "\n";
    private static final String COLON_SPACE = ": ";

    public static final List<String> SIGNATURE_HEADERS =
        ImmutableList.of(
            CreditAgricoleConstants.HeaderKeys.DIGEST,
            CreditAgricoleConstants.HeaderKeys.AUTHORIZATION,
            CreditAgricoleConstants.HeaderKeys.X_REQUEST_ID);

    private CreditAgricoleConfiguration configuration;
    protected EidasProxyConfiguration eidasConf;
    private EidasIdentity eidasIdentity;

    public CreditAgricoleMessageSignInterceptor(
        CreditAgricoleConfiguration configuration,
        EidasProxyConfiguration eidasConf,
        EidasIdentity eidasIdentity) {
        this.configuration = configuration;
        this.eidasConf = eidasConf;
        this.eidasIdentity = eidasIdentity;
    }

    @Override
    protected void appendAdditionalHeaders(HttpRequest request) {
//        if (request.getHeaders().get(HeaderKeys.DATE) == null) {
//            String requestTimestamp =
//                new SimpleDateFormat(Formats.CONSENT_BODY_DATE_FORMAT, Locale.ENGLISH)
//                    .format(new Date());
//            request.getHeaders().add(HeaderKeys.DATE, requestTimestamp);
//        }
//        request.getHeaders()
//            .add(SibsConstants.HeaderKeys.X_IBM_CLIENT_ID, configuration.getClientId());
//        request.getHeaders()
//            .add(
//                SibsConstants.HeaderKeys.TPP_CERTIFICATE,
//                configuration.getClientSigningCertificate());
//        request.getHeaders()
//            .add(SibsConstants.HeaderKeys.TPP_TRANSACTION_ID, SibsUtils.getRequestId());
//        request.getHeaders().add(SibsConstants.HeaderKeys.TPP_REQUEST_ID, SibsUtils.getRequestId());

        request.getHeaders().add(CreditAgricoleConstants.HeaderKeys.X_REQUEST_ID, UUID.randomUUID().toString());
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
        request.getHeaders().add(CreditAgricoleConstants.HeaderKeys.SIGNATURE, signature);
    }

    private String formSignature(String signatureBase64Sha, String headers) {
        return String.format(
            CreditAgricoleConstants.Formats.SIGNATURE_STRING_FORMAT,
            configuration.getClientSigningCertificateSerialNumber(),
            CreditAgricoleConstants.SignatureValues.RSA_SHA256,
            headers,
            signatureBase64Sha);
    }

    @Override
    protected void prepareDigestAndAddAsHeader(HttpRequest request) {
        if (request.getBody() != null) {
            String digest = CreditAgricoleSignatureUtils.getDigest(request.getBody());

            request.getHeaders()
                .add(
                    CreditAgricoleConstants.HeaderKeys.DIGEST,
                    CreditAgricoleConstants.HeaderValues.DIGEST_PREFIX + digest);
        }
    }

    private String serializedHeader(String name, String value) {
        return name.toLowerCase() + COLON_SPACE + value;
    }

    private String signMessage(String toSignString) {
        QsealcSigner signer =
            QsealcSigner.build(
                eidasConf.toInternalConfig(),
                QsealcAlg.EIDAS_RSA_SHA256,
                eidasIdentity,
                configuration.getCertificateId());
        return signer.getSignatureBase64(toSignString.getBytes());
    }

}

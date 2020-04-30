package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.configuration.CreditAgricoleBaseConfiguration;
import se.tink.backend.aggregation.agents.utils.jersey.interceptor.MessageSignInterceptor;
import se.tink.backend.aggregation.configuration.eidas.proxy.EidasProxyConfiguration;
import se.tink.backend.aggregation.eidassigner.QsealcAlg;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;
import se.tink.backend.aggregation.eidassigner.QsealcSignerImpl;
import se.tink.backend.aggregation.eidassigner.identity.EidasIdentity;
import se.tink.backend.aggregation.nxgen.http.filter.engine.FilterOrder;
import se.tink.backend.aggregation.nxgen.http.filter.engine.FilterPhases;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;

@FilterOrder(category = FilterPhases.SECURITY, order = 0)
public class CreditAgricoleBaseMessageSignInterceptor extends MessageSignInterceptor {

    private static final String NEW_LINE = "\n";
    private static final String COLON_SPACE = ": ";

    public static final List<String> SIGNATURE_HEADERS =
            ImmutableList.of(
                    CreditAgricoleBaseConstants.HeaderKeys.DIGEST,
                    CreditAgricoleBaseConstants.HeaderKeys.AUTHORIZATION,
                    CreditAgricoleBaseConstants.HeaderKeys.X_REQUEST_ID);

    private CreditAgricoleBaseConfiguration configuration;
    private EidasProxyConfiguration eidasConf;
    private EidasIdentity eidasIdentity;

    public CreditAgricoleBaseMessageSignInterceptor(
            CreditAgricoleBaseConfiguration configuration,
            EidasProxyConfiguration eidasConf,
            EidasIdentity eidasIdentity) {
        this.configuration = configuration;
        this.eidasConf = eidasConf;
        this.eidasIdentity = eidasIdentity;
    }

    @Override
    protected void appendAdditionalHeaders(HttpRequest request) {
        request.getHeaders()
                .add(
                        CreditAgricoleBaseConstants.HeaderKeys.X_REQUEST_ID,
                        UUID.randomUUID().toString());
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
        request.getHeaders().add(CreditAgricoleBaseConstants.HeaderKeys.SIGNATURE, signature);
    }

    private String formSignature(String signatureBase64Sha, String headers) {
        return String.format(
                CreditAgricoleBaseConstants.Formats.SIGNATURE_STRING_FORMAT,
                configuration.getClientSigningCertificateSerialNumber(),
                CreditAgricoleBaseConstants.SignatureValues.RSA_SHA256,
                headers,
                signatureBase64Sha);
    }

    @Override
    protected void prepareDigestAndAddAsHeader(HttpRequest request) {
        if (request.getBody() != null) {
            String digest = CreditAgricoleBaseSignatureUtils.getDigest(request.getBody());

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
        QsealcSigner signer =
                QsealcSignerImpl.build(
                        eidasConf.toInternalConfig(),
                        QsealcAlg.EIDAS_RSA_SHA256,
                        eidasIdentity,
                        configuration.getCertificateId());
        return signer.getSignatureBase64(toSignString.getBytes());
    }
}

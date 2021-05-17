package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.RedsysConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.RedsysConstants.Signature;
import se.tink.backend.aggregation.configuration.eidas.proxy.EidasProxyConfiguration;
import se.tink.backend.aggregation.eidasidentity.identity.EidasIdentity;
import se.tink.backend.aggregation.eidassigner.QsealcAlg;
import se.tink.backend.aggregation.eidassigner.QsealcSignerImpl;

public class RedsysUtils {
    private static String signPayload(
            EidasProxyConfiguration proxyConfiguration,
            EidasIdentity eidasIdentity,
            String payload) {
        return QsealcSignerImpl.build(
                        proxyConfiguration.toInternalConfig(),
                        QsealcAlg.EIDAS_RSA_SHA256,
                        eidasIdentity)
                .getSignatureBase64(payload.getBytes());
    }

    private static final String[] SIGN_HEADERS = {
        HeaderKeys.DIGEST, HeaderKeys.REQUEST_ID, HeaderKeys.TPP_REDIRECT_URI
    };

    public static String generateRequestSignature(
            String keyId,
            EidasProxyConfiguration eidasProxyConfiguration,
            EidasIdentity eidasIdentity,
            Map<String, Object> headers) {
        ArrayList<String> signedHeaders = Lists.newArrayList();
        ArrayList<String> payloadElements = Lists.newArrayList();
        for (String header : SIGN_HEADERS) {
            if (headers.containsKey(header)) {
                signedHeaders.add(header);
                payloadElements.add(
                        String.format(
                                "%s: %s",
                                header.toLowerCase(Locale.ENGLISH),
                                headers.get(header).toString()));
            }
        }

        final String payloadToSign = Joiner.on("\n").join(payloadElements);
        final String headerList = Joiner.on(" ").join(signedHeaders).toLowerCase(Locale.ENGLISH);
        final String signature = signPayload(eidasProxyConfiguration, eidasIdentity, payloadToSign);
        return String.format(Signature.FORMAT, keyId, headerList, signature);
    }
}

package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.signature;

import lombok.RequiredArgsConstructor;
import org.apache.http.HttpHeaders;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.BpceGroupHttpHeaders;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.configuration.BpceGroupConfiguration;
import se.tink.backend.aggregation.configuration.eidas.proxy.EidasProxyConfiguration;
import se.tink.backend.aggregation.eidassigner.QsealcAlg;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;
import se.tink.backend.aggregation.eidassigner.identity.EidasIdentity;

@RequiredArgsConstructor
public class BpceGroupSignatureHeaderGenerator {

    private static final String ALGORITHM = "algorithm=\"rsa-sha256\"";

    private final EidasProxyConfiguration eidasProxyConfiguration;
    private final EidasIdentity eidasIdentity;
    private final BpceGroupConfiguration bpceGroupConfiguration;

    public String buildSignatureHeader(String authorizationCode, String requestId) {
        return String.format(
                "%s,%s,%s,%s",
                getKeyId(), ALGORITHM, getHeaders(), getSignature(authorizationCode, requestId));
    }

    private String getKeyId() {
        return String.format("keyId=\"%s\"", bpceGroupConfiguration.getKeyId());
    }

    private String getHeaders() {
        return String.format(
                "headers=\"%s %s\"",
                HttpHeaders.AUTHORIZATION, BpceGroupHttpHeaders.X_REQUEST_ID.getName());
    }

    private String getSignature(String authorizationCode, String requestId) {
        final String signatureString =
                String.format(
                        "%s: %s%s%s: %s",
                        HttpHeaders.AUTHORIZATION,
                        authorizationCode,
                        System.lineSeparator(),
                        BpceGroupHttpHeaders.X_REQUEST_ID.getName(),
                        requestId);

        return String.format(
                "signature=\"%s\"",
                QsealcSigner.build(
                                eidasProxyConfiguration.toInternalConfig(),
                                QsealcAlg.EIDAS_RSA_SHA256,
                                eidasIdentity,
                                bpceGroupConfiguration.getEidasQwac())
                        .getSignatureBase64(signatureString.getBytes()));
    }
}

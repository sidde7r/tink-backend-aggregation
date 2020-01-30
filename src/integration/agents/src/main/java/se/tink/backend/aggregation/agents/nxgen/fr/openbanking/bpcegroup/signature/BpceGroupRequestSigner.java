package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.signature;

import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.configuration.eidas.proxy.EidasProxyConfiguration;
import se.tink.backend.aggregation.eidassigner.QsealcAlg;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;
import se.tink.backend.aggregation.eidassigner.identity.EidasIdentity;

@RequiredArgsConstructor
public class BpceGroupRequestSigner {

    private final EidasProxyConfiguration eidasProxyConfiguration;
    private final EidasIdentity eidasIdentity;

    String getSignature(String signatureString) {
        return QsealcSigner.build(
                        eidasProxyConfiguration.toInternalConfig(),
                        QsealcAlg.EIDAS_RSA_SHA256,
                        eidasIdentity)
                .getSignatureBase64(signatureString.getBytes());
    }
}

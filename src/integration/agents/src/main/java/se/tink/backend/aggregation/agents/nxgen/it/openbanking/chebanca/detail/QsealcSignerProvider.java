package se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.detail;

import se.tink.backend.aggregation.configuration.eidas.proxy.EidasProxyConfiguration;
import se.tink.backend.aggregation.eidassigner.QsealcAlg;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;
import se.tink.backend.aggregation.eidassigner.QsealcSignerImpl;
import se.tink.backend.aggregation.eidassigner.identity.EidasIdentity;

public class QsealcSignerProvider {

    public static QsealcSigner getQsealcSigner(
            EidasProxyConfiguration eidasProxyConfig,
            EidasIdentity eidasIdentity,
            String certificateId) {
        return QsealcSignerImpl.build(
                eidasProxyConfig.toInternalConfig(),
                QsealcAlg.EIDAS_RSA_SHA256,
                eidasIdentity,
                certificateId);
    }
}

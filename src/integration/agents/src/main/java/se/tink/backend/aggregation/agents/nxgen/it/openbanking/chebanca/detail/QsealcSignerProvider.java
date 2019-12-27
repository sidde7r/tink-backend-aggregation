package se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.detail;

import se.tink.backend.aggregation.configuration.EidasProxyConfiguration;
import se.tink.backend.aggregation.eidassigner.EidasIdentity;
import se.tink.backend.aggregation.eidassigner.QsealcAlg;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;

public class QsealcSignerProvider {

    public static QsealcSigner getQsealcSigner(
            EidasProxyConfiguration eidasProxyConfig,
            EidasIdentity eidasIdentity,
            String certificateId) {
        return QsealcSigner.build(
                eidasProxyConfig.toInternalConfig(),
                QsealcAlg.EIDAS_RSA_SHA256,
                eidasIdentity,
                certificateId);
    }
}

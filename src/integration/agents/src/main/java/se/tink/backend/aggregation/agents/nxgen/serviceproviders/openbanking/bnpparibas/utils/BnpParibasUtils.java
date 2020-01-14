package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.utils;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.BnpParibasBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.configuration.BnpParibasConfiguration;
import se.tink.backend.aggregation.configuration.eidas.proxy.EidasProxyConfiguration;
import se.tink.backend.aggregation.eidassigner.QsealcAlg;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;
import se.tink.backend.aggregation.eidassigner.identity.EidasIdentity;

public class BnpParibasUtils {

    public static String getAlgorithm() {
        return String.format(
                "%s=\"%s\"",
                BnpParibasBaseConstants.SignatureKeys.ALGORITHM,
                BnpParibasBaseConstants.SignatureKeys.RSA_256);
    }

    public static String getSignature(
            EidasProxyConfiguration configuration,
            BnpParibasConfiguration bnpParibasConfiguration,
            EidasIdentity eidasIdentity) {

        return String.format(
                "%s=\"%s\"",
                BnpParibasBaseConstants.SignatureKeys.SIGNATURE,
                QsealcSigner.build(
                                configuration.toInternalConfig(),
                                QsealcAlg.EIDAS_RSA_SHA256,
                                eidasIdentity,
                                bnpParibasConfiguration.getEidasQwac())
                        .getSignatureBase64("".getBytes()));
    }
}

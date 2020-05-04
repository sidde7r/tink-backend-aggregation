package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.utils;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.BnpParibasBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.configuration.BnpParibasConfiguration;
import se.tink.backend.aggregation.configuration.eidas.proxy.EidasProxyConfiguration;
import se.tink.backend.aggregation.eidassigner.QsealcAlg;
import se.tink.backend.aggregation.eidassigner.QsealcSignerImpl;
import se.tink.backend.aggregation.eidassigner.identity.EidasIdentity;

public class BnpParibasSignatureHeaderProvider {

    public String buildSignatureHeader(
            EidasProxyConfiguration configuration,
            EidasIdentity eidasIdentity,
            String authorizationCode,
            String requestId,
            BnpParibasConfiguration bnpParibasConfiguration) {
        return String.format(
                "%s, %s, %s, %s",
                getKeyId(bnpParibasConfiguration),
                BnpParibasUtils.getAlgorithm(),
                getHeaders(),
                getSignature(
                        configuration,
                        eidasIdentity,
                        authorizationCode,
                        requestId,
                        bnpParibasConfiguration));
    }

    private String getKeyId(BnpParibasConfiguration bnpParibasConfiguration) {
        return String.format(
                "%s=\"%s\"",
                BnpParibasBaseConstants.SignatureKeys.KEY_ID, bnpParibasConfiguration.getKeyId());
    }

    private String getHeaders() {

        return String.format(
                "%s=\"%s %s\"",
                BnpParibasBaseConstants.SignatureKeys.headers,
                BnpParibasBaseConstants.SignatureKeys.AUTHORIZATION,
                BnpParibasBaseConstants.SignatureKeys.X_REQUEST_ID);
    }

    private String getSignature(
            EidasProxyConfiguration configuration,
            EidasIdentity eidasIdentity,
            String authorizationCode,
            String requestId,
            BnpParibasConfiguration bnpParibasConfiguration) {

        String signatureString =
                String.format(
                        "%s: %s%s%s: %s",
                        BnpParibasBaseConstants.SignatureKeys.AUTHORIZATION,
                        authorizationCode,
                        System.lineSeparator(),
                        BnpParibasBaseConstants.SignatureKeys.X_REQUEST_ID,
                        requestId);

        return String.format(
                "%s=\"%s\"",
                BnpParibasBaseConstants.SignatureKeys.SIGNATURE,
                QsealcSignerImpl.build(
                                configuration.toInternalConfig(),
                                QsealcAlg.EIDAS_RSA_SHA256,
                                eidasIdentity)
                        .getSignatureBase64(signatureString.getBytes()));
    }
}

package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.utils;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.BnpParibasBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.configuration.BnpParibasConfiguration;
import se.tink.backend.aggregation.configuration.EidasProxyConfiguration;
import se.tink.backend.aggregation.eidassigner.EidasIdentity;
import se.tink.backend.aggregation.eidassigner.QsealcAlg;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;

public class BnpParibasUtils {

    public static String buildSignatureHeader(
            EidasProxyConfiguration configuration,
            EidasIdentity eidasIdentity,
            String authorizationCode,
            String requestId,
            BnpParibasConfiguration bnpParibasConfiguration) {
        return String.format(
                "%s, %s, %s, %s",
                getKeyId(bnpParibasConfiguration),
                getAlgorithm(),
                getHeaders(),
                getSignature(
                        configuration,
                        eidasIdentity,
                        authorizationCode,
                        requestId,
                        bnpParibasConfiguration));
    }

    private static String getKeyId(BnpParibasConfiguration bnpParibasConfiguration) {
        return String.format(
                "%s=\"%s\"",
                BnpParibasBaseConstants.SignatureKeys.KEY_ID, bnpParibasConfiguration.getKeyId());
    }

    public static String getAlgorithm() {
        return String.format(
                "%s=\"%s\"",
                BnpParibasBaseConstants.SignatureKeys.ALGORITHM,
                BnpParibasBaseConstants.SignatureKeys.RSA_256);
    }

    private static String getHeaders() {

        return String.format(
                "%s=\"%s %s\"",
                BnpParibasBaseConstants.SignatureKeys.headers,
                BnpParibasBaseConstants.SignatureKeys.AUTHORIZATION,
                BnpParibasBaseConstants.SignatureKeys.X_REQUEST_ID);
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

    private static String getSignature(
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
                QsealcSigner.build(
                                configuration.toInternalConfig(),
                                QsealcAlg.EIDAS_RSA_SHA256,
                                eidasIdentity,
                                bnpParibasConfiguration.getEidasQwac())
                        .getSignatureBase64(signatureString.getBytes()));
    }
}

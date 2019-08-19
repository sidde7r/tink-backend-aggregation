package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.utils;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.BnpParibasBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.configuration.BnpParibasConfiguration;
import se.tink.backend.aggregation.configuration.EidasProxyConfiguration;
import se.tink.backend.aggregation.eidas.EidasProxyConstants;
import se.tink.backend.aggregation.eidas.QsealcEidasProxySigner;

public class BnpParibasUtils {

    public static String buildSignatureHeader(
            EidasProxyConfiguration configuration,
            String authorizationCode,
            String requestId,
            BnpParibasConfiguration bnpParibasConfiguration) {
        return String.format(
                "%s, %s, %s, %s",
                getKeyId(bnpParibasConfiguration),
                getAlgorithm(),
                getHeaders(),
                getSignature(configuration, authorizationCode, requestId, bnpParibasConfiguration));
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
            BnpParibasConfiguration bnpParibasConfiguration) {

        return String.format(
                "%s=\"%s\"",
                BnpParibasBaseConstants.SignatureKeys.SIGNATURE,
                new QsealcEidasProxySigner(
                                configuration,
                                bnpParibasConfiguration.getEidasQwac(),
                                EidasProxyConstants.Algorithm.EIDAS_RSA_SHA256)
                        .getSignatureBase64("".getBytes()));
    }

    private static String getSignature(
            EidasProxyConfiguration configuration,
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
                new QsealcEidasProxySigner(
                                configuration,
                                bnpParibasConfiguration.getEidasQwac(),
                                EidasProxyConstants.Algorithm.EIDAS_RSA_SHA256)
                        .getSignatureBase64(signatureString.getBytes()));
    }
}

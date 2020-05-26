package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.utils;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.BnpParibasBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.configuration.BnpParibasConfiguration;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;

public class BnpParibasSignatureHeaderProvider {

    private final QsealcSigner qsealcSigner;

    public BnpParibasSignatureHeaderProvider(QsealcSigner qsealcSigner) {
        this.qsealcSigner = qsealcSigner;
    }

    public String buildSignatureHeader(
            String authorizationCode,
            String requestId,
            BnpParibasConfiguration bnpParibasConfiguration) {
        return String.format(
                "%s, %s, %s, %s",
                getKeyId(bnpParibasConfiguration),
                BnpParibasUtils.getAlgorithm(),
                getHeaders(),
                getSignature(authorizationCode, requestId));
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

    private String getSignature(String authorizationCode, String requestId) {

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
                qsealcSigner.getSignatureBase64(signatureString.getBytes()));
    }
}

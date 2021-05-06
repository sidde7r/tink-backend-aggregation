package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.utils;

import java.security.cert.CertificateException;
import lombok.SneakyThrows;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.BnpParibasBaseConstants;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.configuration.agents.utils.CertificateUtils;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;

public class BnpParibasSignatureHeaderProvider {

    private final QsealcSigner qsealcSigner;

    public BnpParibasSignatureHeaderProvider(QsealcSigner qsealcSigner) {
        this.qsealcSigner = qsealcSigner;
    }

    @SneakyThrows
    public String buildSignatureHeader(
            String authorizationCode, String requestId, AgentConfiguration agentConfiguration) {
        return String.format(
                "%s, %s, %s, %s",
                getKeyId(agentConfiguration),
                BnpParibasUtils.getAlgorithm(),
                getHeaders(),
                getSignature(authorizationCode, requestId));
    }

    private String getKeyId(AgentConfiguration agentConfiguration) throws CertificateException {
        return String.format(
                "%s=\"%s\"",
                BnpParibasBaseConstants.SignatureKeys.KEY_ID,
                CertificateUtils.getCertificateIssuerDN(agentConfiguration.getQsealc()));
    }

    private String getHeaders() {

        return String.format(
                "%s=\"%s %s\"",
                BnpParibasBaseConstants.SignatureKeys.HEADERS,
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

package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.utils;

import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.SocieteGeneraleConstants;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.configuration.SocieteGeneraleConfiguration;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;

@RequiredArgsConstructor
public class SignatureHeaderProvider {

    private final QsealcSigner qsealcSigner;
    private final SocieteGeneraleConfiguration societeGeneraleConfiguration;

    public String buildSignatureHeader(String authorizationCode, String requestId) {
        return String.format(
                "%s, %s, %s, %s",
                getKeyId(),
                SocieteGeneraleSignatureUtils.getAlgorithm(),
                getHeaders(),
                getSignature(authorizationCode, requestId));
    }

    private String getKeyId() {
        return String.format(
                "%s=\"%s\"",
                SocieteGeneraleConstants.SignatureKeys.KEY_ID,
                societeGeneraleConfiguration.getKeyId());
    }

    private String getHeaders() {
        return String.format(
                "%s=\"%s %s\"",
                SocieteGeneraleConstants.SignatureKeys.HEADERS,
                SocieteGeneraleConstants.HeaderKeys.AUTHORIZATION,
                SocieteGeneraleConstants.HeaderKeys.X_REQUEST_ID);
    }

    private String getSignature(String authorizationCode, String requestId) {

        String signatureString =
                String.format(
                        "%s: %s%s%s: %s",
                        SocieteGeneraleConstants.HeaderKeys.AUTHORIZATION,
                        authorizationCode,
                        System.lineSeparator(),
                        SocieteGeneraleConstants.HeaderKeys.X_REQUEST_ID,
                        requestId);

        return String.format(
                "%s=\"%s\"",
                SocieteGeneraleConstants.HeaderKeys.SIGNATURE,
                qsealcSigner.getSignatureBase64(signatureString.getBytes()));
    }
}

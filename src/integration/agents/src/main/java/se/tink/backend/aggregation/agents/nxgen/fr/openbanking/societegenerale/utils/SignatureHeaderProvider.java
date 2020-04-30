package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.utils;

import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.SocieteGeneraleConstants;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.configuration.SocieteGeneraleConfiguration;
import se.tink.backend.aggregation.configuration.eidas.proxy.EidasProxyConfiguration;
import se.tink.backend.aggregation.eidassigner.QsealcAlg;
import se.tink.backend.aggregation.eidassigner.QsealcSignerImpl;
import se.tink.backend.aggregation.eidassigner.identity.EidasIdentity;

public class SignatureHeaderProvider {

    public String buildSignatureHeader(
            EidasProxyConfiguration configuration,
            EidasIdentity eidasIdentity,
            String authorizationCode,
            String requestId,
            SocieteGeneraleConfiguration societeGeneraleConfiguration) {
        return String.format(
                "%s, %s, %s, %s",
                getKeyId(societeGeneraleConfiguration),
                SocieteGeneraleSignatureUtils.getAlgorithm(),
                getHeaders(),
                getSignature(
                        configuration,
                        eidasIdentity,
                        authorizationCode,
                        requestId,
                        societeGeneraleConfiguration));
    }

    private String getKeyId(SocieteGeneraleConfiguration societeGeneraleConfiguration) {
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

    private String getSignature(
            EidasProxyConfiguration configuration,
            EidasIdentity eidasIdentity,
            String authorizationCode,
            String requestId,
            SocieteGeneraleConfiguration societeGeneraleConfiguration) {

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
                QsealcSignerImpl.build(
                                configuration.toInternalConfig(),
                                QsealcAlg.EIDAS_RSA_SHA256,
                                eidasIdentity,
                                societeGeneraleConfiguration.getEidasQwac())
                        .getSignatureBase64(signatureString.getBytes()));
    }
}

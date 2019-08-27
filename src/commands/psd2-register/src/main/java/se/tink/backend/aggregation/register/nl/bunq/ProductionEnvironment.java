package se.tink.backend.aggregation.register.nl.bunq;

import java.security.PublicKey;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;
import se.tink.backend.aggregation.configuration.EidasProxyConfiguration;
import se.tink.backend.aggregation.eidassigner.EidasIdentity;
import se.tink.backend.aggregation.eidassigner.QsealcAlg;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;
import se.tink.backend.aggregation.register.nl.bunq.rpc.RegisterAsPSD2ProviderRequest;

public final class ProductionEnvironment implements Environment {

    private final String qsealcPath;
    private final String qsealcChainPath;
    private final String certificateId;
    private final String clusterId;
    private final String appId;

    ProductionEnvironment(
            final String qsealcPath,
            final String qsealcChainPath,
            final String clusterId,
            final String appId,
            final String certificateId) {
        this.qsealcPath = qsealcPath;
        this.qsealcChainPath = qsealcChainPath;
        this.clusterId = clusterId;
        this.appId = appId;
        this.certificateId = certificateId;
    }

    @Override
    public RegisterAsPSD2ProviderRequest createRegisterRequest(
            final PublicKey installationPublicKey, final String psd2ClientAuthToken) {

        final String qsealc = BunqRegisterUtils.readFileContents(qsealcPath);
        final String qsealcChain = BunqRegisterUtils.readFileContents(qsealcChainPath);
        final String signature =
                getClientPublicKeySignatureAsStringWithProxy(
                        installationPublicKey, psd2ClientAuthToken);
        return new RegisterAsPSD2ProviderRequest(qsealc, qsealcChain, signature);
    }

    private String getClientPublicKeySignatureAsStringWithProxy(
            final PublicKey publicKey, final String token) {

        String clientPublicKeySignatureString = BunqRegisterUtils.keyToPem(publicKey) + token;

        final EidasProxyConfiguration proxyConfig =
                EidasProxyConfiguration.createLocal(BunqRegisterConstants.Urls.EIDAS_PROXY_URL);

        EidasIdentity eidasIdentity =
                new EidasIdentity(clusterId, appId, BunqRegisterCommand.class);

        byte[] signedClientPublicKeySignature =
                QsealcSigner.build(
                                proxyConfig.toInternalConfig(),
                                QsealcAlg.EIDAS_RSA_SHA256,
                                eidasIdentity,
                                certificateId)
                        .getSignature(clientPublicKeySignatureString.getBytes());

        return EncodingUtils.encodeAsBase64String(signedClientPublicKeySignature);
    }
}

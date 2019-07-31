package se.tink.backend.aggregation.register.nl.bunq;

import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;
import se.tink.backend.aggregation.configuration.EidasProxyConfiguration;
import se.tink.backend.aggregation.eidas.EidasProxyConstants.Algorithm;
import se.tink.backend.aggregation.eidas.QsealcEidasProxySigner;
import se.tink.backend.aggregation.eidas.Signer;
import se.tink.backend.aggregation.register.nl.bunq.rpc.RegisterAsPSD2ProviderRequest;

public final class ProductionEnvironment implements Environment {

    private final String qsealcPath;
    private final String qsealcChainPath;
    private final String certificateId;

    ProductionEnvironment(
            final String qsealcPath, final String qsealcChainPath, final String certificateId) {
        this.qsealcPath = qsealcPath;
        this.qsealcChainPath = qsealcChainPath;
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

        byte[] clientPublicKeySignature =
                clientPublicKeySignatureString.getBytes(StandardCharsets.UTF_8);

        final EidasProxyConfiguration proxyConfig =
                new EidasProxyConfiguration(BunqRegisterConstants.Urls.EIDAS_PROXY_URL, true);
        final Signer signer =
                new QsealcEidasProxySigner(proxyConfig, certificateId, Algorithm.EIDAS_RSA_SHA256);

        byte[] signedClientPublicKeySignature = signer.getSignature(clientPublicKeySignature);

        return EncodingUtils.encodeAsBase64String(signedClientPublicKeySignature);
    }
}

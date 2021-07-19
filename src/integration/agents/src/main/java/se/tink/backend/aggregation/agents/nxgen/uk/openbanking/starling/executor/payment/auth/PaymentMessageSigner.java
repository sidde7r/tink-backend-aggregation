package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.executor.payment.auth;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.util.Base64;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.secrets.StarlingSecrets;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;

public class PaymentMessageSigner {

    private final String keyUuId;
    private final PrivateKey privateKey;

    public PaymentMessageSigner(AgentConfiguration<StarlingSecrets> agentConfiguration) {
        StarlingSecrets starlingSecrets = agentConfiguration.getProviderSpecificConfiguration();
        this.keyUuId = starlingSecrets.getPaymentKeyUuid();
        this.privateKey =
                getPrivateKeyFromP12WithPassword(
                        getSigningKeyP12bytes(starlingSecrets.getPaymentSigningKeyP12()),
                        starlingSecrets.getPaymentSigningKeyPassword());
    }

    public String sign(byte[] input) {
        if (privateKey == null) {
            throw new IllegalStateException("Signer failed to init");
        }
        try {
            Signature privateSignature = Signature.getInstance("SHA256withRSA");
            privateSignature.initSign(privateKey);
            privateSignature.update(input);
            byte[] signature = privateSignature.sign();
            return Base64.getEncoder().encodeToString(signature);
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            throw new IllegalStateException("Signer failed to sign");
        }
    }

    public String getKeyUuId() {
        return this.keyUuId;
    }

    private byte[] getSigningKeyP12bytes(String encoded) {
        return Base64.getDecoder().decode(encoded.getBytes());
    }

    private PrivateKey getPrivateKeyFromP12WithPassword(byte[] p12, String password) {
        try {
            InputStream stream = new ByteArrayInputStream(p12);
            KeyStore ks = KeyStore.getInstance("PKCS12");
            ks.load(stream, password.toCharArray());
            return (PrivateKey) ks.getKey(ks.aliases().nextElement(), password.toCharArray());
        } catch (Exception e) {
            return null;
        }
    }
}

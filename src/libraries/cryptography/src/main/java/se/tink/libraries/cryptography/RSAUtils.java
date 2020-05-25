package se.tink.libraries.cryptography;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.Signature;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;

/** @deprecated Use se.tink.backend.aggregation.agents.utils.crypto.RSA instead */
public class RSAUtils {
    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    private static String DEFAULT_SIGNATURE_ALGORITHM = "SHA512withRSA";

    /**
     * Read a private key file from specified path. File should be in format generated by `openssl
     * genrsa -out private_key.pem 4096`
     */
    public static RSAPrivateKey getPrivateKey(String keyPath) {
        try (PEMParser pemReader =
                new PEMParser(new InputStreamReader(new FileInputStream(keyPath)))) {
            PEMKeyPair keyPair = (PEMKeyPair) pemReader.readObject();
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");
            return (RSAPrivateKey) converter.getKeyPair(keyPair).getPrivate();
        } catch (IOException e) {
            throw new RuntimeException("Could not convert key path into valid private key.", e);
        }
    }

    /** Read a public key file from specified path. */
    public static RSAPublicKey getPublicKey(String keyPath) {
        try (PEMParser pemReader =
                new PEMParser(new InputStreamReader(new FileInputStream(keyPath)))) {
            SubjectPublicKeyInfo publicKeyInfo = (SubjectPublicKeyInfo) pemReader.readObject();
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");
            return (RSAPublicKey) converter.getPublicKey(publicKeyInfo);
        } catch (IOException e) {
            throw new RuntimeException("Could not convert key path into valid public key.", e);
        }
    }

    /** Return a signature for signing using Tinks default signing algorithm. */
    public static Signature getSignature(PrivateKey key)
            throws NoSuchAlgorithmException, InvalidKeyException {
        Signature signature = Signature.getInstance(DEFAULT_SIGNATURE_ALGORITHM);
        signature.initSign(key);
        return signature;
    }

    /** Return a signature for verification using Tinks default signing algorithm. */
    public static Signature getSignature(PublicKey key)
            throws NoSuchAlgorithmException, InvalidKeyException {
        Signature signature = Signature.getInstance(DEFAULT_SIGNATURE_ALGORITHM);
        signature.initVerify(key);
        return signature;
    }
}

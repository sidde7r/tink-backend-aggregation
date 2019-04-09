package se.tink.libraries.cryptography;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.Signature;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.util.encoders.DecoderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ECDSAUtils {
    private static final Logger log = LoggerFactory.getLogger(ECDSAUtils.class);

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    private static final String DEFAULT_SIGNATURE_ALGORITHM = "SHA512withECDSA";

    public static ECPrivateKey getPrivateKey(Reader keyReader) throws IOException {
        PEMParser pemReader = new PEMParser(keyReader);
        JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");

        Object privateKeyObject = pemReader.readObject();
        if (privateKeyObject instanceof PrivateKeyInfo) {
            return (ECPrivateKey) converter.getPrivateKey((PrivateKeyInfo) privateKeyObject);
        } else {
            return (ECPrivateKey) converter.getKeyPair((PEMKeyPair) privateKeyObject).getPrivate();
        }
    }

    public static ECPrivateKey getPrivateKey(String key) throws IOException {
        return ECDSAUtils.getPrivateKey(new StringReader(key));
    }

    public static ECPublicKey getPublicKey(Reader keyReader) throws IOException {
        PEMParser pemReader = new PEMParser(keyReader);

        SubjectPublicKeyInfo publicKeyInfo = (SubjectPublicKeyInfo) pemReader.readObject();

        JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");

        return (ECPublicKey) converter.getPublicKey(publicKeyInfo);
    }

    public static ECPublicKey getPublicKey(String key) throws IOException {
        return ECDSAUtils.getPublicKey(new StringReader(key));
    }

    public static ECPrivateKey getPrivateKeyByPath(String path) {
        try (PEMParser pemReader =
                new PEMParser(new InputStreamReader(new FileInputStream(path)))) {

            PEMKeyPair keyPair = (PEMKeyPair) pemReader.readObject();

            JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");
            return (ECPrivateKey) converter.getKeyPair(keyPair).getPrivate();
        } catch (IOException e) {
            log.error("Could not convert key path into valid private key.");
            throw new RuntimeException("Could not convert key path into valid private key.", e);
        }
    }

    public static ECPublicKey getPublicKeyByPath(String keyPath) {
        try (PEMParser pemReader =
                new PEMParser(new InputStreamReader(new FileInputStream(keyPath)))) {

            SubjectPublicKeyInfo publicKeyInfo = (SubjectPublicKeyInfo) pemReader.readObject();

            JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");

            return (ECPublicKey) converter.getPublicKey(publicKeyInfo);
        } catch (IOException e) {
            log.error("Could not convert key path into valid public key.");
            throw new RuntimeException("Could not convert key path into valid public key.", e);
        }
    }

    public static boolean isValidPublicKey(String key) {
        try {
            getPublicKey(key);
            return true;
        } catch (DecoderException | IOException e) {
            return false;
        }
    }

    public static Signature getSignature(PrivateKey key)
            throws NoSuchAlgorithmException, InvalidKeyException {
        Signature signature = Signature.getInstance(DEFAULT_SIGNATURE_ALGORITHM);

        signature.initSign(key);

        return signature;
    }

    public static Signature getSignature(PublicKey key)
            throws NoSuchAlgorithmException, InvalidKeyException {
        Signature signature = Signature.getInstance(DEFAULT_SIGNATURE_ALGORITHM);

        signature.initVerify(key);

        return signature;
    }
}

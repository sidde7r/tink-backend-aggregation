package se.tink.libraries.cryptography;

import java.io.IOException;
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

public class ECDSAUtils {

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

package se.tink.sa.framework.tools.factory;

import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import se.tink.sa.framework.tools.EncryptionCertificateTool;
import se.tink.sa.framework.tools.impl.LocalEncryptionCertificateTool;

public class LocalEncryptionCertificateToolFactory {

    public static EncryptionCertificateTool buildKeyStoreKeyProvider(
            InputStream keyStoreStream,
            char[] keyStorePassword,
            String keyStoreAlias,
            char[] keyPassword) {
        EncryptionCertificateTool encryptionService = null;
        try {
            KeyStore keyStore = getKeyStore(keyStoreStream, keyStorePassword);
            Key key = keyStore.getKey(keyStoreAlias, keyPassword);
            Certificate certificate = keyStore.getCertificate(keyStoreAlias);
            encryptionService =
                    new LocalEncryptionCertificateTool(
                            (PrivateKey) key, ((X509Certificate) certificate));
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        return encryptionService;
    }

    private static KeyStore getKeyStore(InputStream keyStoreStream, char[] keyStorePassword)
            throws IOException, KeyStoreException, CertificateException, NoSuchAlgorithmException {
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(keyStoreStream, keyStorePassword);
        return keyStore;
    }
}

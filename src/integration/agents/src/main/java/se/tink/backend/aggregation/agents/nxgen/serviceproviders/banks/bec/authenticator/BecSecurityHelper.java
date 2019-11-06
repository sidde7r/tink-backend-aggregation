package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.authenticator;

import com.google.api.client.util.Base64;
import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.BecConstants;
import se.tink.backend.aggregation.agents.utils.crypto.RSA;

public final class BecSecurityHelper {

    private static BecSecurityHelper instance;
    private final PublicKey publicKey;
    private final byte[] symmetricKey;

    private BecSecurityHelper(String signingCertificate, String publicKeySalt) {
        publicKey = calculatePublicKey(signingCertificate, publicKeySalt);
        symmetricKey = generateSymmetricKey();
    }

    public static BecSecurityHelper getInstance(String signingCertificate, String publicKeySalt) {
        // Ugly but works
        if (instance == null) {
            synchronized (BecSecurityHelper.class) {
                if (instance == null) {
                    instance = new BecSecurityHelper(signingCertificate, publicKeySalt);
                }
            }
        }
        return instance;
    }

    private PublicKey calculatePublicKey(String signingCertificate, String publicKeySalt) {
        try {
            X509Certificate x509Certificate =
                    (X509Certificate)
                            CertificateFactory.getInstance(BecConstants.Crypto.X509)
                                    .generateCertificate(
                                            (new ByteArrayInputStream(
                                                    signingCertificate.getBytes())));
            byte[] signatureBytes = x509Certificate.getPublicKey().getEncoded();

            X509EncodedKeySpec x509EncodedKeySpec =
                    new X509EncodedKeySpec(
                            blend(Base64.decodeBase64(publicKeySalt), signatureBytes));

            return KeyFactory.getInstance("RSA").generatePublic(x509EncodedKeySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | CertificateException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    private byte[] generateSymmetricKey() {
        KeyGenerator localKeyGenerator;
        try {
            localKeyGenerator = KeyGenerator.getInstance(BecConstants.Crypto.AES);
        } catch (NoSuchAlgorithmException e) {
            throw new AssertionError("Algorithm should exist", e);
        }
        localKeyGenerator.init(256);

        return localKeyGenerator.generateKey().getEncoded();
    }

    public String getKey() {
        return toJsonString(RSA.encryptNonePkcs1((RSAPublicKey) publicKey, symmetricKey));
    }

    public String encrypt(byte[] paramArrayOfByte) {
        try {
            if (paramArrayOfByte != null && symmetricKey != null) {
                return new String(
                        Base64.encodeBase64(encryptPayload(symmetricKey, paramArrayOfByte)),
                        BecConstants.Crypto.UTF8);
            }
            return null;
        } catch (UnsupportedEncodingException | GeneralSecurityException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    // NOTE keep it in case we need to use the data from encrypted payload in authentication phase.
    public byte[] decrypt(String paramString) {
        try {
            if (paramString != null) {
                byte[] arrayOfByte =
                        Base64.decodeBase64(paramString.getBytes(BecConstants.Crypto.UTF8));
                return decryptPayload(symmetricKey, arrayOfByte);
            }
            return null;
        } catch (UnsupportedEncodingException | GeneralSecurityException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    private byte[] decryptPayload(byte[] key, byte[] dataToDec) throws GeneralSecurityException {
        SecretKeySpec localSecretKeySpec = new SecretKeySpec(key, BecConstants.Crypto.AES);
        Cipher localCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        byte[] arrayOfByte1 = new byte[16];
        byte[] arrayOfByte2 = null;
        if (dataToDec != null) {
            int i = dataToDec.length;
            arrayOfByte2 = null;
            if (16 <= i) {
                localCipher.init(2, localSecretKeySpec, new IvParameterSpec(arrayOfByte1));
                byte[] arrayOfByte3 = localCipher.doFinal(dataToDec);
                arrayOfByte2 = new byte[-16 + arrayOfByte3.length];
                System.arraycopy(arrayOfByte3, 16, arrayOfByte2, 0, arrayOfByte2.length);
            }
        }
        return arrayOfByte2;
    }

    private byte[] encryptPayload(byte[] paramArrayOfByte1, byte[] paramArrayOfByte2)
            throws GeneralSecurityException {
        SecretKeySpec localSecretKeySpec =
                new SecretKeySpec(paramArrayOfByte1, BecConstants.Crypto.AES);
        Cipher localCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        byte[] arrayOfByte1 = new byte[16];
        SecureRandom localSecureRandom = new SecureRandom();
        byte[] arrayOfByte2 = new byte[16];
        localSecureRandom.nextBytes(arrayOfByte2);
        byte[] arrayOfByte3 = null;
        if (paramArrayOfByte2 != null) {
            byte[] arrayOfByte4 = new byte[arrayOfByte2.length + paramArrayOfByte2.length];
            System.arraycopy(arrayOfByte2, 0, arrayOfByte4, 0, arrayOfByte2.length);
            System.arraycopy(
                    paramArrayOfByte2,
                    0,
                    arrayOfByte4,
                    arrayOfByte2.length,
                    paramArrayOfByte2.length);
            localCipher.init(1, localSecretKeySpec, new IvParameterSpec(arrayOfByte1));
            arrayOfByte3 = localCipher.doFinal(arrayOfByte4);
        }
        return arrayOfByte3;
    }

    private byte[] blend(byte[] byteA, byte[] byteB) {
        int i = byteA.length;
        int j = -1 + byteB.length;
        byte[] blended = new byte[i];
        for (int k = 0; k < i; k++) {
            blended[k] = ((byte) (byteA[k] ^ byteB[(k % j)]));
        }
        return blended;
    }

    private String toJsonString(byte[] paramArrayOfByte) {
        try {
            return new String(Base64.encodeBase64(paramArrayOfByte), BecConstants.Crypto.UTF8);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("Unsupported encoding: UTF-8", e);
        }
    }
}

package se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.authenticator;

import java.io.ByteArrayInputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.interfaces.RSAPublicKey;
import org.apache.commons.codec.binary.Base64;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.JyskeConstants;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.authenticator.security.Token;
import se.tink.backend.aggregation.agents.utils.crypto.AES;
import se.tink.backend.aggregation.agents.utils.crypto.RSA;

public class JyskeSecurityHelper {

    public static RSAPublicKey convertToPublicKey(byte[] publicKey) {
        try {
            return (RSAPublicKey)
                    CertificateFactory.getInstance(JyskeConstants.Crypto.CERT_TYPE)
                            .generateCertificate(new ByteArrayInputStream(publicKey))
                            .getPublicKey();
        } catch (CertificateException e) {
            throw new SecurityException("Certificate error", e);
        }
    }

    public static String encryptForBankdataWithRSAAndBase64Encode(
            byte[] data, RSAPublicKey publicKey) {
        return new String(
                Base64.encodeBase64(RSA.encryptNoneOaepMgf1(publicKey, data)),
                JyskeConstants.CHARSET);
    }

    public static byte[] encryptWithAESAndBase64Encode(byte[] dataToEnc, Token token) {
        // NOTE: Jyske always prepend 16 bytes junk data to the data for encryption, and ignored in
        // decryption.
        // So it is independent on the IV value.
        return Base64.encodeBase64(AES.encryptCbc(token.getBytes(), new byte[16], dataToEnc));
    }

    public static byte[] base64DecodeAndDecryptAES(String dataToDec, Token token) {
        // NOTE: Jyske uses random IV in AES-CBC decryption, and discard the first block.
        // So it is independent on the IV value.
        return AES.decryptCbc(token.getBytes(), new byte[16], Base64.decodeBase64(dataToDec));
    }

    public static String encryptForServiceWithRSAAndBase64Encode(
            byte[] data, RSAPublicKey publicKey) {
        return new String(
                Base64.encodeBase64(RSA.encryptEcbOaepMgf1(publicKey, data)),
                JyskeConstants.CHARSET);
    }
}

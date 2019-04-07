package se.tink.backend.aggregation.agents.utils.authentication.encap2.utils;

import com.google.common.primitives.Bytes;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;
import java.util.Base64;
import org.apache.commons.codec.binary.Hex;
import se.tink.backend.aggregation.agents.utils.crypto.AES;
import se.tink.backend.aggregation.agents.utils.crypto.EllipticCurve;
import se.tink.backend.aggregation.agents.utils.crypto.Hash;
import se.tink.backend.aggregation.agents.utils.crypto.RSA;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;

public class EncapCryptoUtils {
    public static String computeEncryptedEdbCredentials(
            String bankCode,
            String hardwareId,
            String clientPrivateKeyString,
            String rsaPubKeyString) {
        String plainText = bankCode + ";" + hardwareId;
        byte[] plainTextBytes = plainText.getBytes();
        RSAPublicKey pubKey =
                RSA.getPubKeyFromBytes(EncodingUtils.decodeBase64String(rsaPubKeyString));
        PrivateKey privateKey =
                RSA.getPrivateKeyFromBytes(
                        EncodingUtils.decodeBase64String(clientPrivateKeyString));

        byte[] encryptedPlainText = RSA.encryptEcbPkcs1(pubKey, plainTextBytes);
        byte[] signature = RSA.signSha1(privateKey, plainTextBytes);

        String encryptedPlainTextB64 = EncodingUtils.encodeAsBase64String(encryptedPlainText);
        String signatureB64 = EncodingUtils.encodeAsBase64String(signature);

        return encryptedPlainTextB64 + ";" + signatureB64;
    }

    public static String computeEMD(byte[] rand16BytesKey, byte[] rand16BytesIv, byte[] inputData) {
        byte[] aesCbcResult = AES.encryptCbc(rand16BytesKey, rand16BytesIv, inputData);
        return Base64.getEncoder().encodeToString(aesCbcResult);
    }

    public static String computeEMK(
            byte[] rand16BytesKey, byte[] rand16BytesIv, byte[] serverPubKeyBytes) {
        KeyPair ecKeyPair = EllipticCurve.generateKeyPair("sect233k1");
        PublicKey serverPublicKey = EllipticCurve.convertPEMtoPublicKey(serverPubKeyBytes);

        byte[] derivedECKey =
                EllipticCurve.diffieHellmanDeriveKey(ecKeyPair.getPrivate(), serverPublicKey);
        byte[] compressedECPoint = EllipticCurve.convertPublicKeyToPoint(ecKeyPair, true);
        byte[] sha1Input = Bytes.concat(compressedECPoint, derivedECKey);

        byte[] sha1Hash = sha1Recursion(sha1Input);
        byte[] aesKey = Arrays.copyOfRange(sha1Hash, 0, 16);
        byte[] hmacKey = Arrays.copyOfRange(sha1Hash, 16, 32);

        byte[] aesEcbInput = Bytes.concat(rand16BytesKey, rand16BytesIv);
        byte[] aesEcbOutput = AES.encryptEcbPkcs5(aesKey, aesEcbInput);

        byte[] hmacInput = Bytes.concat(aesEcbOutput, new byte[4]);
        byte[] hmacOutput = Hash.hmacSha1(hmacKey, hmacInput);

        byte[] EMKBytes = Bytes.concat(compressedECPoint, aesEcbOutput, hmacOutput);

        return Base64.getEncoder().encodeToString(EMKBytes);
    }

    public static String computeMAC(byte[] rand16BytesKey, byte[] rand16BytesIv, byte[] inputData) {
        byte[] hmacKey = Bytes.concat(rand16BytesKey, rand16BytesIv);
        byte[] hash = Hash.hmacSha256(hmacKey, inputData);
        return Base64.getEncoder().encodeToString(hash);
    }

    public static String computePublicKeyHash(byte[] pubKeyBytes) {
        byte[] hash = Hash.sha256(pubKeyBytes);
        return Base64.getEncoder().encodeToString(hash);
    }

    public static String decryptEMDResponse(
            byte[] rand16BytesKey, byte[] rand16BytesIv, String EMDResponse) {
        byte[] emdResponseBinary = Base64.getDecoder().decode(EMDResponse);
        byte[] aesCbcResult = AES.decryptCbc(rand16BytesKey, rand16BytesIv, emdResponseBinary);
        return Hex.encodeHexString(aesCbcResult);
    }

    public static boolean verifyMACValue(
            byte[] rand16BytesKey, byte[] rand16BytesIv, String decryptedEMD, String MACResponse) {
        String hashInBase64 =
                computeMAC(
                        rand16BytesKey, rand16BytesIv, EncodingUtils.decodeHexString(decryptedEMD));
        return hashInBase64.equals(MACResponse);
    }

    public static String computeB64ChallengeResponse(
            String authenticationKey, String b64OtpChallenge) {
        byte[] authKeyBytes = EncodingUtils.decodeBase64String(authenticationKey);
        byte[] inputDataBytes = EncodingUtils.decodeBase64String(b64OtpChallenge);
        byte[] result = AES.encryptEcbNoPadding(authKeyBytes, inputDataBytes);
        return EncodingUtils.encodeAsBase64String(result);
    }

    private static byte[] sha1Recursion(byte[] sha1CounterInput) {
        byte[] counter = new byte[4];
        counter[3] = 1;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        return sha1WithCounterRecursion(sha1CounterInput, counter, outputStream, 32);
    }

    private static byte[] sha1WithCounterRecursion(
            byte[] input, byte[] counter, ByteArrayOutputStream outputStream, int outputLen) {

        byte[] sha1Res = Hash.sha1(input, counter);

        if (outputLen <= 20) {
            outputStream.write(sha1Res, 0, outputLen);
            return outputStream.toByteArray();
        } else {
            try {
                outputStream.write(sha1Res);
            } catch (IOException e) {
                throw new IllegalStateException(e.getMessage(), e);
            }
            counter[3]++;
            return sha1WithCounterRecursion(input, counter, outputStream, outputLen - 20);
        }
    }
}

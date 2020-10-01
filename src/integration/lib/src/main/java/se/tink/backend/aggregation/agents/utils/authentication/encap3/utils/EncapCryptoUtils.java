package se.tink.backend.aggregation.agents.utils.authentication.encap3.utils;

import com.google.common.primitives.Bytes;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.KeyPair;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;
import java.util.Base64;
import org.apache.commons.codec.binary.Hex;
import se.tink.backend.aggregation.agents.utils.crypto.AES;
import se.tink.backend.aggregation.agents.utils.crypto.EllipticCurve;
import se.tink.backend.aggregation.agents.utils.crypto.RSA;
import se.tink.backend.aggregation.agents.utils.crypto.hash.Hash;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;

public class EncapCryptoUtils {
    public static String encrypAesCbc(byte[] bytesKey, byte[] bytesIv, byte[] inputData) {
        byte[] aesCbcResult = AES.encryptCbc(bytesKey, bytesIv, inputData);
        return Base64.getEncoder().encodeToString(aesCbcResult);
    }

    public static String computeEMK(
            KeyPair ecKeyPair,
            byte[] rand16BytesKey,
            byte[] rand16BytesIv,
            byte[] serverPubKeyBytes) {
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

    public static String computeRsaEMK(String rsaPubKeyB64, byte[] data) {
        RSAPublicKey rsaPublicKey =
                RSA.getPubKeyFromBytes(Base64.getDecoder().decode(rsaPubKeyB64));
        byte[] emk = RSA.encryptEcbOaepMgf1(rsaPublicKey, data);

        return Base64.getEncoder().encodeToString(emk);
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

    public static String decryptPayloadResponse(
            byte[] aes32BytesKey, byte[] aes16BytesIv, String payload) {
        byte[] payloadResponseBinary = Base64.getDecoder().decode(payload);
        byte[] aesCbcResult = AES.decryptCbc(aes32BytesKey, aes16BytesIv, payloadResponseBinary);
        return new String(aesCbcResult);
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

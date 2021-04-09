package se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.utils;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class WizinkEncoder {

    public static String hashPassword(String deviceId, String plainTextPassword) {
        return getEncodedPassword(encodeToHex(plainTextPassword), deviceId);
    }

    private static String getEncodedPassword(String hexPassword, String deviceId) {
        String xorPassword =
                xorHexedPasswordWithHexedDeviceId(deviceId, hexPassword.replace(" ", ""));
        return encodeToBase64(xorPassword);
    }

    private static String xorHexedPasswordWithHexedDeviceId(String deviceId, String hexPassword) {
        return xor(encodeToHex(deviceId), hexPassword).toUpperCase();
    }

    private static String encodeToHex(String str) {
        return String.format("%x", new BigInteger(1, str.getBytes(StandardCharsets.UTF_8)));
    }

    private static String encodeToBase64(String str) {
        return Base64.getEncoder()
                .encodeToString(str.getBytes(StandardCharsets.UTF_8))
                .replace("\r\n", "");
    }

    private static String xor(String first, String second) {
        return new BigInteger(first, 16).xor(new BigInteger(second, 16)).toString(16);
    }
}

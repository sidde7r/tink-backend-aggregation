package se.tink.backend.aggregation.agents.creditcards.ikano.api.utils;

import com.google.common.base.Strings;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.libraries.strings.StringUtils;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class IkanoCrypt {
    // Do not change the SENSITIVE_PAYLOAD_KEY
    private static final String SENSITIVE_PAYLOAD_KEY = "deviceId";
    private static final String AUTH_SUFFIX = "436452622626346435";

    public static String generateDeviceID(String username) {
        return StringUtils.hashAsUUID("TINK-" + username) + "....";
    }

    public static String generateDeviceAuth(String deviceId) throws NoSuchAlgorithmException {
        String bigInteger = new BigInteger(1, MessageDigest.getInstance("MD5").digest((deviceId + AUTH_SUFFIX).getBytes())).toString(16);

        return Strings.padStart(bigInteger, 32, '0');
    }

    public static String findOrGenerateDeviceIdFor(Credentials credentials) {
        String deviceId = credentials.getSensitivePayload(SENSITIVE_PAYLOAD_KEY);

        if (Strings.isNullOrEmpty(deviceId)) {
            deviceId = generateDeviceID(credentials.getUsername());
            credentials.setSensitivePayload(SENSITIVE_PAYLOAD_KEY, deviceId);
        }

        return deviceId;
    }
}

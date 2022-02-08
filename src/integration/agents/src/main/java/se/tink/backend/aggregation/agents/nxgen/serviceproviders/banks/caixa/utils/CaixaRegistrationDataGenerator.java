package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.caixa.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.UUID;

public class CaixaRegistrationDataGenerator {
    private static final String UA_SHORT_COMPANY = "APPL";
    private static final String UA_IPHONE_BRAND = "_IPHONE_";
    private static final String UA_IPHONE_INDICATOR = "I";
    private static final String UA_COMPANY = "_Apple_";
    private static final String UA_ADAM = "_ADAM";
    private static final String UA_IOS_VERSION = "14.4.2";
    private static final String UA_SHORT_PHONE_MODEL = "h10,4";
    private static final String UA_PHONE_MODEL = "iPhone10,4";

    /**
     * identifier = appNameAndVariant + deviceIdentifier appNameAndVariant =
     * es.lacaixa.mobile.imaginBank_iPhone deviceIdentifier = UUID Identifier. like:
     * E9606BD1-D916-4CB0-A6D6-A7AA1176A177
     *
     * @param username
     * @return
     */
    public static String generateIdentifierByUsername(
            String appName, String username, Base64.Encoder encoder) {
        return generateEncodedIdentifier(appName + uuidFromUsername(username), encoder);
    }

    /**
     * Generate the same uuid by username
     *
     * @param username
     * @return
     */
    private static String uuidFromUsername(String username) {
        return UUID.nameUUIDFromBytes(username.getBytes(StandardCharsets.UTF_8))
                .toString()
                .toUpperCase();
    }

    /**
     * Encode the input: first 28 chars of base64(sha256(input))
     *
     * @param input
     * @return
     */
    public static String generateEncodedIdentifier(String input, Base64.Encoder encoder) {
        try {
            byte[] bytes = input.getBytes(StandardCharsets.US_ASCII);
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(bytes, 0, bytes.length);
            byte[] digest = md.digest();
            return encoder.encodeToString(digest).substring(0, 28);
        } catch (Exception e) {
            throw new IllegalStateException("Cannot generate the app identifier", e);
        }
    }

    /**
     * Generate the User Agent
     *
     * @return
     */
    public static String generateUserAgent(
            String appNamePrefix, String appVersion, String appInstallationId) {
        return appNamePrefix
                + appInstallationId
                + UA_IPHONE_BRAND
                + appVersion
                + UA_COMPANY
                + UA_PHONE_MODEL
                + "_"
                + UA_IOS_VERSION
                + UA_ADAM;
    }

    /**
     * Something like eIAPPLh10,4mXLzxVM9zY2W5vsh4r8x7DJ9JIMp 'e' constant 'I' = iPhone, 'P' = iPad,
     * 'U' = universal 'APPL' constant 'Ph9,3' = short formatted model identifier = first 28 bytes
     * of base64(sha256(appNameAndVariant + deviceIdentifier))
     *
     * @param username final username
     * @return
     */
    public static String generateAppInstallationId(
            String appName,
            String username,
            String userAgentPrefixConstant,
            Base64.Encoder encoder) {

        String identifier = generateIdentifierByUsername(appName, username, encoder);
        return userAgentPrefixConstant
                + UA_IPHONE_INDICATOR
                + UA_SHORT_COMPANY
                + UA_SHORT_PHONE_MODEL
                + identifier;
    }
}

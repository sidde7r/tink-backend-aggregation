package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.caixa.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.UUID;

public class CaixaRegistrationDataGenerator {
    private static final String UA_E_CONSTANT = "e";
    private static final String UA_SHORT_COMPANY = "APPL";
    private static final String UA_IPHONE_BRAND = "_IPHONE_";
    private static final String UA_IPAD_BRAND = "_IPAD_";
    private static final String UA_IPHONE_INDICATOR = "I";
    private static final String UA_IPAD_INDICATOR = "P";
    private static final String UA_COMPANY = "_Apple_";
    private static final String UA_ADAM = "_ADAM";
    private static final String UA_IOS_VERSION = "14.4.2";
    private static final String UA_SHORT_PHONE_MODEL = "h10,4";
    private static final String UA_PHONE_MODEL = "iPhone10,4";

    private static final String APP_NAME_AND_VARIANT = "es.lacaixa.mobile.imaginBank_iPhone";

    /**
     * identifier = appNameAndVariant + deviceIdentifier appNameAndVariant =
     * es.lacaixa.mobile.imaginBank_iPhone deviceIdentifier = UUID Identifier. like:
     * E9606BD1-D916-4CB0-A6D6-A7AA1176A177
     *
     * @param username
     * @return
     */
    public static String generateIdentifierByUsername(String username) {
        return generateEncodedIdentifier(APP_NAME_AND_VARIANT + uuidFromUsername(username));
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
    public static String generateEncodedIdentifier(String input) {
        try {
            byte[] bytes = input.getBytes(StandardCharsets.US_ASCII);
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(bytes, 0, bytes.length);
            byte[] digest = md.digest();
            return Base64.getUrlEncoder().encodeToString(digest).substring(0, 28);
        } catch (Exception e) {
            throw new IllegalStateException("Cannot generate the app identifier", e);
        }
    }

    /**
     * Generate the User Agent
     *
     * @param username
     * @param iPad
     * @return
     */
    public static String generateUserAgent(
            String username, boolean iPad, String appNamePrefix, String appVersion) {
        String brand = iPad ? UA_IPAD_BRAND : UA_IPHONE_BRAND;
        return appNamePrefix
                + generateAppInstallationId(username, iPad)
                + brand
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
     * @param iPad true: iPad, false: iPhone
     * @return
     */
    public static String generateAppInstallationId(String username, boolean iPad) {
        String brandIndicator = iPad ? UA_IPAD_INDICATOR : UA_IPHONE_INDICATOR;
        String identifier = generateIdentifierByUsername(username);
        return UA_E_CONSTANT
                + brandIndicator
                + UA_SHORT_COMPANY
                + UA_SHORT_PHONE_MODEL
                + identifier;
    }
}

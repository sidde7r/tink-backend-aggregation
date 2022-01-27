package se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.utils;

import static se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.ImaginBankConstants.UserAgentValues.APP_NAME_AND_VARIANT;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.ImaginBankConstants.UserAgentValues.UA_ADAM;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.ImaginBankConstants.UserAgentValues.UA_APP_NAME_PREFIX;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.ImaginBankConstants.UserAgentValues.UA_APP_VERSION;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.ImaginBankConstants.UserAgentValues.UA_COMPANY;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.ImaginBankConstants.UserAgentValues.UA_E_CONSTANT;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.ImaginBankConstants.UserAgentValues.UA_IOS_VERSION;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.ImaginBankConstants.UserAgentValues.UA_IPAD_BRAND;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.ImaginBankConstants.UserAgentValues.UA_IPAD_INDICATOR;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.ImaginBankConstants.UserAgentValues.UA_IPHONE_BRAND;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.ImaginBankConstants.UserAgentValues.UA_IPHONE_INDICATOR;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.ImaginBankConstants.UserAgentValues.UA_PHONE_MODEL;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.ImaginBankConstants.UserAgentValues.UA_SHORT_COMPANY;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.ImaginBankConstants.UserAgentValues.UA_SHORT_PHONE_MODEL;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.UUID;

public class ImaginBankRegistrationDataGenerator {

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
    public static String generateUserAgent(String username, boolean iPad) {
        String brand = iPad ? UA_IPAD_BRAND : UA_IPHONE_BRAND;
        return UA_APP_NAME_PREFIX
                + generateAppInstallationId(username, iPad)
                + brand
                + UA_APP_VERSION
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

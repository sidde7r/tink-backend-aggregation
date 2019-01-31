package se.tink.backend.aggregation.agents.banks.danskebank.v2.helpers;

import se.tink.backend.agents.rpc.Credentials;

public class BankIdResourceHelper {
    // prod_sg_public_url
    private static final String BASE_URL_PUBLICSERVICE = "https://publicservice01.danskebank.com";
    // prod_sg_private_url
    private static final String BASE_URL_PRIVATESERVICE = "https://privateservice01.danskebank.com";

    private static final String URL_PUBLICSERVICE_BANKID_AUTH = BASE_URL_PUBLICSERVICE + "/REI/swbankid/auth";
    private static final String URL_PRIVATESERVICE_BANKID_SIGN = BASE_URL_PRIVATESERVICE + "/RES/swbankid/sign";

    private static final String BANKID_AUTH_SERVICE = C0570a.REI.m2098b();
    private static final String BANKID_SIGN_SERVICE = C0570a.RES.m2098b();

    private final C0575g encryptionContext;
    private final C2804f encryptionHelper;

    /**
     * Encryption methods used in Android app to generate auth headers and help out with encryption of messages and
     * authorization headers
     */
    public BankIdResourceHelper(Credentials credentials) {
        encryptionContext = new C0576e(credentials);
        encryptionHelper = new C2804f();
    }

    public String generateAuthorizationHeader(BankIdServiceType service) {
        switch (service) {
        case INITAUTH:
            return encryptionHelper.m12077b(encryptionContext, BANKID_AUTH_SERVICE).m12068a();
        case VERIFYAUTH:
            return encryptionHelper.m12073a(encryptionContext, BANKID_AUTH_SERVICE).m12068a();
        case VERIFYSIGN:
            return encryptionHelper.m12073a(encryptionContext, BANKID_SIGN_SERVICE).m12068a();
        default:
            throw new IllegalArgumentException("Invalid service: " + service.name());
        }
    }

    public static String getServiceUrl(BankIdServiceType service) {
        switch (service) {
        case INITAUTH:
            return URL_PUBLICSERVICE_BANKID_AUTH;
        case VERIFYAUTH:
            return URL_PUBLICSERVICE_BANKID_AUTH;
        case VERIFYSIGN:
            return URL_PRIVATESERVICE_BANKID_SIGN;
        default:
            throw new IllegalArgumentException("Invalid service: " + service.name());
        }
    }

    public C2804f getEncryptionHelper() {
        return encryptionHelper;
    }
}

package se.tink.backend.aggregation.agents.nxgen.se.openbanking.handelsbanken;

import se.tink.libraries.i18n.LocalizableKey;

public class HandelsbankenSEConstants {

    public static final int MAX_FETCH_PERIOD_MONTHS = 15;

    public static class BankIdUserMessage {
        public static final LocalizableKey ACTIVATION_NEEDED =
                new LocalizableKey("You need to activate your BankID in the Handelsbanken app.");
    }

    public static class InitiatePaymentBodyValues {
        public static final String IDENTIFICATION_CODE = "6156";
        public static final String IDENTIFICATION_TYPE = "SESBA";
    }

    public static class CredentialKeys {
        public static final String SCOPE = "scope";
        public static final String USERNAME = "username";
    }

    public static class OAuth2Type {
        public static final String BEARER = "Bearer";
    }

    public static class AccountType {
        public static final String BBAN = "BBAN";
    }
}

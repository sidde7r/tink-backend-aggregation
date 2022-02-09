package se.tink.backend.aggregation.agents.nxgen.se.openbanking.handelsbanken;

import se.tink.libraries.i18n_aggregation.LocalizableKey;

public class HandelsbankenSEConstants {

    public static final int MAX_FETCH_PERIOD_MONTHS = 15;

    public static class BankIdUserMessage {
        public static final LocalizableKey ACTIVATION_NEEDED =
                new LocalizableKey("You need to activate your BankID in the Handelsbanken app.");
    }

    public static class CredentialKeys {
        public static final String SCOPE = "scope";
        public static final String USERNAME = "username";
    }

    public static class CreditorAgentIdentificationType {
        public static final String SE_CLEARING_NUMBER = "SESBA";
    }

    public static class PaymentAccountType {
        public static final String BBAN = "BBAN";
        public static final String PLUSGIRO = "PG";
        public static final String BANKGIRO = "BG";
    }

    public static class PaymentValue {
        public static final int MAX_DEST_MSG_LEN_DOMESTIC = 12;
        public static final int MAX_DEST_MSG_LEN_DOMESTIC_SHB = 14;
        public static final int MAX_DEST_MSG_LEN_GIRO = 210;
        public static final int MAX_CREDITOR_NAME_LENGTH = 35;
    }
}

package se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk;

import se.tink.backend.aggregation.agents.utils.log.LogTag;
import se.tink.backend.aggregation.nxgen.core.account.AccountTypeMapper;
import se.tink.backend.aggregation.rpc.AccountTypes;

public class BawagPskConstants {

    public static final int DISPOSER_NUMBER_LENGTH = 17; // Length when account number is left-padded with zeros

    public static class CLIENT {
        // Values sent in LoginRequest messages originating from the app
        public static final String BANK_CODE = "14000";
    }

    public static class SERVER {
        // Values sent in LoginResponse messages originating from the bank
        public static final String BANK_CODE = "60000";
        public static final String BIC = "BAWAATWW";
        public static final String CODE = "0110";
        public static final String SHORT_NAME = "PSK";
    }

    public static final String CHANNEL = "MOBILE";
    public static final String LANGUAGE = "en";
    public static final String DEV_ID = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
    public static final String DEVICE_IDENTIFIER = "AAAAAAAA-AAAA-AAAA-AAAA-AAAAAAAAAAAA";
    public static final String IS_INCLUDE_LOGIN_IMAGE = "0";
    public static final String BOOKING_DATE = "BOOKING_DATE";
    public static final String TRANSACTION_TYPE = "ALL";
    public static final String ACTION_CALL = "MailBox/countOfUnreadedEmails";

    public static class Header {
        public static final String ACCEPT = "*/*";
        public static final String USER_AGENT = "Tink (+https://www.tink.se/; noc@tink.se) mobileBanking/bawag_5.3 target/PROD";
        public static final String ACCEPT_LANGUAGE = "en-gb";
    }

    public static class URLS {
        public static final String SERVICE_ENDPOINT = "/ebanking.mobile/SelfServiceMobileService";
        public static final String SOAP_NAMESPACE = "urn:selfservicemobile.bawag.com/ws/v0100-draft03";
    }

    public static class MESSAGES {
        public static final String STRING_TOO_SHORT = "String too short, minimum is 5";
        public static final String INPUT_NOT_17_DIGITS = "doesn't match [0-9]{17}";
        public static final String ACCOUNT_LOCKED = "ERR_DISPOSER_DEACTIVATED";
        public static final String INCORRECT_CREDENTIALS = "ERR_LOGIN";
    }

    // TODO make non-static somehow
    public static final AccountTypeMapper PRODUCT_CODE_MAPPER = AccountTypeMapper.builder()
            .put(AccountTypes.CHECKING, "B121", "B400")
            .put(AccountTypes.SAVINGS, "D272", "D264")
            .put(AccountTypes.CREDIT_CARD, "00EC", "00ET", "00PD")
            .put(AccountTypes.LOAN, "S132")
            .build();

    // Fallback mapper; more error-prone because the bank assigns a savings account to "CHECKING" for some reason
    // TODO make non-static somehow
    public static final AccountTypeMapper PRODUCT_TYPE_MAPPER = AccountTypeMapper.builder()
            .put(AccountTypes.CHECKING, "CHECKING")
            .put(AccountTypes.SAVINGS, "SAVINGS")
            .put(AccountTypes.CREDIT_CARD, "CREDIT_CARD")
            .put(AccountTypes.LOAN, "LOAN")
            .build();


    public enum Storage {
        SERVER_SESSION_ID,
        QID,
        PRODUCTS,
        PRODUCT_CODES,
        ACCOUNTS_RESPONSE
    }

    public enum LogTags {
        RESPONSE_NOT_OK,
        TRANSACTION_UNKNOWN_PRODUCT_CODE,
        TRANSACTION_UNKNOWN_PRODUCT_TYPE;

        public LogTag toTag() {
            return LogTag.from(name());
        }
    }
}

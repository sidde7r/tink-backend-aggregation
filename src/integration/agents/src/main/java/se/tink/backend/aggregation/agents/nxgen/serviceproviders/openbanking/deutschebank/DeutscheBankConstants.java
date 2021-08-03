package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank;

import com.google.common.collect.ImmutableList;
import java.util.List;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccountTypeMapper;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.enums.AccountFlag;

public final class DeutscheBankConstants {

    public static final List<String> CHECKING_ACCOUNT_KEYS =
            ImmutableList.of(
                    "CACC",
                    "CASH",
                    "CHAR",
                    "CISH",
                    "COMM",
                    "SLRY",
                    "TRAN",
                    "TRAS",
                    "CurrentAccount",
                    "Current");

    public static final List<String> SAVING_ACCOUNT_KEYS = ImmutableList.of("LLSV", "ONDP", "SVGS");

    public static final TransactionalAccountTypeMapper ACCOUNT_TYPE_MAPPER =
            TransactionalAccountTypeMapper.builder()
                    .put(
                            TransactionalAccountType.CHECKING,
                            AccountFlag.PSD2_PAYMENT_ACCOUNT,
                            CHECKING_ACCOUNT_KEYS.toArray(new String[0]))
                    .put(
                            TransactionalAccountType.SAVINGS,
                            AccountFlag.PSD2_PAYMENT_ACCOUNT,
                            SAVING_ACCOUNT_KEYS.toArray(new String[0]))
                    .build();

    private DeutscheBankConstants() {
        throw new AssertionError();
    }

    public static class ErrorMessages {
        public static final String INVALID_CONFIGURATION =
                "Invalid Configuration: %s cannot be empty or null";
    }

    public static class Urls {
        public static final String CONSENT = "/v1/consents";
        public static final String CONSENT_DETAILS = "/v1/consents/{consentId}";
        public static final String CONSENTS_STATUS = "/v1/consents/{consentId}/status";
        public static final String ACCOUNTS = "/v1/accounts";
        public static final String BALANCES = "/v1/accounts/%s/balances";
        public static final String TRANSACTIONS = "/v1/accounts/%s/transactions";
        public static final String MYBANK_BELGIUM = "https://www.deutschebank.be/mybank/index.html";
        public static final String MYBANK_BELGIUM_IOS =
                "https://apps.apple.com/be/app/mybank-belgium/id1082668633";
        public static final String MYBANK_BELGIUM_ANDROID =
                "https://play.app.goo.gl/?link=https://play.google.com/store/apps/details?id=com.db.pbc.mybankbelgium&ddl=1&pcampaignid=web_ddl_1";
        public static final String PAYMENT = "/v1/{payment-service}/{payment-product}";
        public static final String PAYMENT_STATUS =
                "/v1/{payment-service}/{payment-product}/{paymentId}/status";
    }

    public static class IdKeys {
        public static final String CONSENT_ID = "consentId";
    }

    public static class StorageKeys {
        public static final String CONSENT_ID = "Consent-ID";
        public static final String TRANSACTIONS_URL = "TRANSACTIONS_URL";
    }

    public static class QueryKeys {
        public static final String STATE = "state";
        public static final String BOOKING_STATUS = "bookingStatus";
        public static final String DELTA_LIST = "deltaList";
    }

    public static class HeaderKeys {
        public static final String CONSENT_ID = "Consent-ID";
        public static final String X_REQUEST_ID = "X-Request-ID";
        public static final String TPP_REDIRECT_URI = "TPP-Redirect-URI";
        public static final String PSU_IP_ADDRESS = "PSU-IP-Address";
        public static final String PSU_ID = "PSU-ID";
        public static final String PSU_ID_TYPE = "PSU-ID-Type";
        public static final String TPP_NOK_REDIRECT_URI = "TPP-Nok-Redirect-URI";
    }

    public static class QueryValues {

        public static final String BOTH = "both";
        public static final String BOOKED = "booked";
        public static final String DELTA_LIST = "true";
    }

    public static class CredentialKeys {
        public static final String USERNAME = "username";
        public static final String PASSWORD = "password";
    }

    public static class Accounts {
        public static final String BALANCE_CLOSING_BOOKED = "closingBooked";
        public static final String CLBD = "CLBD";
        public static final String EXPECTED = "expected";
    }

    public static class StatusValues {
        public static final String EXPIRED = "expired";
        public static final String RECEIVED = "received";
        public static final String VALID = "valid";
        public static final String REJECTED = "rejected";
    }

    public static class FormValues {
        public static final int MAX_POLLS_COUNTER = 50;
        public static final String CURRENCY_TYPE = "EUR";
    }

    public static class Configuration {
        public static final String PSU_IP_ADDRESS = "0.0.0.0";
    }

    public static class Parameters {
        public static final String SERVICE_KEY = "service-key";
        public static final String AIS = "ais";
        public static final String PIS = "pis";
    }
}

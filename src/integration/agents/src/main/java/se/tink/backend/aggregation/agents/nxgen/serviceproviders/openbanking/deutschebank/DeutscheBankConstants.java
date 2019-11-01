package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank;

import com.google.common.collect.ImmutableList;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccountTypeMapper;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.enums.AccountFlag;

public final class DeutscheBankConstants {

    public static final String DEFAULT_IP = "0.0.0.0";
    public static final TransactionalAccountTypeMapper ACCOUNT_TYPE_MAPPER =
            TransactionalAccountTypeMapper.builder()
                    .put(
                            TransactionalAccountType.CHECKING,
                            AccountFlag.PSD2_PAYMENT_ACCOUNT,
                            "CACC",
                            "CASH",
                            "CHAR",
                            "CISH",
                            "COMM",
                            "SLRY",
                            "TRAN",
                            "TRAS",
                            "CurrentAccount",
                            "Current")
                    .put(
                            TransactionalAccountType.SAVINGS,
                            AccountFlag.PSD2_PAYMENT_ACCOUNT,
                            "LLSV",
                            "ONDP",
                            "SVGS")
                    .build();

    private DeutscheBankConstants() {
        throw new AssertionError();
    }

    public static class ErrorMessages {
        public static final String INVALID_CONFIGURATION =
                "Invalid Configuration: %s cannot be empty or null";
        public static final String MISSING_CONFIGURATION = "Client Configuration missing.";
        public static final String MISSING_TOKEN = "Cannot find token.";
        public static final String MISSING_BALANCE = "Missing account balance.";
    }

    public static class Urls {
        public static final String CONSENT = "/v1/consents";
        public static final String ACCOUNTS = "/v1/accounts";
        public static final String BALANCES = "/v1/accounts/%s/balances";
        public static final String TRANSACTIONS = "/v1/accounts/%s/transactions";
        public static final String MYBANK_BELGIUM = "https://www.deutschebank.be/mybank/index.html";
        public static final String MYBANK_BELGIUM_IOS =
                "https://apps.apple.com/be/app/mybank-belgium/id1082668633";
        public static final String MYBANK_BELGIUM_ANDRORID =
                "https://play.app.goo.gl/?link=https://play.google.com/store/apps/details?id=com.db.pbc.mybankbelgium&ddl=1&pcampaignid=web_ddl_1";
    }

    public static class IdTags {
        public static final String REGION_ID = "regionId";
    }

    public static class StorageKeys {
        public static final String CONSENT_ID = "Consent-ID";
        public static final String TRANSACTIONS_URL = "TRANSACTIONS_URL";
    }

    public static class QueryKeys {
        public static final String STATE = "state";
        public static final String WITH_BALANCE = "withBalance";
        public static final String DATE_FROM = "dateFrom";
        public static final String DATE_TO = "dateTo";
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

        public static final String WITH_BALANCE = "true";
        public static final String BOOKING_STATUS = "both";
        public static final String DELTA_LIST = "true";
    }

    public static class CredentialKeys {
        public static final String IBAN = "iban";
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
        public static final String REVOKED_BY_PSU = "revokedByPsu";
        public static final String TERMINATED_BY_TPP = "terminatedByTpp";
        public static final ImmutableList<String> FAILED =
                ImmutableList.of(EXPIRED, REVOKED_BY_PSU, TERMINATED_BY_TPP);
    }

    public static class FormValues {
        public static final int MAX_POLLS_COUNTER = 50;
        public static final String CURRENCY_TYPE = "EUR";
        // If the account does not have transactions for the requested period of time, DeutscheBank
        // BE is just rejecting the request.
        public static final String TRANSACTION_REQUEST_REJECTED = "RJCT";
    }
}

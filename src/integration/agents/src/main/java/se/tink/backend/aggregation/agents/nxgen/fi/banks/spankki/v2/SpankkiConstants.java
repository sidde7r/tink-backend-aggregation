package se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2;

import se.tink.backend.aggregation.agents.utils.log.LogTag;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccountTypeMapper;
import se.tink.backend.aggregation.nxgen.core.account.TypeMapper;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.instrument.InstrumentModule.InstrumentType;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.portfolio.PortfolioModule.PortfolioType;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.utils.deviceprofile.DeviceProfile;
import se.tink.backend.aggregation.utils.deviceprofile.DeviceProfileConfiguration;
import se.tink.libraries.account.enums.AccountFlag;

public class SpankkiConstants {
    public static final DeviceProfile DEVICE_PROFILE = DeviceProfileConfiguration.IOS_STABLE;

    public static final String CURRENCY = "EUR";
    public static final TransactionalAccountTypeMapper ACCOUNT_TYPE_MAPPER =
            TransactionalAccountTypeMapper.builder()
                    .put(
                            TransactionalAccountType.CHECKING,
                            AccountFlag.PSD2_PAYMENT_ACCOUNT,
                            "418",
                            "415")
                    .put(
                            TransactionalAccountType.SAVINGS,
                            AccountFlag.PSD2_PAYMENT_ACCOUNT,
                            "443",
                            "419",
                            "427",
                            "416")
                    .build();
    public static final TypeMapper<PortfolioType> PORTFOLIO_TYPE_MAP =
            TypeMapper.<PortfolioType>builder().put(PortfolioType.DEPOT, "ROBO_PORTFOLIO").build();
    public static final TypeMapper<InstrumentType> INSTRUMENT_TYPE_MAP =
            TypeMapper.<InstrumentType>builder().put(InstrumentType.FUND, "FUND_ROBO").build();

    public static class Urls {
        public static final String HOST = "https://mobile.s-pankki.fi";
        public static final String ENCAP_HOST =
                "https://tunnistus.s-pankki.fi/platform-smartdevice/client";

        public static final URL KEEP_ALIVE = new URL(HOST + Endpoints.KEEP_ALIVE);
        public static final URL REQUEST_CHALLENGE = new URL(HOST + Endpoints.REQUEST_CHALLENGE);
        public static final URL RESPONSE_CHALLENGE = new URL(HOST + Endpoints.RESPONSE_CHALLENGE);
        public static final URL LOGIN_USERPWD = new URL(HOST + Endpoints.LOGIN_USERPWD);
        public static final URL LOGIN_KEYCARD = new URL(HOST + Endpoints.LOGIN_KEYCARD);
        public static final URL START_ENCAP = new URL(HOST + Endpoints.START_ENCAP);
        public static final URL POLL_ENCAP = new URL(HOST + Endpoints.POLL_ENCAP);
        public static final URL GET_PHONENUMBER = new URL(HOST + Endpoints.GET_PHONENUMBER);
        public static final URL RECEIVE_OTP = new URL(HOST + Endpoints.RECEIVE_OTP);
        public static final URL VERIFY_OTP = new URL(HOST + Endpoints.VERIFY_OTP);
        public static final URL ENCAP = new URL(ENCAP_HOST);
        public static final URL FETCH_ACCOUNTS = new URL(HOST + Endpoints.FETCH_ACCOUNTS);
        public static final URL FETCH_TRANSACTIONS = new URL(HOST + Endpoints.FETCH_TRANSACTIONS);
        public static final URL FETCH_CARDS = new URL(HOST + Endpoints.FETCH_CARDS);
        public static final URL FETCH_CARD_DETAILS = new URL(HOST + Endpoints.FETCH_CARD_DETAILS);
        public static final URL FETCH_CARD_TRANSACTIONS =
                new URL(HOST + Endpoints.FETCH_CARD_TRANSACTIONS);
        public static final URL FETCH_INVESTMENT_ACCOUNT =
                new URL(HOST + Endpoints.FETCH_INVESTMENT_ACCOUNT);
        public static final URL FETCH_FUND_DETAILS = new URL(HOST + Endpoints.FETCH_FUND_DETAILS);
        public static final URL FETCH_LOANS = new URL(HOST + Endpoints.FETCH_LOANS);
        public static final URL FETCH_LOAN_DETAILS = new URL(HOST + Endpoints.FETCH_LOAN_DETAILS);
    }

    public static class Endpoints {
        public static final String VERSION = "/v2";

        public static final String KEEP_ALIVE = VERSION + "/bank/keepalive/refresh";
        public static final String REQUEST_CHALLENGE = VERSION + "/authentication/device/chreq";
        public static final String RESPONSE_CHALLENGE = VERSION + "/authentication/device/chresp";
        public static final String LOGIN_USERPWD = VERSION + "/authentication/usrpwd";
        public static final String LOGIN_KEYCARD = VERSION + "/authentication/tan";
        public static final String START_ENCAP = VERSION + "/authentication/encap/start";
        public static final String POLL_ENCAP = VERSION + "/authentication/encap/perform";
        public static final String GET_PHONENUMBER = VERSION + "/identification/phonenumber";
        public static final String RECEIVE_OTP = VERSION + "/identification/phonenumber/activate";
        public static final String VERIFY_OTP = VERSION + "/identification/activation/start10";
        public static final String FETCH_ACCOUNTS = VERSION + "/bank/customer/accounts/get";
        public static final String FETCH_TRANSACTIONS =
                VERSION + "/bank/customer/transactions/get/{accountId}/{page}";
        public static final String FETCH_CARDS = VERSION + "/customer/cards/get";
        public static final String FETCH_CARD_DETAILS = VERSION + "/customer/cards/details";
        public static final String FETCH_CARD_TRANSACTIONS =
                VERSION + "/customer/cards/transactions/{contractNr}/{fromDate}/{toDate}";
        public static final String FETCH_INVESTMENT_ACCOUNT = VERSION + "/fim/positionreport";
        public static final String FETCH_FUND_DETAILS = VERSION + "/fim/positiondetails";
        public static final String FETCH_LOANS = VERSION + "/bank/loan/hasLoans";
        public static final String FETCH_LOAN_DETAILS = VERSION + "/bank/loan/list";
    }

    public static class Authentication {
        public static final String REQUEST_TOKEN_HASH_SALT = "23fee939242741f79c4e2bec3d64a5b3";
        public static final String CHALLENGE_RESPONSE_HASH_SALT =
                "45d493b8f45b459c8903a6e4f260b62e";
        public static final String B64_ELLIPTIC_CURVE_PUBLIC_KEY =
                "MFIwEAYHKoZIzj0CAQYFK4EEABoDPgAEAdI87D0d2WOaZq5LBZRBxbmeZnaDvpNutQJNjvb3AVtGSsf2rq1lu4PAmk4bnl2DRlSbtV8spT4l4tDi";
        public static final int KEY_CARD_VALUE_LENGTH = 4;
        public static final int SMS_OTP_VALUE_LENGTH = 4;
    }

    public static class EncapMessage {
        public static final String APPLICATION_ID = "sbaEncapSba";
        public static final String CLIENT_ONLY = "false";
    }

    public static class Request {
        public static final String CLIENT_INFO_APP_NAME = "SBank2.0";
        public static final String CLIENT_INFO_APP_VERSION = "2.7.0.2";
        public static final String CLIENT_INFO_LANG = "sv";
    }

    public static class Headers {
        public static final String X_SMOB_KEY = "X-smob";
        public static final String SPANKKI_USER_AGENT = "spankki/2.4.2";
    }

    public static class QueryKeys {
        public static final String CONTRACT_NR = "contractNr";
        public static final String PRODUCT_CODE = "productCode";
        public static final String PORTFOLIO_ID = "portfolioId";
        public static final String SECURITY_ID = "securityId";
    }

    public static class Storage {
        public static final String SESSION_ID = "sessionId";
        public static final String DEVICE_ID = "deviceId";
        public static final String HARDWARE_ID = "hardwareId";
        public static final String LOGIN_TOKEN = "loginToken";
        public static final String CUSTOMER_ID = "customerId";
        public static final String CUSTOMER_USER_ID = "customerUserId";
        public static final String CUSTOMER_ENTITY = "customerEntity";
    }

    public static class StatusMessages {
        public static final String INTERNAL_ERROR_CODE = "98";
        public static final String INTERNAL_ERROR_MESSAGE = "INTERNAL_SERVER_ERROR";
        public static final String SESSION_EXPIRED_MESSAGE = "SESSION_EXPIRED";
        public static final String USER_LOCKED = "USER_LOCKED";
    }

    public static class IdTags {
        public static final String ACCOUNT_ID = "accountId";
        public static final String PAGE = "page";
        public static final String CONTRACT_NR = "contractNr";
        public static final String FROM_DATE = "fromDate";
        public static final String TO_DATE = "toDate";
    }

    public static class Investments {
        public static final String ACCOUNT_ID_PREFIX = "FUND-";
    }

    public static class Regex {
        public static final String WHITE_SPACE = "\\s+";
    }

    public static class LogTags {
        public static final LogTag CREDIT_CARD = LogTag.from("#spankki_creditcard");
        public static final LogTag CREDIT_CARD_TRANSACTIONS =
                LogTag.from("#spankki_creditcard_transactions");
        public static final LogTag LOAN = LogTag.from("#spankki_loans");
        public static final LogTag LOAN_DETAILS = LogTag.from("#spankki_loan_details");
    }
}

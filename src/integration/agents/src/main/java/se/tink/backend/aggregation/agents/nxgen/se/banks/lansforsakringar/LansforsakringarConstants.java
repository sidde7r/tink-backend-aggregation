package se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar;

import java.util.regex.Pattern;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.utils.log.LogTag;
import se.tink.backend.aggregation.nxgen.core.account.AccountTypeMapper;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.i18n_aggregation.LocalizableEnum;
import se.tink.libraries.i18n_aggregation.LocalizableKey;

public class LansforsakringarConstants {

    public static final int MAX_BANKID_LOGIN_ATTEMPTS = 50;

    public static class Urls {
        public static final String BASE = "https://mobil.lansforsakringar.se/";

        public static final URL FETCH_ACCOUNTS = new URL(BASE + ApiService.FETCH_ACCOUNTS);
        public static final URL INIT_BANKID = new URL(BASE + ApiService.INIT_BANKID);
        public static final URL LOGIN_BANKID = new URL(BASE + ApiService.LOGIN_BANKID);
        public static final URL RENEW_SESSION = new URL(BASE + ApiService.RENEW_SESSION);
        public static final URL FETCH_TRANSACTIONS = new URL(BASE + ApiService.FETCH_TRANSACTIONS);
        public static final URL FETCH_UPCOMING = new URL(BASE + ApiService.FETCH_UPCOMING);
        public static final URL FETCH_CARDS = new URL(BASE + ApiService.FETCH_CARDS);
        public static final URL FETCH_PENSION_WITH_LIFE_INSURANCE =
                new URL(BASE + ApiService.PENSION_WITH_LIFE_INSURANCE);
        public static final URL FETCH_PENSION_OVERVIEW =
                new URL(BASE + ApiService.PENSION_OVERVIEW);
        public static final URL FETCH_PENSION_WITH_LIFE_INSURANCE_AGREEMENT =
                new URL(BASE + ApiService.PENSION_WITH_LIFE_INSURANCE_AGREEMENT);
        public static final URL FETCH_LOAN_OVERVIEW = new URL(BASE + ApiService.LOAN_OVERVIEW);
        public static final URL FETCH_LOAN_DETAILS = new URL(BASE + ApiService.LOAN_DETAILS);
        public static final URL FETCH_PAYMENT_SAVED_RECEPIENTS =
                new URL(BASE + ApiService.PAYMENT_SAVED_RECEPIENTS);
        public static final URL FETCH_TRANSFER_SAVED_RECEPIENTS =
                new URL(BASE + ApiService.TRANSFER_SAVED_RECEPIENTS);
        public static final URL FETCH_PAYMENT_ACCOUNTS =
                new URL(BASE + ApiService.PAYMENT_ACCOUNTS);
        public static final URL FETCH_TRANSFER_SOURCE_ACCOUNTS =
                new URL(BASE + ApiService.TRANSFER_SOURCE_ACCOUNTS);
        public static final URL FETCH_TRANSFER_DESTINATION_ACCOUNTS =
                new URL(BASE + ApiService.TRANSFER_DESTINATION_ACCOUNTS);
        public static final URL FETCH_EINVOICES = new URL(BASE + ApiService.EINVOICES);
        public static final URL EXECUTE_DIRECT_TRANSFER_VALIDATE =
                new URL(BASE + ApiService.DIRECT_TRANSFER_VALIDATE);
        public static final URL EXECUTE_DIRECT_TRANSFER =
                new URL(BASE + ApiService.DIRECT_TRANSFER);
        public static final URL EXECUTE_EXTERNAL_TRANSFER_CREATE_BANKID_REFERENCE =
                new URL(BASE + ApiService.EXTERNAL_TRANSFER_CREATE_BANKID_REFERENCE);
        public static final URL EXECUTE_EXTERNAL_TRANSFER_BANKID_SIGN_VERIFICATION =
                new URL(BASE + ApiService.EXTERNAL_TRANSFER_BANKID_SIGN_VERIFICATION);
        public static final URL EXECUTE_PAYMENT_ADD_INVOICE =
                new URL(BASE + ApiService.PAYMENT_ADD_INVOICE);
        public static final URL EXECUTE_PAYMENT_AND_TRANSFER_VALIDATE =
                new URL(BASE + ApiService.PAYMENT_AND_TRANSFER_VALIDATE);
        public static final URL EXECUTE_PAYMENT_AND_TRANSFER_CREATE_BANKID_REFERENCE =
                new URL(BASE + ApiService.PAYMENT_AND_TRANSFER_CREATE_BANKID_REFERENCE);
        public static final URL EXECUTE_PAYMENT_AND_TRANSFER_BANKID_SIGN_VERIFICATION =
                new URL(BASE + ApiService.PAYMENT_AND_TRANSFER_BANKID_SIGN_VERIFICATION);
        public static final URL FETCH_CARD_TRANSACTIONS =
                new URL(BASE + ApiService.CARD_TRANSACTIONS);
        public static final URL FETCH_ISK = new URL(BASE + ApiService.ISK);
        public static final URL FETCH_INSTRUMENTS_FUND =
                new URL(BASE + ApiService.INSTRUMENTS_FUND);
        public static final URL FETCH_INSTRUMENTS_STOCK =
                new URL(BASE + ApiService.INSTRUMENTS_STOCK);
        public static final URL FETCH_PORTFOLIO_CASH_BALANCE =
                new URL(BASE + ApiService.PORTFOLIO_CASH_BALANCE);
        public static final URL FETCH_INSTRUMENT_WITH_ISIN =
                new URL(BASE + ApiService.INSTRUMENT_WITH_ISIN);
    }

    public static class ApiService {
        public static final String FETCH_TRANSACTIONS = "es/deposit/gettransactions/3.0";
        public static final String FETCH_ACCOUNTS = "appoutlet/startpage/getengagements/4.0";
        public static final String INIT_BANKID = "appoutlet/security/user/bankid/authenticate";
        public static final String LOGIN_BANKID = "appoutlet/security/user/bankid/login/3.0";
        public static final String RENEW_SESSION = "appoutlet/security/session/renew";
        public static final String FETCH_UPCOMING = "appoutlet/account/upcoming/7.0";
        public static final String FETCH_CARDS = "appoutlet/card/list/5.0";
        public static final String PENSION_WITH_LIFE_INSURANCE =
                "es/lifeinsurance/getengagements/1.0";
        public static final String PENSION_WITH_LIFE_INSURANCE_AGREEMENT =
                "es/lifeinsurance/getagreement/1.0";
        public static final String PENSION_OVERVIEW = "appoutlet/pension/overview/withtotal/2.0";
        public static final String LOAN_OVERVIEW = "appoutlet/loan/loans/withtotal";
        public static final String LOAN_DETAILS = "appoutlet/loan/details";
        public static final String PAYMENT_SAVED_RECEPIENTS = "appoutlet/payment/savedrecipients";
        public static final String TRANSFER_SAVED_RECEPIENTS =
                "appoutlet/account/transferrablewithsavedrecipients";
        public static final String PAYMENT_ACCOUNTS = "appoutlet/payment/paymentaccount";
        public static final String TRANSFER_SOURCE_ACCOUNTS =
                "appoutlet/account/transferrable/2.0?direction=from";
        public static final String TRANSFER_DESTINATION_ACCOUNTS =
                "appoutlet/account/transferrable/3.0?direction=to";
        public static final String EINVOICES = "es/payment/geteinvoices/1.0";
        public static final String DIRECT_TRANSFER_VALIDATE =
                "appoutlet/directtransfer/validate/2.0";
        public static final String DIRECT_TRANSFER = "appoutlet/directtransfer/2.0";
        public static final String EXTERNAL_TRANSFER_CREATE_BANKID_REFERENCE =
                "appoutlet/directtransfer/createbankidreference/2.0";
        public static final String EXTERNAL_TRANSFER_BANKID_SIGN_VERIFICATION =
                "appoutlet/directtransfer/bankid/2.0";
        public static final String PAYMENT_ADD_INVOICE = "appoutlet/payment/einvoice/addeinvoices";
        public static final String PAYMENT_AND_TRANSFER_VALIDATE =
                "appoutlet/unsigned/paymentsandtransfers/validate/2.0";
        public static final String PAYMENT_AND_TRANSFER_CREATE_BANKID_REFERENCE =
                "appoutlet/unsigned/paymentsandtransfers/bankid/createreference/2.0";
        public static final String PAYMENT_AND_TRANSFER_BANKID_SIGN_VERIFICATION =
                "appoutlet/unsigned/paymentsandtransfers/bankid/send/2.0";
        public static final String CARD_TRANSACTIONS = "appoutlet/card/transaction";
        public static final String ISK = "appoutlet/depot/investmentsavings/3.0";
        public static final String INSTRUMENTS_FUND =
                "appoutlet/depot/holding/fund/securityholdings/withdetails/2.0";
        public static final String INSTRUMENTS_STOCK =
                "appoutlet/depot/holding/share/securityholdings/2.0";
        public static final String PORTFOLIO_CASH_BALANCE =
                "appoutlet/depot/holding/depotcashbalance/2.0";
        public static final String INSTRUMENT_WITH_ISIN =
                "appoutlet/depot/trading/share/instrumentwithisin";
    }

    public static class StorageKeys {
        public static final String SSN = "ssn";
        public static final String NAME = "name";
        public static final String TICKET = "ticket";
        public static final String ENTERPRISE_SERVICE_PRIMARY_SESSION =
                "enterpriseServicesPrimarySession";
        public static final String CUSTOMER_NAME = "name";
    }

    public static class HeaderKeys {
        public static final String DEVICE_ID = "DeviceId";
        public static final String DEVICE_INFO = "deviceInfo";
        public static final String USER_AGENT = "User-Agent";
        public static final String ERROR_CODE = "Error-Code";
        public static final String USER_SESSION = "USERSESSION";
        public static final String UTOKEN = "Utoken";
    }

    public static class Accounts {
        public static final AccountTypeMapper ACCOUNT_TYPE_MAPPER =
                AccountTypeMapper.builder()
                        .put(AccountTypes.CHECKING, "CHECKING", "DEBIT")
                        .put(AccountTypes.SAVINGS, "SAVINGS")
                        .put(AccountTypes.PENSION, "PENSION")
                        .put(AccountTypes.CREDIT_CARD, "CREDIT_CARD_PRIVATE")
                        .build();
        public static final String CURRENCY = "SEK";
        public static final String SHARE_DEPOT_ACCOUNT_NAME = "Aktiedepå";
        public static final Pattern PATTERN_BG_RECIPIENT = Pattern.compile("^\\d{3,4}-\\d{4}");
        public static final Pattern PATTERN_PG_RECIPIENT = Pattern.compile("^\\d{1,7}-\\d");
    }

    public static class Fetcher {
        public static final int START_PAGE = 0;
        public static final int CREDIT_CARD_START_PAGE = 1;
        public static final String CUSTOMER_PROFILE_TYPE = "CUSTOMER";
        public static final String BOOKED_TRANSACTION_STATUS = "BOOKED";
        public static final String PENDING_TRANSACTION_STATUS = "PENDING";
        public static final String PENSION_ACCOUNT_TYPE = "PENSION";
        public static final String EINVOICE_UNAPPROVED_STATUS = "NEW";
    }

    public static class LogTags {
        public static final LogTag CREDIT_CARD = LogTag.from("lansforsakringar_credit_card");
        public static final LogTag UNKNOWN_LOAN_TYPE = LogTag.from("lansforsakringar_unknown_loan");
        public static final LogTag UNKNOWN_PENSION_TYPE =
                LogTag.from("lansforkaringar_unknown_pension");
    }

    public enum UserMessage implements LocalizableEnum {
        MUST_ACCEPT_TERMS(
                new LocalizableKey(
                        "The first time you use your BankId, you need to accept the terms and conditions. Please login to the Länsförsäkringar with your moible BankId to do this"));
        private final LocalizableKey userMessage;

        UserMessage(LocalizableKey userMessage) {
            this.userMessage = userMessage;
        }

        @Override
        public LocalizableKey getKey() {
            return this.userMessage;
        }
    }

    public class QueryKeys {
        public static final String DEPOT_NUMBER = "depotNumber";
        public static final String ISIN_CODE = "isinCode";
    }
}

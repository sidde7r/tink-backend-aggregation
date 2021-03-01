package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb;

import com.google.common.collect.ImmutableList;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.bankid.status.BankIdStatus;
import se.tink.backend.aggregation.compliance.account_capabilities.AccountCapabilities.Answer;
import se.tink.backend.aggregation.nxgen.core.account.AccountTypeMapper;
import se.tink.backend.aggregation.nxgen.core.account.GenericTypeMapper;
import se.tink.backend.aggregation.nxgen.core.account.TypeMapper;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.i18n.LocalizableEnum;
import se.tink.libraries.i18n.LocalizableKey;

public class SebConstants {
    public static final String DEFAULT_CURRENCY = "SEK";
    public static final int AGE_LIMIT = 18;

    public static class Urls {
        public static URL getUrl(String baseUrl, String path) {
            return new URL(baseUrl + path);
        }

        public static final String CHALLENGE = "/auth/dp/v2/challenge";
        public static final String VERIFY = "/auth/dp/v2/verify";
        public static final String AUTHENTICATE = "/auth/bid/v2/authentications";
        public static final String INITIATE_SESSION = "/PC_BankInit11Session01.asmx/Execute";
        public static final String ACTIVATE_SESSION = "/PC_BankAktivera01Session01.asmx/Execute";
        public static final String ACTIVATE_ROLE = "/PC_BankAktivera01Roll01.asmx/Execute";
        public static final String LIST_CARDS = "/PC_BankLista11Kort_privat05.asmx/Execute";
        public static final String LIST_LOANS = "/PC_BankLista11Laninfo_privat03.asmx/Execute";
        public static final String LIST_TRANSACTIONS = "/PC_BankLista01Rorelse_ftg03.asmx/Execute";
        public static final String LIST_RESERVED_TRANSACTIONS =
                "/PC_BankLista01Skydd01.asmx/Execute";
        public static final String LIST_UPCOMING_TRANSACTIONS =
                "/PC_BankLista11Komm_uppdrag02.asmx/Execute";
        public static final String LIST_PENDING_CREDIT_CARD_TRANSACTIONS =
                "/PC_BankLista11Ofakt_korttran02.asmx/Execute";
        public static final String LIST_BOOKED_CREDIT_CARD_TRANSACTIONS =
                "/PC_BankLista11Fakt_korttran02.asmx/Execute";
        public static final String LIST_INVESTMENT_ACCOUNTS =
                "/Tl_forsakringLista11Enga01.asmx/Execute";
        public static final String INVESTMENT_ACCOUNT_DETAILS =
                "/PC_BankHamta11Savingsvarde01.asmx/Execute";
    }

    public static class HeaderKeys {
        public static final String X_SEB_UUID = "x-seb-uuid";
        public static final String X_SEB_CSRF = "x-seb-csrf";
    }

    public static class Authentication {
        public static final GenericTypeMapper<BankIdStatus, String> statusMapper =
                GenericTypeMapper.<BankIdStatus, String>genericBuilder()
                        .put(BankIdStatus.DONE, "complete")
                        .put(BankIdStatus.WAITING, "pending")
                        .put(BankIdStatus.FAILED_UNKNOWN, "failed")
                        .setDefaultTranslationValue(BankIdStatus.FAILED_UNKNOWN)
                        .build();

        public static final GenericTypeMapper<BankIdStatus, String> hintCodeMapper =
                GenericTypeMapper.<BankIdStatus, String>genericBuilder()
                        .put(BankIdStatus.TIMEOUT, "expired_transaction")
                        .put(BankIdStatus.NO_CLIENT, "seb_unknown_bankid")
                        .put(BankIdStatus.EXPIRED_AUTOSTART_TOKEN, "start_failed")
                        .put(BankIdStatus.CANCELLED, "cancelled", "user_cancel")
                        .setDefaultTranslationValue(BankIdStatus.FAILED_UNKNOWN)
                        .build();
    }

    public static class InitResult {
        public static final String OK = "OK";
    }

    public static class ServiceInputKeys {
        public static final String CUSTOMER_TYPE = "CUSTOMERTYPE";
        public static final String CUSTOMER_ID = "KUND_ID";
        public static final String CUSTOMER_ID_EN = "CUSTOMERID";
        public static final String ACCOUNT_TYPE = "KONTO_TYP";
        public static final String CUSTOMER_NUMBER = "SEB_KUND_ID";
        public static final String MAX_ROWS = "MAX_ROWS";
        public static final String CREDIT_CARD_HANDLE = "BILL_UNIT_HDL";
        public static final String PENDING_TRANSACTIONS = "RESERVE_AMT_FL";
        public static final String INVESTMENT_DETAIL_HANDLE = "DETAIL_URL";
        public static final String EXTRA_INFO = "EXTRA_INFO";
    }

    public static class ServiceInputValues {
        public static final String PRIVATE = "P";
        public static final String BUSINESS = "S";
        public static final String YES = "Y";
        public static final String DEFAULT_ACCOUNT_TYPE = "ICKEFOND";
        public static final int MAX_ROWS = 110;
    }

    // ROR_TYP field in TransactionEntity
    public static class TransactionType {
        public static final String FOREIGN_CARD_TRANSACTION = "5";
    }

    public static class StorageKeys {
        public static final String CUSTOMER_NAME = "customerName";
        public static final String CUSTOMER_NUMBER = "customerNumber";
        public static final String SHORT_USERID = "shortUserId";
        public static final String SSN = "ssn";
        public static final String ACCOUNT_CUSTOMER_ID = "customerId";
        public static final String CREDIT_CARD_ACCOUNT_HANDLE_PREFIX = "card_handle:";
        public static final String COMPANY_NAME = "companyName";
        public static final String HOLDER_NAME = "holderName";
    }

    public static class SystemCode {
        public static final int BANKID_NOT_AUTHORIZED = 2;
        public static final int KYC_ERROR = 9200;
    }

    public enum UserMessage implements LocalizableEnum {
        MUST_AUTHORIZE_BANKID(
                new LocalizableKey(
                        "The first time you use your mobile BankId you have to verify it with your Digipass. Login to the SEB-app with your mobile BankID to do this.")),
        DO_NOT_SUPPORT_YOUTH(
                new LocalizableKey(
                        "It looks like you have SEB Ung. Unfortunately we currently only support SEB's standard login.")),
        MUST_ANSWER_KYC(
                new LocalizableKey(
                        "To continue using this app you must answer some questions from your bank. Please log in with your bank's app or website."));

        private final LocalizableKey userMessage;

        UserMessage(LocalizableKey userMessage) {
            this.userMessage = userMessage;
        }

        @Override
        public LocalizableKey getKey() {
            return userMessage;
        }
    }

    public static final class AccountTypeCode {
        private AccountTypeCode() {
            throw new AssertionError();
        }

        public static final String PRIVATKONTO = "1";
        // 2: NOTARIATKONTO, SKOGSKONTO, FÖRETAGSKONTO
        // sometimes 1 has KTOSLAG_TXT=NOTARIATKONTO, but shows as product=Privatkonto on OB
        public static final String OTHER = "2";
        public static final String BUSINESS_ACCOUNT = "2";
        public static final String PERSONALLONEKONTO = "3";
        public static final String ENKLA_SPARKONTOT_FORETAG = "12";
        public static final String ENKLA_SPARKONTOT = "16";
        public static final String SPECIALINLANINGSKONTO = "17";
        public static final String FUND = "22";
        public static final String IPS = "27";
        public static final String PLACERINGSKONTO = "35";
        public static final String ISK_KAPITALKONTO = "54";

        // To handle subtypes of OTHER, account capabilities mapping tries "<code>:<name>" first
        public static final String OTHER_NOTARIATKONTO = "2:NOTARIATKONTO";
    }

    public static final AccountTypeMapper ACCOUNT_TYPE_MAPPER =
            AccountTypeMapper.builder()
                    .put(
                            AccountTypes.CHECKING,
                            AccountTypeCode.PRIVATKONTO,
                            AccountTypeCode.PERSONALLONEKONTO,
                            AccountTypeCode.SPECIALINLANINGSKONTO)
                    .put(
                            AccountTypes.SAVINGS,
                            AccountTypeCode.ENKLA_SPARKONTOT,
                            AccountTypeCode.ENKLA_SPARKONTOT_FORETAG)
                    .put(
                            AccountTypes.INVESTMENT,
                            AccountTypeCode.FUND,
                            AccountTypeCode.IPS,
                            AccountTypeCode.PLACERINGSKONTO,
                            AccountTypeCode.ISK_KAPITALKONTO)
                    .put(AccountTypes.OTHER, AccountTypeCode.OTHER)
                    .build();

    public static final AccountTypeMapper BUSINESS_ACCOUNT_TYPE_MAPPER =
            AccountTypeMapper.builder()
                    .put(
                            AccountTypes.CHECKING,
                            AccountTypeCode.SPECIALINLANINGSKONTO,
                            AccountTypeCode.BUSINESS_ACCOUNT)
                    .put(AccountTypes.SAVINGS, AccountTypeCode.ENKLA_SPARKONTOT_FORETAG)
                    .put(
                            AccountTypes.INVESTMENT,
                            AccountTypeCode.FUND,
                            AccountTypeCode.IPS,
                            AccountTypeCode.PLACERINGSKONTO,
                            AccountTypeCode.ISK_KAPITALKONTO)
                    .build();
    public static final ImmutableList<AccountTypes> ALLOWED_ACCOUNT_TYPES =
            ImmutableList.<AccountTypes>builder()
                    .add(AccountTypes.SAVINGS)
                    .add(AccountTypes.CHECKING)
                    .add(AccountTypes.INVESTMENT)
                    .build();

    public static final class AccountCapabilities {
        private AccountCapabilities() {
            throw new AssertionError();
        }

        public static final TypeMapper<Answer> CAN_WITHDRAW_CASH_MAPPER =
                TypeMapper.<Answer>builder()
                        .put(
                                Answer.YES,
                                AccountTypeCode.PRIVATKONTO,
                                AccountTypeCode.PERSONALLONEKONTO)
                        .put(
                                Answer.NO,
                                AccountTypeCode.ENKLA_SPARKONTOT,
                                AccountTypeCode.ENKLA_SPARKONTOT_FORETAG,
                                AccountTypeCode.SPECIALINLANINGSKONTO,
                                AccountTypeCode.PLACERINGSKONTO,
                                AccountTypeCode.ISK_KAPITALKONTO,
                                AccountTypeCode.IPS,
                                AccountTypeCode.OTHER_NOTARIATKONTO)
                        .build();

        public static final TypeMapper<Answer> CAN_PLACE_FUNDS_MAPPER =
                TypeMapper.<Answer>builder()
                        .put(
                                Answer.YES,
                                AccountTypeCode.PRIVATKONTO,
                                AccountTypeCode.PERSONALLONEKONTO,
                                AccountTypeCode.ENKLA_SPARKONTOT,
                                AccountTypeCode.SPECIALINLANINGSKONTO,
                                AccountTypeCode.PLACERINGSKONTO,
                                AccountTypeCode.ENKLA_SPARKONTOT_FORETAG,
                                AccountTypeCode.ISK_KAPITALKONTO,
                                AccountTypeCode.IPS,
                                AccountTypeCode.OTHER_NOTARIATKONTO)
                        .build();

        public static final TypeMapper<Answer> CAN_EXECUTE_EXTERNAL_TRANSFER_MAPPER =
                TypeMapper.<Answer>builder()
                        .put(
                                Answer.YES,
                                AccountTypeCode.PRIVATKONTO,
                                AccountTypeCode.PERSONALLONEKONTO,
                                AccountTypeCode.ENKLA_SPARKONTOT,
                                AccountTypeCode.ISK_KAPITALKONTO,
                                AccountTypeCode.ENKLA_SPARKONTOT_FORETAG)
                        .put(
                                Answer.NO,
                                AccountTypeCode.SPECIALINLANINGSKONTO,
                                AccountTypeCode.PLACERINGSKONTO,
                                AccountTypeCode.OTHER_NOTARIATKONTO)
                        .build();

        public static final TypeMapper<Answer> CAN_RECEIVE_EXTERNAL_TRANSFER_MAPPER =
                TypeMapper.<Answer>builder()
                        .put(
                                Answer.YES,
                                AccountTypeCode.PRIVATKONTO,
                                AccountTypeCode.PERSONALLONEKONTO,
                                AccountTypeCode.ENKLA_SPARKONTOT,
                                AccountTypeCode.ISK_KAPITALKONTO,
                                AccountTypeCode.ENKLA_SPARKONTOT_FORETAG)
                        .put(
                                Answer.NO,
                                AccountTypeCode.SPECIALINLANINGSKONTO,
                                AccountTypeCode.PLACERINGSKONTO,
                                AccountTypeCode.OTHER_NOTARIATKONTO)
                        .build();
    }
}

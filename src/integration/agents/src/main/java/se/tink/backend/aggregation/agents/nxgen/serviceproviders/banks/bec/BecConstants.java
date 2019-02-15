package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec;

import com.google.common.collect.ImmutableMap;
import se.tink.backend.aggregation.agents.utils.log.LogTag;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.models.Instrument;

public final class BecConstants {
    public static final class Url {
        static final String APP_SYNC = "/appsync";
        static final String LOGIN_CHALLENGE = "/logon/challenge";
        static final String FETCH_ACCOUNTS = "/konto";
        static final String FETCH_ACCOUNT_DETAIL = "/konto/kontodetaljer";
        static final String FETCH_ACCOUNT_TRANSACTIONS = "/konto/posteringer";
        static final String FETCH_ACCOUNT_UPCOMING_TRANSACTIONS = "/konto/kommendebetalinger";

        static final String FETCH_CARD = "/kort/oversigt";
        static final String FETCH_LOAN = "/tk/laan";
        static final String FETCH_LOAN_DETAILS = "/tk/laandetaljer";
        static final String FETCH_DEPOT = "/depot/oversigt";

        static final String LOGOUT = "/logoff";

        static final String LOAN_NUMBER_PARAMETER = "loanNumber";
        static final String ACCOUNT_ID_PARAMETER = "accountId";
    }

    public static final class Crypto {

        public static final String SIGNING_CERTIFICATE = "-----BEGIN CERTIFICATE-----\n"
                + "MIIDwjCCAqqgAwIBAgIETb6a0TANBgkqhkiG9w0BAQQFADCBojELMAkGA1UEBhMC"
                + "REsxETAPBgNVBAgTCFJvc2tpbGRlMR4wHAYDVQQKExVCYW5rZXJuZXMgRURCIENl"
                + "bnRyYWwxHTAbBgNVBAsTFFRla25pc2sgdWR2LiBzdXBwb3J0MR4wHAYDVQQDExVt"
                + "b2JpbGJhbmsucHJvZC5iZWMuZGsxITAfBgkqhkiG9w0BCQEWEmJlY3NzbGNlcnRz"
                + "QGJlYy5kazAeFw0xMTA1MDIxMTUxNDVaFw00MTA0MjQxMTUxNDVaMIGiMQswCQYD"
                + "VQQGEwJESzERMA8GA1UECBMIUm9za2lsZGUxHjAcBgNVBAoTFUJhbmtlcm5lcyBF"
                + "REIgQ2VudHJhbDEdMBsGA1UECxMUVGVrbmlzayB1ZHYuIHN1cHBvcnQxHjAcBgNV"
                + "BAMTFW1vYmlsYmFuay5wcm9kLmJlYy5kazEhMB8GCSqGSIb3DQEJARYSYmVjc3Ns"
                + "Y2VydHNAYmVjLmRrMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAki1j"
                + "mfDve8au2xDbRF0h2xkWN7IChwyg6Wq+q58GnR3RqiibpirD64g18TuWskdM4ejV"
                + "Tu64soItfqYM3zab5/m5aRTFVg6zcFakoBXsS2hi3Wa6iZX+7SKUVQrUixAbWW/0"
                + "hBnyAsasqRRzcXeQ2+TBu2xUQ5I02NbA5dms97GJT6iJ37gKzHICUxjjByG/bIfQ"
                + "Fjjl5mMJl4ppcxrIhNAXP5zsn2ae6i5nYBwiaaJ9UMNv1ZhLo8fDTHKmnJCOXXJU"
                + "l5IUO9EXetuKOv4GQuFm5ZF++HXJsYyhY7fTzI/fIeIo6BzC8/AcRFNltUFR0kdv"
                + "CsWhKgaTHlX3mjjnmwIDAQABMA0GCSqGSIb3DQEBBAUAA4IBAQANU74YKCuifHoK"
                + "p+5Pvs5VN2cfwPq+CUX37djQW5Bf+mFkK18MwiH9w+PBPKGHOsKDZ/MypkCaXevi"
                + "fXnnn+AzqdgfeMTDXdHLiO+udFz2ezI+OKUuyiiEBfeUdj1j+j6vK9YKhSJN95V3"
                + "SzppeeoxiAvvtCOTfse+ywIbdhsGKm3vtrlkZ2ZQWG7+LNMY0t4ueOtjes6uM+oc"
                + "1IISfsrOj/pnIfkhdjhqBRqW93Xvqv7Peop4QItGH3m97O0QqZZgY/fRndZD7zED"
                + "qQR4eDzrA+p/vI1PMS0mVh/uQHHxaartMFgFWhKZAeTnOQ2eIgaUrh7mI396jNaO"
                + "mBF9hE/h\n"
                + "-----END CERTIFICATE-----\n";

        public static final String PUBLIC_KEY_PRODUCTION_SALT = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAI/e+"
                + "AAFbsYSUMkj1Hq6gloeE5iC/Vt8DmkwiPwxAHkXE19pkcrTiOPVeME4kvEUey4Qb2ty8zIjvy1SoxAKk0Nku0xu4EGyJn+"
                + "/1CDRYp4xOZyfXHVk4y18+4amRrGgLFDh4wg3cesCaJ5ETxoM7lvqb/R+zsg3ZtweJsD1pBxxFdjc7B9BKfDnVUk08CPAZ"
                + "hM9DGZ5HpiBV//sz+TusDZTvWtYtZGBZP6Bc4ApKOd3fS6NFE2IP8c2vJ7j2oeP0aDh/WW8ad5BY/sW0iKTiqi3FFxf1oo"
                + "DjBpfmCcmlEcST4cWZyPfu5p8SZXyKGZ3rtKRoPdMkYBHcLXDweHvSMgAAAAAx";

        public static final String AES = "AES";
        public static final String X509 = "X509";
        public static final String UTF8 = "UTF-8";
    }

    static final class Meta {

        static final String APP_TYPE = "mb0";
        static final String APP_VERSION = "5.1.0";
        static final String LOCALE = "en-GB";
        static final String OS_VERSION = "Android 6.0";
        static final String DEVICE_TYPE = "HTC / HTC One_M8 / MRA58K release-keys / htc_europe / htc_m8 / htc / htc/htc_europe/htc_m8:6.0/MRA58K/662736.4:user/release-keys / htc_m8 / qcom";
        static final String SCREEN_SIZE = "1440*2392";
        static final String BANK_ID = "2";

        static final String LABEL = "MB0-000";
        static final String CIPHER = "AES/CBC/PKCS5Padding";

    }

    public static final class Header {
        static final String PRAGMA_KEY = "Pragma";
        static final String PRAGMA_VALUE = "BECJson/1.0";

        static final String QUERY_PARAM_VERSION_KEY = "version";
        static final String QUERY_PARAM_VERSION_VALUE = "v5";
        static final String QUERY_PARAM_ICONTYPE_KEY = "iconType";
        static final String QUERY_PARAM_ICONTYPE_VALUE = "4";
        static final String QUERY_PARAM_ACCOUNT_ID_KEY = "accountId";
        static final String QUERY_PARAM_BROWSE_ID_KEY = "browseId";
        static final String QUERY_PARAM_NO_DAYS_AHEAD_KEY = "noOfDaysAhead";
        static final String QUERY_PARAM_NO_OF_RECORDS_KEY = "noOfRecords";
        static final String QUERY_PARAM_SEARCH_FROM_AMOUNT_KEY = "searchFromAmount";
        static final String QUERY_PARAM_SEARCH_FROM_DATE_KEY = "searchFromDate";
        static final String QUERY_PARAM_SEARCH_TEXT = "searchText";
        static final String QUERY_PARAM_SEARCH_TO_AMOUNT_KEY = "searchToAmount";
        static final String QUERY_PARAM_SEARCH_TO_DATE_KEY = "searchToDate";
    }

    public static final class ErrorMessage {
        public static final String INVALID_CREDENTIAL = "cpr no./user no. or pin code is incorrect";
        public static final String PIN_LOCKED = "your chosen pin code is locked";
        public static final String NO_MORTGAGE = "you have not taken out a mortgage loan through us";
        public static final String LOAN_NO_DETAILS_EXIST = "no details exist";
    }

    public static final class Log {
        public static final LogTag UNKOWN_ACCOUNT_TYPE = LogTag.from("#dk_bec_unknown_account_type");
        public static final LogTag UNKNOWN_CREDITCARD = LogTag.from("#dk_bec_unknown_creditcard_type");
        public static final LogTag LOANS = LogTag.from("#dk_bec_loan");
        public static final LogTag LOAN_FAILED = LogTag.from("#dk_bec_loan_failed");
        public static final LogTag INVESTMENT_PAPER_TYPE = LogTag.from("#dk_bec_investment_paper_type");
        public static final LogTag INVESTMENT_STOCKS = LogTag.from("#dk_bec_investment_stocks");
    }

    public static final class CreditCard {
        public static final String STATUS_ACTIVE = "active";
    }

    // Lookups are String.contains().
    public static final ImmutableMap<String, AccountTypes> ACCOUNT_TYPES = ImmutableMap.<String, AccountTypes>builder()
            .put("aldersopsparing", AccountTypes.SAVINGS)
            .put("børneopsparing", AccountTypes.SAVINGS)
            .put("ratepension", AccountTypes.PENSION)
            .put("konto personale", AccountTypes.CHECKING)
            .put("personalekonto", AccountTypes.CHECKING)
            .put("young money", AccountTypes.CHECKING)
            .put("mastercard", AccountTypes.CREDIT_CARD)
            .put("personalelån", AccountTypes.LOAN)
            .put("kapitalpension", AccountTypes.INVESTMENT)
            .put("spar nord stjernekonto", AccountTypes.SAVINGS)
            .put("stjernekonto", AccountTypes.CHECKING)
            .put("coop konto", AccountTypes.OTHER)
            .put("ung konto", AccountTypes.CHECKING)
            .put("konto", AccountTypes.CHECKING)
            .put("18-27 konto", AccountTypes.CHECKING)
            .put("lønkonto", AccountTypes.CHECKING)
            .put("al-flex-start", AccountTypes.SAVINGS)
            .put("opsparingskonto", AccountTypes.SAVINGS)
            .put("uddannelseskonto", AccountTypes.SAVINGS)
            .put("driftskonto", AccountTypes.OTHER)
            .put("foreningskonto", AccountTypes.OTHER)
            .put("personale budgetkonto", AccountTypes.OTHER)
            .put("gb masterkonto", AccountTypes.OTHER)
            .put("flexkonto", AccountTypes.OTHER)
            .put("jackpot", AccountTypes.SAVINGS)
            .put("studiekonto", AccountTypes.OTHER)
            .put("multikonto", AccountTypes.OTHER)
            .put("boligopsparing", AccountTypes.SAVINGS)
            .put("stjerneplus", AccountTypes.OTHER)
            .put("budgetkonto", AccountTypes.OTHER)
            .put("superløn", AccountTypes.CHECKING)
            .put("al-børne plus", AccountTypes.SAVINGS)
            .put("lommepengekonto", AccountTypes.CHECKING)
            .put("al-formueflex", AccountTypes.SAVINGS)
            .put("indlån", AccountTypes.SAVINGS)
            .put("spar nord studiekonto", AccountTypes.SAVINGS)
            .put("aktionærkonto", AccountTypes.OTHER)
            .put("ungdomsopsparing", AccountTypes.SAVINGS)
            .put("vestjyskungosparing", AccountTypes.SAVINGS)
            .put("konfirmandkonto", AccountTypes.SAVINGS)
            .put("friværdikonto", AccountTypes.OTHER)
            .put("boliglån", AccountTypes.LOAN)
            .put("opsparing", AccountTypes.SAVINGS)
            .put("fynske teen", AccountTypes.CHECKING)
            .put("børnebørnskonto", AccountTypes.SAVINGS)
            .put("andelsboliglån", AccountTypes.LOAN)
            .put("opsparingsinvest personale", AccountTypes.SAVINGS)
            .put("aktieKonto udløbet", AccountTypes.OTHER)
            .put("andelsprioritetslån", AccountTypes.LOAN)
            .put("udbetalingskonto", AccountTypes.OTHER)
            .put("kredit", AccountTypes.OTHER)
            .build();

    public static final ImmutableMap<String, Instrument.Type> INSTRUMENT_TYPES = ImmutableMap.<String, Instrument.Type>builder()
            .put("1", Instrument.Type.STOCK)
            .build();

    public static final class Loan {
        public static final String INTEREST_DETAILS_KEY = "Yearly interest";
    }
}

package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import lombok.experimental.UtilityClass;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.models.Instrument;
import se.tink.backend.aggregation.agents.utils.log.LogTag;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.NemId2FAMethod;
import se.tink.libraries.i18n_aggregation.LocalizableKey;

@UtilityClass
public class BecConstants {

    @UtilityClass
    public class ScaOptions {
        public static final String CODEAPP_OPTION = "codeapp";
        public static final String KEYCARD_OPTION = "keycard";
        public static final Map<String, NemId2FAMethod>
                SCA_OPTION_TO_SUPPORTED_NEM_ID_METHOD_MAPPING =
                        ImmutableMap.of(
                                CODEAPP_OPTION, NemId2FAMethod.CODE_APP,
                                KEYCARD_OPTION, NemId2FAMethod.CODE_CARD);

        public static final String MIT_ID_OPTION = "mitid";

        public static final String SCATOKEN_OPTION = "scatoken";
        public static final String DEFAULT_OPTION = "default";
    }

    @UtilityClass
    public class Url {
        static final String APP_SYNC = "/appsync";
        static final String PREPARE_SCA = "/logon/SCAprepare";
        static final String SCA = "/logon/SCA";
        static final String NEMID_POLL = "/logon/challenge/pollstate";
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

    @UtilityClass
    public class Crypto {
        public static final String AES = "AES";
        public static final String PUBLIC_KEY =
                "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAqMJMtCR9EGfj88tptmwS+dTRRzemxCmCwrvdKuSgiwFAobUoTmNUM027rFvDMvGjQMC656UdQMbB6oXcFVCBR3J/ibfPylvrohbi+j8otvH4eQNvpzALG/PEnTC0uX/1hibyPAntuenuNlHWD32gkoWvxLyWEGEXFEMB7kvhojl+8CLheKuRMClaUNfHtIuRwRv2cU+bnWeEAde2tcAenta+9+caNpR6kXrxgTe/orFY5Sb7hkL8WRo4p991ZbOYlVJ2HSykD1c2EGPspgxF7apcepifgJXAr6FSPFcj4/jx5028Rf/wqlSptF20TyluCGjp7LjkkuV95g/Qn3IvnQIDAQAB";
    }

    @UtilityClass
    public class Meta {

        public static final String APP_TYPE = "mb1";
        public static final String APP_VERSION = "7.7.1-105";
        public static final String APP_URL = "bec.mobilbank.b00020://";
        public static final String OS_VERSION = "Android 6.0";
        public static final String DEVICE_TYPE =
                "HTC / HTC One_M8 / MRA58K release-keys / htc_europe / htc_m8 / htc / htc/htc_europe/htc_m8:6.0/MRA58K/662736.4:user/release-keys / htc_m8 / qcom";
        public static final String SCREEN_SIZE = "1440*2392";

        public static final String LABEL = "MB1-008";
        public static final String CIPHER = "AES-CBC-PKCS5/RSA-OAEP-SHA256-MGP1";
    }

    @UtilityClass
    public class Header {
        static final String PRAGMA_KEY = "Pragma";
        static final String PRAGMA_VALUE = "BECJson/1.0";

        static final String QUERY_PARAM_VERSION_KEY = "version";
        static final String QUERY_PARAM_FETCH_INSTRUMENTS_VERSION_VALUE = "v4";
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

    @UtilityClass
    public class ErrorMessages {

        public static final Map<String, String> ERROR_MESSAGES_TO_REASON_MAP;

        public static final String NO_MORTGAGES_REASON = "User does not have any mortgages";

        public static final String FUNCTION_NOT_AVAILABLE_REASON = "Function not available";

        static {
            ERROR_MESSAGES_TO_REASON_MAP =
                    ImmutableMap.<String, String>builder()
                            .put("You have not taken out a mortgage", NO_MORTGAGES_REASON)
                            .put("Du har ikke optaget et realkreditlån", NO_MORTGAGES_REASON)
                            .put("Ingen oplysninger fundet.", NO_MORTGAGES_REASON)
                            .put("The required function is not", FUNCTION_NOT_AVAILABLE_REASON)
                            .put("Den ønskede funktion", FUNCTION_NOT_AVAILABLE_REASON)
                            .build();
            /*
            //not existing in Kibana
            "No details exist."
            "Ingen detaljer findes."
             */
        }

        public static final ImmutableList<String> FUNCTION_NOT_AVAILABLE =
                ImmutableList.of(
                        "Den ønskede funktion er ikke tilgængelig i øjeblikket. Prøv igen senere.",
                        "The required function is not currently available. Try again later.",
                        "Der er desværre ikke adgang i øjeblikket. Prøv venligst igen senere.");

        @UtilityClass
        public class Authentication {

            public static final List<String> INCORRECT_CREDENTIALS =
                    ImmutableList.of(
                            "CPR-nr./brugernummer eller pinkode er forkert. Tjek evt. i din netbank om du er tilmeldt.",
                            "CPR no./user no. or PIN code is incorrect. Check in your Netbank that you are registered.",
                            "Pinkode eller nøgle er forkert. Prøv igen.",
                            "error auth response: The entered code is incorrect. Please try again.");
            public static final String RESET_TOKEN = "error auth response: Reset token";
            public static final String PIN_LOCKED = "Your chosen PIN code is locked.";
            public static final String NEMID_BLOCKED = "NemID is blocked. Contact support.";
            public static final LocalizableKey MIT_ID_NOT_SUPPORTED_YET =
                    new LocalizableKey(
                            "We are sorry, but MitID authentication is not supported yet");
        }
    }

    @UtilityClass
    public class StorageKeys {
        public static final String DEVICE_ID_STORAGE_KEY = "device-id";
        public static final String SCA_TOKEN_STORAGE_KEY = "sca-token";
    }

    @UtilityClass
    public class Log {
        public static final LogTag UNKOWN_ACCOUNT_TYPE =
                LogTag.from("#dk_bec_unknown_account_type");
        public static final LogTag LOANS = LogTag.from("#dk_bec_loan");
        public static final LogTag LOAN_FAILED = LogTag.from("#dk_bec_loan_failed");
        public static final LogTag INVESTMENT_PAPER_TYPE =
                LogTag.from("#dk_bec_investment_paper_type");
        public static final LogTag CREDIT_CARD_FETCH_ERROR =
                LogTag.from("#dk_bec_credit_card_fetch_error");
        public static final LogTag BEC_LOG_TAG = LogTag.from("[BEC]");
    }

    @UtilityClass
    public static final class CreditCard {
        public static final List<String> STATUS_ACTIVE = ImmutableList.of("active", "aktivt");
    }

    // Lookups are String.contains().
    public static final ImmutableMap<String, AccountTypes> ACCOUNT_TYPES =
            ImmutableMap.<String, AccountTypes>builder()
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
                    .put("Kombiløn", AccountTypes.OTHER)
                    .put("Club 18-28", AccountTypes.OTHER)
                    .put("Spar'Op", AccountTypes.SAVINGS)
                    .put("Budget", AccountTypes.OTHER)
                    .put("Billån", AccountTypes.LOAN)
                    .put("Money Bunny", AccountTypes.OTHER)
                    .put("Coop Budget", AccountTypes.SAVINGS)
                    .put("Superlån", AccountTypes.LOAN)
                    .put("Udlån", AccountTypes.LOAN)
                    .put("Anfordring med AL-MC Cash", AccountTypes.CHECKING)
                    .put("Klubkasse", AccountTypes.OTHER)
                    .put("3 års opsigelse", AccountTypes.OTHER)
                    .put("Basal Betaling", AccountTypes.CHECKING)
                    .put("MY step", AccountTypes.OTHER)
                    .put("Garanti", AccountTypes.OTHER)
                    .put("PrivatLøn", AccountTypes.CHECKING)
                    .put("Appaløn", AccountTypes.CHECKING)
                    .put("Sirius", AccountTypes.OTHER)
                    .put("Totalløn", AccountTypes.CHECKING)
                    .put("Fynske Ung", AccountTypes.OTHER)
                    .put("Forbrugslån Stjernekunde", AccountTypes.LOAN)
                    .put("Coop Lån 50+", AccountTypes.LOAN)
                    .put("Coop Lån 20+", AccountTypes.LOAN)
                    .put("Lån", AccountTypes.LOAN)
                    .put("al-formueinvest", AccountTypes.INVESTMENT)
                    .put("danske torpare ekstra", AccountTypes.CHECKING)
                    .put("darwin", AccountTypes.INVESTMENT)
                    .put("fast överföringskto", AccountTypes.CHECKING)
                    .put("first step", AccountTypes.CHECKING)
                    .put("handelsbanken business classic", AccountTypes.CREDIT_CARD)
                    .put("handelsbanken business gold", AccountTypes.CREDIT_CARD)
                    .put("handelsbanken business platinum", AccountTypes.CREDIT_CARD)
                    .put("handelsbanken classic", AccountTypes.CREDIT_CARD)
                    .put("handelsbanken gold", AccountTypes.CREDIT_CARD)
                    .put("handelsbanken platinum", AccountTypes.CREDIT_CARD)
                    .put("next step", AccountTypes.CHECKING)
                    .put("nutaraq flex", AccountTypes.CHECKING)
                    .put("plusløn", AccountTypes.CHECKING)
                    .put("privatfordelløn", AccountTypes.CHECKING)
                    .put("privatung+løn", AccountTypes.CHECKING)
                    .put("scorekassen", AccountTypes.CHECKING)
                    .put("selvpension", AccountTypes.PENSION)
                    .put("spar nord business platin 1000", AccountTypes.CREDIT_CARD)
                    .put("spar nord platin 1000", AccountTypes.CREDIT_CARD)
                    .put("spar nord prioritet", AccountTypes.CREDIT_CARD)
                    .put("stjerne invest fri", AccountTypes.INVESTMENT)
                    .put("konto", AccountTypes.CHECKING)
                    .build();

    public static final ImmutableMap<String, Instrument.Type> INSTRUMENT_TYPES =
            ImmutableMap.<String, Instrument.Type>builder().put("1", Instrument.Type.STOCK).build();
}

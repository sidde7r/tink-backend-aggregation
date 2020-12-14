package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.models.Instrument;
import se.tink.backend.aggregation.agents.utils.log.LogTag;
import src.integration.nemid.NemIdSupportedLanguageCode;

public final class BecConstants {

    public static final class ScaOptions {
        public static final String CODEAPP_OPTION = "codeapp";
        public static final String KEYCARD_OPTION = "keycard";
        public static final String SCATOKEN_OPTION = "scatoken";
        public static final String DEFAULT_OPTION = "default";
    }

    public static final class Url {
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

    public static final class Crypto {
        public static final String AES = "AES";
        public static final String X509 = "X509";
        public static final String SIGNING_CERTIFICATE_B64 =
                "LS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0tCk1JSUR3akNDQXFxZ0F3SUJBZ0lFVGI2YTBUQU5CZ2txaGtpRzl3MEJBUVFGQURDQm9qRUxNQWtHQTFVRUJoTUMKUkVzeEVUQVBCZ05WQkFnVENGSnZjMnRwYkdSbE1SNHdIQVlEVlFRS0V4VkNZVzVyWlhKdVpYTWdSVVJDSUVObApiblJ5WVd3eEhUQWJCZ05WQkFzVEZGUmxhMjVwYzJzZ2RXUjJMaUJ6ZFhCd2IzSjBNUjR3SEFZRFZRUURFeFZ0CmIySnBiR0poYm1zdWNISnZaQzVpWldNdVpHc3hJVEFmQmdrcWhraUc5dzBCQ1FFV0VtSmxZM056YkdObGNuUnoKUUdKbFl5NWthekFlRncweE1UQTFNREl4TVRVeE5EVmFGdzAwTVRBME1qUXhNVFV4TkRWYU1JR2lNUXN3Q1FZRApWUVFHRXdKRVN6RVJNQThHQTFVRUNCTUlVbTl6YTJsc1pHVXhIakFjQmdOVkJBb1RGVUpoYm10bGNtNWxjeUJGClJFSWdRMlZ1ZEhKaGJERWRNQnNHQTFVRUN4TVVWR1ZyYm1semF5QjFaSFl1SUhOMWNIQnZjblF4SGpBY0JnTlYKQkFNVEZXMXZZbWxzWW1GdWF5NXdjbTlrTG1KbFl5NWthekVoTUI4R0NTcUdTSWIzRFFFSkFSWVNZbVZqYzNOcwpZMlZ5ZEhOQVltVmpMbVJyTUlJQklqQU5CZ2txaGtpRzl3MEJBUUVGQUFPQ0FROEFNSUlCQ2dLQ0FRRUFraTFqCm1mRHZlOGF1MnhEYlJGMGgyeGtXTjdJQ2h3eWc2V3ErcTU4R25SM1JxaWlicGlyRDY0ZzE4VHVXc2tkTTRlalYKVHU2NHNvSXRmcVlNM3phYjUvbTVhUlRGVmc2emNGYWtvQlhzUzJoaTNXYTZpWlgrN1NLVVZRclVpeEFiV1cvMApoQm55QXNhc3FSUnpjWGVRMitUQnUyeFVRNUkwMk5iQTVkbXM5N0dKVDZpSjM3Z0t6SElDVXhqakJ5Ry9iSWZRCkZqamw1bU1KbDRwcGN4ckloTkFYUDV6c24yYWU2aTVuWUJ3aWFhSjlVTU52MVpoTG84ZkRUSEttbkpDT1hYSlUKbDVJVU85RVhldHVLT3Y0R1F1Rm01WkYrK0hYSnNZeWhZN2ZUekkvZkllSW82QnpDOC9BY1JGTmx0VUZSMGtkdgpDc1doS2dhVEhsWDNtampubXdJREFRQUJNQTBHQ1NxR1NJYjNEUUVCQkFVQUE0SUJBUUFOVTc0WUtDdWlmSG9LCnArNVB2czVWTjJjZndQcStDVVgzN2RqUVc1QmYrbUZrSzE4TXdpSDl3K1BCUEtHSE9zS0RaL015cGtDYVhldmkKZlhubm4rQXpxZGdmZU1URFhkSExpTyt1ZEZ6MmV6SStPS1V1eWlpRUJmZVVkajFqK2o2dks5WUtoU0pOOTVWMwpTenBwZWVveGlBdnZ0Q09UZnNlK3l3SWJkaHNHS20zdnRybGtaMlpRV0c3K0xOTVkwdDR1ZU90amVzNnVNK29jCjFJSVNmc3JPai9wbklma2hkamhxQlJxVzkzWHZxdjdQZW9wNFFJdEdIM205N08wUXFaWmdZL2ZSbmRaRDd6RUQKcVFSNGVEenJBK3AvdkkxUE1TMG1WaC91UUhIeGFhcnRNRmdGV2hLWkFlVG5PUTJlSWdhVXJoN21JMzk2ak5hTwptQkY5aEUvaAotLS0tLUVORCBDRVJUSUZJQ0FURS0tLS0tCgo=";
        public static final String PUBLIC_KEY_SALT =
                "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAI/e+AAFbsYSUMkj1Hq6gloeE5iC/Vt8DmkwiPwxAHkXE19pkcrTiOPVeME4kvEUey4Qb2ty8zIjvy1SoxAKk0Nku0xu4EGyJn+/1CDRYp4xOZyfXHVk4y18+4amRrGgLFDh4wg3cesCaJ5ETxoM7lvqb/R+zsg3ZtweJsD1pBxxFdjc7B9BKfDnVUk08CPAZhM9DGZ5HpiBV//sz+TusDZTvWtYtZGBZP6Bc4ApKOd3fS6NFE2IP8c2vJ7j2oeP0aDh/WW8ad5BY/sW0iKTiqi3FFxf1ooDjBpfmCcmlEcST4cWZyPfu5p8SZXyKGZ3rtKRoPdMkYBHcLXDweHvSMgAAAAAx";
    }

    public static final class Meta {

        public static final String APP_TYPE = "mb0";
        public static final String APP_VERSION = "5.1.0";
        public static final String OS_VERSION = "Android 6.0";
        public static final String DEVICE_TYPE =
                "HTC / HTC One_M8 / MRA58K release-keys / htc_europe / htc_m8 / htc / htc/htc_europe/htc_m8:6.0/MRA58K/662736.4:user/release-keys / htc_m8 / qcom";
        public static final String SCREEN_SIZE = "1440*2392";

        public static final String LABEL = "MB0-000";
        public static final String CIPHER = "AES/CBC/PKCS5Padding";
    }

    public static final class Header {
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

    public static final class ErrorMessageLocalizedParts {
        public static final Map<NemIdSupportedLanguageCode, String> NO_MORTGAGE =
                ImmutableMap.of(
                        NemIdSupportedLanguageCode.EN,
                                "you have not taken out a mortgage loan through us",
                        NemIdSupportedLanguageCode.DA,
                                "Du har ikke optaget et realkreditlån gennem os");
        public static final Map<NemIdSupportedLanguageCode, String> LOAN_NO_DETAILS_EXIST =
                ImmutableMap.of(
                        NemIdSupportedLanguageCode.EN, "no details exist",
                        NemIdSupportedLanguageCode.DA, "ingen detaljer findes");
        public static final Map<NemIdSupportedLanguageCode, String> FUNCTION_NOT_AVAILABLE =
                ImmutableMap.of(
                        NemIdSupportedLanguageCode.EN,
                                "the required function is not currently available. try again later.",
                        NemIdSupportedLanguageCode.DA,
                                "funktion er ikke tilgængelig i øjeblikket. Prøv igen senere.");
    }

    public static final class StorageKeys {
        public static final String KEY_CARD_NUMBER_STORAGE_KEY = "key-card-number";
        public static final String CHALLENGE_STORAGE_KEY = "challenge-number";
        public static final String DEVICE_ID_STORAGE_KEY = "device-id";
        public static final String SCA_TOKEN_STORAGE_KEY = "sca-token";
        public static final String SCA_OPTION_KEY = "sca-option";
        public static final String KEY_CARD_CHALLENGE_RESPONSE_KEY = "challengeResponseValue";
        public static final String TOKEN_STORAGE_KEY = "nem-id-token";
    }

    public static final class Log {
        public static final LogTag UNKOWN_ACCOUNT_TYPE =
                LogTag.from("#dk_bec_unknown_account_type");
        public static final LogTag LOANS = LogTag.from("#dk_bec_loan");
        public static final LogTag LOAN_FAILED = LogTag.from("#dk_bec_loan_failed");
        public static final LogTag INVESTMENT_PAPER_TYPE =
                LogTag.from("#dk_bec_investment_paper_type");
        public static final LogTag CREDIT_CARD_FETCH_ERROR =
                LogTag.from("#dk_bec_credit_card_fetch_error");
    }

    public static final class CreditCard {
        public static final String STATUS_ACTIVE = "active";
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
                    .build();

    public static final ImmutableMap<String, Instrument.Type> INSTRUMENT_TYPES =
            ImmutableMap.<String, Instrument.Type>builder().put("1", Instrument.Type.STOCK).build();

    public static final class Loan {
        public static final String INTEREST_DETAILS_KEY = "Yearly interest";
    }
}

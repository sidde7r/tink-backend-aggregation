package se.tink.backend.aggregation.agents.nxgen.se.business.nordea;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.Map;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;

public class NordeaSEConstants {

    private NordeaSEConstants() {
        throw new IllegalStateException("Utility class");
    }

    public static class Urls {
        private Urls() {}

        public static final String BASE_URL = "https://se.smemobilebank.prod.nordea.com";

        public static final String INIT_BANKID = BASE_URL + Endpoints.INIT_BANKID;
        public static final String POLL_BANKID = BASE_URL + Endpoints.POLL_BANKID;
        public static final String FETCH_TOKEN = BASE_URL + Endpoints.FETCH_TOKEN;
        public static final String FETCH_ACCOUNT = BASE_URL + Endpoints.FETCH_ACCOUNTS;
        public static final String FETCH_ACCOUNT_DETAILS =
                BASE_URL + Endpoints.FETCH_ACCOUNT_DETAILS;
        public static final String FETCH_TRANSACTIONS = BASE_URL + Endpoints.FETCH_TRANSACTIONS;
    }

    public static class Endpoints {
        private Endpoints() {}

        public static final String INIT_BANKID =
                "/SE/MobileBankIdServiceV1.1/MobileBankIdInitialAuthentication";
        public static final String POLL_BANKID =
                "/SE/MobileBankIdServiceV1.1/MobileBankIdAuthenticationResult/";
        public static final String FETCH_TOKEN = "/SE/AuthenticationServiceV1.1/SecurityToken";
        public static final String FETCH_ACCOUNTS = "/SE/BankingServiceV1.1/initialContext";
        public static final String FETCH_ACCOUNT_DETAILS = "/SE/BankingServiceV1.1/Accounts/";
        public static final String FETCH_TRANSACTIONS = "/SE/BankingServiceV1.1/Transactions";
    }

    public static class QueryKeys {
        private QueryKeys() {}

        public static final String ACCOUNT_ID = "productId";
        public static final String CONTINUE_KEY = "continueKey";
    }

    public static class Headers {
        private Headers() {}

        public static final String REQUEST_ID = "x-Request-Id";
        public static final String SECURITY_TOKEN = "x-Security-Token";
    }

    public static final ImmutableMap<String, Object> NORDEA_CUSTOM_HEADERS =
            ImmutableMap.<String, Object>builder()
                    .put("x-App-Country", "SE")
                    .put("x-App-Language", "en")
                    .put("x-App-Name", "SME")
                    .put("x-App-Version", "1.3.5-18")
                    .put("x-Device-Make", "Apple")
                    .put("x-Device-Model", "iPhone9,4")
                    .put("x-Platform-Type", "iOS")
                    .put("x-Platform-Version", "13.3.1")
                    .put("User-Agent", "SMEMobileBankSE/18 CFNetwork/1121.2.2 Darwin/19.3.0")
                    .build();

    public static class StorageKeys {
        private StorageKeys() {}

        public static final String SECURITY_TOKEN = "security_token";
        public static final String HOLDER_NAME = "holder_name";
    }

    public static class BankIdStatus {
        private BankIdStatus() {}

        public static final String COMPLETE = "COMPLETE";
        public static final String WAITING = "OUTSTANDING_TRANSACTION";
        public static final String USER_SIGNING = "USER_SIGN";
    }

    public static class ErrorCodes {
        private ErrorCodes() {}

        public static final String NO_CLIENT = "MBS8636"; // BankId no client
    }

    public static class ErrorMessages {
        private ErrorMessages() {}

        public static final String URL_ENCODING_ERROR = "Url is not well defined.";
    }

    public static class AccountType {
        private AccountType() {}

        private static final String OMBUDSKONTO = "Ombudskonto";
        private static final String DEPOSIT = "Deposit";
        private static final String FASTRANTEPLACERING = "Fastränteplacering";
        private static final String CENTRALKONTO = "Centralkonto";

        private static final Map<String, String> ACCOUNT_NAMES_BY_CODE = Maps.newHashMap();
        private static final Map<String, TransactionalAccountType> ACCOUNT_TYPES_BY_CODE =
                Maps.newHashMap();

        static {
            addType("SE0000", "Personkonto", TransactionalAccountType.CHECKING);
            addType("SE0001", "Fn-konto", TransactionalAccountType.CHECKING);
            addType("SE0002", OMBUDSKONTO, TransactionalAccountType.CHECKING);
            addType("SE0004", "Synskadekonto", TransactionalAccountType.CHECKING);
            addType("SE0005", "Utlandslönekonto", TransactionalAccountType.CHECKING);
            addType("SE1101", "Private Bankingkonto", TransactionalAccountType.CHECKING);
            addType("SE0100", "Personkonto Solo", TransactionalAccountType.CHECKING);
            addType("SE0101", "Fn-konto Solo", TransactionalAccountType.CHECKING);
            addType("SE0104", "Synskadekonto Solo", TransactionalAccountType.CHECKING);
            addType("SE0105", "Utlandslönekto Solo", TransactionalAccountType.CHECKING);
            addType("SE0200", "Personkonto-student", TransactionalAccountType.CHECKING);
            addType("SE0300", "Personkonto-ungdom", TransactionalAccountType.CHECKING);
            addType("SE0302", OMBUDSKONTO, TransactionalAccountType.CHECKING);
            addType("SE0304", "Synskadekonto", TransactionalAccountType.CHECKING);
            addType("SE0402", "IPS cash account", TransactionalAccountType.CHECKING);
            addType("SE0500", "Depålikvidkonto", TransactionalAccountType.CHECKING);
            addType("SE0501", "ISK Trader likvidkonto", TransactionalAccountType.CHECKING);
            addType("SE0502", OMBUDSKONTO, TransactionalAccountType.CHECKING);
            addType("SE0600", "Pgkonto Privat", TransactionalAccountType.CHECKING);
            addType("SE0606", "Eplusgiro Privat", TransactionalAccountType.CHECKING);
            addType("SE0700", "Pgkonto Privat Ftg", TransactionalAccountType.CHECKING);
            addType("SE0706", "Eplusgiro Privat Ftg", TransactionalAccountType.CHECKING);
            addType("SE0900", "Flexkonto", TransactionalAccountType.CHECKING);

            addType("SE1000", "Checkkonto", TransactionalAccountType.CHECKING);
            addType("SE1001", "Pensionskredit", TransactionalAccountType.CHECKING);
            addType("SE1002", "Boflex", TransactionalAccountType.CHECKING);
            addType("SE1003", "Boflex pension", TransactionalAccountType.CHECKING);
            addType("SE1004", "Buffertkonto kredit", TransactionalAccountType.CHECKING);
            addType("SE1100", "Aktielikvidkonto", TransactionalAccountType.CHECKING);
            addType("SE1200", "Externt Konto", TransactionalAccountType.CHECKING);
            addType("SE1211", "Zb Ext Top", TransactionalAccountType.CHECKING);
            addType("SE1212", "Zb Externt Sub Gr Lvl", TransactionalAccountType.CHECKING);
            addType("SE1213", "Zb Externt Sub Acc", TransactionalAccountType.CHECKING);
            addType("SE1214", "Zb Externt Adj Acc", TransactionalAccountType.CHECKING);
            addType("SE1300", "Cross-Border Account", TransactionalAccountType.CHECKING);
            addType("SE1311", "Zb Cross-Border Top", TransactionalAccountType.CHECKING);
            addType("SE1312", "Zb C-B Sub Gr Level", TransactionalAccountType.CHECKING);
            addType("SE1313", "Zb C-B Sub Account", TransactionalAccountType.CHECKING);
            addType("SE1314", "Zb Cross-Border Adj", TransactionalAccountType.CHECKING);
            addType("SE1400", "Föreningskonto", TransactionalAccountType.CHECKING);
            addType("SE1411", "Zb Föreningskonto Top", TransactionalAccountType.CHECKING);
            addType("SE1412", "Zb Före.Kto Sub Gr Level", TransactionalAccountType.CHECKING);
            addType("SE1413", "Zb Föreningskonto Sub Acc", TransactionalAccountType.CHECKING);
            addType("SE1414", "Zb Föreningskonto Adj Acc", TransactionalAccountType.CHECKING);
            addType("SE1500", "Församlingskonto", TransactionalAccountType.CHECKING);
            addType("SE1600", "Baskonto", TransactionalAccountType.CHECKING);
            addType("SE1700", "Direktkonto", TransactionalAccountType.CHECKING);
            addType("SE1701", "Affärskonto", TransactionalAccountType.CHECKING);
            addType("SE1711", "Zb Direktkonto Top", TransactionalAccountType.CHECKING);
            addType("SE1712", "Zb Dir.Konto Sub Gr Level", TransactionalAccountType.CHECKING);
            addType("SE1713", "Zb Dir.Konto Sub Account", TransactionalAccountType.CHECKING);
            addType("SE1714", "Zb Dir.Konto Adj Account", TransactionalAccountType.CHECKING);
            addType("SE1800", "Företagskonto", TransactionalAccountType.CHECKING);
            addType("SE1801", "Top Account", TransactionalAccountType.CHECKING);
            addType("SE1802", "Sub Group Level", TransactionalAccountType.CHECKING);
            addType("SE1803", "Sub Account", TransactionalAccountType.CHECKING);
            addType("SE1804", "Adjustement Account", TransactionalAccountType.CHECKING);
            addType("SE1811", "Zb Företagskonto Top", TransactionalAccountType.CHECKING);
            addType("SE1812", "Zb Ftgskonto Sub Gr Level", TransactionalAccountType.CHECKING);
            addType("SE1813", "Zb Företagskonto Sub Acc", TransactionalAccountType.CHECKING);
            addType("SE1814", "Zb Företagskonto Adj Acc", TransactionalAccountType.CHECKING);
            addType("SE1900", "Arbetsgivarkonto", TransactionalAccountType.CHECKING);
            addType("SE1901", "Avdragsmottagarkonto", TransactionalAccountType.CHECKING);
            addType("SE1902", "Floatkonto", TransactionalAccountType.CHECKING);

            addType("SE2000", "Girokapital Kfm", TransactionalAccountType.CHECKING);
            addType("SE2100", "Specialkonto", TransactionalAccountType.CHECKING);
            addType("SE2200", "Sparkonto Företag", TransactionalAccountType.SAVINGS);
            addType("SE2300", "Koncern Plusgirokonto", TransactionalAccountType.CHECKING);
            addType("SE2311", "Koncern Toppkonto", TransactionalAccountType.CHECKING);
            addType("SE2312", "Koncern Samlingskto", TransactionalAccountType.CHECKING);
            addType("SE2313", "Koncern Transkonto", TransactionalAccountType.CHECKING);
            addType("SE2400", "Myndighetskonto", TransactionalAccountType.CHECKING);
            addType("SE2405", "Myndighetskonto", TransactionalAccountType.CHECKING);
            addType("SE2411", "Myndighet Toppkonto", TransactionalAccountType.CHECKING);
            addType("SE2412", "Myndighet Samlingskto", TransactionalAccountType.CHECKING);
            addType("SE2413", "Myndighet Transkonto", TransactionalAccountType.CHECKING);
            addType("SE2499", "Avsl Myndighetskonto", TransactionalAccountType.CHECKING);
            addType("SE2500", "Plusgirokonto Nordea", TransactionalAccountType.CHECKING);
            addType("SE2700", "Internt Arbetskonto", TransactionalAccountType.CHECKING);
            addType("SE2900", "Plusgirokonto", TransactionalAccountType.CHECKING);

            addType("SE4000", "Spara Kapital", TransactionalAccountType.SAVINGS);
            addType("SE4300", "ISK Classic likvidkonto", TransactionalAccountType.SAVINGS);
            addType("SE4309", "ISK Classic likvidkonto", TransactionalAccountType.SAVINGS);
            addType("SE4400", "Skatteutjämningskonto", TransactionalAccountType.SAVINGS);
            addType("SE4401", "Skogskonto", TransactionalAccountType.SAVINGS);
            addType("SE4402", "Skogsskadekonto", TransactionalAccountType.SAVINGS);
            addType("SE4403", "Upphovsmannakonto", TransactionalAccountType.SAVINGS);
            addType("SE4404", "Allm Investeringskonto", TransactionalAccountType.SAVINGS);
            addType("SE4405", "Uppfinnarkonto", TransactionalAccountType.SAVINGS);
            addType("SE4500", "Sparkonto", TransactionalAccountType.SAVINGS);
            addType("SE4600", DEPOSIT, TransactionalAccountType.SAVINGS);
            addType("SE4601", DEPOSIT, TransactionalAccountType.SAVINGS);
            addType("SE4602", FASTRANTEPLACERING, TransactionalAccountType.SAVINGS);
            addType("SE4603", "Dagsinlåning", TransactionalAccountType.SAVINGS);
            addType("SE4604", DEPOSIT, TransactionalAccountType.SAVINGS);
            addType("SE4605", FASTRANTEPLACERING, TransactionalAccountType.SAVINGS);
            addType("SE4606", FASTRANTEPLACERING, TransactionalAccountType.SAVINGS);
            addType("SE4607", FASTRANTEPLACERING, TransactionalAccountType.SAVINGS);
            addType("SE4608", "Placering 4 År", TransactionalAccountType.SAVINGS);
            addType("SE4609", FASTRANTEPLACERING, TransactionalAccountType.SAVINGS);
            addType("SE4610", "Bonuskonto, utgåva", TransactionalAccountType.SAVINGS);
            addType("SE4611", "Tillväxtkonto", TransactionalAccountType.SAVINGS);
            addType("SE4700", "Planeringskonto 2 År", TransactionalAccountType.SAVINGS);
            addType("SE4800", "Planeringskonto 3 År", TransactionalAccountType.SAVINGS);
            addType("SE4900", "Planeringskonto 4 År", TransactionalAccountType.SAVINGS);

            addType("SE5000", "Planeringskonto 5 År", TransactionalAccountType.SAVINGS);
            addType("SE5100", "Kapitalkonto", TransactionalAccountType.SAVINGS);
            addType("SE5200", "Skogslikvidkonto", TransactionalAccountType.SAVINGS);
            addType("SE5400", "Banksparkonto Ips", TransactionalAccountType.SAVINGS);
            addType("SE5500", "Ungbonus", TransactionalAccountType.SAVINGS);
            addType("SE5700", "Förmånskonto", TransactionalAccountType.SAVINGS);
            addType("SE5900", "Privatcertifikat", TransactionalAccountType.SAVINGS);

            addType("SE6000", "Affärsgiro", TransactionalAccountType.SAVINGS);
            addType("SE6013", "Zb Företagskonto Sub Acc", TransactionalAccountType.SAVINGS);
            addType("SE6100", "Plusgirokonto Företag", TransactionalAccountType.CHECKING);
            addType("SE6200", "Föreningsgiro", TransactionalAccountType.SAVINGS);
            addType("SE6300", "Pgkonto Förening", TransactionalAccountType.SAVINGS);
            addType("SE6600", "Plusgirokonto", TransactionalAccountType.SAVINGS);
            addType("SE6700", "Pensionssparkonto", TransactionalAccountType.SAVINGS);

            addType("SE7000", "Nostro", TransactionalAccountType.SAVINGS);
            addType("SE7100", "Loro 1", TransactionalAccountType.SAVINGS);
            addType("SE7200", "Kvk Valutatoppkonto", TransactionalAccountType.SAVINGS);
            addType("SE7300", "Kvk Toppkonto", TransactionalAccountType.SAVINGS);
            addType("SE7320", "Kvk Summeringskonto", TransactionalAccountType.SAVINGS);
            addType("SE7321", "Kvk Valutasumm Kto", TransactionalAccountType.SAVINGS);
            addType("SE7330", "Kvk Transaktionskto", TransactionalAccountType.SAVINGS);
            addType("SE7350", "Kvk Preliminäröppnat", TransactionalAccountType.SAVINGS);
            addType("SE7400", "Överföringskonto", TransactionalAccountType.SAVINGS);
            addType("SE7401", "Arbetskonto", TransactionalAccountType.SAVINGS);
            addType("SE7402", "Internkonto", TransactionalAccountType.SAVINGS);
            addType("SE7500", "Internt Kassakonto", TransactionalAccountType.SAVINGS);
            addType("SE7600", "Kontoöversikt", TransactionalAccountType.SAVINGS);
            addType("SE7601", "Zb Top Account", TransactionalAccountType.SAVINGS);
            addType("SE7602", "Zb Sub Group Level", TransactionalAccountType.SAVINGS);
            addType("SE7603", "Zb Sub Account", TransactionalAccountType.SAVINGS);
            addType("SE7604", "Zb Adjustement Account", TransactionalAccountType.SAVINGS);
            addType("SE7605", "Z Hdjustement Account", TransactionalAccountType.SAVINGS);
            addType("SE7700", CENTRALKONTO, TransactionalAccountType.SAVINGS);
            addType("SE7701", CENTRALKONTO, TransactionalAccountType.SAVINGS);
            addType("SE7702", CENTRALKONTO, TransactionalAccountType.SAVINGS);
            addType("SE7703", CENTRALKONTO, TransactionalAccountType.SAVINGS);
            addType("SE7800", "Samlingsnummer", TransactionalAccountType.SAVINGS);
            addType("SE7900", "Dispositionsnummer", TransactionalAccountType.SAVINGS);
            addType("SE7901", "Belastningsnummer", TransactionalAccountType.SAVINGS);

            addType("SE46100601", "Bonuskonto 2006 utgåva 1", TransactionalAccountType.CHECKING);
            addType("SE46100602", "Bonuskonto 2006 utgåva 2", TransactionalAccountType.CHECKING);
            addType("SE46100603", "Bonuskonto 2006 utgåva 3", TransactionalAccountType.CHECKING);
            addType("SE46100604", "Bonuskonto 2006 utgåva 4", TransactionalAccountType.CHECKING);
            addType("SE46100605", "Bonuskonto 2006 utgåva 5", TransactionalAccountType.CHECKING);
            addType("SE46100606", "Bonuskonto 2006 utgåva 6", TransactionalAccountType.CHECKING);
            addType("SE46100607", "Bonuskonto 2006 utgåva 7", TransactionalAccountType.CHECKING);
            addType("SE46100608", "Bonuskonto 2006 utgåva 8", TransactionalAccountType.CHECKING);
            addType("SE46100609", "Bonuskonto 2006 utgåva 9", TransactionalAccountType.CHECKING);
            addType("SE46100610", "Bonuskonto 2006 utgåva 10", TransactionalAccountType.CHECKING);
            addType("SE46100611", "Bonuskonto 2006 utgåva 11", TransactionalAccountType.CHECKING);
            addType("SE46100612", "Bonuskonto 2006 utgåva 12", TransactionalAccountType.CHECKING);
            addType("SE46100701", "Bonuskonto 2007 utgåva 1", TransactionalAccountType.CHECKING);
            addType("SE46100702", "Bonuskonto 2007 utgåva 2", TransactionalAccountType.CHECKING);
            addType("SE46100703", "Bonuskonto 2007 utgåva 3", TransactionalAccountType.CHECKING);
            addType("SE46100704", "Bonuskonto 2007 utgåva 4", TransactionalAccountType.CHECKING);
            addType("SE46100705", "Bonuskonto 2007 utgåva 5", TransactionalAccountType.CHECKING);
            addType("SE46100706", "Bonuskonto 2007 utgåva 6", TransactionalAccountType.CHECKING);
            addType("SE46100707", "Bonuskonto 2007 utgåva 7", TransactionalAccountType.CHECKING);
            addType("SE46100708", "Bonuskonto 2007 utgåva 8", TransactionalAccountType.CHECKING);
            addType("SE46100709", "Bonuskonto 2007 utgåva 9", TransactionalAccountType.CHECKING);
            addType("SE46100710", "Bonuskonto 2007 utgåva 10", TransactionalAccountType.CHECKING);
            addType("SE46100711", "Bonuskonto 2007 utgåva 11", TransactionalAccountType.CHECKING);
            addType("SE46100712", "Bonuskonto 2007 utgåva 12", TransactionalAccountType.CHECKING);
            addType("SE46100719", "Bonuskonto 2007 utgåva 19", TransactionalAccountType.CHECKING);
            addType("SE46100801", "Bonuskonto 2008 utgåva 1", TransactionalAccountType.CHECKING);
            addType("SE46100802", "Bonuskonto 2008 utgåva 2", TransactionalAccountType.CHECKING);
            addType("SE46100803", "Bonuskonto 2008 utgåva 3", TransactionalAccountType.CHECKING);
            addType("SE46100804", "Bonuskonto 2008 utgåva 4", TransactionalAccountType.CHECKING);
            addType("SE46100805", "Bonuskonto 2008 utgåva 5", TransactionalAccountType.CHECKING);
            addType("SE46100806", "Bonuskonto 2008 utgåva 6", TransactionalAccountType.CHECKING);
            addType("SE46100807", "Bonuskonto 2008 utgåva 7", TransactionalAccountType.CHECKING);
            addType("SE46100808", "Bonuskonto 2008 utgåva 8", TransactionalAccountType.CHECKING);
            addType("SE46100809", "Bonuskonto 2008 utgåva 9", TransactionalAccountType.CHECKING);
            addType("SE46100810", "Bonuskonto 2008 utgåva 10", TransactionalAccountType.CHECKING);
            addType("SE46100811", "Bonuskonto 2008 utgåva 11", TransactionalAccountType.CHECKING);
            addType("SE46100812", "Bonuskonto 2008 utgåva 12", TransactionalAccountType.CHECKING);
            addType("SE46100901", "Bonuskonto 2009 utgåva 1", TransactionalAccountType.CHECKING);
            addType("SE46100902", "Bonuskonto 2009 utgåva 2", TransactionalAccountType.CHECKING);
            addType("SE46100903", "Bonuskonto 2009 utgåva 3", TransactionalAccountType.CHECKING);
            addType("SE46100904", "Bonuskonto 2009 utgåva 4", TransactionalAccountType.CHECKING);
            addType("SE46100905", "Bonuskonto 2009 utgåva 5", TransactionalAccountType.CHECKING);
            addType("SE46100906", "Bonuskonto 2009 utgåva 6", TransactionalAccountType.CHECKING);
            addType("SE46100907", "Bonuskonto 2009 utgåva 7", TransactionalAccountType.CHECKING);
            addType("SE46100908", "Bonuskonto 2009 utgåva 8", TransactionalAccountType.CHECKING);
            addType("SE46100909", "Bonuskonto 2009 utgåva 9", TransactionalAccountType.CHECKING);
            addType("SE46100910", "Bonuskonto 2009 utgåva 10", TransactionalAccountType.CHECKING);
            addType("SE46100911", "Bonuskonto 2009 utgåva 11", TransactionalAccountType.CHECKING);
            addType("SE46100912", "Bonuskonto 2009 utgåva 12", TransactionalAccountType.CHECKING);
            addType("SE46101001", "Bonuskonto 2010 utgåva 1", TransactionalAccountType.CHECKING);
            addType("SE46101002", "Bonuskonto 2010 utgåva 2", TransactionalAccountType.CHECKING);
            addType("SE46101003", "Bonuskonto 2010 utgåva 3", TransactionalAccountType.CHECKING);
            addType("SE46101004", "Bonuskonto 2010 utgåva 4", TransactionalAccountType.CHECKING);
            addType("SE46101005", "Bonuskonto 2010 utgåva 5", TransactionalAccountType.CHECKING);
            addType("SE46101006", "Bonuskonto 2010 utgåva 6", TransactionalAccountType.CHECKING);
            addType("SE46101007", "Bonuskonto 2010 utgåva 7", TransactionalAccountType.CHECKING);
            addType("SE46101008", "Bonuskonto 2010 utgåva 8", TransactionalAccountType.CHECKING);
            addType("SE46101009", "Bonuskonto 2010 utgåva 9", TransactionalAccountType.CHECKING);
            addType("SE46101010", "Bonuskonto 2010 utgåva 10", TransactionalAccountType.CHECKING);
            addType("SE46101011", "Bonuskonto 2010 utgåva 11", TransactionalAccountType.CHECKING);
            addType("SE46101012", "Bonuskonto 2010 utgåva 12", TransactionalAccountType.CHECKING);
            addType("SE46101101", "Bonuskonto 2011 utgåva 1", TransactionalAccountType.CHECKING);
            addType("SE46101102", "Bonuskonto 2011 utgåva 2", TransactionalAccountType.CHECKING);
            addType("SE46101103", "Bonuskonto 2011 utgåva 3", TransactionalAccountType.CHECKING);
            addType("SE46101104", "Bonuskonto 2011 utgåva 4", TransactionalAccountType.CHECKING);
            addType("SE46101105", "Bonuskonto 2011 utgåva 5", TransactionalAccountType.CHECKING);
            addType("SE46101106", "Bonuskonto 2011 utgåva 6", TransactionalAccountType.CHECKING);
            addType("SE46101107", "Bonuskonto 2011 utgåva 7", TransactionalAccountType.CHECKING);
            addType("SE46101108", "Bonuskonto 2011 utgåva 8", TransactionalAccountType.CHECKING);
            addType("SE46101109", "Bonuskonto 2011 utgåva 9", TransactionalAccountType.CHECKING);
            addType("SE46101110", "Bonuskonto 2011 utgåva 10", TransactionalAccountType.CHECKING);
            addType("SE46101111", "Bonuskonto 2011 utgåva 11", TransactionalAccountType.CHECKING);
            addType("SE46101112", "Bonuskonto 2011 utgåva 12", TransactionalAccountType.CHECKING);
            addType("SE46101201", "Bonuskonto 2012 utgåva 1", TransactionalAccountType.CHECKING);
            addType("SE46101202", "Bonuskonto 2012 utgåva 2", TransactionalAccountType.CHECKING);
            addType("SE46101203", "Bonuskonto 2012 utgåva 3", TransactionalAccountType.CHECKING);
            addType("SE46101204", "Bonuskonto 2012 utgåva 4", TransactionalAccountType.CHECKING);
            addType("SE46101205", "Bonuskonto 2012 utgåva 5", TransactionalAccountType.CHECKING);
            addType("SE46101206", "Bonuskonto 2012 utgåva 6", TransactionalAccountType.CHECKING);
            addType("SE46101207", "Bonuskonto 2012 utgåva 7", TransactionalAccountType.CHECKING);
            addType("SE46101208", "Bonuskonto 2012 utgåva 8", TransactionalAccountType.CHECKING);
            addType("SE46101209", "Bonuskonto 2012 utgåva 9", TransactionalAccountType.CHECKING);
            addType("SE46101210", "Bonuskonto 2012 utgåva 10", TransactionalAccountType.CHECKING);
            addType("SE46101211", "Bonuskonto 2012 utgåva 11", TransactionalAccountType.CHECKING);
            addType("SE46101212", "Bonuskonto 2012 utgåva 12", TransactionalAccountType.CHECKING);

            addType(
                    "Företagslån Nordea Bank",
                    "Företagslån Nordea Bank",
                    TransactionalAccountType.SAVINGS);
        }

        public static String getAccountNameForCode(String code) {
            return ACCOUNT_NAMES_BY_CODE.getOrDefault(code, "");
        }

        public static TransactionalAccountType getAccountTypeForCode(String code) {
            return ACCOUNT_TYPES_BY_CODE.getOrDefault(code, TransactionalAccountType.CHECKING);
        }

        private static void addType(String code, String name, TransactionalAccountType type) {
            ACCOUNT_TYPES_BY_CODE.put(code, type);
            ACCOUNT_NAMES_BY_CODE.put(code, name);
        }
    }
}

package se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargnenew;

import static se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargnenew.CaisseEpargneConstants.MembershipTypes.PART;
import static se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargnenew.CaisseEpargneConstants.MembershipTypes.PRO;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class CaisseEpargneConstants {
    public static class Urls {

        static final String STEP_PATH = "/step";
        private static final URL AS_EX_ANO_BASE_URL =
                new URL("https://www.as-ex-ano-groupe.caisse-epargne.fr");
        private static final URL RS_EX_ANO_BASE_URL =
                new URL("https://www.rs-ex-ano-groupe.caisse-epargne.fr");
        private static final URL AS_EX_ATH_BASE_URL =
                new URL("https://www.as-ex-ath-groupe.caisse-epargne.fr");
        private static final URL RS_EX_ATH_BASE_URL =
                new URL("https://www.rs-ex-ath-groupe.caisse-epargne.fr");

        public static final URL ICG_AUTH_BASE = new URL("https://www.icgauth.caisse-epargne.fr");
        static final URL SOAP_BASE = new URL("https://www.s.caisse-epargne.fr");
        static final URL WS_BAD = SOAP_BASE.concat("/V22/WsBad/WsBad.asmx");
        static final URL IDENTIFICATION_ROUTING =
                RS_EX_ANO_BASE_URL.concat("/bapi/user/v1/users/identificationRouting");
        static final URL OAUTH2_TOKEN = AS_EX_ANO_BASE_URL.concat("/api/oauth/token");
        static final URL OAUTH_V2_AUTHORIZE = AS_EX_ATH_BASE_URL.concat("/api/oauth/v2/authorize");
        static final URL GET_BENEFICIARIES =
                RS_EX_ATH_BASE_URL.concat("/bapi/transfer/v2/transferCreditors");
    }

    public static class CookieKeys {}

    public static class FormKeys {
        public static final String CLIENT_ID = "client_id";
        public static final String CLIENT_SECRET = "client_secret";
        public static final String GRANT_TYPE = "grant_type";
        public static final String SAML_REQUEST = "SAMLRequest";
    }

    public static class FormValues {
        public static final String CLIENT_ID = "8a7e499e-8f67-4377-91d3-74e4cbdd7a42";
        public static final String CLIENT_SECRET = "eade1e43-60b2-456e-810f-c7b9a85eae5f";
        public static final String GRANT_CLIENT_CREDENTIALS = "client_credentials";
    }

    public static class HeaderKeys {
        public static final String USER_AGENT = "User-Agent";
        public static final String ESTABLISHMENT_ID = "ID_ETABLISSEMENT";
        static final String SOAP_ACTION = "SOAPAction";
        static final String VERSION_WS_BAD = "VersionWsbad";
        public static String CONTENT_TYPE = "Content-Type";
        public static final String COOKIE = "Cookie";
        static String SET_COOKIE = "Set-Cookie";
    }

    public static class HeaderValues {
        public static final String GET_ACCOUNTS =
                "http://caisse-epargne.fr/webservices/GetSyntheseCpteAbonnement";
        public static final String GET_ACCOUNT_DETAILS =
                "http://caisse-epargne.fr/webservices/GetRice";
        public static final String GET_TRANSACTIONS =
                "http://caisse-epargne.fr/webservices/GetHistoriqueOperationsByCompte";
        static final String SSO_BAPI = "http://caisse-epargne.fr/webservices/sso_BAPI";
        static final String VERSION_WS_BAD_22 = "V22";
        static final String CAISSE_DARWIN = "CaisseEpargne/742 CFNetwork/978.0.7 Darwin/18.7.0";
    }

    public static class QueryKeys {
        public static final String NONCE = "nonce";
        public static final String RESPONSE_TYPE = "response_type";
        public static final String CLIENT_ID = "client_id";
        public static final String REDIRECT_URI = "redirect_uri";
        public static final String BEARER = "Bearer";
        public static final String ACCESS_TOKEN = "access_token";
        public static final String TOKEN_TYPE = "token_type";
        public static final String EXPIRES_IN = "expires_in";
        public static final String ID_TOKEN = "id_token";
        static final String LOGIN_HINT = "login_hint";
        static final String BPCESTA = "bpcesta";
        static final String CDETAB = "cdetab";
        static final String CLAIMS = "claims";
        static final String SECRED_IT = "secret_id";
        static final String DISPLAY = "display";
    }

    public static class QueryValues {
        public static final String SECRET = "34791847-2cfe-4992-bff2-1c3327b92fab";
        public static final String CLIENT_ID = "f4ee2144-0d68-4b90-ae78-25e255a1f3ac";
        static final String TOUCH = "touch";
        static final String ID_TOKEN_TOKEN = "id_token token";
        static final String CONTAINER_APP_BAPI_SETUP_SUCCESS = "containerApp://BAPIStepUpSuccess";
    }

    public static class ResponseValues {
        public static final String AUTHENTICATION_FAILED = "AUTHENTICATION_FAILED";
        public static final String FAILED_AUTHENTICATION = "FAILED_AUTHENTICATION";
        public static final String PASSWORD = "PASSWORD";
        public static final String RESPONSE_STATUS_SUCCESS = "0";
        public static final String AUTHENTICATION_LOCKED = "AUTHENTICATION_LOCKED";
        public static final String AUTHENTICATION_SUCCESS = "AUTHENTICATION_SUCCESS";
    }

    public static class RequestValues {
        public static final String PASSWORD = "PASSWORD";
        public static final int PAGE_SIZE = 100;
        public static final String TRANSACTION_REQUEST_TYPE_SUBSEQUENT = "S";
        public static final String TRANSACTION_REQUEST_TYPE_INITIAL = "D";
        static final String IT_ENTITY_02 = "02";
    }

    public static final Map<MembershipTypes, String> MEMBERSHIP_TYPES_TO_VALUE_MAP =
            Stream.of(
                            new SimpleImmutableEntry<>(PART, "part"),
                            new SimpleImmutableEntry<>(PRO, "pro"))
                    .collect(Collectors.toMap(Entry::getKey, Entry::getValue));

    public static class StorageKeys {
        public static final String TOKEN = "token";
        static final String TERM_ID = "termId";
        public static final String FINAL_AUTH_RESPONSE = "finalAuthResponse";
        static final String REDIRECT_LOCATION = "redirectLocation";
    }

    public static class ResponseValue {
        public static final String RETURN_CODE_OK = "0000";
        public static final String TRANSACTION_TYPE_INCOME = "C";
    }

    public enum MembershipTypes {
        UNKNOWN(""),
        PART("1"),
        PRO("2");
        private final String name;

        MembershipTypes(String name) {
            this.name = name;
        }

        public static MembershipTypes fromString(String text) {
            return Arrays.stream(MembershipTypes.values())
                    .filter(type -> type.name.equalsIgnoreCase(text))
                    .findFirst()
                    .orElse(UNKNOWN);
        }

        public String toString() {
            return name;
        }
    }

    public static class SoapKeys {
        public static final String VALID_SUBSCRIPTION = "AbonnementValide";
        public static final String SURNAME = "Nom";
        public static final String NAME = "Prenom";
        public static final String INTERNAL_ACCOUNT = "CompteInterneSynt";
    }

    public static class ResponseKeys {
        public static final String ACCOUNT_DETAILS_RESULT = "GetRiceResult";
        public static final String ACCOUNTS_RESPONSE = "GetSyntheseCpteAbonnementResult";
        public static final String TRANSACTIONS_RESULT = "GetHistoriqueOperationsByCompteResult";
    }

    public static class SoapXmlFragment {
        public static final String PREFIX =
                "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"><soap:Body>";
        public static final String SUFFIX = "</soap:Body></soap:Envelope>";
    }
}

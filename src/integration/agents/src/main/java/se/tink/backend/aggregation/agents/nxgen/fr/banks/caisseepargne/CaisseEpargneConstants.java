package se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne;

import static se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.CaisseEpargneConstants.MembershipTypes.PART;
import static se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.CaisseEpargneConstants.MembershipTypes.PRO;

import com.google.common.collect.ImmutableMap;
import java.util.Arrays;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class CaisseEpargneConstants {
    public static class Urls {
        private Urls() {}

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
        static final URL BENEFICIARIES =
                RS_EX_ATH_BASE_URL.concat("/bapi/transfer/v2/transferCreditors");
        public static final String SAML_TRANSACTION_PATH = "/dacsrest/api/v1u0/transaction";
    }

    public static class FormKeys {
        private FormKeys() {}

        public static final String CLIENT_ID = "client_id";
        public static final String CLIENT_SECRET = "client_secret";
        public static final String GRANT_TYPE = "grant_type";
        public static final String SAML_RESPONSE = "SAMLResponse";
        public static final String SAML_REQUEST = "SAMLRequest";
    }

    public static class FormValues {
        private FormValues() {}

        public static final String CLIENT_ID = "8a7e499e-8f67-4377-91d3-74e4cbdd7a42";
        public static final String CLIENT_SECRET = "eade1e43-60b2-456e-810f-c7b9a85eae5f";
        public static final String GRANT_CLIENT_CREDENTIALS = "client_credentials";
    }

    public static class HeaderKeys {
        private HeaderKeys() {}

        public static final String USER_AGENT = "User-Agent";
        public static final String X_SECURE_PASS_TYPE = "X-SecurePass-Type";
        static final String ESTABLISHMENT_ID = "ID_ETABLISSEMENT";
        static final String X_STEP_UP_TOKEN = "X-StepUp-Token";
        static final String SOAP_ACTION = "SOAPAction";
        static final String VERSION_WS_BAD = "VersionWsbad";
        public static final String CONTENT_TYPE = "Content-Type";
        public static final String COOKIE = "Cookie";
        static final String SET_COOKIE = "Set-Cookie";
    }

    public static class HeaderValues {
        private HeaderValues() {}

        public static final String GET_ACCOUNTS =
                "http://caisse-epargne.fr/webservices/GetSyntheseCpteAbonnement";
        public static final String GET_ACCOUNT_DETAILS =
                "http://caisse-epargne.fr/webservices/GetRice";
        public static final String GET_TRANSACTIONS =
                "http://caisse-epargne.fr/webservices/GetHistoriqueOperationsByCompte";
        public static final String SSO_BAPI = "http://caisse-epargne.fr/webservices/sso_BAPI";
        static final String VERSION_WS_BAD_22 = "V22";
        static final String CAISSE_DARWIN = "CaisseEpargne/742 CFNetwork/978.0.7 Darwin/18.7.0";
    }

    public static class QueryKeys {
        private QueryKeys() {}

        public static final String NONCE = "nonce";
        public static final String RESPONSE_TYPE = "response_type";
        public static final String CLIENT_ID = "client_id";
        public static final String REDIRECT_URI = "redirect_uri";
        public static final String BEARER = "Bearer";
        public static final String ACCESS_TOKEN = "access_token";
        public static final String TOKEN_TYPE = "token_type";
        public static final String EXPIRES_IN = "expires_in";
        public static final String ID_TOKEN = "id_token";
        public static final String TRANSACTION_ID = "transactionID";
        static final String ID_TOKEN_HINT = "id_token_hint";
        static final String LOGIN_HINT = "login_hint";
        static final String BPCESTA = "bpcesta";
        static final String CDETAB = "cdetab";
        static final String CLAIMS = "claims";
        static final String SECRET_IT = "secret_id";
        static final String DISPLAY = "display";
    }

    public static class QueryValues {
        private QueryValues() {}

        public static final String SECRET = "34791847-2cfe-4992-bff2-1c3327b92fab";
        public static final String CLIENT_ID = "f4ee2144-0d68-4b90-ae78-25e255a1f3ac";
        static final String TOUCH = "touch";
        static final String ID_TOKEN_TOKEN = "id_token token";
        static final String CONTAINER_APP_BAPI_SETUP_SUCCESS = "containerApp://BAPIStepUpSuccess";
    }

    public static class ResponseValues {
        private ResponseValues() {}

        public static final String NEGATIVE_BALANCE = "D";
        public static final String AUTHENTICATION_FAILED = "AUTHENTICATION_FAILED";
        public static final String FAILED_AUTHENTICATION = "FAILED_AUTHENTICATION";
        public static final String PASSWORD = "PASSWORD";
        public static final String RESPONSE_STATUS_SUCCESS = "0";
        public static final String AUTHENTICATION_LOCKED = "AUTHENTICATION_LOCKED";
        public static final String AUTHENTICATION_SUCCESS = "AUTHENTICATION_SUCCESS";
        public static final String AUTHENTICATION = "AUTHENTICATION";
        public static final String RETURN_CODE_OK = "0000";
        public static final String TRANSACTION_TYPE_INCOME = "C";
    }

    public static class RequestValues {
        private RequestValues() {}

        public static final int PAGE_SIZE = 100;
        public static final String TRANSACTION_REQUEST_TYPE_SUBSEQUENT = "S";
        public static final String TRANSACTION_REQUEST_TYPE_INITIAL = "D";
        public static final String CAISSE_EPARGNE = "CAISSE_EPARGNE";
        static final String IT_ENTITY_02 = "02";

        public enum ValidationTypes {
            PASSWORD("PASSWORD"),
            OTP("SMS");
            private final String name;

            ValidationTypes(String name) {
                this.name = name;
            }

            public String getName() {
                return name;
            }
        }
    }

    public static final ImmutableMap<MembershipTypes, String> MEMBERSHIP_TYPES_TO_VALUE_MAP =
            ImmutableMap.<MembershipTypes, String>builder()
                    .put(PART, "part")
                    .put(PRO, "pro")
                    .build();

    public static class StorageKeys {
        private StorageKeys() {}

        public static final String TOKEN = "token";
        public static final String IDENTIFICATION_ROUTING_RESPONSE =
                "identificationRoutingResponse";
        public static final String CREDENTIALS_RESPONSE = "credentialsResponse";
        public static final String BANK_ID = "bankId";
        public static final String SAML_TRANSACTION_PATH = "samlTransactionPath";
        public static final String COULD_AUTO_AUTHENTICATE = "couldAutoAuthenticate";
        static final String TERM_ID = "termId";
        public static final String FINAL_AUTH_RESPONSE = "finalAuthResponse";
        static final String REDIRECT_LOCATION = "redirectLocation";
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

        @Override
        public String toString() {
            return name;
        }
    }

    public static class SoapKeys {
        private SoapKeys() {}

        public static final String VALID_SUBSCRIPTION = "AbonnementValide";
        public static final String SURNAME = "Nom";
        public static final String NAME = "Prenom";
        public static final String INTERNAL_ACCOUNT = "CompteInterneSynt";
    }

    public static class ResponseKeys {
        private ResponseKeys() {}

        public static final String ACCOUNT_DETAILS_RESULT = "GetRiceResult";
        public static final String ACCOUNTS_RESPONSE = "GetSyntheseCpteAbonnementResult";
        public static final String TRANSACTIONS_RESULT = "GetHistoriqueOperationsByCompteResult";
    }

    public static class SoapXmlFragment {
        private SoapXmlFragment() {}

        public static final String PREFIX =
                "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"><soap:Body>";
        public static final String SUFFIX = "</soap:Body></soap:Envelope>";
    }

    public static class Step {
        private Step() {}

        public static final String AUTHORIZE = "AUTHORIZE";
        public static final String CREATE_BENEFICIARY = "CREATE_BENEFICIARY";
    }

    public static class ErrorCodes {
        private ErrorCodes() {}

        public static final String INSUFFICIENT_AUTHENTICATION = "insufficientAuthLevel";
        public static final String BENEFICIARY = "bpce.beneficiaire";
    }

    public static class ErrorMessages {
        private ErrorMessages() {}

        public static final String INSUFFICIENT_AUTHENTICATION =
                "Niveau dauthentification insuffisant";
        public static final String BENEFICIARY_ALREADY_EXISTS =
                "Ajout impossible, cet IBAN existe déjà dans la liste de vos bénéficiaires.";
    }
}

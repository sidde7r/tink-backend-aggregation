package se.tink.backend.aggregation.agents.nxgen.fr.banks.labanquepostale;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.regex.Pattern;
import se.tink.backend.aggregation.agents.utils.log.LogTag;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class LaBanquePostaleConstants {

    public static final String CURRENCY = "EUR";
    public static final String MARKET = "fr";
    public static final String PROVIDER_NAME = "fr-labanquepostale-password";
    public static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public static class Regex {

        static final String NUMPAD_URL_GROUP_NAME = "numpadquery";
        static final Pattern NUMPAD_QUERY_PATTERN =
                Pattern.compile(
                        String.format(
                                "(?:background:url\\()(?<%s>loginform\\?.+)(?:\\))",
                                NUMPAD_URL_GROUP_NAME));
    }

    private static class ApiServices {

        static final String KEEP_ALIVE_PATH =
                "/ws_qh5/bad/mobile/rest/api/v2/clients/A2G/statut-abonnements";
        static final String INIT_LOGIN_PATH = "/wsost/OstBrokerWeb/pagehandler";
        static final String GET_NUMPAD_BASE_PATH = "wsost/OstBrokerWeb/";
        static final String SUBMIT_LOGIN_PATH = "wsost/OstBrokerWeb/auth";
        static final String EQUIPMENTS_COMPTES =
                "ws_qh5/bad/mobile/canalREST/equipements/comptes_2.0.0.ea";
        static final String LISTE_MOUBVEMENTS_CNE =
                "ws_qh5/bad/mobile/canalREST/listemouvements/listeMouvementsCNE.ea";
        static final String LISTE_MOUBVEMENTS_CPP =
                "ws_qh5/bad/mobile/canalREST/listemouvements/listeMouvementsCCP.ea";
        static final String DECONNEXION =
                "ws_qh5/bad/mobile/canalJSON/authentification/deconnexion.ea";
    }

    public static class Urls {

        private static final String BASE = "https://m.labanquepostale.fr/";

        public static final URL KEEP_ALIVE = new URL(BASE + ApiServices.KEEP_ALIVE_PATH);
        public static final URL INIT_LOGIN = new URL(BASE + ApiServices.INIT_LOGIN_PATH);
        public static final URL GET_NUMPAD_BASE = new URL(BASE + ApiServices.GET_NUMPAD_BASE_PATH);
        public static final URL SUBMIT_LOGIN = new URL(BASE + ApiServices.SUBMIT_LOGIN_PATH);
        public static final URL ACCOUNTS = new URL(BASE + ApiServices.EQUIPMENTS_COMPTES);
        public static final URL TRANSACTIONS_SAVINGS_ACCOUNTS =
                new URL(BASE + ApiServices.LISTE_MOUBVEMENTS_CNE);
        public static final URL TRANSACTIONS_CHECKING_ACCOUNTS =
                new URL(BASE + ApiServices.LISTE_MOUBVEMENTS_CPP);
        public static final URL DISCONNECTION = new URL(BASE + ApiServices.DECONNEXION);
    }

    public static class QueryParams {

        public static final String TAM_OP = "TAM_OP";
        public static final String ERROR_CODE = "ERROR_CODE";
        public static final String URL = "URL";
        public static final String URL_BACKEND = "urlbackend";
        public static final String ORIGIN = "origin";
        public static final String PASSWORD = "password";
        public static final String USERNAME = "username";
        public static final String CV = "cv";
        public static final String CVVS = "cvvs";
        public static final String ERROR_PARAM = "param";
        protected static final String CODE_MEDIA = "codeMedia";
        protected static final String COMPTE_NUMERO = "compte.numero";
        protected static final String TYPE_RECHERCHE = "typeRecherche";
        protected static final String APPEL_ASSUARANCES = "appelAssuarances";
        protected static final String APPEL_PRETS = "appelPrets";
    }

    public static class QueryDefaultValues {

        public static final String LOGIN = "login";
        public static final String ZERO = "0x00000000";
        public static final String TACTILE = "tactile";
        public static final String TRUE = "true";
        public static final String MOBILE_AUTH_BACKEND =
                "/ws_qh5/bad/mobile/canalJSON/authentification/vide-identif.ea?"
                        + "origin=tactile&codeMedia=9241&version=06_00_01.004";
        protected static final String _9241 = "9241";
        protected static final String _10 = "10";
    }

    public static class AuthConfig {

        public static final int PASSWORD_LENGTH = 10;
        public static final int NUMPAD_WIDTH = 4;
        public static final int NUMPAD_HEIGHT = 4;
        public static final int NUMPAD_KEY_PADDING = 4;
    }

    public static class ErrorMessages {

        public static final String DUPLICATE_DIGITS = "OCR returned duplicate digits.";
        public static final String NO_NUMPAD_IMAGE = "Could not read numpad to image.";
        public static final String NO_NUMPAD_PARAMS = "Could not find numpad params in html doc.";
        public static final String UNKNOWN_ERROR = "Unknown error code %s was encountered.";
        public static final String WRONG_DIGIT_COUNT =
                String.format(
                        "Wrong number of keys found. Expected: %s", AuthConfig.PASSWORD_LENGTH);
    }

    public static class ErrorCodes {

        public static final String INCORRECT_CREDENTIALS = "0x132120c8";
    }

    public static class Logging {
        public static final LogTag UNKNOWN_ACCOUNT_TYPE =
                LogTag.from(PROVIDER_NAME + "-unknown-account-type");
    }

    public static class AccountType {
        private static final ImmutableMap<String, TransactionalAccountType> KNOWN_PRODUCT_CODES =
                ImmutableMap.<String, TransactionalAccountType>builder()
                        .put("000001", TransactionalAccountType.CHECKING)
                        .put("000002", TransactionalAccountType.SAVINGS)
                        .build();

        public static Optional<TransactionalAccountType> translate(String productCode) {
            if (Strings.isNullOrEmpty(productCode)) {
                return Optional.empty();
            }
            return Optional.ofNullable(
                    KNOWN_PRODUCT_CODES.getOrDefault(productCode.toUpperCase(), null));
        }
    }
}

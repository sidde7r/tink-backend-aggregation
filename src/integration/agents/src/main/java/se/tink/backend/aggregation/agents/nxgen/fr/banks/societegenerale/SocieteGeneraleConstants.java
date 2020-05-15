package se.tink.backend.aggregation.agents.nxgen.fr.banks.societegenerale;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import java.time.ZoneId;
import java.util.Optional;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.utils.log.LogTag;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class SocieteGeneraleConstants {

    public static final String CURRENCY = "EUR";
    public static final ZoneId ZONE_ID = ZoneId.of("Europe/Paris");
    public static final String MARKET = "fr";
    public static final String PROVIDER_NAME = "fr-societegenerale-password";

    public static class FormParam {
        public static final String BIO_TOKEN = "bio_token";
        public static final String CIBLE = "cible";
        public static final String CODSEC = "codsec";
        public static final String CRYPTOCVCS = "cryptocvcs";
        public static final String DPE = "dpe";
        public static final String JETON = "jeton";
        public static final String USER_ID = "user_id";
        public static final String VK_OP = "vk_op";
    }

    public static class QueryParam {
        static final String CRYPTOGRAMME = "cryptogramme";
        static final String NIV_AUTHENT = "niv_authent";
        static final String MODE_CLAVIER = "modeClavier";
        static final String VK_VISUEL = "vk_visuel";
        static final String B_64_ID_PRESTA = "b64_idPresta";
        static final String B_64_NUMERO_CARTE = "b64_numeroCarte";
        static final String A_100_TIMESTAMPREF = "a100_timestampref";
        static final String N_15_NB_OCC = "n15_nbOcc";
        static final String N_15_RANG_OCC = "n15_rangOcc";
    }

    public static class Default {
        static final String AUTHENTIFIE = "AUTHENTIFIE";
        public static final String ZERO = "0";
        static final String VK_WIDESCREEN = "vk_widescreen";
        public static final String EMPTY = "";
        public static final String _300 = "300";
        public static final String AUTH = "auth";
    }

    public static class Url {

        private static final String HOST = "https://app.secure.particuliers.societegenerale.mobi";

        static final URL SEC_VK_GEN_CRYPTO = new URL(HOST + "/sec/vk/gen_crypto.json");
        static final URL SEC_VK_GEN_UI = new URL(HOST + "/sec/vk/gen_ui");
        static final URL SEC_VK_AUTHENT = new URL(HOST + "/sec/vk/authent.json");
        static final URL GET_AUTH_INFO = new URL(HOST + "/getauthinfo.json");
        static final URL SBM_MOB_MOB_SBM_RLV_SNT_CPT =
                new URL(HOST + "/sbm-mob/mob/sbm-rlv-snt-cpt.json");
        static final URL ABM_RESTIT_CAV_LISTE_OPERATIONS =
                new URL(HOST + "/abm/restit/cav/listeOperations.json");
        public static final URL LOGOUT = new URL(HOST + "/logout.json");
    }

    public static class Logging {
        public static final LogTag UNKNOWN_ACCOUNT_TYPE =
                LogTag.from(PROVIDER_NAME + "-unknown-account-type");
        static final LogTag PARSE_FAILURE = LogTag.from(PROVIDER_NAME + "-parse-failure");
        static final LogTag REQUEST_NOT_OK = LogTag.from(PROVIDER_NAME + "-request-nok");
    }

    public static class AccountType {

        /** SocGen product code -> Tink account type */
        private static final ImmutableMap<String, TransactionalAccountType> ACCOUNT_TYPES_MAP =
                ImmutableMap.<String, TransactionalAccountType>builder()
                        .put("050", TransactionalAccountType.CHECKING)
                        .build();

        /**
         * null/empty means the product code is unknown, and {@link AccountTypes#OTHER} means the
         * (known) product code should be disregarded (not mapped to any Tink object).
         */
        public static Optional<TransactionalAccountType> translate(String productCode) {
            if (Strings.isNullOrEmpty(productCode)) {
                return Optional.empty();
            }
            return Optional.ofNullable(ACCOUNT_TYPES_MAP.get(productCode.toLowerCase()));
        }
    }

    public static class StorageKey {
        public static final String DEVICE_ID = "device_id";
        public static final String TOKEN = "token";
        public static final String SESSION_KEY = "sessionKey";
        public static final String TECHNICAL_ID = "technicalId";
        public static final String TECHNICAL_CARD_ID = "technicalCardId";
    }
}

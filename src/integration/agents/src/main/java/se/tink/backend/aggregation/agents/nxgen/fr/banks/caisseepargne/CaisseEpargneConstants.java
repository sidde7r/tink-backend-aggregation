package se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.nxgen.http.URL;

public class CaisseEpargneConstants {

    public static final String MARKET = "FR";
    public static final String PROVIDER_NAME = "fr-caisseepargne-password";

    private static final String BASE_URL = "https://www.s.caisse-epargne.fr";

    public static class AccountType {

        private static final ImmutableMap<String, AccountTypes> ACCOUNT_TYPES_MAP =
                ImmutableMap.<String, AccountTypes>builder()
                        .put("02", AccountTypes.CHECKING)
                        .put("04", AccountTypes.CHECKING)
                        .put("10", AccountTypes.SAVINGS)
                        .build();

        public static Optional<AccountTypes> translate(String productCode) {
            if (Strings.isNullOrEmpty(productCode)) {
                return Optional.empty();
            }
            return Optional.ofNullable(
                    ACCOUNT_TYPES_MAP.getOrDefault(productCode.toLowerCase(), null));
        }
    }

    public static class HeaderKey {
        public static final String X_AUTH_KEY = "X-auth-key";
        public static final String SOAP_ACTION = "SOAPAction";
    }

    public static class LogMessage {
        public static final String UNKNOWN_ACCOUNT_TYPE = "{} Unknown account type: {}";
    }

    public static class LogTag {
        public static final se.tink.backend.aggregation.agents.utils.log.LogTag
                UNKNOWN_ACCOUNT_TYPE =
                        se.tink.backend.aggregation.agents.utils.log.LogTag.from(
                                PROVIDER_NAME + "-unknown-account-type");
        public static final se.tink.backend.aggregation.agents.utils.log.LogTag PARSE_FAILURE =
                se.tink.backend.aggregation.agents.utils.log.LogTag.from(
                        PROVIDER_NAME + "-parse-failure");
        public static final se.tink.backend.aggregation.agents.utils.log.LogTag REQUEST_NOT_OK =
                se.tink.backend.aggregation.agents.utils.log.LogTag.from(
                        PROVIDER_NAME + "-request-nok");
    }

    public static class RequestValue {
        public static final String INSTITUTION = "CAISSE_EPARGNE";
        public static final String SERVICE_LEVEL = "PAR";
        public static final int MODE_ALD = 0;
        public static final String REQUEST_TYPE_INITIAL = "D";
        public static final String REQUEST_TYPE_SUBSEQUENT = "S";
        public static final int PAGE_SIZE = 100;
    }

    public static class ResponseValue {
        public static final String RETURN_CODE_OK = "0000";
        public static final String TRANSACTION_TYPE_INCOME = "R";
    }

    public static class SoapAction {
        public static final String AUTHENTIFIER =
                "http://caisse-epargne.fr/webservices/Authentifier";
        public static final String GET_INFOS_CLIENT =
                "http://caisse-epargne.fr/webservices/GetInfosClient";
        public static final String DECONNEXION = "http://caisse-epargne.fr/webservices/Deconnexion";
        public static final String GET_SYNTHESE_CPTE_ABONNEMENT =
                "http://caisse-epargne.fr/webservices/GetSyntheseCpteAbonnement";
        public static final String GET_HISTORIQUE_OPERATIONS_BY_COMPTE =
                "http://caisse-epargne.fr/webservices/GetHistoriqueOperationsByCompte";
    }

    public static class SoapXmlFragment {
        static final String XMLNS_TO_REMOVE = "xmlns=\"\"";
        static final String XMLNS_TO_ADD = "xmlns=\"http://caisse-epargne.fr/webservices/\"";
        static final String PREFIX =
                "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"><soap:Body>";
        static final String POSTFIX = "</soap:Body></soap:Envelope>";
    }

    public static class StorageKey {
        public static final String DEVICE_ID = "device_id";
    }

    public static class Url {
        public static final URL WS_BAD = new URL(BASE_URL + "/V22/WsBad/WsBad.asmx");
    }
}

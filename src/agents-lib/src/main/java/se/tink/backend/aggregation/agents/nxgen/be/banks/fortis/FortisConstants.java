package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis;

import se.tink.backend.aggregation.nxgen.http.URL;

public class FortisConstants {
    public static final String LANGUAGE = "en";

    public enum Url {
        GET_DISTRIBUTOR_AUTHENTICATION_MEANS(
                createUrlWithHost("/EBIA-pr01/rpc/means/getDistributorAuthenticationMeans")),
        CREATE_AUTHENTICATION_PROCESS(
                createUrlWithHost("/EBIA-pr01/rpc/auth/createAuthenticationProcess")),
        GENERATE_CHALLENGES(createUrlWithHost("/EBIA-pr01/rpc/auth/createAuthenticationProcess")),
        GET_USER_INFO(createUrlWithHost("/TFPL-pr01/rpc/intermediatelogon/getUserinfo")),
        GET_ILLEGAL_PASSWORD_LIST(
                createUrlWithHost("/TFPL-pr01/rpc/password/getIllegalPasswordList")),
        PREPARE_CONTRACT_UPDATE(
                createUrlWithHost("/TFPL-pr01/rpc/contractUpdate/prepareContractUpdate")),
        EXECUTE_CONTRACT_UPDATE(
                createUrlWithHost("/TFPL-pr01/rpc/contractUpdate/executeContractUpdate")),
        GET_AVAILABILITY(createUrlWithHost("/TFPL-pr01/rpc/availability/getAvailability")),
        GET_VIEW_ACCOUNT_LIST(createUrlWithHost("/AC52-pr01/rpc/accounts/getViewAccountList")),
        CHECK_DOCUMENT_AVAILABILITY(
                createUrlWithHost("/ZMPL-pr50/rpc/zoomit/V1/checkDocumentsAvailability")),
        GET_E_BANKING_USERS(createUrlWithHost("/EBIA-pr01/rpc/identAuth/getEBankingUsers"));

        private URL url;

        Url(String url) {
            this.url = new URL(url);
        }

        public URL parameter(String key, String value) {
            return url.parameter(key, value);
        }

        public URL queryParam(String key, String value) {
            return url.queryParam(key, value);
        }

        public static final String HOST = "https://app.easybanking.bnpparibasfortis.be";

        private static String createUrlWithHost(String uri) {
            return HOST + uri;
        }
    }
}

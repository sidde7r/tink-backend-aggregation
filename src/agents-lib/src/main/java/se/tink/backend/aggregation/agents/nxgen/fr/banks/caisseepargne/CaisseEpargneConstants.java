package se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne;

import se.tink.backend.aggregation.nxgen.http.URL;

public class CaisseEpargneConstants {

    public static final String MARKET = "FR";
    public static final String PROVIDER_NAME = "fr-caisseepargne-password";

    public static class Url {

        private static final String BASE = "https://www.s.caisse-epargne.fr";

        public static final URL WS_BAD = new URL(BASE + "/V22/WsBad/WsBad.asmx");
    }

    public static class QueryParam {

    }

    public static class Header {
        public static final String X_AUTH_KEY = "X-auth-key";
        public static final String SOAP_ACTION = "SOAPAction";
    }

    public static class Default {
        public static final String X_AUTH_KEY = "60626852464971856873";
    }

    public static class StorageKey {
        public static final String DEVICE_ID = "device_id";
    }

}

package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbrandedcards;

public abstract class SebBrandedCardsConstants {

    public static class Urls {
        private static final String BASE_URL = "https://id.sebkort.com";
        public static final String AUTH = BASE_URL + "/ath/tpp/tpplogin";
        public static final String TOKEN = "/mga4/sps/oauth/oauth20/token";
        private static final String BASE_CREDIT_CARD_ACCOUNTS = "/tpp/ais/v1/identified4";

        public static final String CREDIT_CARD_ACCOUNTS =
                BASE_CREDIT_CARD_ACCOUNTS + "/branded-card-accounts";
        public static final String CREDIT_CARD_TRANSACTIONS =
                BASE_CREDIT_CARD_ACCOUNTS + "/branded-card-accounts/{accountId}/transactions";
    }

    public static class Market {
        public static final String INTEGRATION_NAME = "seb";
        public static final String CLIENT_NAME = "tink";
    }

    public static class QueryKey {
        public static final String BRAND_ID = "brandId";
    }

    public static class BrandedCards {

        public static class Sweden {
            public static final String CIRCLE_K = "stse";
            public static final String EUROCARD = "ecse";
            public static final String FINNAIR = "fase";
            public static final String INGO = "jese";
            public static final String NK = "nkse";
            public static final String NORDIC_CHOICE_CLUB = "cose";
            public static final String OPEL = "opse";
            public static final String QUINTESSENTIALLY = "quse";
            public static final String SAAB = "sbse";
            public static final String SAS = "sase";
            public static final String SJ = "sjse";
            public static final String WALLET = "wase";
        }

        public static class Norway {
            public static final String CIRCLE_K = "stno";
            public static final String ESSO = "esno";
            public static final String EUROCARD = "ecno";
            public static final String FINNAIR = "fano";
            public static final String GLOBECARD = "gcno";
            public static final String NORDIC_CHOICE_CLUB = "cono";
            public static final String SAS = "sano";
            public static final String SEB_SELECTED = "ssno";
            public static final String VOLVO_KORTET = "vono";
        }

        public static class Denmark {
            public static final String EUROCARD = "ecdk";
            public static final String GLOBECARD = "gcdk";
            public static final String JYSKE = "jydk";
            public static final String QUINTESSENTIALLY = "qudk";
            public static final String SAS = "sadk";
        }

        public static class Finland {
            public static final String EUROCARD = "ecfi";
            public static final String FINNAIR = "fafi";
            public static final String SAS = "safi";
        }
    }
}

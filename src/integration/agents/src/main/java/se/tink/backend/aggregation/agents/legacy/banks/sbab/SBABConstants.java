package se.tink.backend.aggregation.agents.banks.sbab;

public class SBABConstants {

    public static class Url {
        private static final String HOST = "https://secure.sbab.se/";
        private static final String TRANSFER_BASE_ENDPOINT = HOST + "api/transfer-facade-service/";

        public static final String SAVED_RECIPIENTS = TRANSFER_BASE_ENDPOINT + "saved-recipients";

    }
}

package se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.rpc.entities.constants;

public final class EntitiesConstants {

    private EntitiesConstants() {
        throw new AssertionError();
    }

    public static class Urls {
        public static final String SOAP_NAMESPACE =
                "urn:selfservicemobile.bawag.com/ws/v0100-draft03";
    }

    public static class Messages {
        public static final String INPUT_NOT_17_DIGITS = "doesn't match [0-9]{17}";
        public static final String STRING_TOO_SHORT = "String too short, minimum is 5";
    }
}

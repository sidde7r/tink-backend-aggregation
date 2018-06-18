package se.tink.backend.aggregation.agents.nxgen.de.banks.targo;

import se.tink.backend.aggregation.nxgen.http.HeaderEnum;

public class TargoBankDEConstants {
    public static final String URL = "https://m.targobank.de/wsmobile/en/";

    //TODO: Think better about this solution
    public enum Headers implements HeaderEnum {
        URL("Host", "m.targobank.de");
        private final String key;
        private final String value;

        Headers(String key, String value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public String getKey() {
            return null;
        }

        @Override
        public String getValue() {
            return null;
        }
    }
}

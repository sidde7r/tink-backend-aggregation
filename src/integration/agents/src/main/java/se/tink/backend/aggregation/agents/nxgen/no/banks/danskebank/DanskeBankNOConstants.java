package se.tink.backend.aggregation.agents.nxgen.no.banks.danskebank;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DanskeBankNOConstants {

    public static final String HTTP_ORIGIN = "https://www.danskebank.dk";
    public static final String BANKID_DYNAMIC_JS_URL = "/Functions?stage=LogonStep1&secsystem=%s";
    public static final String NEMID_HTML_BOX_URL =
            "https://www.danskebank.dk/Documents/nemid.html?_=";
    public static final String AUTHENTICATION_FINISH_URL = "ebanking/ext/InitLogon";

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class JsonKeys {
        public static final String DEV_SECRET = "devSecret";
        public static final String CHALLENGE = "challenge";
        public static final String RESPONSE_DATA = "responseData";
        public static final String DEV_SERIAL_NUMBER = "devSerialNo";
    }
}

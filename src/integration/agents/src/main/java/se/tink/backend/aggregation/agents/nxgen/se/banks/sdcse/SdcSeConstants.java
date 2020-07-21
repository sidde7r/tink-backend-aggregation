package se.tink.backend.aggregation.agents.nxgen.se.banks.sdcse;

import se.tink.backend.aggregation.agents.models.TransactionTypes;
import se.tink.backend.aggregation.agents.utils.log.LogTag;

public class SdcSeConstants {

    public static class Market {
        public static final String SWEDEN = "SE";
        public static final String BASE_URL = "https://prod.smartse.sdc.dk/restapi/";
        public static final String PHONE_COUNTRY_CODE = "+46";
    }

    public static class Fetcher {
        public static final LogTag LOAN_LOGGING = LogTag.from("#loan_logging_sdc_se");
        public static final LogTag INVESTMENTS_LOGGING = LogTag.from("#investment_logging_sdc_se");
    }

    // do we really need this?
    public enum SdcSeTransactionType {
        CREDIT_CARD("Kortköp ", 8, TransactionTypes.CREDIT_CARD),
        CASH_WITHDRAWAL("Kontantuttag ", 13, TransactionTypes.WITHDRAWAL),
        AUTOGIRO("Autogiro ", 9, TransactionTypes.PAYMENT),
        DEPOSIT_OTHER_BANK("Insättning från annan bank ", 27, TransactionTypes.DEFAULT),
        DEPOSIT("Insättning ", 11, TransactionTypes.DEFAULT),
        CHARGEBACK("Återbetalning ", 14, TransactionTypes.DEFAULT);

        private final String label;
        private final int length;
        private final TransactionTypes type;

        SdcSeTransactionType(String label, int length, TransactionTypes type) {
            this.label = label;
            this.length = length;
            this.type = type;
        }

        public String getLabel() {
            return label;
        }

        public int getLength() {
            return length;
        }

        public TransactionTypes getType() {
            return type;
        }
    }

    public static class Secret {
        public static final String PUBLIC_CERT =
                "-----BEGIN CERTIFICATE-----\n"
                        + "MIIFRzCCBC+gAwIBAgIMEnlaWUkS4StrJyODMA0GCSqGSIb3DQEBCwUAMGYxCzAJ\n"
                        + "BgNVBAYTAkJFMRkwFwYDVQQKExBHbG9iYWxTaWduIG52LXNhMTwwOgYDVQQDEzNH\n"
                        + "bG9iYWxTaWduIE9yZ2FuaXphdGlvbiBWYWxpZGF0aW9uIENBIC0gU0hBMjU2IC0g\n"
                        + "RzIwHhcNMTcwNjE1MTE0MTA3WhcNMjAwNjE1MTE0MTA3WjB1MQswCQYDVQQGEwJE\n"
                        + "SzERMA8GA1UECBMIQmFsbGVydXAxETAPBgNVBAcTCEJhbGxlcnVwMRAwDgYDVQQL\n"
                        + "EwdTREMgQS9TMRAwDgYDVQQKEwdTREMgQS9TMRwwGgYDVQQDExNQcm9kLnNtYXJ0\n"
                        + "c2Uuc2RjLmRrMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAunsEyCiX\n"
                        + "h8hZYrqy+xrvIiHhUjuMa/j6ETEAtnM198OQpkr9ICqEeY9h0cvaUfMZ12TTTMLy\n"
                        + "s7FZtEJVZ8o1zWODsogm2TkwNLUtf8O/mGX7rG6yn9art7xi2dJJWOhFxZeeIMfB\n"
                        + "lZYr1Q5Sza/UOHH9g+Og+H7uKS++bpZPK0CXPakWXIEmdtY8FNzF3xs1lfpQV5kF\n"
                        + "whJ27GpKHy4cbOQYC0MoNNUNqu7FT9pZw4DqZ301yplWR01dOenOEGxB6OXqaL1U\n"
                        + "292bQlLpn/lDwsRmWZuSGPtDoV1ERL9CUA/Mv6g2A/qxcHau89fyEhBNuR4b0KR0\n"
                        + "jXis5OpuRaENAQIDAQABo4IB5DCCAeAwDgYDVR0PAQH/BAQDAgWgMIGgBggrBgEF\n"
                        + "BQcBAQSBkzCBkDBNBggrBgEFBQcwAoZBaHR0cDovL3NlY3VyZS5nbG9iYWxzaWdu\n"
                        + "LmNvbS9jYWNlcnQvZ3Nvcmdhbml6YXRpb252YWxzaGEyZzJyMS5jcnQwPwYIKwYB\n"
                        + "BQUHMAGGM2h0dHA6Ly9vY3NwMi5nbG9iYWxzaWduLmNvbS9nc29yZ2FuaXphdGlv\n"
                        + "bnZhbHNoYTJnMjBWBgNVHSAETzBNMEEGCSsGAQQBoDIBFDA0MDIGCCsGAQUFBwIB\n"
                        + "FiZodHRwczovL3d3dy5nbG9iYWxzaWduLmNvbS9yZXBvc2l0b3J5LzAIBgZngQwB\n"
                        + "AgIwCQYDVR0TBAIwADBJBgNVHR8EQjBAMD6gPKA6hjhodHRwOi8vY3JsLmdsb2Jh\n"
                        + "bHNpZ24uY29tL2dzL2dzb3JnYW5pemF0aW9udmFsc2hhMmcyLmNybDAeBgNVHREE\n"
                        + "FzAVghNQcm9kLnNtYXJ0c2Uuc2RjLmRrMB0GA1UdJQQWMBQGCCsGAQUFBwMBBggr\n"
                        + "BgEFBQcDAjAdBgNVHQ4EFgQUJUy3p/xb61JfEQU+viaLIEtesyIwHwYDVR0jBBgw\n"
                        + "FoAUlt5h8b0cFilTHMDMfTuDAEDmGnwwDQYJKoZIhvcNAQELBQADggEBAMYcGWNV\n"
                        + "nxe1DtQKmWqqvwuXHfwW3W7Yon+mxgqNX5VXXYi9aWyH8oEj3Dc3gfnY2WWgE6Ka\n"
                        + "vFUtrh/eck1i1dW1xYYqDOMlBk9mUDD5WOJpMbaijgB7ssO7QvzKuad4AwiYkC1w\n"
                        + "GwLDqxefQ7nFhbl6Q1rRUJ6XRknIRFFqU1Ns9ZV1G1euDS783aE6Mx3wHzm8YuLb\n"
                        + "kvt31PnAsbFZCcCtf3RRcT9MDAONvwAYvDmE/5O/XWnv452kn3pxN95hNBtG/ZsZ\n"
                        + "8inSFwQfcWJifOp6NSHOVVjnuruxWrEI2rcgaXyeIMfZK8k2qdDRdu0ueMofec1o\n"
                        + "A5pNgUNIAlvJtfA=\n"
                        + "-----END CERTIFICATE-----";
    }
}

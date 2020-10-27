package se.tink.backend.aggregation.agents.nxgen.fo.banks.sdcfo;

import se.tink.backend.aggregation.agents.utils.log.LogTag;

public class SdcFoConstants {
    public static class Market {
        public static final String BASE_URL = "https://prod.smartfo.sdc.dk/restapi/";
        public static final String PHONE_COUNTRY_CODE = "+298";
    }

    public static class Fetcher {
        public static final LogTag LOAN_LOGGING = LogTag.from("#loan_logging_sdc_fo");
        public static final LogTag INVESTMENTS_LOGGING = LogTag.from("#investment_logging_sdc_fo");
    }

    public static class Secret {
        public static final String PUBLIC_CERT =
                "-----BEGIN CERTIFICATE-----\n"
                        + "MIIFRzCCBC+gAwIBAgIMbhJ7wDHM4fmyF4KhMA0GCSqGSIb3DQEBCwUAMGYxCzAJ\n"
                        + "BgNVBAYTAkJFMRkwFwYDVQQKExBHbG9iYWxTaWduIG52LXNhMTwwOgYDVQQDEzNH\n"
                        + "bG9iYWxTaWduIE9yZ2FuaXphdGlvbiBWYWxpZGF0aW9uIENBIC0gU0hBMjU2IC0g\n"
                        + "RzIwHhcNMTcwNjE1MTE0MTAzWhcNMjAwNjE1MTE0MTAzWjB1MQswCQYDVQQGEwJE\n"
                        + "SzERMA8GA1UECBMIQmFsbGVydXAxETAPBgNVBAcTCEJhbGxlcnVwMRAwDgYDVQQL\n"
                        + "EwdTREMgQS9TMRAwDgYDVQQKEwdTREMgQS9TMRwwGgYDVQQDExNQcm9kLnNtYXJ0\n"
                        + "Zm8uc2RjLmRrMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAkx7DipYD\n"
                        + "tD57up5eeVlPHp4RZyVJf6NR2P1m0WzNoz0sqZgQy2FVFOH92GUFVhLhAQ19Rdaf\n"
                        + "7kjbDGl3kSSfYT91vx5Ra5VJvtvhW+fRnbTOvHSncxP37Sm1wkJr5K6Mb9wEG2Hi\n"
                        + "/9j6h3+wIwXCxkHdDXJ4G25S/X9RiJTBzS26EWwJZasrur60EUgCjCDS0F8OoSCn\n"
                        + "E69nbfzTYkcXisxpgb5owLjCysTNbCjn2zBpLqM9byGqYkDjr7W1RNDlGatmy2R0\n"
                        + "kzqovyjNvh8GKY08wYSfgQTC6pYcQ7KwhiPu0t/bj824v12KeEXFdSVOFkuES7iI\n"
                        + "CpNYNDcwRsPJowIDAQABo4IB5DCCAeAwDgYDVR0PAQH/BAQDAgWgMIGgBggrBgEF\n"
                        + "BQcBAQSBkzCBkDBNBggrBgEFBQcwAoZBaHR0cDovL3NlY3VyZS5nbG9iYWxzaWdu\n"
                        + "LmNvbS9jYWNlcnQvZ3Nvcmdhbml6YXRpb252YWxzaGEyZzJyMS5jcnQwPwYIKwYB\n"
                        + "BQUHMAGGM2h0dHA6Ly9vY3NwMi5nbG9iYWxzaWduLmNvbS9nc29yZ2FuaXphdGlv\n"
                        + "bnZhbHNoYTJnMjBWBgNVHSAETzBNMEEGCSsGAQQBoDIBFDA0MDIGCCsGAQUFBwIB\n"
                        + "FiZodHRwczovL3d3dy5nbG9iYWxzaWduLmNvbS9yZXBvc2l0b3J5LzAIBgZngQwB\n"
                        + "AgIwCQYDVR0TBAIwADBJBgNVHR8EQjBAMD6gPKA6hjhodHRwOi8vY3JsLmdsb2Jh\n"
                        + "bHNpZ24uY29tL2dzL2dzb3JnYW5pemF0aW9udmFsc2hhMmcyLmNybDAeBgNVHREE\n"
                        + "FzAVghNQcm9kLnNtYXJ0Zm8uc2RjLmRrMB0GA1UdJQQWMBQGCCsGAQUFBwMBBggr\n"
                        + "BgEFBQcDAjAdBgNVHQ4EFgQUoyyZDDhOMsBdK5kelKKs0R9l6jUwHwYDVR0jBBgw\n"
                        + "FoAUlt5h8b0cFilTHMDMfTuDAEDmGnwwDQYJKoZIhvcNAQELBQADggEBAJfM9Cd/\n"
                        + "y/VncRw4ty09OfGcWKBAaA6gGSlHwr3q2uGmZ2QT/7cisatQ+DEGvKdd6zj9zh9z\n"
                        + "wUp3F9RH2eJW+YU03OCpwrXEPFDjqUjUXR4r3ww9Kb49/D6PJh932ML1tg7h3+4U\n"
                        + "mLjD/oTmRJu1iYwBiKgBziVIqMf0Fj+wFhzZ92TgHr1Gdih6nrun+muEPfauuBjN\n"
                        + "kDp/Bkw3SSplTZp41pkSX44Qr064wl2IG5yrncaN+7oh9PBjsvg5q1QjAQOaTKox\n"
                        + "KVT4rlaABfY4eYWVRr+q1JFDfTqGoI40+0v0kyIZWncd0HHjOf1HhboJFB+r9dl9\n"
                        + "kGarDgvUhUSKKyU=\n"
                        + "-----END CERTIFICATE-----";
    }
}

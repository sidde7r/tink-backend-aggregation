package se.tink.backend.aggregation.agents.nxgen.dk.banks.sdcdk;

import com.google.common.collect.ImmutableList;
import se.tink.backend.aggregation.agents.utils.log.LogTag;

public class SdcDkConstants {

    public static class Authentication {
        public static final ImmutableList<String> BANKS_WITH_PIN_AUTHENTICATION =
                ImmutableList.of("9740", "9922", "0537", "9684");
    }

    public static class Market {
        public static final String DENMARK = "DK";
        public static final String BASE_URL = "https://prod.smartdk.sdc.dk/restapi/";
        public static final String PHONE_COUNTRY_CODE = "+45";
    }

    public static class Fetcher {
        public static final LogTag LOAN_LOGGING = LogTag.from("#loan_logging_sdc_dk");
        public static final LogTag INVESTMENTS_LOGGING = LogTag.from("#investment_logging_sdc_dk");
    }

    public static class Secret {
        public static final String PUBLIC_CERT =
                "-----BEGIN CERTIFICATE-----\n"
                        + "MIIFRzCCBC+gAwIBAgIML3vNmSD2l29X+NliMA0GCSqGSIb3DQEBCwUAMGYxCzAJ\n"
                        + "BgNVBAYTAkJFMRkwFwYDVQQKExBHbG9iYWxTaWduIG52LXNhMTwwOgYDVQQDEzNH\n"
                        + "bG9iYWxTaWduIE9yZ2FuaXphdGlvbiBWYWxpZGF0aW9uIENBIC0gU0hBMjU2IC0g\n"
                        + "RzIwHhcNMTcwNjE1MTEzNjA2WhcNMjAwNjE1MTEzNjA2WjB1MQswCQYDVQQGEwJE\n"
                        + "SzERMA8GA1UECBMIQmFsbGVydXAxETAPBgNVBAcTCEJhbGxlcnVwMRAwDgYDVQQL\n"
                        + "EwdTREMgQS9TMRAwDgYDVQQKEwdTREMgQS9TMRwwGgYDVQQDExNQcm9kLnNtYXJ0\n"
                        + "ZGsuc2RjLmRrMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA0CzCdZTd\n"
                        + "Kpwq/tBKVbAZzugb2r75GPOWlMK/gzl6p1ignL5x5bDoFYIePV5QXjnhi5bHoFvp\n"
                        + "9dKAQGuL2co3xwp5rLfOBd3VxWLP98x6HDoaV628w7UU9SC3CftHSz59d2L2FHVc\n"
                        + "55kgJRAa2YyGjwjmiAZniPXoQhNZsf0QzdXT5Ns8b7iLvlFDDrOnuEkXLFNrzNiI\n"
                        + "9nmnUpixAnfLud9/EzIUpfmhXh/DZsrDUMsUTTZkS1WywozkpQGYRPBlj7OuV96n\n"
                        + "N5V0qK62RMZqZBNbmjIsdtLSYcbOYYwiEkfMzy+kDeyvJrAw3yOKzQsecf10Cylk\n"
                        + "XMFlHhLuWb+EDQIDAQABo4IB5DCCAeAwDgYDVR0PAQH/BAQDAgWgMIGgBggrBgEF\n"
                        + "BQcBAQSBkzCBkDBNBggrBgEFBQcwAoZBaHR0cDovL3NlY3VyZS5nbG9iYWxzaWdu\n"
                        + "LmNvbS9jYWNlcnQvZ3Nvcmdhbml6YXRpb252YWxzaGEyZzJyMS5jcnQwPwYIKwYB\n"
                        + "BQUHMAGGM2h0dHA6Ly9vY3NwMi5nbG9iYWxzaWduLmNvbS9nc29yZ2FuaXphdGlv\n"
                        + "bnZhbHNoYTJnMjBWBgNVHSAETzBNMEEGCSsGAQQBoDIBFDA0MDIGCCsGAQUFBwIB\n"
                        + "FiZodHRwczovL3d3dy5nbG9iYWxzaWduLmNvbS9yZXBvc2l0b3J5LzAIBgZngQwB\n"
                        + "AgIwCQYDVR0TBAIwADBJBgNVHR8EQjBAMD6gPKA6hjhodHRwOi8vY3JsLmdsb2Jh\n"
                        + "bHNpZ24uY29tL2dzL2dzb3JnYW5pemF0aW9udmFsc2hhMmcyLmNybDAeBgNVHREE\n"
                        + "FzAVghNQcm9kLnNtYXJ0ZGsuc2RjLmRrMB0GA1UdJQQWMBQGCCsGAQUFBwMBBggr\n"
                        + "BgEFBQcDAjAdBgNVHQ4EFgQUYHDjqilZ1aKt4L4XnlHnKRDWB6kwHwYDVR0jBBgw\n"
                        + "FoAUlt5h8b0cFilTHMDMfTuDAEDmGnwwDQYJKoZIhvcNAQELBQADggEBADKUdUsi\n"
                        + "15G38tVPODowfEvtPuby8nn2tq0DJ/GuZG0MLlJapPICZ+yqwaGd3udnS18mKJBu\n"
                        + "kKzIX6GSdD/FxNY04p+c6Y2Ci4A+lGHz78HBq8UImc3im2T7po+/MSNYYIDX5y06\n"
                        + "MaOdv2T1m8Kj7AGF6lt5cBfE+gK0GmYxXXtctPR6wFoN+l4FvRQSdCfM29WnDbiB\n"
                        + "ADasdg/Tsv46jRI8MvrTghSgy/DKTGE6fN9m5PrKoxNNLnivQzr1wNRspv2sPkCx\n"
                        + "dN3eRuWFGlpf0VL2FC6zk8KdXMo+kdjQdrqGwtZnTanghXSkZtau2KS36i/NQ8vU\n"
                        + "YKvrGA4dX3WVbyo=\n"
                        + "-----END CERTIFICATE-----";
    }
}

package se.tink.backend.aggregation.agents.nxgen.de.openbanking.postbank;

import se.tink.libraries.i18n.LocalizableParametrizedKey;

public final class PostbankConstants {

    public static class InfoScreen {
        public static final LocalizableParametrizedKey INSTRUCTIONS =
                new LocalizableParametrizedKey(
                        "Please open the BestSign app on device \"{0}\" and confirm login. Then click the \"Submit\" button");
    }

    public static class PollStatus {
        public static final int MAX_POLL_ATTEMPTS = 40;
        public static final String FINALISED = "finalised";
        public static final String FAILED = "failed";
    }

    public static class Crypto {
        public static final String URL =
                "https://xs2a.db.com/pb/aspsp-certificates/tpp-pb-password_cert.pem";

        public static final String CERTIFICATE =
                "MIIGvDCCBaSgAwIBAgIQDzoMwsSCfrBvKbZ2rDOOxDANBgkqhkiG9w0BAQsFADBE"
                        + "MQswCQYDVQQGEwJVUzEVMBMGA1UEChMMRGlnaUNlcnQgSW5jMR4wHAYDVQQDExVE"
                        + "aWdpQ2VydCBFViBSU0EgQ0EgRzIwHhcNMjEwMzAyMDAwMDAwWhcNMjIwMzA2MjM1"
                        + "OTU5WjCCAQ4xHTAbBgNVBA8MFFByaXZhdGUgT3JnYW5pemF0aW9uMRMwEQYLKwYB"
                        + "BAGCNzwCAQMTAkRFMRcwFQYLKwYBBAGCNzwCAQITBkhlc3NlbjEiMCAGCysGAQQB"
                        + "gjc8AgEBExFGcmFua2Z1cnQgYW0gTWFpbjESMBAGA1UEBRMJSFJCIDMwMDAwMQsw"
                        + "CQYDVQQGEwJERTEPMA0GA1UECBMGSGVzc2VuMRowGAYDVQQHExFGcmFua2Z1cnQg"
                        + "YW0gTWFpbjEYMBYGA1UECRMPVGF1bnVzYW5sYWdlIDEyMRkwFwYDVQQKExBEZXV0"
                        + "c2NoZSBCYW5rIEFHMRgwFgYDVQQDEw90cHAucG9zdGJhbmsuZGUwggEiMA0GCSqG"
                        + "SIb3DQEBAQUAA4IBDwAwggEKAoIBAQCmhc9FfFxE/8IeRPRtqv23qI4fG9Djvv6L"
                        + "4nm1MVdkF4FmHhskfXUCdynEw9mu6pEXWFc4SM5AFJzMlk6O8/QTfWZEtQhXQszn"
                        + "a1skKucEvKU1MuS2sxWlhqBA1yj00Whs6rfJKrqhUTf0rhlaupSOQPStK9TzThra"
                        + "XIrOgDYwaODbpbRPI20ssqPQop24+PSqvCyE5uuyBt2zCT0iwtnMFf8HHf+MrV0g"
                        + "7ScgXaebwmlBq9hppJTiKG7FxNp3ByDlujqIJbCELONEZ3HTrvxsV7KB0IMq6oST"
                        + "ozvVAHryO7m3RJIwZZqaCxv6Z9FODnXN70iMbeKO/49kiBoNEPaVAgMBAAGjggLc"
                        + "MIIC2DAfBgNVHSMEGDAWgBRqTlC/mGidW3sgddRZAXlIZpIyBjAdBgNVHQ4EFgQU"
                        + "s94FFni1xjpAv+R023HrCyqy6lgwGgYDVR0RBBMwEYIPdHBwLnBvc3RiYW5rLmRl"
                        + "MA4GA1UdDwEB/wQEAwIFoDAdBgNVHSUEFjAUBggrBgEFBQcDAQYIKwYBBQUHAwIw"
                        + "dQYDVR0fBG4wbDA0oDKgMIYuaHR0cDovL2NybDMuZGlnaWNlcnQuY29tL0RpZ2lD"
                        + "ZXJ0RVZSU0FDQUcyLmNybDA0oDKgMIYuaHR0cDovL2NybDQuZGlnaWNlcnQuY29t"
                        + "L0RpZ2lDZXJ0RVZSU0FDQUcyLmNybDBKBgNVHSAEQzBBMAsGCWCGSAGG/WwCATAy"
                        + "BgVngQwBATApMCcGCCsGAQUFBwIBFhtodHRwOi8vd3d3LmRpZ2ljZXJ0LmNvbS9D"
                        + "UFMwcwYIKwYBBQUHAQEEZzBlMCQGCCsGAQUFBzABhhhodHRwOi8vb2NzcC5kaWdp"
                        + "Y2VydC5jb20wPQYIKwYBBQUHMAKGMWh0dHA6Ly9jYWNlcnRzLmRpZ2ljZXJ0LmNv"
                        + "bS9EaWdpQ2VydEVWUlNBQ0FHMi5jcnQwDAYDVR0TAQH/BAIwADCCAQMGCisGAQQB"
                        + "1nkCBAIEgfQEgfEA7wB1ACl5vvCeOTkh8FZzn2Old+W+V32cYAr4+U1dJlwlXceE"
                        + "AAABd/Nr9f8AAAQDAEYwRAIgJZK/ZbZBp/cRG8NZNAkKRqSWnjUwVLPmqb4veAej"
                        + "2tQCICS0x0po4GyAmyPXFXBqgHxJAHLDKxRoDsbXqhRefyQDAHYAIkVFB1lVJFaW"
                        + "P6Ev8fdthuAjJmOtwEt/XcaDXG7iDwIAAAF382v1XwAABAMARzBFAiAaAUjB1CZw"
                        + "gcv9O9ZhaxkVXXyQ57KLzQSiU38MT2BsNgIhAJWaLrpfgFfnJr82OflxJOjTmV5M"
                        + "4ZAUTuHYOCBrV2qzMA0GCSqGSIb3DQEBCwUAA4IBAQCMA1MtiSVa85nt8v6mXZCF"
                        + "dqL/J1Yk2Sh2I6yhCR3Hd7CaGvTp7+zqstTc+RESa4PmugTDGJjIRMwoRR3LgFV8"
                        + "0K20y/EAlMk/1YKO2K27RyCZhs4/uF2MHfy6+PXvCaoZaTxn1A6M5vUntdMTjUp0"
                        + "KlY55oQpDNpaq+X3jrzK12gTnGD7fB8ZpmbQ2LSCXH9WqN25LaEJjGv7/vP1Z0je"
                        + "g7NbR8oiwuTxIl45QjtBAGwHopzbp5nFpQtR3UXQhwsIlVvfGrI9qXTYyo2P0qIh"
                        + "U1fy8rI9hhZya8gs8X/CpruJ3G+6KT6pniIvrZMkZU6DAWIEDnndl1gsvC5MJ6dM";
    }

    private PostbankConstants() {
        throw new AssertionError();
    }
}

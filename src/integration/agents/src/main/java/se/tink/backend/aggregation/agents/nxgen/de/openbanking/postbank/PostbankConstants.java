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
                "MIIHkzCCBnugAwIBAgIQBbJNjSt9mP8bUddvu+/bEjANBgkqhkiG9w0BAQsFADB1"
                        + "MQswCQYDVQQGEwJVUzEVMBMGA1UEChMMRGlnaUNlcnQgSW5jMRkwFwYDVQQLExB3"
                        + "d3cuZGlnaWNlcnQuY29tMTQwMgYDVQQDEytEaWdpQ2VydCBTSEEyIEV4dGVuZGVk"
                        + "IFZhbGlkYXRpb24gU2VydmVyIENBMB4XDTE5MDUxNTAwMDAwMFoXDTIxMDUxNTEy"
                        + "MDAwMFowggEkMR0wGwYDVQQPDBRQcml2YXRlIE9yZ2FuaXphdGlvbjETMBEGCysG"
                        + "AQQBgjc8AgEDEwJERTEXMBUGCysGAQQBgjc8AgECEwZIZXNzZW4xIjAgBgsrBgEE"
                        + "AYI3PAIBARMRRnJhbmtmdXJ0IGFtIE1haW4xEjAQBgNVBAUTCUhSQiA0NzE0MTEL"
                        + "MAkGA1UEBhMCREUxHDAaBgNVBAgTE05vcmRyaGVpbi1XZXN0ZmFsZW4xDTALBgNV"
                        + "BAcTBEJvbm4xKzApBgNVBAoTIkRCIFByaXZhdC0gdW5kIEZpcm1lbmt1bmRlbmJh"
                        + "bmsgQUcxHDAaBgNVBAsTE1Bvc3RiYW5rIFN5c3RlbXMgQUcxGDAWBgNVBAMTD3Rw"
                        + "cC5wb3N0YmFuay5kZTCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAIeu"
                        + "7qhuU1Jw9bwXDYPbjojpUq83PzHe1kXxWStLo3iYv3h0Rd7W9JntcLG0yLSS49K/"
                        + "iiosvO1MNmVlca6n7eJwj0ymy3foUx5Bg1d+QCyUI5zjQmb1J+pzL/mop+mQKlnR"
                        + "j9+ZPMfq+KTvesz7So69Gi1RGUfDc3QWpvt/aMI/rJxkzqba9+VBbhZ9MOJUY7Mq"
                        + "o5X9fmLrpL3KX8juVHJ7X2qFUJnEb7ElvKJ02TrFwQuBh+JJyknSaV6qoS249aEn"
                        + "7mzGRoTZK6XQiaWj25XRQaI1utSr5kgQsrxtjoupmf3A8FGh8iXz+rIJCrRN2hHi"
                        + "Pyzl4RTo8Owujd9O12ECAwEAAaOCA2wwggNoMB8GA1UdIwQYMBaAFD3TUKXWoK3u"
                        + "80pgCmXTIdT4+NYPMB0GA1UdDgQWBBSBT/ghO5+JxXv9ZkXUYNwaYyrexTAaBgNV"
                        + "HREEEzARgg90cHAucG9zdGJhbmsuZGUwDgYDVR0PAQH/BAQDAgWgMB0GA1UdJQQW"
                        + "MBQGCCsGAQUFBwMBBggrBgEFBQcDAjB1BgNVHR8EbjBsMDSgMqAwhi5odHRwOi8v"
                        + "Y3JsMy5kaWdpY2VydC5jb20vc2hhMi1ldi1zZXJ2ZXItZzIuY3JsMDSgMqAwhi5o"
                        + "dHRwOi8vY3JsNC5kaWdpY2VydC5jb20vc2hhMi1ldi1zZXJ2ZXItZzIuY3JsMEsG"
                        + "A1UdIAREMEIwNwYJYIZIAYb9bAIBMCowKAYIKwYBBQUHAgEWHGh0dHBzOi8vd3d3"
                        + "LmRpZ2ljZXJ0LmNvbS9DUFMwBwYFZ4EMAQEwgYgGCCsGAQUFBwEBBHwwejAkBggr"
                        + "BgEFBQcwAYYYaHR0cDovL29jc3AuZGlnaWNlcnQuY29tMFIGCCsGAQUFBzAChkZo"
                        + "dHRwOi8vY2FjZXJ0cy5kaWdpY2VydC5jb20vRGlnaUNlcnRTSEEyRXh0ZW5kZWRW"
                        + "YWxpZGF0aW9uU2VydmVyQ0EuY3J0MAkGA1UdEwQCMAAwggF/BgorBgEEAdZ5AgQC"
                        + "BIIBbwSCAWsBaQB2AO5Lvbd1zmC64UJpH6vhnmajD35fsHLYgwDEe4l6qP3LAAAB"
                        + "arqYwFYAAAQDAEcwRQIgb38UFfpVBE8IHNIKcXwtay9naGr0PT+rzJehTnu1Wn4C"
                        + "IQD2mplj7Y51PYjOBZus0LtvzvCSwLH+Jgt36Ccohse1UwB3AFYUBpov18Ls0/Xh"
                        + "vUSyPsdGdrm8mRFcwO+UmFXWidDdAAABarqYwNoAAAQDAEgwRgIhALz14mR7xqRR"
                        + "gg+JaRB+g4awZTUhFrr7ZoKqXFwub0DEAiEAil4thMB1c4uhfsOAexaJeIA6RUwE"
                        + "Tc4suCP/JsDCTMoAdgCHdb/nWXz4jEOZX73zbv9WjUdWNv9KtWDBtOr/XqCDDwAA"
                        + "AWq6mMKmAAAEAwBHMEUCIQCXH0/oJV+Y8BbqewAvTgl9xtXyxWLZQPFNCMMwrp09"
                        + "YQIgFGUrU1i2DzLnbets6vxlWqWFOPGzsqvRcbxFWzG/J+cwDQYJKoZIhvcNAQEL"
                        + "BQADggEBAE4VGSnUpLX6/pFae8aB08LGC5C+bUQ4mZ8mgpkc2e7Kxi0s22Xe4z9/"
                        + "Q8HzBNQ/zxadDTd5hAoM7wuN300VH4mB9PE9BlOOlGOdqDmCjam4XCuJr/0uFf82"
                        + "pM9/i2DEtH49BpU1siYvJdDXkIcXhb6ggCpISS6TDpcNinQ4Ug1bkDugk/GgbzZa"
                        + "KeBo5ZbP9xJBxsZ4ir8b/6UTHB9hvXy9AZFBAU0r09RZEzpNfXo8cTd7JFSKzT8V"
                        + "pb1rVF4l6X3rLFepgPFMM33t8hJ9bJG3DwpUA8WmuSf/09j3B007mAeMFNpMMId7"
                        + "rCjkCZSxcYgQc8dvvFOTXx3XvP7PdVM=";
    }

    private PostbankConstants() {
        throw new AssertionError();
    }
}

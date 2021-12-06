package se.tink.backend.aggregation.agents.nxgen.se.creditcards.entercard.coop;

import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.util.Collections;
import se.tink.libraries.cryptography.parser.Jks;
import se.tink.libraries.cryptography.parser.Pem;

class CoopTrustMaterialProvider {
    private static final String THAWTE_EV_RSA_CA_2018_INTERMEDIATE =
            "-----BEGIN CERTIFICATE-----\n"
                    + "MIIEoDCCA4igAwIBAgIQBpaPlkroI1bHThfCtTZbADANBgkqhkiG9w0BAQsFADBs\n"
                    + "MQswCQYDVQQGEwJVUzEVMBMGA1UEChMMRGlnaUNlcnQgSW5jMRkwFwYDVQQLExB3\n"
                    + "d3cuZGlnaWNlcnQuY29tMSswKQYDVQQDEyJEaWdpQ2VydCBIaWdoIEFzc3VyYW5j\n"
                    + "ZSBFViBSb290IENBMB4XDTE3MTEwNjEyMjI1N1oXDTI3MTEwNjEyMjI1N1owXzEL\n"
                    + "MAkGA1UEBhMCVVMxFTATBgNVBAoTDERpZ2lDZXJ0IEluYzEZMBcGA1UECxMQd3d3\n"
                    + "LmRpZ2ljZXJ0LmNvbTEeMBwGA1UEAxMVVGhhd3RlIEVWIFJTQSBDQSAyMDE4MIIB\n"
                    + "IjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAp0Cu52zmdJFnSezXMKvL0rso\n"
                    + "WgA/1X7OxjMQHsAllID1eDG836ptJXSTPg+DoEenHfkKyw++wXobgahr0cU/2v8R\n"
                    + "WR3fID53ZDhEGHzS+Ol7V+HRtZG5teMWCY7gldtBQH0r7xUEp/3ISVsZUVBqtUmL\n"
                    + "VJlf9nxJD6Cxp4LBlcJJ8+N6kSkV+fA+WdQc0HYhXSg3PxJP7XSU28Wc7gf6y9kZ\n"
                    + "zQhK4WrZLRrHHbHC2QXdqQYUxR927QV+UCNXnlbTcZy2QpxWTPLzK+/cKXX4cwP6\n"
                    + "MGF7+8RnUgHlij/5V2k/tIF9ep4B72ucqaS/UhEPpIN/T7A3OAw995yrB38glQID\n"
                    + "AQABo4IBSTCCAUUwHQYDVR0OBBYEFOcB/AwWGMp9sozshyejb2GBO4Q5MB8GA1Ud\n"
                    + "IwQYMBaAFLE+w2kD+L9HAdSYJhoIAu9jZCvDMA4GA1UdDwEB/wQEAwIBhjAdBgNV\n"
                    + "HSUEFjAUBggrBgEFBQcDAQYIKwYBBQUHAwIwEgYDVR0TAQH/BAgwBgEB/wIBADA0\n"
                    + "BggrBgEFBQcBAQQoMCYwJAYIKwYBBQUHMAGGGGh0dHA6Ly9vY3NwLmRpZ2ljZXJ0\n"
                    + "LmNvbTBLBgNVHR8ERDBCMECgPqA8hjpodHRwOi8vY3JsMy5kaWdpY2VydC5jb20v\n"
                    + "RGlnaUNlcnRIaWdoQXNzdXJhbmNlRVZSb290Q0EuY3JsMD0GA1UdIAQ2MDQwMgYE\n"
                    + "VR0gADAqMCgGCCsGAQUFBwIBFhxodHRwczovL3d3dy5kaWdpY2VydC5jb20vQ1BT\n"
                    + "MA0GCSqGSIb3DQEBCwUAA4IBAQAWGka+5ffLpfFuzT+WlwDRwhyTZSunnvecZWZT\n"
                    + "PPKXipynjpXx5dK8YG+2XoH74285GR1UABuvHMFV94XeDET9Pzz5s/NHS1/eAr5e\n"
                    + "GdwfBl80XwPkwXaYqzRtw6J4RAxeLqcbibhUQv9Iev9QcP0kNPyJu413Xov76mSu\n"
                    + "JlGThKzcurJPive2eLmwmoIgTPH11N/IIO9nHLVe8KTkt+FGgZCOWHA3kbFBZR39\n"
                    + "Mn2hFS974rhUkM+VS9KbCiQQ5OwkfbZ/6BINkE1CMtiESZ2WkbxJKPsF3dN7p9DF\n"
                    + "YWiQSbYjFP+rCT0/MkaHHYUkEvLNPgyJ6z29eMf0DjLu/SXJ\n"
                    + "-----END CERTIFICATE-----";

    static KeyStore coopTrustStore() {
        try {
            return Jks.getSystemTrustStoreWithAdditionalCertificates(
                    Collections.singletonList(
                            Pem.parseCertificate(
                                    THAWTE_EV_RSA_CA_2018_INTERMEDIATE.getBytes(
                                            StandardCharsets.US_ASCII))));
        } catch (CertificateException e) {

            throw new IllegalStateException("could not load trust mateerial", e);
        }
    }
}

package se.tink.backend.aggregation.nxgen.http.truststrategy;

import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;

@Ignore
public class TrustRootCaStrategyTest {

    private static final String JKS_PASSWORD = "tinktink";
    // This JKS contain the UK openbanking root CA certificate
    private static final byte[] JKS_DATA = EncodingUtils.decodeBase64String(
            "/u3+7QAAAAIAAAACAAAAAgAEcm9vdAAAAWXmvwC2AAVYLjUwOQAABWQwggVgMIIDSKADAgECAgRZ\n"
                    + "xPz2MA0GCSqGSIb3DQEBCwUAMFAxCzAJBgNVBAYTAkdCMRQwEgYDVQQKEwtPcGVuQmFua2luZzEr\n"
                    + "MCkGA1UEAxMiT3BlbkJhbmtpbmcgUHJlLVByb2R1Y3Rpb24gUm9vdCBDQTAeFw0xNzA5MjIxMTM5\n"
                    + "NDJaFw0zNzA5MjIxMjA5NDJaMFAxCzAJBgNVBAYTAkdCMRQwEgYDVQQKEwtPcGVuQmFua2luZzEr\n"
                    + "MCkGA1UEAxMiT3BlbkJhbmtpbmcgUHJlLVByb2R1Y3Rpb24gUm9vdCBDQTCCAiIwDQYJKoZIhvcN\n"
                    + "AQEBBQADggIPADCCAgoCggIBAJi6TezweqU1AvpqEm4fJuzAKAhaQO/pTs7dZs1Y14hHc90AR3bc\n"
                    + "SmlxPavrP3r5OsuoYKB1/rPZii4s63rYDYmoniIlpUWO3R72PltCo+E0RWJ5Ko0HzGFSWkYURd2P\n"
                    + "sY/DTeq9J6oYhNJ+OxX1WLw1mBSq1gp1k9VrUUve9HVhU3Tnac7+0V7CgWfkB0QL9Kukg/JeTnRK\n"
                    + "9fNvH28EmcDowsnwKgj3I3IjuPpDIvfk55R/pzt8NOQsHZV/b1GMv9iEnLvQZe3SxRsvk31F7Ovw\n"
                    + "mWHAYQ9GmvQP6srJgugZYtS2hK9jA8Lzl48xaZ2VTA5oLn00tSKGLLi6zMTao/YImyUd06GBaQJD\n"
                    + "zor7KBr6UKHoZfuo9ko2dLdiyNn7bejX5k3Nspw4axsJ0/+FfNNr8Px/Kj/QxMypewRs6qqhwPUJ\n"
                    + "8Y2/UF+fatt7jWgoC/Z+IRh/hCur1/XqOtMrh/dtQDVXvGK7ZhEMV1InsJm+2ukvA9c/9Z5hlp47\n"
                    + "5IlCrb4CDrtoSL70D5nL5CNbCMKiqA0kZFmISNCFTuBfo3P0Z5E1XdEYdQpSpG0WszG8bQzUWM4M\n"
                    + "LuSiwSxxm0jgWQFMu0ChXheo6wu1+baTGtMDnzN/52eUhvnUyd7rd/kZR3j4LJvdJRy+oWPgVC3M\n"
                    + "g7jgW8CutoWQK8L++LkhgA29AgMBAAGjQjBAMA4GA1UdDwEB/wQEAwIBBjAPBgNVHRMBAf8EBTAD\n"
                    + "AQH/MB0GA1UdDgQWBBTsOI4L2vP5Nz6Q3n1fauZgzXlCgzANBgkqhkiG9w0BAQsFAAOCAgEAGJ0b\n"
                    + "qleE0uYs7MeXuU/eP9IQTgyr0NAuSKt8zcl5eZw4HTErCq+nMlKN4rH36FiVBEqishzlAPWEsqqM\n"
                    + "bsKpQ1wKA8ZdT3qKh9xQ+2EUj5+CQkvrIw62UuKJ2UWVyFiYgeTZKOExQ/9NDPAd7YJrSywJGDIn\n"
                    + "UMSRrGS6sRvYfyNlWkRWUcmtzeK3HNibhSfXVmH5us2+tl9zY+HLh+7cdySmHEKwnvplunKK5nuV\n"
                    + "acNPqE40f7MQchOwSZiyN7XFGINHcwlK3LvF/Tqp7tQQq2wWDAa7NL/ghAhEop+CTVFRkfpbEr5m\n"
                    + "wH0exhU/+xJURlY1RPfP03iG94is/9KjQDy2gAgPSc06oLmj3IF1BMo30hfk16HzTU9NfXzf+384\n"
                    + "LYkQUA++jojFe0yl8jZpPGukFYogFfnGAWzmUkfucnwIqRYh0ajqGGvHwh+WkAq6r5hcuFYVa7LI\n"
                    + "/pHxV9FdZG/dmJlVeuCwPHZEDhDavgATYGMbHJhb+/EXvcHdh/TKLUgE5D135WpPyFI7vrNhZj7W\n"
                    + "vn6pbOypPi0ghYbpkDxyRyemOfGAz9ecPC0kgKrcNHwLatJ7J53fH0IK0BuAqm9zZsZZm0hJm2L3\n"
                    + "dKbLlqi0yb7hoqmbsRlF9KwPaSeciffVudpbdIOiQuIbs6cUVTaUyRyuUUWxN5arlKf9dQMAAAAC\n"
                    + "AAdpc3N1aW5nAAABZea/OkYABVguNTA5AAAGFzCCBhMwggP7oAMCAQICBFnE/UcwDQYJKoZIhvcN\n"
                    + "AQELBQAwUDELMAkGA1UEBhMCR0IxFDASBgNVBAoTC09wZW5CYW5raW5nMSswKQYDVQQDEyJPcGVu\n"
                    + "QmFua2luZyBQcmUtUHJvZHVjdGlvbiBSb290IENBMB4XDTE3MDkyMjEyNDY1N1oXDTI3MDkyMjEz\n"
                    + "MTY1N1owUzELMAkGA1UEBhMCR0IxFDASBgNVBAoTC09wZW5CYW5raW5nMS4wLAYDVQQDEyVPcGVu\n"
                    + "QmFua2luZyBQcmUtUHJvZHVjdGlvbiBJc3N1aW5nIENBMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8A\n"
                    + "MIIBCgKCAQEAssq0YNoxdNY14V9yEeOKd9/GQaM5/FpGWgjfDAfgJQX7lxw+pgv9L/jf2eFyOhOa\n"
                    + "cwK5k4aPA+muiQ6QB5pnn6hkqjl1W3JWL9bRxY5gtRbSqpBR00zMoZeNZxDKNHZ71gOOWzLBptMj\n"
                    + "Iv7RDOVW+XUmOGa/9i1oFEwxVIrIFG8iCB0/i+qsT1DwqTbhHfgFlXPStEpZoWgvlCH1TvN4BNfv\n"
                    + "Wq9nTjUhWiv2uOOVLOjJ6oCr35LynWkdOjpPxIRoIgGTkXMmetRGBVVcyXsMuTaxUhd5sIj2LzMa\n"
                    + "E+W8XMABL8agV6Uw7/J40H0fnIGQ3paHitVKplvw6Ufo49yjRwIDAQABo4IB8DCCAewwDgYDVR0P\n"
                    + "AQH/BAQDAgEGMBIGA1UdEwEB/wQIMAYBAf8CAQAwgeAGA1UdIASB2DCB1TCB0gYLKwYBBAGodYEG\n"
                    + "AWQwgcIwKgYIKwYBBQUHAgEWHmh0dHA6Ly9vYi50cnVzdGlzLmNvbS9wb2xpY2llczCBkwYIKwYB\n"
                    + "BQUHAgIwgYYMgYNVc2Ugb2YgdGhpcyBDZXJ0aWZpY2F0ZSBjb25zdGl0dXRlcyBhY2NlcHRhbmNl\n"
                    + "IG9mIHRoZSBPcGVuQmFua2luZyBSb290IENBIENlcnRpZmljYXRpb24gUG9saWNpZXMgYW5kIENl\n"
                    + "cnRpZmljYXRlIFByYWN0aWNlIFN0YXRlbWVudDBqBggrBgEFBQcBAQReMFwwMgYIKwYBBQUHMAKG\n"
                    + "Jmh0dHA6Ly9vYi50cnVzdGlzLmNvbS9vYnRlc3Ryb290Y2EuY3J0MCYGCCsGAQUFBzABhhpodHRw\n"
                    + "Oi8vb2IudHJ1c3Rpcy5jb20vb2NzcDA3BgNVHR8EMDAuMCygKqAohiZodHRwOi8vb2IudHJ1c3Rp\n"
                    + "cy5jb20vb2JfcHBfcm9vdGNhLmNybDAfBgNVHSMEGDAWgBTsOI4L2vP5Nz6Q3n1fauZgzXlCgzAd\n"
                    + "BgNVHQ4EFgQUUHORxiFy03f0/gASBoFceXluP1AwDQYJKoZIhvcNAQELBQADggIBAHUYNh/bi8M5\n"
                    + "RqrxxoyM9MnTNbRJboxHFiaeTHoqK4pCtKzDpcVVOmRQZVz3ztQD7tHGV8Xx8FrPdc6DQqRl3mwu\n"
                    + "rYgxUN/piOKssLYqaYwam+kn7d+zZZFw80BE6JrGO8BE7lCd9cRQNBXHCPFpQFM1DgRPDgBqOZAG\n"
                    + "eBU2v55HfHNsJrsEqfvf7PoEHtCvQZMcsKD9VgBHVAGilt0RPcrk2xwP2Rf88+P0acPxQf4WITsb\n"
                    + "w1eqiJ5eQexK0h+K+L+A9o4hOmqQHJ+S0/ntGgWvxIBgMYMgIK2KcNbPkwlCaLfttu8Ub18Xnmc1\n"
                    + "2tooLHfgvOOQRokpbYc69JqmA2BFiTHwKWfgZTLcWuyvU9w9pelzL9dkaDZxFnuh+cWs07wS4k4m\n"
                    + "1t3OYo/FliiTbUG4JqKET2iiSJftK5ueDBR9BQ8Sfj+JgEeqmvCUvddZ0tuogrBPRUFSudXY7nLd\n"
                    + "+PRWATlTOfefxpJE8/ZNb18NQXQ+9ZTYyNNGI3+9wALby/1+AqUc/djWoLIUtiGuWt6o4xa48e8+\n"
                    + "Ie0LTCRo7lLI0yP3PsoyGMAGeYHpReYM+5dnZ94OgeNKbu8tRHsJXyEIfx1TBdGaRdvVy+vu7ImE\n"
                    + "AhoT+XDSdOH6Lc6gz2k9l5ToRN8jZI6fNrswlr4S1CYBNw/VfTy9Fw/8pDRD9BYq9ZVb/zHGVb6t\n"
                    + "pl0gsn8BCoO0Djg=");

    @Test(expected = HttpResponseException.class)
    public void testTrustedRootCa() {
        TinkHttpClient httpClient = new TinkHttpClient();
        httpClient.trustRootCaCertificate(JKS_DATA, JKS_PASSWORD);
        httpClient.request("https://modelobank2018.o3bank.co.uk:4201/token").get(String.class);
    }

    @Test(expected = HttpClientException.class)
    public void testNonTrustedRootCa() {
        TinkHttpClient httpClient = new TinkHttpClient();
        httpClient.request("https://modelobank2018.o3bank.co.uk:4201/token").get(String.class);
    }
}

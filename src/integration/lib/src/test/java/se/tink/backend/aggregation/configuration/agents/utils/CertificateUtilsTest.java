package se.tink.backend.aggregation.configuration.agents.utils;

import static se.tink.backend.aggregation.configuration.agents.utils.CertificateBusinessScope.AIS;

import java.io.IOException;
import java.security.cert.CertificateException;
import java.util.Collections;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

public class CertificateUtilsTest {

    private static final String TEST_CERT =
            "LS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0tCk1JSUdLakNDQlJLZ0F3SUJBZ0lFV2YrZlREQU5CZ2txaGtpRzl"
                    + "3MEJBUXNGQURCRU1Rc3dDUVlEVlFRR0V3SkgKUWpFVU1CSUdBMVVFQ2hNTFQzQmxia0poYm10cGJtY3"
                    + "hIekFkQmdOVkJBTVRGazl3Wlc1Q1lXNXJhVzVuSUVsegpjM1ZwYm1jZ1EwRXdIaGNOTWpBd016STFNV"
                    + "FF5TkRFeFdoY05Nakl3TXpJMU1UUTFOREV4V2pCNk1Rc3dDUVlEClZRUUdFd0pIUWpFdk1DMEdBMVVF"
                    + "Q2hNbVVHRjVVR0ZzSUNoRmRYSnZjR1VwSUZNdVlTQnlMbXd1SUdWMElFTnAKWlNCVExrTXVRUzR4SFR"
                    + "BYkJnTlZCR0VURkZCVFJFeFZMVU5UVTBZdFFqQXdNREF3TXpVeE1Sc3dHUVlEVlFRRApFeEl3TURFMU"
                    + "9EQXdNREF4TUROVlFXdEJRVTB3Z2dFaU1BMEdDU3FHU0liM0RRRUJBUVVBQTRJQkR3QXdnZ0VLCkFvS"
                    + "UJBUUNxREkrSjdsZjVaL0srZ2t6YXQweHJYdm5NRHVXRDlqYUVzdGNSUk9CTUtRVFVhUTIwRTFGQm5l"
                    + "UGYKWllnWHFVRHpIamFNamRFZmR3YUxjL2F2bGpCc1lVT094ZFVmR3QySkRuc20zVkJRa0FBTnhONlY"
                    + "zd3ErZEJXbwp0VDJnNkxaeGQwS0FVRGFlQTRRRTBsa1J5dW10Yk8vYnpMOVJCakFVK3BPVG1adFc2eG"
                    + "hrOXpWaVJici9PZjJsClVVdGZCVkFtdnY3dnozQm9yYkdSRDV5aUhYOG0rd1c5cm5DUjZ2SXhiMGFic"
                    + "VJwQ2k1TFcwc25Ba3pPY0M5ODMKOE1CWGRKNnBlOUZadjEyK1NpRTR0QnppTzNqVENoMTFtZFZKQ0k4"
                    + "RVM0U1R6Sm5VZVBHM0J6VHk3b3hXQldpUQpzaExYYzFlTk1rUE9HWnY2eElCd081c2NjQUVSQWdNQkF"
                    + "BR2pnZ0xzTUlJQzZEQU9CZ05WSFE4QkFmOEVCQU1DCkI0QXdhUVlJS3dZQkJRVUhBUU1FWFRCYk1CTU"
                    + "dCZ1FBamtZQkJqQUpCZ2NFQUk1R0FRWURNRVFHQmdRQWdaZ24KQWpBNk1CTXdFUVlIQkFDQm1DY0JBd"
                    + "3dHVUZOUVgwRkpEQnRHYVc1aGJtTnBZV3dnUTI5dVpIVmpkQ0JCZFhSbwpiM0pwZEhrTUJrZENMVVpE"
                    + "UVRBZ0JnTlZIU1VCQWY4RUZqQVVCZ2dyQmdFRkJRY0RBUVlJS3dZQkJRVUhBd0l3CmdnRlNCZ05WSFN"
                    + "BRWdnRkpNSUlCUlRDQ0FVRUdDeXNHQVFRQnFIV0JCZ0VCTUlJQk1EQTFCZ2dyQmdFRkJRY0MKQVJZcG"
                    + "FIUjBjRG92TDI5aUxuUnlkWE4wYVhNdVkyOXRMM0J5YjJSMVkzUnBiMjR2Y0c5c2FXTnBaWE13Z2ZZR"
                    + "wpDQ3NHQVFVRkJ3SUNNSUhwRElIbVZHaHBjeUJEWlhKMGFXWnBZMkYwWlNCcGN5QnpiMnhsYkhrZ1pt"
                    + "OXlJSFZ6ClpTQjNhWFJvSUU5d1pXNGdRbUZ1YTJsdVp5Qk1hVzFwZEdWa0lHRnVaQ0JoYzNOdlkybGh"
                    + "kR1ZrSUU5d1pXNGcKUW1GdWEybHVaeUJUWlhKMmFXTmxjeTRnU1hSeklISmxZMlZwY0hRc0lIQnZjM0"
                    + "5sYzNOcGIyNGdiM0lnZFhObApJR052Ym5OMGFYUjFkR1Z6SUdGalkyVndkR0Z1WTJVZ2IyWWdkR2hsS"
                    + "UU5d1pXNGdRbUZ1YTJsdVp5Qk1hVzFwCmRHVmtJRU5sY25ScFptbGpZWFJsSUZCdmJHbGplU0JoYm1R"
                    + "Z2NtVnNZWFJsWkNCa2IyTjFiV1Z1ZEhNZ2RHaGwKY21WcGJpNHdjZ1lJS3dZQkJRVUhBUUVFWmpCa01"
                    + "DWUdDQ3NHQVFVRkJ6QUJoaHBvZEhSd09pOHZiMkl1ZEhKMQpjM1JwY3k1amIyMHZiMk56Y0RBNkJnZ3"
                    + "JCZ0VGQlFjd0FvWXVhSFIwY0RvdkwyOWlMblJ5ZFhOMGFYTXVZMjl0CkwzQnliMlIxWTNScGIyNHZhW"
                    + "E56ZFdsdVoyTmhMbU55ZERBL0JnTlZIUjhFT0RBMk1EU2dNcUF3aGk1b2RIUncKT2k4dmIySXVkSEox"
                    + "YzNScGN5NWpiMjB2Y0hKdlpIVmpkR2x2Ymk5cGMzTjFhVzVuWTJFdVkzSnNNQjhHQTFVZApJd1FZTUJ"
                    + "hQUZKOUp2MDQycDZ6RER5dklSL1FmS1J2QWVRc0ZNQjBHQTFVZERnUVdCQlJPOHR4S05adFFkUytJCk"
                    + "xEdnB3TmhVcTRiYW5qQU5CZ2txaGtpRzl3MEJBUXNGQUFPQ0FRRUFyR0lkaWt3SkhEbEMvTDFxcHJFd"
                    + "Dc0M3YKUTdndzMyMUxzSDE0eUt2K0F0Z2xlUzR3ZWcxOUNvU2dDOEY5dUtDNlR3UTVINWoyV1RoRGFo"
                    + "T2RhbHh2V3l1eQpjNDYrVklnTnBwNTB0QU84SnUwMzVYLzZVblZUZWNFTFhweUM0VlgvN0RkK2MxNTV"
                    + "Rakd2U2NRMmNuMDBTc2lUCnlPSmNHZTVYVEMzR1NuRHZlNGZWUWU2bFFQZmQxMzg1UnF5Q1ptMlN2bS"
                    + "tiaXYxRDlVWTN3WG83aTExTW5ubXIKUVU3Rzl4TWwxcGFuVWhQODVwQjJvSjg2RytSUDNBWC9QZ2doU"
                    + "Wp3R09pYnVKV3JkYWZqTlhGOWpBSGJha3c2WQpvTTg2WjlYUk9xVmF0bE82UG50Vnp2cTQxNFI5MWpN"
                    + "Q2hvTlQyZXVQVCs0QnFmbVVEcnJzZEovS1c5VENMUT09Ci0tLS0tRU5EIENFUlRJRklDQVRFLS0tLS0=";

    @Test
    public void testGetPublicKey() throws CertificateException {
        final String EXPECTED_PUBLIC_KEY_PEM =
                "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAq"
                        + "gyPie5X+WfyvoJM2rdMa175zA7lg/Y2hLLXEUTgTCkE1GkNtBNRQZ3j32WIF6lA8x42jI3RH3cGi3P2r5YwbGFDjsXV"
                        + "HxrdiQ57Jt1QUJAADcTeld8KvnQVqLU9oOi2cXdCgFA2ngOEBNJZEcrprWzv28y/UQYwFPqTk5mbVusYZPc1YkW6/zn"
                        + "9pVFLXwVQJr7+789waK2xkQ+coh1/JvsFva5wkeryMW9Gm6kaQouS1tLJwJMznAvfN/DAV3SeqXvRWb9dvkohOLQc4j"
                        + "t40woddZnVSQiPBEuEk8yZ1Hjxtwc08u6MVgVokLIS13NXjTJDzhmb+sSAcDubHHABEQIDAQAB";
        Assert.assertEquals(EXPECTED_PUBLIC_KEY_PEM, CertificateUtils.getPublicKeyPem(TEST_CERT));
    }

    @Test
    public void testGetScopes() throws IOException {
        List<CertificateBusinessScope> businessScopeFromFromCertificate =
                CertificateUtils.getBusinessScopeFromCertificate(TEST_CERT);
        Assert.assertEquals(Collections.singletonList(AIS), businessScopeFromFromCertificate);
    }

    @Test
    public void testGetDerBase64() throws CertificateException {
        final String expectedResult =
                "MIIGKjCCBRKgAwIBAgIEWf+fTDANBgkqhkiG9w0BAQsFADBEMQswCQYDVQQGEwJHQj"
                        + "EUMBIGA1UEChMLT3BlbkJhbmtpbmcxHzAdBgNVBAMTFk9wZW5CYW5raW5nIElzc3VpbmcgQ0EwHhcNMjAwMzI1MT"
                        + "QyNDExWhcNMjIwMzI1MTQ1NDExWjB6MQswCQYDVQQGEwJHQjEvMC0GA1UEChMmUGF5UGFsIChFdXJvcGUpIFMuYS"
                        + "ByLmwuIGV0IENpZSBTLkMuQS4xHTAbBgNVBGETFFBTRExVLUNTU0YtQjAwMDAwMzUxMRswGQYDVQQDExIwMDE1OD"
                        + "AwMDAxMDNVQWtBQU0wggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQCqDI+J7lf5Z/K+gkzat0xrXvnMDu"
                        + "WD9jaEstcRROBMKQTUaQ20E1FBnePfZYgXqUDzHjaMjdEfdwaLc/avljBsYUOOxdUfGt2JDnsm3VBQkAANxN6V3w"
                        + "q+dBWotT2g6LZxd0KAUDaeA4QE0lkRyumtbO/bzL9RBjAU+pOTmZtW6xhk9zViRbr/Of2lUUtfBVAmvv7vz3Borb"
                        + "GRD5yiHX8m+wW9rnCR6vIxb0abqRpCi5LW0snAkzOcC9838MBXdJ6pe9FZv12+SiE4tBziO3jTCh11mdVJCI8ES4"
                        + "STzJnUePG3BzTy7oxWBWiQshLXc1eNMkPOGZv6xIBwO5sccAERAgMBAAGjggLsMIIC6DAOBgNVHQ8BAf8EBAMCB4"
                        + "AwaQYIKwYBBQUHAQMEXTBbMBMGBgQAjkYBBjAJBgcEAI5GAQYDMEQGBgQAgZgnAjA6MBMwEQYHBACBmCcBAwwGUF"
                        + "NQX0FJDBtGaW5hbmNpYWwgQ29uZHVjdCBBdXRob3JpdHkMBkdCLUZDQTAgBgNVHSUBAf8EFjAUBggrBgEFBQcDAQ"
                        + "YIKwYBBQUHAwIwggFSBgNVHSAEggFJMIIBRTCCAUEGCysGAQQBqHWBBgEBMIIBMDA1BggrBgEFBQcCARYpaHR0cD"
                        + "ovL29iLnRydXN0aXMuY29tL3Byb2R1Y3Rpb24vcG9saWNpZXMwgfYGCCsGAQUFBwICMIHpDIHmVGhpcyBDZXJ0aW"
                        + "ZpY2F0ZSBpcyBzb2xlbHkgZm9yIHVzZSB3aXRoIE9wZW4gQmFua2luZyBMaW1pdGVkIGFuZCBhc3NvY2lhdGVkIE"
                        + "9wZW4gQmFua2luZyBTZXJ2aWNlcy4gSXRzIHJlY2VpcHQsIHBvc3Nlc3Npb24gb3IgdXNlIGNvbnN0aXR1dGVzIG"
                        + "FjY2VwdGFuY2Ugb2YgdGhlIE9wZW4gQmFua2luZyBMaW1pdGVkIENlcnRpZmljYXRlIFBvbGljeSBhbmQgcmVsYX"
                        + "RlZCBkb2N1bWVudHMgdGhlcmVpbi4wcgYIKwYBBQUHAQEEZjBkMCYGCCsGAQUFBzABhhpodHRwOi8vb2IudHJ1c3"
                        + "Rpcy5jb20vb2NzcDA6BggrBgEFBQcwAoYuaHR0cDovL29iLnRydXN0aXMuY29tL3Byb2R1Y3Rpb24vaXNzdWluZ2"
                        + "NhLmNydDA/BgNVHR8EODA2MDSgMqAwhi5odHRwOi8vb2IudHJ1c3Rpcy5jb20vcHJvZHVjdGlvbi9pc3N1aW5nY2"
                        + "EuY3JsMB8GA1UdIwQYMBaAFJ9Jv042p6zDDyvIR/QfKRvAeQsFMB0GA1UdDgQWBBRO8txKNZtQdS+ILDvpwNhUq4"
                        + "banjANBgkqhkiG9w0BAQsFAAOCAQEArGIdikwJHDlC/L1qprEt743vQ7gw321LsH14yKv+AtgleS4weg19CoSgC8"
                        + "F9uKC6TwQ5H5j2WThDahOdalxvWyuyc46+VIgNpp50tAO8Ju035X/6UnVTecELXpyC4VX/7Dd+c155QjGvScQ2cn"
                        + "00SsiTyOJcGe5XTC3GSnDve4fVQe6lQPfd1385RqyCZm2Svm+biv1D9UY3wXo7i11MnnmrQU7G9xMl1panUhP85p"
                        + "B2oJ86G+RP3AX/PgghQjwGOibuJWrdafjNXF9jAHbakw6YoM86Z9XROqVatlO6PntVzvq414R91jMChoNT2euPT+"
                        + "4BqfmUDrrsdJ/KW9TCLQ==";
        String base64EncodedDer =
                CertificateUtils.getDerEncodedCertFromBase64EncodedCertificate(TEST_CERT);
        Assert.assertEquals(expectedResult, base64EncodedDer);
    }

    @Test
    public void testGetSN() throws CertificateException {
        final String expectedSn = "1509924684";
        final String expectedSnHex = "59ff9f4c";
        String sn = CertificateUtils.getSerialNumber(TEST_CERT, 10);
        Assert.assertEquals(expectedSn, sn);
        String snHex = CertificateUtils.getSerialNumber(TEST_CERT, 16);
        Assert.assertEquals(expectedSnHex, snHex);
    }

    @Test
    public void testGetOrganizationIdentifier() throws CertificateException {
        final String expectedOrgId = "PSDLU-CSSF-B00000351";

        String organizationId = CertificateUtils.getOrganizationIdentifier(TEST_CERT);
        Assert.assertEquals(expectedOrgId, organizationId);
    }

    @Test
    public void testGetOrganizationName() throws CertificateException {
        final String expectedOrgName = "PayPal (Europe) S.a r.l. et Cie S.C.A.";

        String organizationName = CertificateUtils.getOrganizationName(TEST_CERT);
        Assert.assertEquals(expectedOrgName, organizationName);
    }
}

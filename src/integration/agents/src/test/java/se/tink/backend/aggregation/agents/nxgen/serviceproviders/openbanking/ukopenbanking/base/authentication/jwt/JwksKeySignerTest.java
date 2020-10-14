package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.authentication.jwt;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jwt.SignedJWT;
import java.text.ParseException;
import java.util.Collections;
import java.util.Map;
import java.util.NoSuchElementException;
import org.hamcrest.core.Is;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.authenticator.jwt.JwksClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.authenticator.jwt.JwksKeySigner;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.jwt.signer.iface.JwtSigner.Algorithm;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@RunWith(MockitoJUnitRunner.class)
public class JwksKeySignerTest {

    private static final URL JWKS_ENDPOINT = new URL("https://localhost:8888/keys.jwks");
    private static final Map<String, Object> HEADER_CLAIMS = Collections.emptyMap();
    private static final Map<String, Object> PAYLOAD_CLAIMS = Collections.emptyMap();

    private static final String JWKS =
            "{\"keys\":[{\n"
                    + "\"kid\" : \"HS96N1e5wBVh172U2vKxsVRreN4\",\n"
                    + "\"kty\" : \"RSA\",\n"
                    + "\"n\" : \"vwQeR92WXLYYG2Za-OaQn8TEZ5X8UXpl19N_8t_9-tXdWSXWG4GNjFsmVVMvd8y__ho6sYLBQEhf2QcdA5U-8ZmtrSenHXvXXGJaVJnVgGk2vCbM6cOajqMcrK_VBcGmICVwrG2ArcWIvUbBqvlVWlImAy4Zx7dYYpQ26j_Ls0j7j2PVgAR0DjE6zgGAmAX_qkRYx7JQB0f_aU4lZA9VhHhaIxnD_BGn5kkjUURtboM__ONs5GZZXPq3IT6o2XPPk064G312LumC-nW2WDkbiRqviTg3wnw37f1HsU9-zj_r3c6qCWvegw8B4CIKbz8Rv2aaZXRND-vsUiatawIW9Q\",\n"
                    + "\"e\" : \"AQAB\",\n"
                    + "\"use\" : \"tls\",\n"
                    + "\"x5c\" : [ \"MIIFpjCCBI6gAwIBAgIEWf9BZjANBgkqhkiG9w0BAQsFADBEMQswCQYDVQQGEwJHQjEUMBIGA1UEChMLT3BlbkJhbmtpbmcxHzAdBgNVBAMTFk9wZW5CYW5raW5nIElzc3VpbmcgQ0EwHhcNMTgxMDE3MTYzMTU4WhcNMjAxMDE3MTcwMTU4WjBhMQswCQYDVQQGEwJHQjEUMBIGA1UEChMLT3BlbkJhbmtpbmcxGzAZBgNVBAsTEjAwMTU4MDAwMDE2aTQ0SUFBUTEfMB0GA1UEAxMWMWhpN2x2QVo2bFZyS1JDaU9Jam5YSjCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAL8EHkfdlly2GBtmWvjmkJ/ExGeV/FF6ZdfTf/Lf/frV3Vkl1huBjYxbJlVTL3fMv/4aOrGCwUBIX9kHHQOVPvGZra0npx1711xiWlSZ1YBpNrwmzOnDmo6jHKyv1QXBpiAlcKxtgK3FiL1Gwar5VVpSJgMuGce3WGKUNuo/y7NI+49j1YAEdA4xOs4BgJgF/6pEWMeyUAdH/2lOJWQPVYR4WiMZw/wRp+ZJI1FEbW6DP/zjbORmWVz6tyE+qNlzz5NOuBt9di7pgvp1tlg5G4kar4k4N8J8N+39R7FPfs4/693Oqglr3oMPAeAiCm8/Eb9mmmV0TQ/r7FImrWsCFvUCAwEAAaOCAoEwggJ9MA4GA1UdDwEB/wQEAwIHgDAgBgNVHSUBAf8EFjAUBggrBgEFBQcDAQYIKwYBBQUHAwIwggFSBgNVHSAEggFJMIIBRTCCAUEGCysGAQQBqHWBBgEBMIIBMDA1BggrBgEFBQcCARYpaHR0cDovL29iLnRydXN0aXMuY29tL3Byb2R1Y3Rpb24vcG9saWNpZXMwgfYGCCsGAQUFBwICMIHpDIHmVGhpcyBDZXJ0aWZpY2F0ZSBpcyBzb2xlbHkgZm9yIHVzZSB3aXRoIE9wZW4gQmFua2luZyBMaW1pdGVkIGFuZCBhc3NvY2lhdGVkIE9wZW4gQmFua2luZyBTZXJ2aWNlcy4gSXRzIHJlY2VpcHQsIHBvc3Nlc3Npb24gb3IgdXNlIGNvbnN0aXR1dGVzIGFjY2VwdGFuY2Ugb2YgdGhlIE9wZW4gQmFua2luZyBMaW1pdGVkIENlcnRpZmljYXRlIFBvbGljeSBhbmQgcmVsYXRlZCBkb2N1bWVudHMgdGhlcmVpbi4wcgYIKwYBBQUHAQEEZjBkMCYGCCsGAQUFBzABhhpodHRwOi8vb2IudHJ1c3Rpcy5jb20vb2NzcDA6BggrBgEFBQcwAoYuaHR0cDovL29iLnRydXN0aXMuY29tL3Byb2R1Y3Rpb24vaXNzdWluZ2NhLmNydDA/BgNVHR8EODA2MDSgMqAwhi5odHRwOi8vb2IudHJ1c3Rpcy5jb20vcHJvZHVjdGlvbi9pc3N1aW5nY2EuY3JsMB8GA1UdIwQYMBaAFJ9Jv042p6zDDyvIR/QfKRvAeQsFMB0GA1UdDgQWBBSDarmjHwnS9N6U+txreh4a0q8I0zANBgkqhkiG9w0BAQsFAAOCAQEAsh/kC1xYzga2JXDvwVvsN44gvAbrkCfcU8LEbYxVyTz6EbITyQkw2UXvRye1cewPImjQekiTawW+ojliIAP33tRfv3keqQaDnkeDTSChwoRbDbTHgQFk/3ZKLZypm/tOuIXBeDbMeQ5vpLzflOTUUafZgZu1Zca2OQxI/RHD34ggSZNUfe24ZJKy9FF/kwTWYB4AIDCSAa1xO3KeZtOdhpt3HviqrBW4q76hZSUagtHuufA9oOuTdx0VbGCg+K0CwGk0+cEDtD+lblCecZ+75ClHqe6N3V5c2ZQUh/OmTggkPHiur1dDQ+jqCuegWeiXNrZnCAwblKydropJVUcQDA==\" ],\n"
                    + "\"x5t\" : \"NSvK-T7DNYbTJJWoiW6_NjJkf6s=\",\n"
                    + "\"x5u\" : \"https://keystore.openbanking.org.uk/00158000016i44IAAQ/HS96N1e5wBVh172U2vKxsVRreN4.pem\",\n"
                    + "\"x5t#S256\" : \"IHJd7bJ4yKxw8E4gooI4sjGUybe_QKTsJphVHMNuKoI=\"\n"
                    + "},{\""
                    + "kty\":\"RSA\","
                    + "\"x5t#S256\":\"H-NG4-cxEFOZwB2bJCP3iJ1e82uxVhwwBx92U9PRrd0\","
                    + "\"e\":\"AQAB\","
                    + "\"kid\":\"319249827807263620565033445582138561170152977440\","
                    + "\"x5c\":[\"MIIDazCCAlOgAwIBAgIUN+uoSriXyOHRNL8g69vcn8EZdCAwDQYJKoZIhvcNAQELBQAwRTELMAkGA1UEBhMCQVUxEzARBgNVBAgMClNvbWUtU3RhdGUxITAfBgNVBAoMGEludGVybmV0IFdpZGdpdHMgUHR5IEx0ZDAeFw0yMDEwMDcxNDE3MDVaFw0yMDExMDYxNDE3MDVaMEUxCzAJBgNVBAYTAkFVMRMwEQYDVQQIDApTb21lLVN0YXRlMSEwHwYDVQQKDBhJbnRlcm5ldCBXaWRnaXRzIFB0eSBMdGQwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQDt1CsZxr5n32SYvvFjxAnQ9q61hheh8bhqgP0xqZqJvseL1PCmBPI1soyIrSPUwr0EAEOoVK12eUfxrfGVCCeipe5wroU61frKQ0By612t2Jt6l/Cj9G7djhUm/5ig/NNoJZKUt+XmlywlSKi5csYBCZzh5WWxN8f64EKUVt7o6ra8TxrWS2jywGgZ/PCYmjT2yFcC4iFwSlyskaslPSp62L/ks8GQxxs/PNcXqWYXcgf4Hl9i/d+Ak0dtefeagPhhh69b66st5QRYX0abfrF6WXp7YKNM4qMP5J86tzhOUTjEFePfmZPaIW9WEJPxbMR8I0tNrNrVd1l887RVPvR3AgMBAAGjUzBRMB0GA1UdDgQWBBRykE1SRhTcL+tsjeExT9XH8o8nbTAfBgNVHSMEGDAWgBRykE1SRhTcL+tsjeExT9XH8o8nbTAPBgNVHRMBAf8EBTADAQH/MA0GCSqGSIb3DQEBCwUAA4IBAQCM4THceSviWc/RJm4mz/+/6DREVAAuQ7Z4DSd8e0jJnbt3/yDFO17wjdxv+FRvChyXSZtKFY1Wzy63gWQfhMiX0b8EoP0uxee9JdPdhERV/+TlzjmiGrslfQXyHrVdUM1kgoqyA2fycXfNIhhqZ6+YYgW+grTAcR1WorB0YmcAJ/MEeKK3w2LgpPUp/IQKDW2rBy9HMPtBot9SFM+EfVhnZZmHe67mQdDtGGziukp2D33/citmzCoG1FcnlUwH8znOLRo5sOvqAwUdCZwONv6v7AtY5fE/APp/SFnCZS6d0Oj0mGC42Irxop6v0VmkGvDOB6l3wfaoPfOyBojj5jFI\"],"
                    + "\"n\":\"7dQrGca-Z99kmL7xY8QJ0PautYYXofG4aoD9Mamaib7Hi9TwpgTyNbKMiK0j1MK9BABDqFStdnlH8a3xlQgnoqXucK6FOtX6ykNAcutdrdibepfwo_Ru3Y4VJv-YoPzTaCWSlLfl5pcsJUiouXLGAQmc4eVlsTfH-uBClFbe6Oq2vE8a1kto8sBoGfzwmJo09shXAuIhcEpcrJGrJT0qeti_5LPBkMcbPzzXF6lmF3IH-B5fYv3fgJNHbXn3moD4YYevW-urLeUEWF9Gm36xell6e2CjTOKjD-SfOrc4TlE4xBXj35mT2iFvVhCT8WzEfCNLTaza1XdZfPO0VT70dw\""
                    + "}]"
                    + "}";

    @Mock private JwksClient jwksClient;

    private JwksKeySigner jwksKeySigner;

    @Before
    public void setUp() throws Exception {
        String privateKey =
                "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQDt1CsZxr5n32SY\n"
                        + "vvFjxAnQ9q61hheh8bhqgP0xqZqJvseL1PCmBPI1soyIrSPUwr0EAEOoVK12eUfx\n"
                        + "rfGVCCeipe5wroU61frKQ0By612t2Jt6l/Cj9G7djhUm/5ig/NNoJZKUt+Xmlywl\n"
                        + "SKi5csYBCZzh5WWxN8f64EKUVt7o6ra8TxrWS2jywGgZ/PCYmjT2yFcC4iFwSlys\n"
                        + "kaslPSp62L/ks8GQxxs/PNcXqWYXcgf4Hl9i/d+Ak0dtefeagPhhh69b66st5QRY\n"
                        + "X0abfrF6WXp7YKNM4qMP5J86tzhOUTjEFePfmZPaIW9WEJPxbMR8I0tNrNrVd1l8\n"
                        + "87RVPvR3AgMBAAECggEAIKkKYjfQ3iLnhbJzI+hAENpPk84eYLb5RGcCXleUSWZR\n"
                        + "sPpFMgdr28PxWLOuQAGeQ3Zl1DAMwkBViPMCQh0klEmFPSQ6V6IbFru1lhKMgttE\n"
                        + "Psmu2lUvBG4GqZxgMXuG7L58KsA2ajxIa7ZWtWD1oB7Z1IvUM0odxG4XtpdXdVTC\n"
                        + "hLoQ7DwZvWNUGVQxhqYovE4JNmK//MdDQtGif2uopsJij7wr0VJPsY4Kcypk/i6u\n"
                        + "0HV8bgYZtbaJdSaTom/0WJ08OkfnZw6u+48Lo1x6LKymuz1uMwl62GvWh1rypy7E\n"
                        + "oYI3Nhne4iMsEB8K+GLSc1IbrBZTLmQJp2y9r+d+gQKBgQD/AS9DmNno82i1ZwtW\n"
                        + "0SjBN0Qv1jTeXklrTpc1x5JwbTNVTwXWWjqSh2CxZxD7nGI5HWIlVHdhwMMiLH+N\n"
                        + "HNRgKQf3OfLMfDif0hWUpeBXNqK366o0OLn1x4ah5R2Z1/F1/kh7IHcvXpW1PCJi\n"
                        + "zlEgqPCf1dLuQu0VI6mwxNPkbwKBgQDuwdIVaTf8bZAW6yRDrqYI9FgT7x/EylhS\n"
                        + "s6CmfOT6nDy0Blr8zmG5iF2DLku5p1Sdf9F6Wul8RoTZJTcmpog4Z1SS082WIiPq\n"
                        + "Q3FQCscntuPyWeQhslGydl7t/1CEO5/SsZUutgBYTuLukqyRaM2B+NetHMWxRhqJ\n"
                        + "Mxr9+H3EeQKBgB737tx/uJ8QyHuXSwo9Fnz4kEBa/EqxGqt8fOKcHQ2oRV6r3SKe\n"
                        + "DJT0+eoxgR99a3RSPDQaYPEvLsN8sfTaNnSDJyOAFxt8TogiRFqsFmWhDfvWE/KJ\n"
                        + "nRPbKl+I6qJfVkSlH/pMBA1tqDK9XdOwCKw1uBtBJX+oAxDZJud/z9fjAoGAGRrC\n"
                        + "J0QwAdfXXcGNx5wWkzx2AmqTUZLhJJkEnFznir5g16EPciOVPPfstT1p8vR4PjNR\n"
                        + "aGckw896aoayPNsn2Z5r0yB4LoAafKhowrzPYgql/zaaeTyRwI0XwDinU7AdRv8r\n"
                        + "K4Uxlhd9Xw6HHKcDbNYof2hWFSTE54Y7IVc+iikCgYEA1/6KZNqs97sJOrpX3GRX\n"
                        + "gP98NkirZ9EPrXEJWFUQwIQH/PraeKtZLcYEgd40jjAuL5bNEjsBbuCSzpjX2yXw\n"
                        + "RrmyQllgstvkAXP7GcVVlgjHrWw+VK0PwWvzwf5Xe64Z6+t9tNPzQxQOQEBtelkd\n"
                        + "9REQaFpNB85+0bB9d7OQOvA=";
        jwksKeySigner = new JwksKeySigner(privateKey, JWKS_ENDPOINT, jwksClient);
    }

    @Test
    public void shouldPickSigningKeyIdFromJWKSAndSignJWT() throws ParseException {
        // given
        JWKSet jwkSet = JWKSet.parse(JWKS);
        when(jwksClient.get(JWKS_ENDPOINT)).thenReturn(jwkSet);

        // when
        String result = jwksKeySigner.sign(Algorithm.RS256, HEADER_CLAIMS, PAYLOAD_CLAIMS, true);

        // then
        SignedJWT signedJWT = SignedJWT.parse(result);
        assertThat(
                signedJWT.getHeader().getKeyID(),
                Is.is("319249827807263620565033445582138561170152977440"));
        assertThat(signedJWT.getHeader().getAlgorithm().getName(), Is.is("RS256"));
    }

    @Test
    public void shouldThrowsNoSuchElementExceptionWhenPrivateKeyDoesNotHavePairInJWKS()
            throws ParseException {
        String emptyJwks = "{\"keys\":[]}";
        JWKSet jwkSet = JWKSet.parse(emptyJwks);
        when(jwksClient.get(JWKS_ENDPOINT)).thenReturn(jwkSet);

        assertThatExceptionOfType(NoSuchElementException.class)
                .isThrownBy(
                        () ->
                                jwksKeySigner.sign(
                                        Algorithm.RS256, HEADER_CLAIMS, PAYLOAD_CLAIMS, true))
                .withNoCause();
    }
}

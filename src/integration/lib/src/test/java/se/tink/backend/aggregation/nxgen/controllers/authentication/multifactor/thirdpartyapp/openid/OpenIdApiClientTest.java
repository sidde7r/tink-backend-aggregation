package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.security.PublicKey;
import java.util.Map;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@RunWith(MockitoJUnitRunner.class)
public class OpenIdApiClientTest {

    private static final String WELL_KNOWN_EXAMPLE =
            "{\n"
                    + "  \"version\": 1.0,\n"
                    + "  \"issuer\": \"xxx\",\n"
                    + "  \"authorization_endpoint\": \"xxx\",\n"
                    + "  \"registration_endpoint\": \"xxx\",\n"
                    + "  \"token_endpoint\": \"xxx\",\n"
                    + "  \"jwks_uri\": \"http://jwks\",\n"
                    + "  \"scopes_supported\": [\"accounts\", \"payments\", \"openid\", \"fundsconfirmations\"],\n"
                    + "  \"claims_supported\": [\"sub\",\"iss\",\"auth_time\",\"acr\",\"openbanking_intent_id\"],\n"
                    + "  \"response_types_supported\": [\"code id_token\", \"code\"],\n"
                    + "  \"grant_types_supported\": [\"authorization_code\", \"client_credentials\", \"refresh_token\"],\n"
                    + "  \"subject_types_supported\": [\"pairwise\"],\n"
                    + "  \"id_token_signing_alg_values_supported\": [\"PS256\"],\n"
                    + "  \"request_object_signing_alg_values_supported\": [\"PS256\"],\n"
                    + "  \"token_endpoint_auth_methods_supported\": [\"private_key_jwt\", \"tls_client_auth\"],\n"
                    + "  \"claims_parameter_supported\": true,\n"
                    + "  \"request_parameter_supported\": true,\n"
                    + "  \"request_uri_parameter_supported\": false,\n"
                    + "  \"token_endpoint_auth_signing_alg_values_supported\": [\"PS256\"],\n"
                    + "  \"acr_values_supported\": [\"urn:openbanking:psd2:sca\"],\n"
                    + "  \"tls_client_certificate_bound_access_tokens\": true\n"
                    + "}";

    private static final String JWKS_EXAMPLE =
            "{\n"
                    + "\t\"keys\" : [{\n"
                    + "\t\t\t\"kty\" : \"RSA\",\n"
                    + "\t\t\t\"x5t#S256\" : \"N9ma6qWKxCnw3WlJi8-sjy6-K-0zZllYAKZYWYQBCac\",\n"
                    + "\t\t\t\"e\" : \"AQAB\",\n"
                    + "\t\t\t\"use\" : \"sig\",\n"
                    + "\t\t\t\"kid\" : \"external\",\n"
                    + "\t\t\t\"x5c\" : [\"MIIGXzCCBUegAwIBAgIQC9gB6Oun1vt7she1GImF\\/zANBgkqhkiG9w0BAQsFADBNMQswCQYDVQQGEwJVUzEVMBMGA1UEChMMRGlnaUNlcnQgSW5jMScwJQYDVQQDEx5EaWdpQ2VydCBTSEEyIFNlY3VyZSBTZXJ2ZXIgQ0EwHhcNMjAwMTIwMDAwMDAwWhcNMjAwOTI2MTIwMDAwWjCBlzELMAkGA1UEBhMCR0IxDzANBgNVBAcTBkxvbmRvbjEvMC0GA1UEChMmSFNCQyBHcm91cCBNYW5hZ2VtZW50IFNlcnZpY2VzIExpbWl0ZWQxGTAXBgNVBAsTEEhEUyBPcGVuIEJhbmtpbmcxKzApBgNVBAMTIlBTRDItRXh0ZXJuYWwtU2lnbmF0dXJlLmhzYmMuY28udWswggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQCUZllOaY7S7Ml8WTw6UfqpRRFTnN\\/5wA6UruvE4hpi+0ena0r6\\/NOYUvysGKCHbGGjIlxfbulOk1vG3isEu5Jd7oQq9rLA1Dycr5tsTEmf3d5LAnrxf5UKe7VmvFPqbimomh251gOcN0Gb3hJyKCFsspYMDYD+eYXEumn9L3UL6PzdzN2LCvkuuPfqfMfF8ttheDbzv+I0EV630RPN3nHTVjOXPgvTvK5STKKv1z5qUGA9M83A8NGtY7XgICMoc\\/bd2We5gUtBu3RrvOJsdsE0l4wqju4HqfCyPzAavBFhipfymcxPebhnQ++nhbWbQ6NPqS95NVcOCRAOdzRNZuppAgMBAAGjggLuMIIC6jAfBgNVHSMEGDAWgBQPgGEcgjFh1S8o541GOLQs4cbZ4jAdBgNVHQ4EFgQUoyXF9vfBfUV1641lfcljK+RZVOUwLQYDVR0RBCYwJIIiUFNEMi1FeHRlcm5hbC1TaWduYXR1cmUuaHNiYy5jby51azAOBgNVHQ8BAf8EBAMCBaAwHQYDVR0lBBYwFAYIKwYBBQUHAwEGCCsGAQUFBwMCMGsGA1UdHwRkMGIwL6AtoCuGKWh0dHA6Ly9jcmwzLmRpZ2ljZXJ0LmNvbS9zc2NhLXNoYTItZzYuY3JsMC+gLaArhilodHRwOi8vY3JsNC5kaWdpY2VydC5jb20vc3NjYS1zaGEyLWc2LmNybDBMBgNVHSAERTBDMDcGCWCGSAGG\\/WwBATAqMCgGCCsGAQUFBwIBFhxodHRwczovL3d3dy5kaWdpY2VydC5jb20vQ1BTMAgGBmeBDAECAjB8BggrBgEFBQcBAQRwMG4wJAYIKwYBBQUHMAGGGGh0dHA6Ly9vY3NwLmRpZ2ljZXJ0LmNvbTBGBggrBgEFBQcwAoY6aHR0cDovL2NhY2VydHMuZGlnaWNlcnQuY29tL0RpZ2lDZXJ0U0hBMlNlY3VyZVNlcnZlckNBLmNydDAJBgNVHRMEAjAAMIIBBAYKKwYBBAHWeQIEAgSB9QSB8gDwAHcApLkJkLQYWBSHuxOizGdwCjw1mAT5G9+443fNDsgN3BAAAAFvwwL8uwAABAMASDBGAiEAhvsrEmj6p1XrK9vinMfOHLuC3rKOq5QshRXDakP1Vo8CIQDrdunYFLP0mpJPwz4AcPP9KwCP9lUw6x7uqF7WB1LPYwB1AF6nc\\/nfVsDntTZIfdBJ4DJ6kZoMhKESEoQYdZaBcUVYAAABb8MC\\/GYAAAQDAEYwRAIgZ7Rgk3\\/LeNnJuHymSbB0j8RM86r\\/By8rQ7ZLIrbMELwCIGIXrLCtJqoOXHpUFYyZktms22fdmgtoIYumdSt1fYAfMA0GCSqGSIb3DQEBCwUAA4IBAQAHL1sC8cnIQG+wQpRo3V02\\/ZSRLNbFI1g++Y0pgQfu1xqHaJVDB9ZYF8EeBTD7abt22d6XNjJ6qm5b1CJyeoA9fK\\/b3QycGNP8CF3P+EUxdaVE6pfnQbbare80nO97iA3ZJrztaI6rJQgNhDtN8IA5fvsjxbgivuIt7MKS0tkDfFyquE3WUb7q4Is8G4J1tR2Po9z3JPkukKTe3ulke2DiFMTvZDtZRaRbgtKUBVBaABkhA1eBcqK6DdW\\/xBd54cmyGFrVQT123o9BYvqATPaDwGWZtKbRSlfPbo7qDyzXj\\/IQDvooBqwOHEc6SQ+cTn09KW1Ql34fXSwBfPcF1i\\/G\"],\n"
                    + "\t\t\t\"n\" : \"lGZZTmmO0uzJfFk8OlH6qUURU5zf-cAOlK7rxOIaYvtHp2tK-vzTmFL8rBigh2xhoyJcX27pTpNbxt4rBLuSXe6EKvaywNQ8nK-bbExJn93eSwJ68X-VCnu1ZrxT6m4pqJodudYDnDdBm94ScighbLKWDA2A_nmFxLpp_S91C-j83czdiwr5Lrj36nzHxfLbYXg287_iNBFet9ETzd5x01Yzlz4L07yuUkyir9c-alBgPTPNwPDRrWO14CAjKHP23dlnuYFLQbt0a7zibHbBNJeMKo7uB6nwsj8wGrwRYYqX8pnMT3m4Z0Pvp4W1m0OjT6kveTVXDgkQDnc0TWbqaQ\"\n"
                    + "\t\t}\n"
                    + "\t]\n"
                    + "}";

    private TinkHttpClient httpClient;

    private URL wellKnownUrl;

    private OpenIdApiClient apiClient;

    @Before
    public void setup() {
        httpClient = mock(TinkHttpClient.class);
        wellKnownUrl = new URL("anything");
        apiClient = new OpenIdApiClient(httpClient, null, null, null, null, wellKnownUrl, null);
    }

    @Test
    public void shouldGetJwksPublicKeys() {
        RequestBuilder wellKnownRequestBuilder = mockRequestBuilder(WELL_KNOWN_EXAMPLE);
        when(httpClient.request(eq(wellKnownUrl))).thenReturn(wellKnownRequestBuilder);
        RequestBuilder jwksRequestBuilder = mockRequestBuilder(JWKS_EXAMPLE);
        when(httpClient.request(eq(new URL("http://jwks")))).thenReturn(jwksRequestBuilder);

        Optional<Map<String, PublicKey>> jwkPublicKeys = apiClient.getJwkPublicKeys();
        assertThat(jwkPublicKeys).isPresent();
        assertThat(jwkPublicKeys.get()).hasSize(1);
        assertThat(jwkPublicKeys.get().get("external").getAlgorithm()).isEqualTo("RSA");
        assertThat(jwkPublicKeys.get().get("external").getFormat()).isEqualTo("X.509");
    }

    private RequestBuilder mockRequestBuilder(String contents) {
        RequestBuilder toReturn = mock(RequestBuilder.class);
        when(toReturn.get(eq(String.class))).thenReturn(contents);
        return toReturn;
    }
}

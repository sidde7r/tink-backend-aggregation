package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.rpc.ErrorResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

public class OpenIdConsentValidatorTest {

    private final Map<String, ErrorResponse> knownProvidersIssuesWithContents = new HashMap<>();

    @Before
    public void setUp() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        knownProvidersIssuesWithContents.put(
                "uk-santander-oauth2",
                objectMapper.readValue(
                        "{\"Code\":\"403 Forbidden\",\"Message\":\"Permissions Error\",\"Errors\":[{\"ErrorCode\":\"UK.OBIE.Resource.NotFound\",\"Message\":\"Consent not authorised\"}]}",
                        ErrorResponse.class));
        knownProvidersIssuesWithContents.put(
                "uk-barclays-oauth2",
                objectMapper.readValue(
                        "{\"Code\":\"400 Bad Request\",\"Id\":\"2c7cb790-a388-43c3-9062-4a24fd478ef8\",\"Message\":\"Consent validation failed. \",\"Errors\":[{\"ErrorCode\":\"UK.OBIE.Resource.InvalidConsentStatus\",\"Message\":\"The requested Consent ID doesn't exist or do not have valid status. \"}]}",
                        ErrorResponse.class));
        knownProvidersIssuesWithContents.put("null_as_body", null);
        knownProvidersIssuesWithContents.put(
                "empty_body", objectMapper.readValue("{}", ErrorResponse.class));
    }

    @Test
    public void shouldCheckConsentResponseSantanderWhereConsentIsInvalid() {
        // given
        HttpResponse response = mock(HttpResponse.class);
        given(response.getStatus()).willReturn(403);
        given(response.getBody(ErrorResponse.class))
                .willReturn(knownProvidersIssuesWithContents.get("uk-santander-oauth2"));
        given(response.hasBody()).willReturn(true);

        // when
        boolean result = OpenIdConsentValidator.hasInvalidConsent(response);

        // then
        assertThat(result).isTrue();
    }

    @Test
    public void shouldCheckConsentResponseBarclaysWhereConsentIsInvalid() {
        // given
        HttpResponse response = mock(HttpResponse.class);
        given(response.getStatus()).willReturn(400);
        given(response.getBody(ErrorResponse.class))
                .willReturn(knownProvidersIssuesWithContents.get("uk-barclays-oauth2"));
        given(response.hasBody()).willReturn(true);

        // when
        boolean result = OpenIdConsentValidator.hasInvalidConsent(response);

        // then
        assertThat(result).isTrue();
    }

    @Test
    public void shouldCheckResponseWhichHas400StatusButBodyAsANull() {
        // given
        HttpResponse response = mock(HttpResponse.class);
        given(response.getStatus()).willReturn(400);
        given(response.getBody(ErrorResponse.class))
                .willReturn(knownProvidersIssuesWithContents.get("null_as_body"));
        given(response.hasBody()).willReturn(false);

        // when
        boolean result = OpenIdConsentValidator.hasInvalidConsent(response);

        // then
        assertThat(result).isFalse();
    }

    @Test
    public void shouldCheckResponseWhichHas200StatusButBodyAsANull() {
        // given
        HttpResponse response = mock(HttpResponse.class);
        given(response.getStatus()).willReturn(200);
        given(response.getBody(ErrorResponse.class))
                .willReturn(knownProvidersIssuesWithContents.get("null_as_body"));
        given(response.hasBody()).willReturn(false);

        // when
        boolean result = OpenIdConsentValidator.hasInvalidConsent(response);

        // then
        assertThat(result).isFalse();
    }

    @Test
    public void shouldCheckResponseWhereConsentShouldBeValid() {
        // given
        HttpResponse response = mock(HttpResponse.class);
        given(response.getStatus()).willReturn(200);
        given(response.getBody(ErrorResponse.class))
                .willReturn(knownProvidersIssuesWithContents.get("empty_body"));
        given(response.hasBody()).willReturn(true);

        // when
        boolean result = OpenIdConsentValidator.hasInvalidConsent(response);

        // then
        assertThat(result).isFalse();
    }

    @Test
    public void shouldCheckErrorResponseWhichHasEmptyErrorMessage() {
        // given
        HttpResponse response = mock(HttpResponse.class);
        given(response.getStatus()).willReturn(403);
        given(response.getBody(ErrorResponse.class))
                .willReturn(knownProvidersIssuesWithContents.get("empty_body"));
        given(response.hasBody()).willReturn(true);

        // when
        boolean result = OpenIdConsentValidator.hasInvalidConsent(response);

        // then
        assertThat(result).isFalse();
    }
}

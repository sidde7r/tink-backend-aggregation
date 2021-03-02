package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.rpc.ErrorResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

public class OpenIdConsentValidatorTest {

    private static final String DIFFERENT_JSON_MESSAGE = "{}";
    private final Map<String, String> knownProvidersIssuesWithContents = new HashMap<>();

    private ObjectMapper objectMapper;

    @Before
    public void setUp() throws Exception {
        this.objectMapper = new ObjectMapper();
        knownProvidersIssuesWithContents.put(
                "uk-santander-oauth2",
                "{\"Code\":\"403 Forbidden\",\"Message\":\"Permissions Error\",\"Errors\":[{\"ErrorCode\":\"UK.OBIE.Resource.NotFound\",\"Message\":\"Consent not authorised\"}]}");
        knownProvidersIssuesWithContents.put(
                "uk-barclays-oauth2",
                "{\"Code\":\"400 Bad Request\",\"Id\":\"2c7cb790-a388-43c3-9062-4a24fd478ef8\",\"Message\":\"Consent validation failed. \",\"Errors\":[{\"ErrorCode\":\"UK.OBIE.Resource.InvalidConsentStatus\",\"Message\":\"The requested Consent ID doesn't exist or do not have valid status. \"}]}");
    }

    @Test
    public void shouldCheckConsentResponseSantanderWhereConsentIsInvalid() throws IOException {
        // given
        HttpResponse response = mock(HttpResponse.class);
        given(response.getStatus()).willReturn(403);
        given(response.getBody(ErrorResponse.class))
                .willReturn(
                        objectMapper.readValue(
                                knownProvidersIssuesWithContents.get("uk-santander-oauth2"),
                                ErrorResponse.class));

        // when
        boolean result = OpenIdConsentValidator.hasInvalidConsent(response);

        // then
        assertThat(result).isTrue();
    }

    @Test
    public void shouldCheckConsentResponseBarclaysWhereConsentIsInvalid() throws IOException {
        // given
        HttpResponse response = mock(HttpResponse.class);
        given(response.getStatus()).willReturn(400);
        given(response.getBody(ErrorResponse.class))
                .willReturn(
                        objectMapper.readValue(
                                knownProvidersIssuesWithContents.get("uk-barclays-oauth2"),
                                ErrorResponse.class));

        // when
        boolean result = OpenIdConsentValidator.hasInvalidConsent(response);

        // then
        assertThat(result).isTrue();
    }

    @Test
    public void shouldCheckResponseWhereConsentShouldBeValid() throws IOException {
        // given
        HttpResponse response = mock(HttpResponse.class);
        given(response.getStatus()).willReturn(200);
        given(response.getBody(ErrorResponse.class))
                .willReturn(objectMapper.readValue(DIFFERENT_JSON_MESSAGE, ErrorResponse.class));

        // when
        boolean result = OpenIdConsentValidator.hasInvalidConsent(response);

        // then
        assertThat(result).isFalse();
    }

    @Test
    public void shouldCheckErrorResponseWhichHasDifferentErrorMessage() throws IOException {
        // given
        HttpResponse response = mock(HttpResponse.class);
        given(response.getStatus()).willReturn(403);
        given(response.getBody(ErrorResponse.class))
                .willReturn(objectMapper.readValue(DIFFERENT_JSON_MESSAGE, ErrorResponse.class));

        // when
        boolean result = OpenIdConsentValidator.hasInvalidConsent(response);

        // then
        assertThat(result).isFalse();
    }
}

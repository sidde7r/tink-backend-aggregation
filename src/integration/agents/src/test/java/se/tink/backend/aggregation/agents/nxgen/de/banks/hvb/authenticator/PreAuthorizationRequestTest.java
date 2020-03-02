package se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.authenticator;

import static java.util.Collections.singletonList;
import static javax.ws.rs.core.HttpHeaders.ACCEPT;
import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.MapEntry.entry;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.TestFixtures.givenApplicationSessionId;
import static se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.TestFixtures.givenBaseUrl;
import static se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.TestFixtures.givenClientId;
import static se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.TestFixtures.givenPin;
import static se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.TestFixtures.givenSessionId;
import static se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.TestFixtures.givenStaticHeaders;
import static se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.TestFixtures.givenUsername;
import static se.tink.backend.aggregation.nxgen.http.request.HttpMethod.POST;

import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.authenticator.PreAuthorizationRequest.RequestBody;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.authenticator.PreAuthorizationRequest.RequestBody.ChallengeResponse;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.authenticator.PreAuthorizationRequest.RequestBody.ChallengeResponse.UserLoginRequest;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.authenticator.PreAuthorizationRequest.ResponseBody;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.authenticator.PreAuthorizationRequest.ResponseBody.Successes;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.authenticator.PreAuthorizationRequest.ResponseBody.Successes.UserLoginResponse;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.authenticator.PreAuthorizationRequest.ResponseBody.Successes.UserLoginResponse.User;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.authenticator.PreAuthorizationRequest.ResponseBody.Successes.UserLoginResponse.User.Attributes;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.scaffold.ExternalApiCallResult;

public class PreAuthorizationRequestTest {

    private TinkHttpClient httpClient = mock(TinkHttpClient.class);
    private ConfigurationProvider configurationProvider = mock(ConfigurationProvider.class);

    private PreAuthorizationRequest tested =
            new PreAuthorizationRequest(httpClient, configurationProvider);

    @Test
    public void prepareRequestShouldReturnHttpRequestForValidArg() {
        // given
        when(configurationProvider.getBaseUrl()).thenReturn(givenBaseUrl());
        when(configurationProvider.getStaticHeaders()).thenReturn(givenStaticHeaders());

        AuthenticationData givenAuthenticationData =
                AuthenticationData.forAuthorization(
                        givenClientId(),
                        givenUsername(),
                        givenPin(),
                        null,
                        null,
                        givenApplicationSessionId());

        // when
        HttpRequest results = tested.prepareRequest(givenAuthenticationData);

        // then
        assertThat(results.getBody()).isEqualTo(expectedBody());
        assertThat(results.getUrl())
                .isEqualTo(new URL(givenBaseUrl() + "/preauth/v1/preauthorize"));
        assertThat(results.getMethod()).isEqualTo(POST);
        assertThat(results.getHeaders())
                .isNotEmpty()
                .contains(
                        entry(CONTENT_TYPE, singletonList(APPLICATION_JSON)),
                        entry(
                                ACCEPT,
                                singletonList(
                                        "text/javascript, text/html, application/xml, text/xml, */*")),
                        entry("x-wl-app-version", singletonList("4.1.0")),
                        entry("AvantiPIN", singletonList(givenPin())),
                        entry(
                                "AvantiSec",
                                singletonList(
                                        "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"
                                                + "XXXXXXXXXXXXXXXXXXXXXXXXXXXX")));
        assertThat(results.getHeaders().size()).isGreaterThan(5);
    }

    private static RequestBody expectedBody() {
        return new RequestBody()
                .setClientId(givenClientId())
                .setScope("RegisteredClient UCAuthenticatedUser")
                .setChallengeResponse(expectedChallengeResponse());
    }

    private static ChallengeResponse expectedChallengeResponse() {
        return new ChallengeResponse().setUserLogin(expectedUserLoginRequest());
    }

    private static UserLoginRequest expectedUserLoginRequest() {
        return new UserLoginRequest()
                .setUid(givenUsername())
                .setPin(givenPin())
                .setCheckType("login")
                .setIp("127.0.0.1")
                .setIpMsite("127.0.0.1")
                .setPlatform("Avanti")
                .setWlVersion("4.1.0")
                .setEnvironment("HV")
                .setApplicationSessionId(givenApplicationSessionId())
                .setHttpAccept("*")
                .setHttpAcceptEncoding("*")
                .setHttpAcceptLanguage("*")
                .setHttpReferrer("*")
                .setUserAgent("Mozilla")
                .setOperatingSystem("iOS")
                .setOsVersion("12.4.3");
    }

    @Test
    public void parseResponseShouldReturnSessionIdForValidResponse() {
        // given
        int givenStatus = 200;

        HttpResponse givenResponse = mock(HttpResponse.class);
        when(givenResponse.getBody(ResponseBody.class)).thenReturn(givenResponseBody());
        when(givenResponse.getStatus()).thenReturn(givenStatus);

        // when
        ExternalApiCallResult<String> result = tested.parseResponse(givenResponse);

        // then
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getResult()).isEqualTo(givenSessionId());
    }

    private static ResponseBody givenResponseBody() {
        return new ResponseBody()
                .setSuccesses(
                        new Successes()
                                .setUserLogin(
                                        new UserLoginResponse()
                                                .setUser(
                                                        new User()
                                                                .setAttributes(
                                                                        new Attributes()
                                                                                .setSessionId(
                                                                                        givenSessionId())))));
    }
}

package se.tink.backend.aggregation.agents.nxgen.it.banks.ing.authenticator.registration;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.it.banks.ing.TestFixtures.givenJSessionId;

import com.google.common.collect.ImmutableMap;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import javax.ws.rs.core.NewCookie;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.aggregation.agents.nxgen.it.banks.ing.ConfigurationProvider;
import se.tink.backend.aggregation.agents.nxgen.it.banks.ing.TestAsserts;
import se.tink.backend.aggregation.agents.nxgen.it.banks.ing.TestFixtures;
import se.tink.backend.aggregation.agents.nxgen.it.banks.ing.authenticator.registration.Login2ExternalApiCall.Arg;
import se.tink.backend.aggregation.agents.nxgen.it.banks.ing.authenticator.registration.Login2ExternalApiCall.Result;
import se.tink.backend.aggregation.agents.nxgen.it.banks.ing.scaffold.ExternalApiCallResult;
import se.tink.backend.aggregation.nxgen.http.HttpRequestImpl;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.request.HttpMethod;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class Login2ExternalApiCallTest {

    private TinkHttpClient httpClient = mock(TinkHttpClient.class);
    private CommonDataProvider commonDataProvider = mock(CommonDataProvider.class);
    private ConfigurationProvider configurationProvider = mock(ConfigurationProvider.class);

    private Login2ExternalApiCall tested =
            new Login2ExternalApiCall(httpClient, configurationProvider, commonDataProvider);

    @Before
    public void setupMock() {
        Mockito.reset(commonDataProvider);
        Mockito.reset(configurationProvider);
    }

    @Test
    public void prepareRequestShouldReturnHttpRequestForValidArg() {
        // given
        int givenPinDigit1 = 1;
        int givenPinDigit2 = 2;
        int givenPinDigit3 = 3;

        int givenValue1 = 9;
        int givenValue2 = 8;
        int givenValue3 = 7;

        Map<Integer, Integer> pinKeyboardMap =
                ImmutableMap.of(
                        0,
                        0,
                        givenPinDigit1,
                        givenValue1,
                        givenPinDigit2,
                        givenValue2,
                        givenPinDigit3,
                        givenValue3);

        Arg givenArg =
                new Arg()
                        .setDeviceId(TestFixtures.givenDeviceId())
                        .setPinPositions(asList(2, 4, 6))
                        .setPin(
                                format(
                                        "%s%s%s%s%s%s",
                                        0, givenPinDigit1, 0, givenPinDigit2, 0, givenPinDigit3))
                        .setPinKeyboardMap(pinKeyboardMap);

        when(configurationProvider.getBaseUrl()).thenReturn(TestFixtures.givenBaseUrl());
        when(commonDataProvider.prepareFpe(TestFixtures.givenDeviceId()))
                .thenReturn(TestFixtures.givenFpe());
        when(commonDataProvider.getStaticHeaders()).thenReturn(TestFixtures.givenStaticHeaders());

        // when
        HttpRequest request = tested.prepareRequest(givenArg);

        // then
        TestAsserts.assertHttpRequestsEquals(
                request, givenLogin2HttpRequest(givenValue1, givenValue2, givenValue3));
    }

    private static HttpRequest givenLogin2HttpRequest(
            Integer value1, Integer value2, Integer value3) {
        return new HttpRequestImpl(
                HttpMethod.POST,
                new URL(TestFixtures.givenBaseUrl() + "/MobileFlow/login2.htm"),
                TestFixtures.givenStaticHeadersUrlEncodedForm(),
                format(
                        "is_passcode_enrolled=true"
                                + "&is_fingerprint_enrolled=false"
                                + "&challengeId=-"
                                + "&is_touchid_available=true"
                                + "&is_login_with_touch_id_enabled="
                                + "&r=0"
                                + "&showTouchIDNotEnable=true"
                                + "&was_login_activation_already_asked="
                                + "&oid=-"
                                + "&fpe=%s"
                                + "&push_active=false"
                                + "&value1=%d"
                                + "&aid=-"
                                + "&otml_context=c1"
                                + "&is_iphone_x=false"
                                + "&fingerprint=%s"
                                + "&value3=%d"
                                + "&device_os=iOS%%2012.4"
                                + "&geolocation_permission=false"
                                + "&show_push_activation_page=true"
                                + "&isBiometricButtonDisabled=true"
                                + "&value2=%d",
                        TestFixtures.givenFpeUrlEncoded(),
                        value1,
                        TestFixtures.givenDeviceId(),
                        value3,
                        value2));
    }

    @Test
    public void parseResponseShouldReturnSessionIdAnd302WithNewLocationWhenSuccess()
            throws URISyntaxException {
        // given
        int givenStatus = 302;
        HttpResponse httpResponse = mock(HttpResponse.class);
        when(httpResponse.getStatus()).thenReturn(givenStatus);
        when(httpResponse.getCookies())
                .thenReturn(singletonList(new NewCookie("JSESSIONID", givenJSessionId())));

        URI givenNewLocation = new URI("http://newLocation.htm");
        when(httpResponse.getLocation()).thenReturn(givenNewLocation);

        // when
        ExternalApiCallResult<Result> result = tested.parseResponse(httpResponse);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getStatusCode()).isEqualTo(givenStatus);
        assertThat(result.getRedirectLocation()).isEqualTo(givenNewLocation);

        assertThat(result.getResult()).isNotNull();
        assertThat(result.getResult().getJSessionId()).isEqualTo(givenJSessionId());
    }

    @Test
    public void parseResponseShouldThrowRuntimeExceptionWhenJSessionIdMissing() {
        // given
        int givenStatus = 302;
        HttpResponse httpResponse = mock(HttpResponse.class);
        when(httpResponse.getStatus()).thenReturn(givenStatus);
        when(httpResponse.getCookies()).thenReturn(emptyList());

        // when
        Throwable thrown = catchThrowable(() -> tested.parseResponse(httpResponse));

        // then
        assertThat(thrown)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Required value JSESSIONID is missing");
    }
}

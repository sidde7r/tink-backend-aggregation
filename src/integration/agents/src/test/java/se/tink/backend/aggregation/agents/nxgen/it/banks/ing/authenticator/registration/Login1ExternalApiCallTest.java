package se.tink.backend.aggregation.agents.nxgen.it.banks.ing.authenticator.registration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.aggregation.agents.nxgen.it.banks.ing.ConfigurationProvider;
import se.tink.backend.aggregation.agents.nxgen.it.banks.ing.TestAsserts;
import se.tink.backend.aggregation.agents.nxgen.it.banks.ing.TestFixtures;
import se.tink.backend.aggregation.agents.nxgen.it.banks.ing.authenticator.registration.Login1ExternalApiCall.Arg;
import se.tink.backend.aggregation.agents.nxgen.it.banks.ing.authenticator.registration.Login1ExternalApiCall.Result;
import se.tink.backend.aggregation.agents.nxgen.it.banks.ing.scaffold.ExternalApiCallResult;
import se.tink.backend.aggregation.nxgen.http.HttpRequestImpl;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.request.HttpMethod;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class Login1ExternalApiCallTest {

    private TinkHttpClient httpClient = mock(TinkHttpClient.class);
    private ConfigurationProvider configurationProvider = mock(ConfigurationProvider.class);
    private CommonDataProvider commonDataProvider = mock(CommonDataProvider.class);
    private PinKeyboardMapper pinKeyboardMapper = mock(PinKeyboardMapper.class);
    private OtmlParser otmlParser = mock(OtmlParser.class);

    private Login1ExternalApiCall tested =
            new Login1ExternalApiCall(
                    httpClient,
                    configurationProvider,
                    commonDataProvider,
                    pinKeyboardMapper,
                    otmlParser);

    @Before
    public void setupMock() {
        Mockito.reset(commonDataProvider);
        Mockito.reset(pinKeyboardMapper);
    }

    @Test
    public void prepareRequestShouldReturnHttpRequestForValidArg() {
        // given
        Arg arg =
                new Arg()
                        .setDeviceId(TestFixtures.givenDeviceId())
                        .setBirthDate(TestFixtures.givenBirthDate())
                        .setPersonId(TestFixtures.givenPersonId());

        when(configurationProvider.getBaseUrl()).thenReturn(TestFixtures.givenBaseUrl());
        when(commonDataProvider.prepareFpe(TestFixtures.givenDeviceId()))
                .thenReturn(TestFixtures.givenFpe());
        when(commonDataProvider.getStaticHeaders()).thenReturn(TestFixtures.givenStaticHeaders());

        // when
        HttpRequest request = tested.prepareRequest(arg);

        // then
        TestAsserts.assertHttpRequestsEquals(request, givenLogin1HttpRequest());
    }

    private static HttpRequest givenLogin1HttpRequest() {
        return new HttpRequestImpl(
                HttpMethod.POST,
                new URL(TestFixtures.givenBaseUrl() + "/MobileFlow/login1.htm"),
                TestFixtures.givenStaticHeadersUrlEncodedForm(),
                String.format(
                        "is=BAB50C27-3DE2-4FC4-9086-9D7B85A08B2C"
                                + "&visibility=visible"
                                + "&omtl_context=c1"
                                + "&day=%s"
                                + "&aid=-"
                                + "&month=%s"
                                + "&year=%s"
                                + "&oid=-"
                                + "&fpe=%s"
                                + "&personId=%s",
                        TestFixtures.BIRTH_DAY,
                        TestFixtures.BIRTH_MONTH,
                        TestFixtures.BIRTH_YEAR,
                        TestFixtures.givenFpeUrlEncoded(),
                        TestFixtures.givenPersonId()));
    }

    @Test
    public void parseResponseShouldReturnResultWhenHttpResponseIsCorrectlyParsed() {
        // given
        String dataSources = TestFixtures.givenLogin1OtmlDatasources();
        OtmlResponse otmlResponse = new OtmlResponse().setDatasources(dataSources);
        HttpResponse httpResponse = mock(HttpResponse.class);
        when(httpResponse.getBody(OtmlResponse.class)).thenReturn(otmlResponse);
        when(httpResponse.getStatus()).thenReturn(200);

        when(otmlParser.getPinKeyboardImages(dataSources))
                .thenReturn(TestFixtures.givenKeyboardImagesBase64());
        when(otmlParser.getPinPositions(dataSources)).thenReturn(TestFixtures.givenPinPositions());

        when(pinKeyboardMapper.toPinKeyboardMap(TestFixtures.givenKeyboardImagesBase64()))
                .thenReturn(TestFixtures.givenMapOfKeyboardImageValueToIndex());

        // when
        ExternalApiCallResult<Result> result = tested.parseResponse(httpResponse);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getStatusCode()).isEqualTo(200);

        assertThat(result.getResult()).isNotNull();
        assertThat(result.getResult().getPinKeyboardMap())
                .isEqualTo(TestFixtures.givenMapOfKeyboardImageValueToIndex());
        assertThat(result.getResult().getPinNumberPositions())
                .isEqualTo(TestFixtures.givenPinPositions());
    }
}

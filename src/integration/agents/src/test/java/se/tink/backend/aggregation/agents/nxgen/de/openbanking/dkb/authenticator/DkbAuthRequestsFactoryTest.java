package se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.authenticator;

import static java.util.Collections.singletonList;
import static javax.ws.rs.core.HttpHeaders.ACCEPT;
import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static javax.ws.rs.core.HttpHeaders.COOKIE;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.MapEntry.entry;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.DkbConstants.HeaderKeys.PSD_2_AUTHORIZATION_HEADER;
import static se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token.createBearer;
import static se.tink.backend.aggregation.nxgen.http.request.HttpMethod.GET;
import static se.tink.backend.aggregation.nxgen.http.request.HttpMethod.POST;
import static se.tink.backend.aggregation.nxgen.http.request.HttpMethod.PUT;

import java.time.LocalDate;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.DkbStorage;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.DkbUserIpInformation;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.authenticator.DkbAuthRequestsFactory.ConsentAuthorizationMethod;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.authenticator.DkbAuthRequestsFactory.ConsentAuthorizationOtp;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.authenticator.DkbAuthRequestsFactory.SelectedAuthMethod;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.authenticator.DkbAuthRequestsFactory.TanCode;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.authenticator.DkbAuthRequestsFactory.UserCredentials;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.configuration.DkbConfiguration;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;

public class DkbAuthRequestsFactoryTest {

    private static final String HTTP_BASE_URL = "http://base.url";
    private static final String SESSION_ID = "sessionId";
    private static final String XSRF_TOKEN = "xsrfToken";

    private DkbConfiguration configMock = mock(DkbConfiguration.class);
    private DkbStorage storageMock = mock(DkbStorage.class);
    private DkbUserIpInformation dkbUserIpInformation = mock(DkbUserIpInformation.class);

    private DkbAuthRequestsFactory tested =
            new DkbAuthRequestsFactory(configMock, storageMock, dkbUserIpInformation);

    @Before
    public void setupMocks() {
        when(configMock.getBaseUrl()).thenReturn(HTTP_BASE_URL);

        when(storageMock.getJsessionid()).thenReturn(SESSION_ID);
        when(storageMock.getXsrfToken()).thenReturn(XSRF_TOKEN);
    }

    @Test
    public void generateAuth1stFactorRequestShouldReturnValidHttpRequest() {
        // given
        String givenUsername = "username";
        String givenPassword = "password";

        // when
        HttpRequest result = tested.generateAuth1stFactorRequest(givenUsername, givenPassword);

        // then
        assertThat(result.getUrl())
                .hasToString(HTTP_BASE_URL + "/pre-auth/psd2-auth/v1/auth/token");
        assertThat(result.getMethod()).isEqualTo(POST);
        assertThat(result.getBody()).isEqualTo(new UserCredentials(givenUsername, givenPassword));
        assertThat(result.getHeaders())
                .contains(
                        entry(CONTENT_TYPE, singletonList(APPLICATION_JSON_TYPE)),
                        entry(ACCEPT, singletonList(APPLICATION_JSON_TYPE)));
    }

    @Test
    public void generateAuthMethodSelectionRequestShouldReturnValidHttpRequest() {
        // given
        String givenMethodId = "selectedMethodId";

        // when
        HttpRequest result = tested.generateAuthMethodSelectionRequest(givenMethodId);

        // then
        assertThat(result.getUrl()).hasToString(HTTP_BASE_URL + "/pre-auth/psd2-auth/v1/challenge");
        assertThat(result.getMethod()).isEqualTo(POST);
        assertThat(result.getBody()).isEqualTo(new SelectedAuthMethod(givenMethodId));
        assertThat(result.getHeaders())
                .contains(
                        entry(CONTENT_TYPE, singletonList(APPLICATION_JSON_TYPE)),
                        entry(ACCEPT, singletonList(APPLICATION_JSON_TYPE)),
                        entry(COOKIE, singletonList("JSESSIONID=" + SESSION_ID)),
                        entry("X-XSRF-TOKEN", singletonList(XSRF_TOKEN)));
    }

    @Test
    public void generateTanSubmissionRequestShouldReturnValidHttpRequest() {
        // given
        String givenCode = "tanCode";

        // when
        HttpRequest result = tested.generateTanSubmissionRequest(givenCode);

        // then
        assertThat(result.getUrl()).hasToString(HTTP_BASE_URL + "/pre-auth/psd2-auth/v1/challenge");
        assertThat(result.getMethod()).isEqualTo(PUT);
        assertThat(result.getBody()).isEqualTo(new TanCode(givenCode));
        assertThat(result.getHeaders())
                .contains(
                        entry(CONTENT_TYPE, singletonList(APPLICATION_JSON_TYPE)),
                        entry(ACCEPT, singletonList(APPLICATION_JSON_TYPE)),
                        entry(COOKIE, singletonList("JSESSIONID=" + SESSION_ID)),
                        entry("X-XSRF-TOKEN", singletonList(XSRF_TOKEN)));
    }

    @Test
    public void generateCreateConsentRequestShouldReturnValidHttpRequest() {
        // given
        String givenAccessToken = "accessToken";
        when(storageMock.getAccessToken())
                .thenReturn(Optional.of(createBearer(givenAccessToken, null, 0)));
        LocalDate givenDate = LocalDate.parse("2020-01-01");

        // when
        HttpRequest result = tested.generateCreateConsentRequest(givenDate);

        // then
        assertThat(result.getUrl()).hasToString(HTTP_BASE_URL + "/psd2/v1/consents");
        assertThat(result.getMethod()).isEqualTo(POST);
        assertThat(result.getHeaders())
                .contains(
                        entry(CONTENT_TYPE, singletonList(APPLICATION_JSON_TYPE)),
                        entry(ACCEPT, singletonList(APPLICATION_JSON_TYPE)),
                        entry(
                                PSD_2_AUTHORIZATION_HEADER,
                                singletonList("Bearer " + givenAccessToken)));
    }

    @Test
    public void generateGetConsentRequestShouldReturnValidHttpRequest() {
        // given
        String givenConsentId = "consentId";
        String givenAccessToken = "accessToken";
        when(storageMock.getAccessToken())
                .thenReturn(Optional.of(createBearer(givenAccessToken, null, 0)));

        // whe
        HttpRequest result = tested.generateGetConsentRequest(givenConsentId);

        // then
        assertThat(result.getUrl())
                .hasToString(HTTP_BASE_URL + "/psd2/v1/consents/" + givenConsentId);
        assertThat(result.getMethod()).isEqualTo(GET);
        assertThat(result.getHeaders())
                .contains(
                        entry(CONTENT_TYPE, singletonList(APPLICATION_JSON_TYPE)),
                        entry(ACCEPT, singletonList(APPLICATION_JSON_TYPE)),
                        entry(
                                PSD_2_AUTHORIZATION_HEADER,
                                singletonList("Bearer " + givenAccessToken)));
    }

    @Test
    public void generateConsentAuthorizationRequestShouldReturnValidHttpRequest() {
        // given
        String givenConsentId = "consentId";
        String givenAccessToken = "accessToken";
        when(storageMock.getAccessToken())
                .thenReturn(Optional.of(createBearer(givenAccessToken, null, 0)));

        // when
        HttpRequest result = tested.generateConsentAuthorizationRequest(givenConsentId);

        // then
        assertThat(result.getUrl())
                .hasToString(
                        HTTP_BASE_URL + "/psd2/v1/consents/" + givenConsentId + "/authorisations");
        assertThat(result.getMethod()).isEqualTo(POST);
        assertThat(result.getBody()).isEqualTo("{}");
        assertThat(result.getHeaders())
                .contains(
                        entry(CONTENT_TYPE, singletonList(APPLICATION_JSON_TYPE)),
                        entry(ACCEPT, singletonList(APPLICATION_JSON_TYPE)),
                        entry(
                                PSD_2_AUTHORIZATION_HEADER,
                                singletonList("Bearer " + givenAccessToken)));
    }

    @Test
    public void generateConsentAuthorizationMethodRequestShouldReturnValidHttpRequest() {
        // given
        String givenConsentId = "consentId";
        String givenAuthorizationId = "authorisationId";
        String givenMethodId = "methodId";
        String givenAccessToken = "accessToken";
        when(storageMock.getAccessToken())
                .thenReturn(Optional.of(createBearer(givenAccessToken, null, 0)));

        // when
        HttpRequest result =
                tested.generateConsentAuthorizationMethodRequest(
                        givenConsentId, givenAuthorizationId, givenMethodId);

        // then
        assertThat(result.getUrl())
                .hasToString(
                        HTTP_BASE_URL
                                + "/psd2/v1/consents/"
                                + givenConsentId
                                + "/authorisations/"
                                + givenAuthorizationId);
        assertThat(result.getMethod()).isEqualTo(PUT);
        assertThat(result.getBody()).isEqualTo(new ConsentAuthorizationMethod(givenMethodId));
        assertThat(result.getHeaders())
                .contains(
                        entry(CONTENT_TYPE, singletonList(APPLICATION_JSON_TYPE)),
                        entry(ACCEPT, singletonList(APPLICATION_JSON_TYPE)),
                        entry(
                                PSD_2_AUTHORIZATION_HEADER,
                                singletonList("Bearer " + givenAccessToken)));
    }

    @Test
    public void generateConsentAuthorizationOtpRequestShouldReturnValidHttpRequest() {
        // given
        String givenConsentId = "consentId";
        String givenAuthorizationId = "authorisationId";
        String givenCode = "otpCode";
        String givenAccessToken = "accessToken";
        when(storageMock.getAccessToken())
                .thenReturn(Optional.of(createBearer(givenAccessToken, null, 0)));

        // when
        HttpRequest result =
                tested.generateConsentAuthorizationOtpRequest(
                        givenConsentId, givenAuthorizationId, givenCode);

        // then
        assertThat(result.getUrl())
                .hasToString(
                        HTTP_BASE_URL
                                + "/psd2/v1/consents/"
                                + givenConsentId
                                + "/authorisations/"
                                + givenAuthorizationId);
        assertThat(result.getMethod()).isEqualTo(PUT);
        assertThat(result.getBody()).isEqualTo(new ConsentAuthorizationOtp(givenCode));
        assertThat(result.getHeaders())
                .contains(
                        entry(CONTENT_TYPE, singletonList(APPLICATION_JSON_TYPE)),
                        entry(ACCEPT, singletonList(APPLICATION_JSON_TYPE)),
                        entry(
                                PSD_2_AUTHORIZATION_HEADER,
                                singletonList("Bearer " + givenAccessToken)));
    }
}

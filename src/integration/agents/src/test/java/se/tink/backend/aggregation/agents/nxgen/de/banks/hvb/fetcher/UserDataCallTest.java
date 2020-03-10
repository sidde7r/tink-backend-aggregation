package se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.fetcher;

import static java.util.Collections.singletonList;
import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static javax.ws.rs.core.MediaType.APPLICATION_FORM_URLENCODED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.MapEntry.entry;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.TestFixtures.givenAuthorization;
import static se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.TestFixtures.givenBaseUrl;
import static se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.TestFixtures.givenStaticHeaders;
import static se.tink.backend.aggregation.nxgen.http.request.HttpMethod.POST;

import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.ConfigurationProvider;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.scaffold.ExternalApiCallResult;

public class UserDataCallTest {

    private TinkHttpClient httpClient = mock(TinkHttpClient.class);
    private ConfigurationProvider configurationProvider = mock(ConfigurationProvider.class);

    private UserDataCall tested = new UserDataCall(httpClient, configurationProvider);

    @Test
    public void prepareRequestShouldReturnHttpRequestForValidArg() {
        // given
        when(configurationProvider.getBaseUrl()).thenReturn(givenBaseUrl());
        when(configurationProvider.getStaticHeaders()).thenReturn(givenStaticHeaders());

        // when
        HttpRequest results = tested.prepareRequest(givenAuthorization());

        // then
        assertThat(results.getBody()).isNull();
        assertThat(results.getUrl())
                .isEqualTo(
                        new URL(givenBaseUrl() + "/adapters/UC_MBX_GL_BE_FACADE_NJ/userDataList"));
        assertThat(results.getMethod()).isEqualTo(POST);
        assertThat(results.getHeaders())
                .isNotEmpty()
                .contains(
                        entry(CONTENT_TYPE, singletonList(APPLICATION_FORM_URLENCODED)),
                        entry(AUTHORIZATION, singletonList(givenAuthorization())));
    }

    @Test
    public void parseResponseShouldReturnUserDataResponseForValidResponse() {
        // given
        int givenStatus = 200;
        UserDataResponse givenUserDataResponse = new UserDataResponse();

        HttpResponse givenResponse = mock(HttpResponse.class);
        when(givenResponse.getBody(UserDataResponse.class)).thenReturn(givenUserDataResponse);
        when(givenResponse.getStatus()).thenReturn(givenStatus);

        // when
        ExternalApiCallResult<UserDataResponse> result = tested.parseResponse(givenResponse);

        // then
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getResult()).isEqualTo(givenUserDataResponse);
    }
}

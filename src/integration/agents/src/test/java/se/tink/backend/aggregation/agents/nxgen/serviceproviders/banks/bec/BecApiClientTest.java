package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import javax.ws.rs.core.MediaType;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import se.tink.backend.aggregation.agents.exceptions.ThirdPartyAppException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.authenticator.BecSecurityHelper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.authenticator.rpc.NemIdPollResponse;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class BecApiClientTest {

    private static final String NEM_ID_POLL_URL =
            "https://eticket.prod.bec.dk/mobilbank/logon/challenge/pollstate";
    private BecApiClient apiClient;

    private BecSecurityHelper securityHelper;
    private TinkHttpClient client;
    private BecUrlConfiguration agentUrl;

    private RequestBuilder requestBuilder;

    @Before
    public void setUp() {
        securityHelper = null;
        client = mock(TinkHttpClient.class, Answers.RETURNS_DEEP_STUBS);
        agentUrl = new BecUrlConfiguration("");
        requestBuilder = mock(RequestBuilder.class);

        apiClient = new BecApiClient(securityHelper, client, agentUrl);

        given(client.request(NEM_ID_POLL_URL)).willReturn(requestBuilder);

        given(
                        requestBuilder.header(
                                BecConstants.Header.PRAGMA_KEY, BecConstants.Header.PRAGMA_VALUE))
                .willReturn(requestBuilder);
        given(requestBuilder.type(MediaType.APPLICATION_JSON_TYPE)).willReturn(requestBuilder);
    }

    @Test
    public void pollNemIdShouldSucceed() throws ThirdPartyAppException {
        // given
        String token = "sample token";
        // and
        given(requestBuilder.queryParam("token", token)).willReturn(requestBuilder);
        // and
        given(requestBuilder.get(NemIdPollResponse.class))
                .willReturn(
                        SerializationUtils.deserializeFromString(
                                "{\"state\":1}", NemIdPollResponse.class));
        // when
        apiClient.pollNemId(token);

        // then
        verify(client).request(NEM_ID_POLL_URL);
        verify(requestBuilder)
                .header(BecConstants.Header.PRAGMA_KEY, BecConstants.Header.PRAGMA_VALUE);
        verify(requestBuilder).type(MediaType.APPLICATION_JSON_TYPE);
        verify(requestBuilder).queryParam("token", token);
        verify(requestBuilder).get(NemIdPollResponse.class);
    }

    @Test
    public void pollNemIdShouldThrowTimeOutExceptionWhenReturnedStateDifferentThan1() {
        // given
        String token = "sample token";
        // and
        given(requestBuilder.queryParam("token", token)).willReturn(requestBuilder);
        // and
        given(requestBuilder.get(NemIdPollResponse.class))
                .willReturn(
                        SerializationUtils.deserializeFromString(
                                "{\"state\":2}", NemIdPollResponse.class));
        // when
        Throwable t = catchThrowable(() -> apiClient.pollNemId(token));

        // then
        verify(client).request(NEM_ID_POLL_URL);
        verify(requestBuilder)
                .header(BecConstants.Header.PRAGMA_KEY, BecConstants.Header.PRAGMA_VALUE);
        verify(requestBuilder).type(MediaType.APPLICATION_JSON_TYPE);
        verify(requestBuilder).queryParam("token", token);
        verify(requestBuilder).get(NemIdPollResponse.class);
        // and
        assertThat(t).isInstanceOf(ThirdPartyAppException.class).hasMessage("NemID TIMEOUT.");
    }
}

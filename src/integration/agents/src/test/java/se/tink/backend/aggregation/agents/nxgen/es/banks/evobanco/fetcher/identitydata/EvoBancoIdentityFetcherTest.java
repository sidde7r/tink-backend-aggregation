package se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.fetcher.identitydata;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.file.Paths;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.SupplementalInfoException;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.EvoBancoApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.fetcher.identitydata.rpc.EvoBancoIdentityDataResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.filter.entity.EvoBancoErrorResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.libraries.identitydata.IdentityData;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class EvoBancoIdentityFetcherTest {
    private static final String TEST_DATA_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/es/banks/evobanco/resources";
    private EvoBancoApiClient apiClient;
    private EvoBancoIdentityDataFetcher evoBancoIdentityDataFetcher;

    @Before
    public void setup() {
        apiClient = mock(EvoBancoApiClient.class);
        evoBancoIdentityDataFetcher = new EvoBancoIdentityDataFetcher(apiClient);
    }

    @Test
    public void shouldFetchIdentityData() {
        // given
        HttpResponse response = mockResponse();

        when(apiClient.fetchIdentityData()).thenReturn(response);

        // when
        IdentityData identityData = evoBancoIdentityDataFetcher.fetchIdentityData();

        // then
        Assert.assertEquals(identityData.getDateOfBirth().toString(), "1999-01-01");
        Assert.assertEquals(identityData.getFullName(), "ROBERTO ALEJANDRO VALDES");
    }

    @Test
    public void shouldHandleSessionException() {
        // given
        HttpResponseException response = mockResponseException();

        when(apiClient.fetchIdentityData()).thenThrow(response);

        // when
        Throwable throwable = catchThrowable(() -> evoBancoIdentityDataFetcher.fetchIdentityData());

        // expected
        assertThat(throwable).isExactlyInstanceOf(SupplementalInfoException.class);
    }

    private HttpResponse mockResponse() {
        EvoBancoIdentityDataResponse evoBancoIdentityDataResponse =
                SerializationUtils.deserializeFromString(
                        Paths.get(TEST_DATA_PATH, "identity_data.json").toFile(),
                        EvoBancoIdentityDataResponse.class);

        HttpResponse response = mock(HttpResponse.class);
        given(response.getStatus()).willReturn(200);
        given(response.getBody(EvoBancoIdentityDataResponse.class))
                .willReturn(evoBancoIdentityDataResponse);

        return response;
    }

    private HttpResponseException mockResponseException() {
        HttpResponse mocked = mock(HttpResponse.class);
        when(mocked.getStatus()).thenReturn(401);
        HttpResponseException exception = new HttpResponseException(null, mocked);

        when(exception.getResponse().getBody(EvoBancoErrorResponse.class))
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                "{\"response\":{\"message\":\"Token de acceso inv?lido\",\"codigo\":\"KO\"}}",
                                EvoBancoErrorResponse.class));

        return exception;
    }
}

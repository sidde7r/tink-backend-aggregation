package se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.session;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.vavr.jackson.datatype.VavrModule;
import java.io.IOException;
import java.nio.file.Paths;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.SabadellApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.creditcards.rpc.ContactDataResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.rpc.ErrorResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class SabadellSessionHandlerTest {

    private static final String DATA_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/es/banks/sabadell/resources";

    private SabadellApiClient apiClient;
    private SabadellSessionHandler sessionHandler;

    @Before
    public void setup() {
        apiClient = mock(SabadellApiClient.class);
        sessionHandler = new SabadellSessionHandler(apiClient);
    }

    @Test
    public void shouldFetchContactData() throws IOException {

        // given
        final ContactDataResponse contactDataResponse =
                loadSampleData("contactData.json", ContactDataResponse.class);
        when(apiClient.fetchContactData()).thenReturn(contactDataResponse);

        // when
        Throwable throwable = catchThrowable(() -> sessionHandler.keepAlive());

        // then
        Assert.assertNull(throwable);
    }

    @Test
    public void shouldFetchErrorContactData() {

        // given
        HttpResponseException exception = mockResponse(403);
        when(apiClient.fetchContactData()).thenThrow(exception);

        // when
        Throwable thrown = catchThrowable(() -> sessionHandler.keepAlive());

        // then
        assertThat(thrown).isExactlyInstanceOf(SessionException.class);
    }

    private HttpResponseException mockResponse(int status) {
        HttpResponse mocked = mock(HttpResponse.class);
        when(mocked.getStatus()).thenReturn(status);
        HttpResponseException exception = new HttpResponseException(null, mocked);

        when(exception.getResponse().getBody(ErrorResponse.class))
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                "{\"errorMessage\":\"La sesión ha expirado por seguridad. Si lo desea conéctese de nuevo.\",\"code\":\"100\",\"errorMessageTitle\":\"¡Vaya! Algo no ha ido bien...\",\"errorMessageDetail\":\"\",\"errorCode\":\"100\",\"severity\":\"FATAL\",\"labelCta\":\"\",\"operationCta\":\"\",\"clickToCall\":\"\",\"evento\":\"\",\"nombreOperativa\":\"\",\"idCanal\":\"\"}\n",
                                ErrorResponse.class));

        return exception;
    }

    private <T> T loadSampleData(String path, Class<T> cls) throws IOException {
        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new VavrModule());
        return objectMapper.readValue(Paths.get(DATA_PATH, path).toFile(), cls);
    }
}

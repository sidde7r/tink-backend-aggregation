package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.nio.file.Paths;
import java.util.Map;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import lombok.SneakyThrows;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.configuration.SparebankApiConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.fetcher.card.rpc.CardResponse;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@RunWith(JUnitParamsRunner.class)
public class SparebankApiClientTest {

    private static final String TEST_DATA_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/sparebank/resources";

    private TinkHttpClient tinkClientMock;
    private SparebankApiClient apiClient;

    @Before
    public void setup() {
        tinkClientMock = mock(TinkHttpClient.class);
        QsealcSigner signer = mock(QsealcSigner.class);
        SparebankApiConfiguration apiConfiguration = mockApiConfiguration();
        SparebankStorage storage = mock(SparebankStorage.class);

        apiClient = new SparebankApiClient(tinkClientMock, signer, apiConfiguration, storage);
    }

    @Test
    public void should_return_correct_credit_cards_response() {
        // given
        CardResponse cardResponse = deserializeFromFile("cardAccount.json", CardResponse.class);
        RequestBuilder okResponse = mockOkResponse(cardResponse);
        when(tinkClientMock.request(any(URL.class))).thenReturn(okResponse);

        // when
        CardResponse actualResponse = apiClient.fetchCards();

        // then
        assertThat(actualResponse).isEqualTo(cardResponse);
    }

    @Test
    public void should_return_empty_credit_cards_response_on_404() {
        // given
        HttpResponseException hre = mockHttpException(404);
        RequestBuilder errorResponse = mockErrorResponse(hre);
        when(tinkClientMock.request(any(URL.class))).thenReturn(errorResponse);

        // when
        CardResponse actualResponse = apiClient.fetchCards();

        // then
        assertThat(actualResponse).isEqualTo(CardResponse.empty());
    }

    @Test
    @Parameters(value = {"400", "401", "429", "500", "503"})
    public void should_rethrow_credit_cards_http_response_exceptions_for_codes_other_than_404(
            int status) {
        // given
        HttpResponseException hre = mockHttpException(status);
        RequestBuilder errorResponse = mockErrorResponse(hre);
        when(tinkClientMock.request(any(URL.class))).thenReturn(errorResponse);

        // when
        Throwable throwable = catchThrowable(() -> apiClient.fetchCards());

        // then
        assertThat(throwable).isNotNull().isEqualTo(hre);
    }

    private HttpResponseException mockHttpException(int status) {
        HttpResponseException hre = mock(HttpResponseException.class);

        HttpResponse httpResponse = mock(HttpResponse.class);
        when(hre.getResponse()).thenReturn(httpResponse);
        when(httpResponse.getStatus()).thenReturn(status);

        return hre;
    }

    private static <T> RequestBuilder mockOkResponse(T response) {
        RequestBuilder requestBuilder = mockRequestBuilder();
        when(requestBuilder.get(any())).thenReturn(response);
        return requestBuilder;
    }

    private static <T> RequestBuilder mockErrorResponse(HttpResponseException hre) {
        RequestBuilder requestBuilder = mockRequestBuilder();
        when(requestBuilder.get(any())).thenThrow(hre);
        return requestBuilder;
    }

    private static RequestBuilder mockRequestBuilder() {
        RequestBuilder requestBuilder = mock(RequestBuilder.class);
        when(requestBuilder.headers(any(Map.class))).thenReturn(requestBuilder);
        when(requestBuilder.header(any(String.class), any())).thenReturn(requestBuilder);
        return requestBuilder;
    }

    private static SparebankApiConfiguration mockApiConfiguration() {
        return SparebankApiConfiguration.builder()
                .baseUrl("https://example.base.url")
                .redirectUrl("https://example.redirect.url")
                .qsealcBase64("w/e")
                .certificateSerialNumberInHex("w/e")
                .certificateIssuerDN("w/e")
                .userIp("w/e")
                .isUserPresent(true)
                .build();
    }

    @SneakyThrows
    @SuppressWarnings("SameParameterValue")
    private static <T> T deserializeFromFile(String filePath, Class<T> tClass) {
        File file = Paths.get(TEST_DATA_PATH, filePath).toFile();
        return new ObjectMapper().readValue(file, tClass);
    }
}

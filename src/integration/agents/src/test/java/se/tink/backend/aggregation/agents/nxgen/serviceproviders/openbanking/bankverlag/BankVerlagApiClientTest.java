package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankverlag;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.sun.jersey.core.util.MultivaluedMapImpl;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.ConstantLocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGeneratorImpl;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class BankVerlagApiClientTest {
    private BankverlagApiClient apiClient;
    private TinkHttpClient client;
    private BankverlagStorage storage;

    private final String PASSWORD = "dummyPassword";
    private final String USERNAME = "dummyUsername";
    private final String URL = "dummyUrl";

    @Before
    public void init() {
        client = mock(TinkHttpClient.class);
        BankverlagHeaderValues headerValues = new BankverlagHeaderValues("dummyAspsp", "dummyIp");
        storage = new BankverlagStorage(new PersistentStorage(), new SessionStorage());

        apiClient =
                new BankverlagApiClient(
                        client,
                        headerValues,
                        storage,
                        new RandomValueGeneratorImpl(),
                        new ConstantLocalDateTimeSource(),
                        mock(BankverlagErrorHandler.class));
    }

    @Test
    public void
            initializeAuthorization_should_save_Auth_Method_from_response_header_if_header_available() {

        // given
        RequestBuilder requestBuilder = mock(RequestBuilder.class);
        HttpResponse response = mock(HttpResponse.class);
        when(client.request(any(URL.class))).thenReturn(requestBuilder);
        when(requestBuilder.accept(any(MediaType.class))).thenReturn(requestBuilder);
        when(requestBuilder.accept(any(String.class))).thenReturn(requestBuilder);
        when(requestBuilder.type(any(String.class))).thenReturn(requestBuilder);
        when(requestBuilder.header(any(), any())).thenReturn(requestBuilder);
        when(requestBuilder.post(any(), any())).thenReturn(response);

        MultivaluedMap<String, String> headersMap = new MultivaluedMapImpl();
        headersMap.putSingle("Aspsp-Sca-Approach", "DECOUPLED");
        when(response.getHeaders()).thenReturn(headersMap);
        when(response.getHeaders()).thenReturn(headersMap);

        // when
        apiClient.initializeAuthorization(URL, USERNAME, PASSWORD);

        // then
        assertThat(storage.getPushOtpFromHeader()).isEqualToIgnoringCase("PUSH_OTP");
    }
}

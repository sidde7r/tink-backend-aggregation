package se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.filters;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Paths;
import org.apache.commons.io.FileUtils;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.DnbConstants;
import se.tink.backend.aggregation.nxgen.http.HttpRequestImpl;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpMethod;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class DnbErrorsFilterTest {

    private static final String RESOURCES_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/no/banks/dnb/resources";

    private DnbErrorsFilter dnbErrorsFilter;
    private HttpRequest httpRequest;
    private Filter mockedFilter;

    @Before
    public void setup() {
        dnbErrorsFilter = new DnbErrorsFilter();
        httpRequest =
                new HttpRequestImpl(HttpMethod.GET, URL.of(DnbConstants.Url.FETCH_ACCOUNT_DETAILS));
        mockedFilter = mock(Filter.class);
        dnbErrorsFilter.setNext(mockedFilter);
    }

    @Test
    public void shouldHandleNoAccessError() {
        // Given
        HttpResponse mockedResponse = getMockedResponse(true, 403);
        when(mockedFilter.handle(any())).thenReturn(mockedResponse);

        // When
        Throwable throwable = Assertions.catchThrowable(() -> dnbErrorsFilter.handle(httpRequest));

        // Then
        assertThat(throwable).isInstanceOf(LoginException.class);
    }

    @Test
    public void shouldNotFilterWhenStatusIsOk() {
        // Given
        HttpResponse mockedResponse = getMockedResponse(false, 200);
        when(mockedFilter.handle(any())).thenReturn(mockedResponse);

        // When & Then
        assertThatCode(() -> dnbErrorsFilter.handle(httpRequest)).doesNotThrowAnyException();
    }

    @Test
    public void shouldReturnResponseWhenHasNoBody() {
        // Given
        HttpResponse mockedResponse = getMockedResponse(false, 403);
        when(mockedFilter.handle(any())).thenReturn(mockedResponse);

        // When
        HttpResponse result = dnbErrorsFilter.handle(httpRequest);

        // Then
        assertThat(result).isEqualTo(mockedResponse);
    }

    private HttpResponse getMockedResponse(boolean hasBody, int status) {
        HttpResponse mockedResponse = mock(HttpResponse.class);
        when(mockedResponse.hasBody()).thenReturn(hasBody);
        when(mockedResponse.getBody(any())).thenReturn(getNoAccessExceptionBody());
        when(mockedResponse.getStatus()).thenReturn(status);
        return mockedResponse;
    }

    private String getNoAccessExceptionBody() {
        try {
            return FileUtils.readFileToString(
                    Paths.get(RESOURCES_PATH, "noAccessResponse.html").toFile(), "UTF-8");
        } catch (IOException e) {
            return null;
        }
    }
}

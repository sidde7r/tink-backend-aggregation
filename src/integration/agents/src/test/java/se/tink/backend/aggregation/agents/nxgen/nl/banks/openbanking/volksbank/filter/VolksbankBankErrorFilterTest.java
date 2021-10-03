package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.ws.rs.core.MediaType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@RunWith(MockitoJUnitRunner.class)
public class VolksbankBankErrorFilterTest {

    private BankErrorResponseFilter bankErrorFilter;
    private PersistentStorage persistentStorage;
    private Filter nextFilter;

    @Before
    public void setUp() {

        persistentStorage = mock(PersistentStorage.class);
        bankErrorFilter = new BankErrorResponseFilter(persistentStorage);
        nextFilter = mock(Filter.class);
    }

    @Test
    public void shouldThrowSessionError() {

        // given
        final String responseBody =
                "[\n"
                        + "   {\n"
                        + "      \"category\":\"ERROR\",\n"
                        + "      \"code\":\"CONSENT_EXPIRED\",\n"
                        + "      \"text\":\"The expiration date of the mandate has been expired.\"\n"
                        + "   }\n"
                        + "]";

        HttpResponse mockedResponse = mockResponse(responseBody);
        Filter nextFilter = mock(Filter.class);
        when(nextFilter.handle(any())).thenThrow(new HttpResponseException(null, mockedResponse));

        // when
        bankErrorFilter.setNext(nextFilter);
        Throwable t = catchThrowable(() -> bankErrorFilter.handle(null));

        // then
        assertThat(t)
                .isInstanceOf(SessionException.class)
                .hasMessage("Consent Status: CONSENT_EXPIRED");
    }

    @Test
    public void shouldNotThrowSessionError() {

        // given
        final String responseBody =
                "{\n"
                        + "   \"error\":\"invalid_request\",\n"
                        + "   \"error_description\":\"The request failed due to some unknown reason\"\n"
                        + "}";

        HttpResponse response = mockResponse(responseBody);
        when(nextFilter.handle(any())).thenReturn(response);

        // when
        bankErrorFilter.setNext(nextFilter);
        bankErrorFilter.handle(null);

        // then
        verify(nextFilter, times(1)).handle(any());
    }

    static HttpResponse mockResponse(String responseBody) {
        HttpResponse mocked = mock(HttpResponse.class);
        when(mocked.getBody(String.class)).thenReturn(responseBody);
        when(mocked.getType()).thenReturn(MediaType.APPLICATION_JSON_TYPE);
        return mocked;
    }
}

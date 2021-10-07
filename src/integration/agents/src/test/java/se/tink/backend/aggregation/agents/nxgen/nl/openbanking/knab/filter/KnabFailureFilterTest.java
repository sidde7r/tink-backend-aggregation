package se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

@RunWith(MockitoJUnitRunner.class)
public class KnabFailureFilterTest {

    private KnabFailureFilter knabFailureFilter;
    private Filter nextFilter;
    private HttpRequest httpRequest;

    @Before
    public void setUp() {
        httpRequest = mock(HttpRequest.class);
        nextFilter = mock(Filter.class);
        knabFailureFilter = new KnabFailureFilter();
    }

    @Test
    public void shouldThrowBankSideFailureWhenStatusIsForbidden() {
        // given
        final String responseBody =
                "{ \"httpCode\":\"403\", \"httpMessage\":\"Forbidden\", \"moreInformation\":\"Forbidden\" }";
        HttpResponse mockedResponse = FilterTestHelperUtility.mockResponse(403, responseBody);

        // when
        when(mockedResponse.getBody(String.class)).thenReturn(responseBody);
        knabFailureFilter.setNext(nextFilter);
        when(nextFilter.handle(any(HttpRequest.class))).thenReturn(mockedResponse);

        // then
        Throwable t = catchThrowable(() -> knabFailureFilter.handle(httpRequest));
        assertThat(t)
                .isInstanceOf(BankServiceException.class)
                .hasMessage("Code status : 403. Error body : " + responseBody);
    }

    @Test
    public void shouldReturnHttpResponseIfThereIfResponseIsEmpty() {
        // given
        HttpResponse mockedResponse = mock(HttpResponse.class);

        // when
        knabFailureFilter.setNext(nextFilter);
        when(nextFilter.handle(any(HttpRequest.class))).thenReturn(mockedResponse);

        // then
        assertEquals(knabFailureFilter.handle(httpRequest), mockedResponse);
    }
}

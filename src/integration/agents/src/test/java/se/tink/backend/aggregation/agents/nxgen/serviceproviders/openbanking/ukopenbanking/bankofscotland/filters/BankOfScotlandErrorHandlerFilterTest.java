package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.bankofscotland.filters;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.aggregation.agents.exceptions.refresh.AccountRefreshException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.AccountBalanceEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.rpc.ErrorResponse;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.bankofscotland.filters.BankOfScotlandErrorHandlerFilter;
import se.tink.backend.aggregation.nxgen.http.HttpRequestImpl;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpMethod;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class BankOfScotlandErrorHandlerFilterTest {

    private BankOfScotlandErrorHandlerFilter errorHandlerFilter;
    private HttpResponse response = Mockito.mock(HttpResponse.class);
    private Filter nextFilter = mock(Filter.class);

    @Before
    public void setUp() {
        errorHandlerFilter = new BankOfScotlandErrorHandlerFilter();
    }

    @Test
    public void shouldHandleCancelledAccountResponse() throws Exception {
        // given
        ErrorResponse responseBody =
                objectFromString(
                        "{\"Code\":\"403 Forbidden\",\"Message\":\"Forbidden\",\"Errors\":[{\"ErrorCode\":\"UK.OBIE.Resource.NotFound\",\"Message\":\"Forbidden - Account closed or suspended\"}]}",
                        ErrorResponse.class);
        given(response.getStatus()).willReturn(403);
        given(response.getBody(ErrorResponse.class)).willReturn(responseBody);

        // when
        when(nextFilter.handle(any())).thenReturn(response);
        errorHandlerFilter.setNext(nextFilter);

        // then
        Assertions.assertThatCode(() -> errorHandlerFilter.handle(null))
                .isInstanceOf(AccountRefreshException.class)
                .hasMessage("Account closed or suspended");
    }

    @Test
    public void shouldReturnHttpResponseWhenNoCodeErrorFound() throws Exception {
        // given
        ErrorResponse responseBody =
                objectFromString(
                        "{\"Code\":\"403 Forbidden\",\"Message\":\"Forbidden\",\"Errors\":[{\"ErrorCode\":\"undefined\",\"Message\":\"unknown error\"}]}",
                        ErrorResponse.class);
        given(response.getStatus()).willReturn(403);
        given(response.getBody(ErrorResponse.class)).willReturn(responseBody);

        // when
        when(nextFilter.handle(any())).thenReturn(response);
        errorHandlerFilter.setNext(nextFilter);

        // then
        assertThat(errorHandlerFilter.handle(new HttpRequestImpl(HttpMethod.POST, new URL(""))))
                .isNotNull()
                .isInstanceOf(HttpResponse.class);
    }

    @Test
    public void shouldNotUseFilterWhenHappyPathHappen() throws JsonProcessingException {
        // given
        String bodyToParse =
                "{\"AccountId\":\"00cbdfe0-00c0-00ea-bb00-00000000d00c\",\"Amount\":{\"Amount\":\"65.29\",\"Currency\":\"GBP\"},\"CreditDebitIndicator\":\"Credit\",\"Type\":\"InterimBooked\",\"DateTime\":\"2021-09-01T09:53:15Z\",\"CreditLine\":[{\"Included\":false}]}";
        AccountBalanceEntity responseBody =
                objectFromString(bodyToParse, AccountBalanceEntity.class);
        given(response.getStatus()).willReturn(200);
        given(response.getBody(AccountBalanceEntity.class)).willReturn(responseBody);

        // when
        when(nextFilter.handle(any())).thenReturn(response);
        errorHandlerFilter.setNext(nextFilter);

        // then
        assertThatCode(
                        () ->
                                errorHandlerFilter.handle(
                                        new HttpRequestImpl(HttpMethod.POST, new URL(""))))
                .doesNotThrowAnyException();
    }

    private <T> T objectFromString(String jsonString, Class<T> clazz)
            throws JsonProcessingException {
        return new ObjectMapper().readValue(jsonString, clazz);
    }
}

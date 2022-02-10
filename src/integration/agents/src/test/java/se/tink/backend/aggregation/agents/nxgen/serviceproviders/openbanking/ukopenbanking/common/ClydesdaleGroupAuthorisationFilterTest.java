package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.AccountBalanceEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.rpc.UkObErrorResponse;
import se.tink.backend.aggregation.nxgen.http.HttpRequestImpl;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpMethod;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class ClydesdaleGroupAuthorisationFilterTest {

    private ClydesdaleGroupAuthorisationFilter errorHandlerFilter;
    private HttpResponse response = mock(HttpResponse.class);
    private Filter nextFilter = mock(Filter.class);

    @Before
    public void setUp() {
        errorHandlerFilter = new ClydesdaleGroupAuthorisationFilter();
    }

    @Test
    public void shouldHandleAuthenticationErrorAccountResponse() throws Exception {
        // given
        UkObErrorResponse responseBody =
                objectFromString(
                        "{\"Code\":\"403 Forbidden\",\"Id\":\"00000000-6f9a-483a-76ba-000000000000\",\"Message\":\"Forbidden\",\"Errors\":[{\"ErrorCode\":\"UK.CYBG.Forbidden\",\"Message\":\"Forbidden\"}]}",
                        UkObErrorResponse.class);
        given(response.getStatus()).willReturn(403);
        given(response.getBody(UkObErrorResponse.class)).willReturn(responseBody);
        given(nextFilter.handle(any())).willReturn(response);

        // when
        errorHandlerFilter.setNext(nextFilter);

        // then
        Assertions.assertThatCode(() -> errorHandlerFilter.handle(null))
                .isInstanceOf(BankServiceException.class);
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
        given(nextFilter.handle(any())).willReturn(response);

        // when
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

package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.barclays.filter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.rpc.ErrorResponse;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.barclays.filter.BarclaysInvalidDataFilter;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

@RunWith(MockitoJUnitRunner.class)
public class BarclaysInvalidDataFilterTest {

    BarclaysInvalidDataFilter objectUnderTest = new BarclaysInvalidDataFilter();
    @Mock HttpResponse response;
    @Mock Filter nextFilter;

    @Test
    public void shouldThrowBankServiceException() throws IOException {
        // given
        ErrorResponse errorResponse =
                new ObjectMapper()
                        .readValue(
                                "{\"Code\":\"400 Bad Request\",\"Id\":\"2ecd8abb-66a8-48bb-828f-8c17d88a395b\","
                                        + "\"Message\":\"Request cannot be fulfilled. \",\"Errors\":[{"
                                        + "\"ErrorCode\":\"UK.OBIE.Field.Unexpected\","
                                        + "\"Message\":\"Accounts request could not be processed due to invalid data. \"}]}",
                                ErrorResponse.class);
        given(response.getStatus()).willReturn(400);
        given(response.getBody(ErrorResponse.class)).willReturn(errorResponse);

        when(nextFilter.handle(any())).thenReturn(response);
        objectUnderTest.setNext(nextFilter);

        // expected
        Assertions.assertThatCode(() -> objectUnderTest.handle(null))
                .isInstanceOf(BankServiceException.class)
                .hasMessage("Cause: BankServiceError.DEFAULT_MESSAGE");
    }

    @Test
    public void shouldNotThrowExceptionWhenStatusDoesNotMatch() throws IOException {
        // given
        ErrorResponse errorResponse =
                new ObjectMapper()
                        .readValue(
                                "{\"Code\":\"200 Bad Request\",\"Id\":\"2ecd8abb-66a8-48bb-828f-8c17d88a395b\","
                                        + "\"Message\":\"Request cannot be fulfilled. \",\"Errors\":[{"
                                        + "\"ErrorCode\":\"UK.OBIE.Field.Unexpected\","
                                        + "\"Message\":\"Accounts request could not be processed due to invalid data. \"}]}",
                                ErrorResponse.class);
        given(response.getStatus()).willReturn(200);
        given(response.getBody(ErrorResponse.class)).willReturn(errorResponse);

        when(nextFilter.handle(any())).thenReturn(response);
        objectUnderTest.setNext(nextFilter);

        // expected
        Assertions.assertThatCode(() -> objectUnderTest.handle(null)).doesNotThrowAnyException();
    }

    @Test
    public void shouldNotThrowExceptionWhenStatusWhenCodeDoesNotMatch() throws IOException {
        // given
        ErrorResponse errorResponse =
                new ObjectMapper()
                        .readValue(
                                "{\"Code\":\"400 Bad Request\",\"Id\":\"2ecd8abb-66a8-48bb-828f-8c17d88a395b\","
                                        + "\"Message\":\"Request cannot be fulfilled. \",\"Errors\":[{\""
                                        + "ErrorCode\":\"UK.OBIE.DUMMY.CODE\","
                                        + "\"Message\":\"Accounts request could not be processed due to invalid data. \"}]}",
                                ErrorResponse.class);
        given(response.getStatus()).willReturn(400);
        given(response.getBody(ErrorResponse.class)).willReturn(errorResponse);

        when(nextFilter.handle(any())).thenReturn(response);
        objectUnderTest.setNext(nextFilter);

        // expected
        Assertions.assertThatCode(() -> objectUnderTest.handle(null)).doesNotThrowAnyException();
    }

    @Test
    public void shouldNotThrowExceptionWhenStatusWhenMesssageDoesNotMatch() throws IOException {
        // given
        ErrorResponse errorResponse =
                new ObjectMapper()
                        .readValue(
                                "{\"Code\":\"400 Bad Request\",\"Id\":\"2ecd8abb-66a8-48bb-828f-8c17d88a395b\","
                                        + "\"Message\":\"Request cannot be fulfilled. \",\"Errors\":[{"
                                        + "\"ErrorCode\":\"UK.OBIE.Field.Unexpected\","
                                        + "\"Message\":\"Dummy error message.\"}]}",
                                ErrorResponse.class);
        given(response.getStatus()).willReturn(400);
        given(response.getBody(ErrorResponse.class)).willReturn(errorResponse);

        when(nextFilter.handle(any())).thenReturn(response);
        objectUnderTest.setNext(nextFilter);

        // expected
        Assertions.assertThatCode(() -> objectUnderTest.handle(null)).doesNotThrowAnyException();
    }
}

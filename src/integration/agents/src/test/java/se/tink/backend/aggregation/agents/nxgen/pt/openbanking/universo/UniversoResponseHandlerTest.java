package se.tink.backend.aggregation.agents.nxgen.pt.openbanking.universo;

import com.google.common.collect.Lists;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.fetcher.entities.ErrorEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.fetcher.rpc.ErrorResponse;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

@RunWith(JUnitParamsRunner.class)
public class UniversoResponseHandlerTest {

    @Mock private HttpResponse response;
    @Mock private HttpRequest request;
    @Mock private ErrorResponse errorResponse;
    @Mock private ErrorEntity errorEntity;

    private final UniversoResponseHandler universoResponseHandler = new UniversoResponseHandler();

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void shouldMapServiceBlockedErrorToBankSideError() {
        // given
        bankReturns403ErrorWithServiceBlockedCode();

        // expect
        Assertions.assertThatThrownBy(
                        () -> universoResponseHandler.handleResponse(request, response))
                .isInstanceOf(BankServiceException.class)
                .hasFieldOrPropertyWithValue("error", BankServiceError.BANK_SIDE_FAILURE);
    }

    @Test
    public void shouldMapBackgroundRefreshNumberExceededErrorToBankSideError() {
        // given
        bankReturns429ErrorWithAccessExceededCode();

        // expect
        Assertions.assertThatThrownBy(
                        () -> universoResponseHandler.handleResponse(request, response))
                .isInstanceOf(BankServiceException.class)
                .hasFieldOrPropertyWithValue("error", BankServiceError.ACCESS_EXCEEDED);
    }

    @Test
    public void shouldDoNothingWhenStatus200() {
        // given
        Mockito.when(response.getStatus()).thenReturn(200);

        // when
        Throwable throwable =
                Assertions.catchThrowable(
                        () -> universoResponseHandler.handleResponse(request, response));

        // then
        Assertions.assertThat(throwable).isNull();
    }

    @Test
    @Parameters({"403", "429"})
    public void shouldThrowGeneralHttpExceptionWhenErrorIsNotHandled(int statusCode) {
        // given
        bankReturnsErrorWithEmptyTppMessages(statusCode);

        // expect
        Assertions.assertThatThrownBy(
                        () -> universoResponseHandler.handleResponse(request, response))
                .isInstanceOf(HttpResponseException.class);
    }

    @Test
    @Parameters({"403", "429"})
    public void shouldThrowGeneralHttpExceptionWhenResponseHasNoBody(int statusCode) {
        // given
        bankReturnsErrorWithNoBody(statusCode);

        // expect
        Assertions.assertThatThrownBy(
                        () -> universoResponseHandler.handleResponse(request, response))
                .isInstanceOf(HttpResponseException.class);
    }

    @Test
    @Parameters({"403", "429"})
    public void shouldThrowGeneralHttpExceptionWhenTppMessageIsNull(int statusCode) {
        // given
        bankReturnsErrorWithNullTppMessages(statusCode);

        // expect
        Assertions.assertThatThrownBy(
                        () -> universoResponseHandler.handleResponse(request, response))
                .isInstanceOf(HttpResponseException.class);
    }

    private void bankReturns403ErrorWithServiceBlockedCode() {
        Mockito.when(errorEntity.getCode()).thenReturn("SERVICE_BLOCKED");
        Mockito.when(response.getStatus()).thenReturn(403);
        Mockito.when(response.hasBody()).thenReturn(true);
        Mockito.when(response.getBody(ErrorResponse.class)).thenReturn(errorResponse);
        Mockito.when(errorResponse.getTppMessages()).thenReturn(Lists.newArrayList(errorEntity));
    }

    private void bankReturns429ErrorWithAccessExceededCode() {
        Mockito.when(errorEntity.getCode()).thenReturn("ACCESS_EXCEEDED");
        Mockito.when(response.getStatus()).thenReturn(429);
        Mockito.when(response.hasBody()).thenReturn(true);
        Mockito.when(response.getBody(ErrorResponse.class)).thenReturn(errorResponse);
        Mockito.when(errorResponse.getTppMessages()).thenReturn(Lists.newArrayList(errorEntity));
    }

    private void bankReturnsErrorWithEmptyTppMessages(int statusCode) {
        Mockito.when(errorResponse.getTppMessages()).thenReturn(Lists.newArrayList());
        Mockito.when(response.getStatus()).thenReturn(statusCode);
        Mockito.when(response.hasBody()).thenReturn(true);
        Mockito.when(response.getBody(ErrorResponse.class)).thenReturn(errorResponse);
    }

    private void bankReturnsErrorWithNoBody(int statusCode) {
        Mockito.when(response.getStatus()).thenReturn(statusCode);
        Mockito.when(response.hasBody()).thenReturn(false);
    }

    private void bankReturnsErrorWithNullTppMessages(int statusCode) {
        Mockito.when(errorResponse.getTppMessages()).thenReturn(null);
        Mockito.when(response.getStatus()).thenReturn(statusCode);
        Mockito.when(response.hasBody()).thenReturn(true);
        Mockito.when(response.getBody(ErrorResponse.class)).thenReturn(errorResponse);
    }
}

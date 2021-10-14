package se.tink.backend.aggregation.agents.nxgen.pt.openbanking.universo;

import com.google.common.collect.Lists;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.fetcher.entities.ErrorEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.fetcher.rpc.ErrorResponse;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

@RunWith(MockitoJUnitRunner.class)
public class UniversoResponseHandlerTest {

    @Mock private HttpRequest request;

    @Mock private HttpResponse response;

    @Mock private ErrorResponse errorResponse;

    @Mock private ErrorEntity errorEntity;

    private UniversoResponseHandler objectUnderTest = new UniversoResponseHandler();

    @Test
    public void shouldMapServiceBlockedErrorToBankSideError() {
        // given
        Mockito.when(errorEntity.getCategory()).thenReturn("ERROR");
        Mockito.when(errorEntity.getCode()).thenReturn("SERVICE_BLOCKED");
        Mockito.when(response.getStatus()).thenReturn(403);
        Mockito.when(errorResponse.getTppMessages()).thenReturn(Lists.newArrayList(errorEntity));
        Mockito.when(response.getBody(ErrorResponse.class)).thenReturn(errorResponse);

        // when
        Throwable throwable =
                Assertions.catchThrowable(() -> objectUnderTest.handleResponse(request, response));

        // then
        Assertions.assertThat(throwable).isInstanceOf(BankServiceException.class);
        BankServiceException ex = (BankServiceException) throwable;
        Assertions.assertThat(ex.getError()).isEqualTo(BankServiceError.BANK_SIDE_FAILURE);
    }

    @Test
    public void shouldDoNothingWhenStatus200() {
        // given
        Mockito.when(response.getStatus()).thenReturn(200);

        // when
        Throwable throwable =
                Assertions.catchThrowable(() -> objectUnderTest.handleResponse(request, response));

        // then
        Assertions.assertThat(throwable).isNull();
    }

    @Test
    public void shouldThrowGeneralHttpExceptionWhenErrorIsNotHandled() {
        // given
        Mockito.when(response.getStatus()).thenReturn(403);
        Mockito.when(errorResponse.getTppMessages()).thenReturn(Lists.newArrayList(errorEntity));
        Mockito.when(errorResponse.getTppMessages()).thenReturn(Lists.newArrayList(errorEntity));
        Mockito.when(response.getBody(ErrorResponse.class)).thenReturn(errorResponse);

        // when
        Throwable throwable =
                Assertions.catchThrowable(() -> objectUnderTest.handleResponse(request, response));

        // then
        Assertions.assertThat(throwable).isInstanceOf(HttpResponseException.class);
    }
}

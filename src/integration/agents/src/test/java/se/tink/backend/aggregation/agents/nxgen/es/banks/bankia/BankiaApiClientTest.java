package se.tink.backend.aggregation.agents.nxgen.es.banks.bankia;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.vavr.control.Either;
import javax.ws.rs.core.MediaType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.RequestFactory.Scope;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.loan.rpc.LoanDetailsErrorCode;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.loan.rpc.LoanDetailsRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.loan.rpc.LoanDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.rpc.ErrorResponse;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@RunWith(MockitoJUnitRunner.class)
public class BankiaApiClientTest {
    public static final String PRODUCT_CODE = "product_code";
    public static final String LOAN_IDENTIFIER = "loan_identifier";
    public static final String UNKNOWN_EXTERNAL_ERROR_CODE = "111";
    @Mock private TinkHttpClient client;
    @Mock private PersistentStorage persistentStorage;
    @Mock private RequestFactory requestFactory;
    @InjectMocks private BankiaApiClient bankiaApiClient;

    @Test
    public void shouldGetLoanDetails() {
        // given
        LoanDetailsRequest request =
                new LoanDetailsRequest(PRODUCT_CODE).setLoanIdentifier(LOAN_IDENTIFIER);

        RequestBuilder requestBuilder = mock(RequestBuilder.class);
        when(requestFactory.create(Scope.WITH_SESSION, request.getURL()))
                .thenReturn(requestBuilder);
        when(requestBuilder.body(request, MediaType.APPLICATION_JSON)).thenReturn(requestBuilder);
        when(requestBuilder.post(LoanDetailsResponse.class))
                .thenReturn(mock(LoanDetailsResponse.class));

        // when
        Either<LoanDetailsErrorCode, LoanDetailsResponse> loanDetails =
                bankiaApiClient.getLoanDetails(request);

        // then
        assertThat(loanDetails.isRight()).isTrue();
    }

    @Test
    public void
            shouldMapErrorCodeAsUnknownErrorWhenTheExceptionIsDifferentThanHttpResponseException() {
        // given
        LoanDetailsRequest request =
                new LoanDetailsRequest(PRODUCT_CODE).setLoanIdentifier(LOAN_IDENTIFIER);

        RequestBuilder requestBuilder = mock(RequestBuilder.class);
        when(requestFactory.create(Scope.WITH_SESSION, request.getURL()))
                .thenReturn(requestBuilder);
        when(requestBuilder.body(request, MediaType.APPLICATION_JSON)).thenReturn(requestBuilder);
        when(requestBuilder.post(LoanDetailsResponse.class)).thenThrow(new RuntimeException());

        // when
        Either<LoanDetailsErrorCode, LoanDetailsResponse> loanDetails =
                bankiaApiClient.getLoanDetails(request);

        // then
        assertThat(loanDetails.isLeft()).isTrue();
        assertThat(loanDetails.getLeft()).isEqualTo(LoanDetailsErrorCode.UNKNOWN_ERROR);
    }

    @Test
    public void
            shouldMapErrorCodeAsUnknownErrorWhenTheExceptionHasBeenThrownWithUnknownExternalErrorCode() {
        // given
        LoanDetailsRequest request =
                new LoanDetailsRequest(PRODUCT_CODE).setLoanIdentifier(LOAN_IDENTIFIER);

        RequestBuilder requestBuilder = mock(RequestBuilder.class);
        when(requestFactory.create(Scope.WITH_SESSION, request.getURL()))
                .thenReturn(requestBuilder);
        when(requestBuilder.body(request, MediaType.APPLICATION_JSON)).thenReturn(requestBuilder);
        HttpResponse response = mock(HttpResponse.class);
        when(requestBuilder.post(LoanDetailsResponse.class))
                .thenThrow(new HttpResponseException(null, response));

        ErrorResponse errorResponse = mock(ErrorResponse.class);
        when(response.getBody(ErrorResponse.class)).thenReturn(errorResponse);
        when(errorResponse.getOperationResult()).thenReturn(UNKNOWN_EXTERNAL_ERROR_CODE);

        // when
        Either<LoanDetailsErrorCode, LoanDetailsResponse> loanDetails =
                bankiaApiClient.getLoanDetails(request);

        // then
        assertThat(loanDetails.isLeft()).isTrue();
        assertThat(loanDetails.getLeft()).isEqualTo(LoanDetailsErrorCode.UNKNOWN_ERROR);
    }

    @Test
    public void
            shouldMapErrorCodeAsNotExistWhenTheExceptionHasBeenThrownWithProperExternalErrorCode() {
        // given
        LoanDetailsRequest request =
                new LoanDetailsRequest(PRODUCT_CODE).setLoanIdentifier(LOAN_IDENTIFIER);

        RequestBuilder requestBuilder = mock(RequestBuilder.class);
        when(requestFactory.create(Scope.WITH_SESSION, request.getURL()))
                .thenReturn(requestBuilder);
        when(requestBuilder.body(request, MediaType.APPLICATION_JSON)).thenReturn(requestBuilder);
        HttpResponse response = mock(HttpResponse.class);
        when(requestBuilder.post(LoanDetailsResponse.class))
                .thenThrow(new HttpResponseException(null, response));

        ErrorResponse errorResponse = mock(ErrorResponse.class);
        when(response.getBody(ErrorResponse.class)).thenReturn(errorResponse);
        when(errorResponse.getOperationResult())
                .thenReturn(LoanDetailsErrorCode.NOT_EXISTS.getErrorCodeValue());

        // when
        Either<LoanDetailsErrorCode, LoanDetailsResponse> loanDetails =
                bankiaApiClient.getLoanDetails(request);

        // then
        assertThat(loanDetails.isLeft()).isTrue();
        assertThat(loanDetails.getLeft()).isEqualTo(LoanDetailsErrorCode.NOT_EXISTS);
    }
}

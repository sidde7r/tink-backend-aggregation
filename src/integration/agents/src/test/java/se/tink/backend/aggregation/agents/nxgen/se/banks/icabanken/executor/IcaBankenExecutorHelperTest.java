package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.executor;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.aggregation.agents.contexts.CompositeAgentContext;
import se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.IcaBankenApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.executor.rpc.TransferRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.transfer.rpc.TransferResponse;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.libraries.i18n_aggregation.Catalog;

public class IcaBankenExecutorHelperTest {
    private final int ICABANKEN_UNKNOWN_ERROR_CODE = 1000;

    private IcaBankenApiClient apiClient;
    private CompositeAgentContext context;
    private Catalog catalog;
    private SupplementalInformationController supplementalInformationController;

    private IcaBankenExecutorHelper helper;

    @Before
    public void setup() {
        apiClient = mock(IcaBankenApiClient.class);
        context = mock(CompositeAgentContext.class);
        catalog = mock(Catalog.class);
        supplementalInformationController = mock(SupplementalInformationController.class);
    }

    @Test
    public void putTransferInOutboxTest() {
        // given
        TransferRequest transferRequest = mock(TransferRequest.class);

        when(apiClient.putAssignmentInOutbox(any(TransferRequest.class)))
                .thenReturn(mock(TransferResponse.class));

        helper =
                new IcaBankenExecutorHelper(
                        apiClient, context, catalog, supplementalInformationController);

        // when
        helper.putTransferInOutbox(transferRequest);

        // then
        verify(apiClient, times(1)).putAssignmentInOutbox(transferRequest);
    }

    @Test(expected = TransferExecutionException.class)
    public void putTransferInOutboxTestShouldThrowTransferExecutionExceptionWhenConflict() {
        // given
        TransferRequest transferRequest = mock(TransferRequest.class);

        HttpResponseException conflictException =
                getMockedResponseExceptionWithMessageError(HttpStatus.SC_CONFLICT);

        when(apiClient.putAssignmentInOutbox(any(TransferRequest.class)))
                .thenThrow(conflictException);

        helper =
                new IcaBankenExecutorHelper(
                        apiClient, context, catalog, supplementalInformationController);

        // when then
        helper.putTransferInOutbox(transferRequest);
    }

    @Test(expected = HttpResponseException.class)
    public void putTransferInOutboxTestShouldThrowHttpResponseExceptionWhenUnknownError() {
        // given
        TransferRequest transferRequest = mock(TransferRequest.class);

        HttpResponseException conflictException =
                getMockedResponseExceptionWithMessageError(HttpStatus.SC_INTERNAL_SERVER_ERROR);

        when(apiClient.putAssignmentInOutbox(any(TransferRequest.class)))
                .thenThrow(conflictException);

        helper =
                new IcaBankenExecutorHelper(
                        apiClient, context, catalog, supplementalInformationController);

        // when then
        helper.putTransferInOutbox(transferRequest);
    }

    private HttpResponseException getMockedResponseExceptionWithMessageError(int statusCode) {
        HttpResponse httpResponse = mock(HttpResponse.class);
        when(httpResponse.getStatus()).thenReturn(statusCode);

        TransferResponse mockResponse = mock(TransferResponse.class, Mockito.RETURNS_DEEP_STUBS);
        when(mockResponse.getResponseStatus().getCode()).thenReturn(ICABANKEN_UNKNOWN_ERROR_CODE);
        when(mockResponse.getResponseStatus().getClientMessage()).thenReturn("error message");
        when(httpResponse.getBody(TransferResponse.class)).thenReturn(mockResponse);

        return new HttpResponseException(mock(HttpRequest.class), httpResponse);
    }
}

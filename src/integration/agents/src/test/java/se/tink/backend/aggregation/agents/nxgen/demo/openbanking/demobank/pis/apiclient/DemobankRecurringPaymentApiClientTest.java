package se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.pis.apiclient;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.pis.DemobankDtoMappers;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.pis.apiclient.error.DemobankErrorHandler;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.pis.storage.DemobankStorage;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.transfer.rpc.Frequency;

public class DemobankRecurringPaymentApiClientTest {

    private final DemobankDtoMappers mappers = mock(DemobankDtoMappers.class);
    private final DemobankErrorHandler demobankErrorHandler = mock(DemobankErrorHandler.class);
    private final DemobankPaymentRequestFilter requestFilter =
            mock(DemobankPaymentRequestFilter.class);
    private final DemobankStorage storage = mock(DemobankStorage.class);
    private final TinkHttpClient client = mock(TinkHttpClient.class);
    private final String callbackUri = "localhost";

    private final DemobankRecurringPaymentApiClient apiClient =
            new DemobankRecurringPaymentApiClient(
                    mappers, demobankErrorHandler, requestFilter, storage, client, callbackUri);

    @Test(expected = PaymentException.class)
    public void remap_exception_on_http_exception() throws PaymentException {
        // given
        PaymentRequest paymentRequest = mock(PaymentRequest.class);
        Payment payment = mock(Payment.class, RETURNS_DEEP_STUBS);
        when(payment.getStartDate()).thenReturn(LocalDate.MIN);
        when(payment.getEndDate()).thenReturn(LocalDate.MAX);
        when(payment.getFrequency()).thenReturn(Frequency.MONTHLY);
        when(paymentRequest.getPayment()).thenReturn(payment);

        // and
        HttpResponseException e = mock(HttpResponseException.class);
        when(client.request(any(String.class))).thenThrow(e);

        // and
        doThrow(new PaymentException("error")).when(demobankErrorHandler).remapException(e);

        // when
        apiClient.createPayment(paymentRequest);
    }
}

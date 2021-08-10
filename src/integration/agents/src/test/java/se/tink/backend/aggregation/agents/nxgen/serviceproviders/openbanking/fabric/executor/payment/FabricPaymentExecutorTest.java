package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.executor.payment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.file.Paths;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.FabricApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.executor.payment.rpc.FabricPaymentResponse;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.MockRandomValueGenerator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.serialization.utils.SerializationUtils;
import se.tink.libraries.transfer.rpc.PaymentServiceType;

@RunWith(JUnitParamsRunner.class)
public class FabricPaymentExecutorTest {

    private static final String TEST_DATA_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/fabric/executor/payment/resources/";

    @Test
    @Parameters
    public void
            when_deleting_recurring_payment_response_should_have_proper_status_depending_on_response(
                    String fileName, PaymentStatus paymentStatus) {
        // given
        URL url =
                new URL(
                        "https://psdgw-sella.fabrick.com/api/fabrick/psd2/v1/periodic-payments/sepa-credit-transfers/paymentId");
        FabricPaymentExecutor paymentExecutor = createFabricPaymentExecutor(fileName, url);

        // when
        PaymentResponse paymentResponse =
                paymentExecutor.delete(
                        new PaymentRequest(
                                new Payment.Builder()
                                        .withPaymentServiceType(PaymentServiceType.PERIODIC)
                                        .withUniqueId("paymentId")
                                        .build()));

        // then
        assertThat(paymentResponse.getPayment().getStatus()).isEqualTo(paymentStatus);
    }

    private FabricPaymentExecutor createFabricPaymentExecutor(String fileName, URL url) {
        TinkHttpClient tinkHttpClient = createTinkHttpClient(fileName, url);

        FabricApiClient apiClient =
                new FabricApiClient(
                        tinkHttpClient,
                        new PersistentStorage(),
                        new MockRandomValueGenerator(),
                        new SessionStorage(),
                        "userIp",
                        "baseUrl");
        return new FabricPaymentExecutor(
                apiClient,
                mock(SupplementalInformationHelper.class),
                new SessionStorage(),
                new StrongAuthenticationState("state"));
    }

    private TinkHttpClient createTinkHttpClient(String fileName, URL url) {
        TinkHttpClient tinkHttpClient = mock(TinkHttpClient.class);
        RequestBuilder requestBuilder = mock(RequestBuilder.class);
        when(tinkHttpClient.request(url)).thenReturn(requestBuilder);
        when(requestBuilder.header(any(String.class), any(Object.class)))
                .thenReturn(requestBuilder);
        when(requestBuilder.type(any(String.class))).thenReturn(requestBuilder);
        when(requestBuilder.delete(FabricPaymentResponse.class))
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                Paths.get(TEST_DATA_PATH, fileName).toFile(),
                                FabricPaymentResponse.class));
        return tinkHttpClient;
    }

    private Object
            parametersForWhen_deleting_recurring_payment_response_should_have_proper_status_depending_on_response() {
        return new Object[] {
            new Object[] {"delete_payment_response_200_cancelled.json", PaymentStatus.CANCELLED},
            new Object[] {"delete_payment_response_202_accepted.json", PaymentStatus.PENDING}
        };
    }
}

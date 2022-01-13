package se.tink.backend.aggregation.agents.utils.berlingroup.payment.helper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.payment.rpc.Payment.Builder;
import se.tink.libraries.payments.common.model.PaymentScheme;
import se.tink.libraries.transfer.rpc.PaymentServiceType;

public class PaymentUrlUtilTest {

    private static final URL INPUT_TEST_URL_WITH_PARAMS =
            new URL(
                    "https://www.example.com/v1/{payment-service}/{payment-product}/{paymentId}/status");
    private static final URL INPUT_TEST_URL_WITHOUT_PARAMS =
            new URL("https://www.example.com/v1/1/2/3/status");

    @Test
    public void shouldNotReplaceAnythingIfPaymentHasNoUsefulData() {
        // given
        PaymentRequest paymentRequest = new PaymentRequest(new Builder().build());

        // when
        URL url =
                PaymentUrlUtil.fillCommonPaymentParams(INPUT_TEST_URL_WITH_PARAMS, paymentRequest);

        // then
        assertThat(url).isEqualTo(INPUT_TEST_URL_WITH_PARAMS);
    }

    @Test
    public void shouldNotReplaceAnythingIfNoProperParamsInUrl() {
        // given
        PaymentRequest paymentRequest =
                new PaymentRequest(
                        new Builder()
                                .withUniqueId("uniqueId1234")
                                .withPaymentScheme(PaymentScheme.SEPA_CREDIT_TRANSFER)
                                .withPaymentServiceType(PaymentServiceType.SINGLE)
                                .build());

        // when
        URL url =
                PaymentUrlUtil.fillCommonPaymentParams(
                        INPUT_TEST_URL_WITHOUT_PARAMS, paymentRequest);

        // then
        assertThat(url).isEqualTo(INPUT_TEST_URL_WITHOUT_PARAMS);
    }

    @Test
    public void shouldReplaceThingsCorrectlyForSinglePayment() {
        // given
        PaymentRequest paymentRequest =
                new PaymentRequest(
                        new Builder()
                                .withUniqueId("uniqueId1234")
                                .withPaymentScheme(PaymentScheme.SEPA_CREDIT_TRANSFER)
                                .withPaymentServiceType(PaymentServiceType.SINGLE)
                                .build());

        // when
        URL url =
                PaymentUrlUtil.fillCommonPaymentParams(INPUT_TEST_URL_WITH_PARAMS, paymentRequest);

        // then
        assertThat(url.toString())
                .isEqualTo(
                        "https://www.example.com/v1/payments/sepa-credit-transfers/uniqueId1234/status");
    }

    @Test
    public void shouldReplaceThingsCorrectlyForRecurringPayment() {
        // given
        PaymentRequest paymentRequest =
                new PaymentRequest(
                        new Builder()
                                .withUniqueId("uniqueId1234")
                                .withPaymentScheme(PaymentScheme.SEPA_INSTANT_CREDIT_TRANSFER)
                                .withPaymentServiceType(PaymentServiceType.PERIODIC)
                                .build());

        // when
        URL url =
                PaymentUrlUtil.fillCommonPaymentParams(INPUT_TEST_URL_WITH_PARAMS, paymentRequest);

        // then
        assertThat(url.toString())
                .isEqualTo(
                        "https://www.example.com/v1/periodic-payments/instant-sepa-credit-transfers/uniqueId1234/status");
    }
}

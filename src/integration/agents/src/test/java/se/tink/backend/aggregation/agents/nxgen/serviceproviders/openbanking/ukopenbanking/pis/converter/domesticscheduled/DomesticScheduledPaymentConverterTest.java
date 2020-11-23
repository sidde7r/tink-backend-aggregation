package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.converter.domesticscheduled;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentTestFixtures.createDomesticScheduledPaymentConsentResponse;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentTestFixtures.createDomesticScheduledPaymentResponse;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentTestFixtures.createPaymentResponse;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentTestFixtures.createPaymentResponseForConsent;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.converter.common.ConverterTestBase;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.dto.domesticscheduled.DomesticScheduledPaymentConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.dto.domesticscheduled.DomesticScheduledPaymentResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;

public class DomesticScheduledPaymentConverterTest extends ConverterTestBase {

    private DomesticScheduledPaymentConverter domesticScheduledPaymentConverter;

    @Before
    public void setUp() {
        domesticScheduledPaymentConverter = new DomesticScheduledPaymentConverter();
    }

    @Test
    public void shouldConvertConsentResponseDtoToTinkPaymentResponse() {
        // given
        final DomesticScheduledPaymentConsentResponse responseMock =
                createDomesticScheduledPaymentConsentResponse();

        // when
        final PaymentResponse returned =
                domesticScheduledPaymentConverter.convertConsentResponseDtoToTinkPaymentResponse(
                        responseMock);

        // then
        final PaymentResponse expected = createPaymentResponseForConsent();

        validatePaymentResponsesAreEqual(returned, expected);
    }

    @Test
    public void shouldConvertResponseDtoToPaymentResponse() {
        // given
        final DomesticScheduledPaymentResponse responseMock =
                createDomesticScheduledPaymentResponse();

        // when
        final PaymentResponse returned =
                domesticScheduledPaymentConverter.convertResponseDtoToPaymentResponse(responseMock);

        // then
        final PaymentResponse expected = createPaymentResponse();

        validatePaymentResponsesAreEqual(returned, expected);
    }

    private static void validatePaymentResponsesAreEqual(
            PaymentResponse returned, PaymentResponse expected) {
        validatePaymentsAreEqual(returned.getPayment(), expected.getPayment());
        assertThat(returned.getPayment().getExecutionDate())
                .isEqualTo(expected.getPayment().getExecutionDate());
        assertThat(returned.getStorage()).isEqualTo(expected.getStorage());
    }
}

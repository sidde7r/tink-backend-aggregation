package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.domesticscheduled.converter;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentTestFixtures.createDomesticScheduledPaymentConsentResponse;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentTestFixtures.createDomesticScheduledPaymentResponse;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentTestFixtures.createPaymentResponse;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentTestFixtures.createPaymentResponseForConsent;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingTestValidator.validatePaymentResponsesForDomesticScheduledPaymentsAreEqual;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.domesticscheduled.dto.DomesticScheduledPaymentConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.domesticscheduled.dto.DomesticScheduledPaymentResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;

public class DomesticScheduledPaymentConverterTest {

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

        validatePaymentResponsesForDomesticScheduledPaymentsAreEqual(returned, expected);
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

        validatePaymentResponsesForDomesticScheduledPaymentsAreEqual(returned, expected);
    }
}

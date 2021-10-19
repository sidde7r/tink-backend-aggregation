package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.validator;

import static org.mockito.BDDMockito.given;

import java.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentValidationException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.libraries.payment.enums.PaymentType;
import se.tink.libraries.payments.common.model.PaymentScheme;

public class FrCreatePaymentRequestValidatorTest {

    private LocalDateTimeSource localDateTimeSource;
    private FrCreatePaymentRequestValidator validator;

    @Before
    public void setUp() {
        localDateTimeSource = Mockito.mock(LocalDateTimeSource.class);
        validator = new FrCreatePaymentRequestValidator(localDateTimeSource);
    }

    @Test(expected = PaymentValidationException.class)
    public void shouldThrowExceptionWhenExecutionDateIsNotTodayForInstantPayment()
            throws PaymentValidationException {
        // given
        given(localDateTimeSource.now()).willReturn(LocalDateTime.of(2020, 1, 2, 13, 30, 5));

        CreatePaymentRequest createPaymentRequest =
                new CreatePaymentRequest.Builder()
                        .withPaymentType(PaymentType.DOMESTIC)
                        .withPaymentScheme(PaymentScheme.SEPA_INSTANT_CREDIT_TRANSFER)
                        .withExecutionDate("2020-01-01T00:01:00.000+01:00")
                        .build();

        // when
        validator.validate(createPaymentRequest);
    }

    @Test
    public void shouldNotThrowAnyExceptionsWhenExecutionDateIsTodayForInstantPayment()
            throws PaymentValidationException {
        // given
        given(localDateTimeSource.now()).willReturn(LocalDateTime.of(2020, 1, 1, 13, 30, 5));

        CreatePaymentRequest createPaymentRequest =
                new CreatePaymentRequest.Builder()
                        .withPaymentType(PaymentType.DOMESTIC)
                        .withPaymentScheme(PaymentScheme.SEPA_INSTANT_CREDIT_TRANSFER)
                        .withExecutionDate("2020-01-01T00:01:00.000+01:00")
                        .build();

        // when
        validator.validate(createPaymentRequest);
    }

    @Test
    public void shouldNotThrowAnyExceptionsWhenExecutionDateIsNotTodayForNotInstantPayment()
            throws PaymentValidationException {
        // given
        given(localDateTimeSource.now()).willReturn(LocalDateTime.of(2020, 1, 2, 13, 30, 5));

        CreatePaymentRequest createPaymentRequest =
                new CreatePaymentRequest.Builder()
                        .withPaymentType(PaymentType.DOMESTIC)
                        .withPaymentScheme(PaymentScheme.SEPA_CREDIT_TRANSFER)
                        .withExecutionDate("2020-01-01T00:01:00.000+01:00")
                        .build();

        // when
        validator.validate(createPaymentRequest);
    }
}

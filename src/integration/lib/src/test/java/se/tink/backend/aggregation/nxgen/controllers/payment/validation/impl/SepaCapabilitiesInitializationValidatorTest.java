package se.tink.backend.aggregation.nxgen.controllers.payment.validation.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.catchThrowable;
import static se.tink.libraries.payments.common.model.PaymentScheme.SEPA_CREDIT_TRANSFER;
import static se.tink.libraries.payments.common.model.PaymentScheme.SEPA_INSTANT_CREDIT_TRANSFER;
import static se.tink.libraries.transfer.rpc.PaymentServiceType.PERIODIC;
import static se.tink.libraries.transfer.rpc.PaymentServiceType.SINGLE;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.backend.aggregation.agents.agent.Agent;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentValidationException;
import se.tink.libraries.enums.MarketCode;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.payments.common.model.PaymentScheme;
import se.tink.libraries.transfer.rpc.PaymentServiceType;

@RunWith(JUnitParamsRunner.class)
public class SepaCapabilitiesInitializationValidatorTest {

    private static final MarketCode TEST_MARKET_CODE = MarketCode.DE;

    private SepaCapabilitiesInitializationValidator sepaCapabilitiesInitializationValidator;

    @Before
    public void setup() {}

    @Test
    @Parameters(method = "classesAndPaymentsThatDoNotMatch")
    public void shouldThrowWhenServiceSchemeAndCapabilityDoNotMatch(
            Class<? extends Agent> agentClass, Payment payment) {
        // given
        sepaCapabilitiesInitializationValidator =
                new SepaCapabilitiesInitializationValidator(agentClass, TEST_MARKET_CODE);

        // when
        Throwable throwable =
                catchThrowable(
                        () ->
                                sepaCapabilitiesInitializationValidator
                                        .throwIfNotPossibleToInitialize(payment));

        // then
        assertThat(throwable)
                .isInstanceOf(PaymentValidationException.class)
                .hasMessage("Payment validation failed.")
                .extracting("internalStatus")
                .isEqualTo("INVALID_PAYMENT_TYPE");
    }

    private Object[] classesAndPaymentsThatDoNotMatch() {
        return new Object[] {
            new Object[] {
                InstantPayValidationTestAgent.class, buildTestPayemnt(SINGLE, SEPA_CREDIT_TRANSFER)
            },
            new Object[] {
                RecurringPayValidationTestAgent.class,
                buildTestPayemnt(SINGLE, SEPA_CREDIT_TRANSFER)
            },
            new Object[] {
                NormalPayValidationTestAgent.class,
                buildTestPayemnt(SINGLE, SEPA_INSTANT_CREDIT_TRANSFER)
            },
            new Object[] {
                RecurringPayValidationTestAgent.class,
                buildTestPayemnt(SINGLE, SEPA_INSTANT_CREDIT_TRANSFER)
            },
            new Object[] {
                InstantPayValidationTestAgent.class,
                buildTestPayemnt(PERIODIC, SEPA_CREDIT_TRANSFER)
            },
            new Object[] {
                SinglePayValidationTestAgent.class, buildTestPayemnt(PERIODIC, SEPA_CREDIT_TRANSFER)
            },
            new Object[] {
                NormalPayValidationTestAgent.class,
                buildTestPayemnt(PERIODIC, SEPA_INSTANT_CREDIT_TRANSFER)
            },
            new Object[] {
                SinglePayValidationTestAgent.class,
                buildTestPayemnt(PERIODIC, SEPA_INSTANT_CREDIT_TRANSFER)
            },
        };
    }

    @Test
    @Parameters(method = "classesAndPaymentsThatDoMatch")
    public void shouldPassWithoutExceptionWhenServiceSchemeAndCapabilityDoMatch(
            Class<? extends Agent> agentClass, Payment payment) {
        // given
        sepaCapabilitiesInitializationValidator =
                new SepaCapabilitiesInitializationValidator(agentClass, TEST_MARKET_CODE);

        // when & then
        assertThatCode(
                        () ->
                                sepaCapabilitiesInitializationValidator
                                        .throwIfNotPossibleToInitialize(payment))
                .doesNotThrowAnyException();
    }

    private Object[] classesAndPaymentsThatDoMatch() {
        return new Object[] {
            new Object[] {
                NormalPayValidationTestAgent.class, buildTestPayemnt(SINGLE, SEPA_CREDIT_TRANSFER)
            },
            new Object[] {
                SinglePayValidationTestAgent.class, buildTestPayemnt(SINGLE, SEPA_CREDIT_TRANSFER)
            },
            new Object[] {
                InstantPayValidationTestAgent.class,
                buildTestPayemnt(SINGLE, SEPA_INSTANT_CREDIT_TRANSFER)
            },
            new Object[] {
                SinglePayValidationTestAgent.class,
                buildTestPayemnt(SINGLE, SEPA_INSTANT_CREDIT_TRANSFER)
            },
            new Object[] {
                NormalPayValidationTestAgent.class, buildTestPayemnt(PERIODIC, SEPA_CREDIT_TRANSFER)
            },
            new Object[] {
                RecurringPayValidationTestAgent.class,
                buildTestPayemnt(PERIODIC, SEPA_CREDIT_TRANSFER)
            },
            new Object[] {
                InstantPayValidationTestAgent.class,
                buildTestPayemnt(PERIODIC, SEPA_INSTANT_CREDIT_TRANSFER)
            },
            new Object[] {
                RecurringPayValidationTestAgent.class,
                buildTestPayemnt(PERIODIC, SEPA_INSTANT_CREDIT_TRANSFER)
            },
        };
    }

    @Test
    @Parameters(method = "paymentsWithMissingFields")
    public void shouldPassWithoutExceptionWhenEitherSchemeOrTypeMissing(Payment payment) {
        // given
        sepaCapabilitiesInitializationValidator =
                new SepaCapabilitiesInitializationValidator(
                        NormalPayValidationTestAgent.class, TEST_MARKET_CODE);

        // when & then
        assertThatCode(
                        () ->
                                sepaCapabilitiesInitializationValidator
                                        .throwIfNotPossibleToInitialize(payment))
                .doesNotThrowAnyException();
    }

    private Object[] paymentsWithMissingFields() {
        return new Object[] {
            buildTestPayemnt(SINGLE, null), buildTestPayemnt(null, SEPA_CREDIT_TRANSFER)
        };
    }

    @Test
    @Parameters(method = "paymentsWithUnexpectedSchemes")
    public void shouldPassWithoutExceptionWhenPaymentWithNonSepaScheme(Payment payment) {
        // given
        sepaCapabilitiesInitializationValidator =
                new SepaCapabilitiesInitializationValidator(
                        NormalPayValidationTestAgent.class, TEST_MARKET_CODE);

        // when & then
        assertThatCode(
                        () ->
                                sepaCapabilitiesInitializationValidator
                                        .throwIfNotPossibleToInitialize(payment))
                .doesNotThrowAnyException();
    }

    private Object[] paymentsWithUnexpectedSchemes() {
        return new Object[] {
            buildTestPayemnt(SINGLE, PaymentScheme.FASTER_PAYMENTS),
            buildTestPayemnt(SINGLE, PaymentScheme.NORWEGIAN_DOMESTIC_CREDIT_TRANSFER),
            buildTestPayemnt(
                    SINGLE, PaymentScheme.INSTANT_NORWEGIAN_DOMESTIC_CREDIT_TRANSFER_STRAKS)
        };
    }

    @Test
    public void shouldPassWithoutExceptionWhenExecutedOnAgentWithoutAnyCapability() {
        // given
        sepaCapabilitiesInitializationValidator =
                new SepaCapabilitiesInitializationValidator(
                        ValidationTestAgent.class, TEST_MARKET_CODE);

        // when & then
        assertThatCode(
                        () ->
                                sepaCapabilitiesInitializationValidator
                                        .throwIfNotPossibleToInitialize(
                                                buildTestPayemnt(SINGLE, SEPA_CREDIT_TRANSFER)))
                .doesNotThrowAnyException();
    }

    private Payment buildTestPayemnt(PaymentServiceType serviceType, PaymentScheme scheme) {
        return new Payment.Builder()
                .withPaymentServiceType(serviceType)
                .withPaymentScheme(scheme)
                .build();
    }
}

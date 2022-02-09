package src.agent_sdk.compatibility_layers.aggregation_service.test.payments;

import java.time.LocalDate;
import java.util.List;
import org.junit.Ignore;
import org.mockito.Mockito;
import se.tink.agent.runtime.environment.OperationImpl;
import se.tink.agent.runtime.environment.UtilitiesImpl;
import se.tink.agent.runtime.instance.AgentInstance;
import se.tink.agent.runtime.models.payments.PaymentImpl;
import se.tink.agent.runtime.test.utils.FakeRandomGeneratorImpl;
import se.tink.agent.runtime.test.utils.FakeSleeperImpl;
import se.tink.agent.runtime.test.utils.FakeTimeGeneratorImpl;
import se.tink.agent.sdk.environment.Operation;
import se.tink.agent.sdk.environment.Utilities;
import se.tink.agent.sdk.models.payments.payment.Creditor;
import se.tink.agent.sdk.models.payments.payment.Debtor;
import se.tink.agent.sdk.models.payments.payment.Payment;
import se.tink.agent.sdk.models.payments.payment.PaymentType;
import se.tink.agent.sdk.models.payments.payment.RemittanceInformation;
import se.tink.agent.sdk.models.payments.payment.RemittanceInformationType;
import se.tink.agent.sdk.models.payments.payment_reference.PaymentReference;
import se.tink.agent.sdk.storage.SerializableStorage;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import src.agent_sdk.compatibility_layers.aggregation_service.src.payments.BulkPaymentInitiation;
import src.agent_sdk.compatibility_layers.aggregation_service.src.payments.report.PaymentInitiationReport;

@Ignore
public final class PaymentInitiationTestHelper {
    public static final Utilities UTILITIES =
            new UtilitiesImpl(
                    new FakeRandomGeneratorImpl(),
                    new FakeTimeGeneratorImpl(),
                    new FakeSleeperImpl(),
                    null,
                    null,
                    null);
    public static final Operation OPERATION =
            new OperationImpl(new SerializableStorage(), null, null, null, null, null, null);

    // The tests will not utilize/test supplemental info.
    public static final SupplementalInformationController SUPPLEMENTAL_INFORMATION_CONTROLLER =
            Mockito.mock(SupplementalInformationController.class);

    public static final Debtor DEBTOR_1 = new Debtor(new IbanIdentifier("SE1234567890"));

    public static final Creditor CREDITOR_ALICE =
            new Creditor(new IbanIdentifier("FR1282828282"), "Alice");

    public static final Creditor CREDITOR_BOB =
            new Creditor(new IbanIdentifier("DE442123551231"), "Bob");

    public static final Creditor CREDITOR_EVE =
            new Creditor(new IbanIdentifier("IT800128371723"), "Eve");

    public static final Payment PAYMENT_1 =
            new PaymentImpl(
                    "pay-1",
                    PaymentType.SEPA_CREDIT_TRANSFER,
                    DEBTOR_1,
                    "Debtor message",
                    CREDITOR_ALICE,
                    ExactCurrencyAmount.inEUR(123),
                    new RemittanceInformation(RemittanceInformationType.REFERENCE, "some-ref-1"),
                    LocalDate.of(2021, 3, 14));

    public static final PaymentReference PAYMENT_1_REF =
            PaymentReference.builder().payment(PAYMENT_1).noBankReference().build();

    public static final Payment PAYMENT_2 =
            new PaymentImpl(
                    "pay-2",
                    PaymentType.SEPA_CREDIT_TRANSFER,
                    DEBTOR_1,
                    "Debtor message",
                    CREDITOR_BOB,
                    ExactCurrencyAmount.inEUR(123),
                    new RemittanceInformation(RemittanceInformationType.REFERENCE, "some-ref-2"),
                    LocalDate.of(2021, 3, 14));

    public static final PaymentReference PAYMENT_2_REF =
            PaymentReference.builder().payment(PAYMENT_2).noBankReference().build();

    public static final Payment PAYMENT_3 =
            new PaymentImpl(
                    "pay-3",
                    PaymentType.SEPA_CREDIT_TRANSFER,
                    DEBTOR_1,
                    "Debtor message",
                    CREDITOR_EVE,
                    ExactCurrencyAmount.inEUR(123),
                    new RemittanceInformation(RemittanceInformationType.REFERENCE, "some-ref-3"),
                    LocalDate.of(2021, 3, 14));

    public static final PaymentReference PAYMENT_3_REF =
            PaymentReference.builder().payment(PAYMENT_3).noBankReference().build();

    public static PaymentInitiationReport initiateBulkPayments(
            Object agent, List<Payment> payments) {
        AgentInstance agentInstance =
                AgentInstance.createFromInstance(agent.getClass(), agent, OPERATION, UTILITIES);

        BulkPaymentInitiation bulkPaymentInitiation =
                new BulkPaymentInitiation(SUPPLEMENTAL_INFORMATION_CONTROLLER, agentInstance);

        return bulkPaymentInitiation.initiateBulkPayments(payments);
    }
}

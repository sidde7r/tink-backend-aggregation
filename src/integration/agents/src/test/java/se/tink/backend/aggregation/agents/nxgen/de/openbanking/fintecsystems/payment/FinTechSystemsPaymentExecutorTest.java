package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fintecsystems.payment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.PersistentLogin;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fintecsystems.TestDataReader;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintecsystems.FinTecSystemsApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintecsystems.FinTecSystemsStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintecsystems.payment.FinTechSystemsPaymentExecutor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintecsystems.payment.rpc.FinTechSystemsPayment;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintecsystems.payment.rpc.FinTechSystemsPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintecsystems.payment.rpc.FinTechSystemsSession;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.payments.common.model.PaymentScheme;
import se.tink.libraries.transfer.enums.RemittanceInformationType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;

public class FinTechSystemsPaymentExecutorTest {

    private static final String TEST_REDIRECT_URL = "https://example.com";

    private static final String TEST_TRANSACTION_ID = "1234567890qwertyuiop";

    private static final String CREDITOR_IBAN = "DE65500105176282491145";
    private static final String DEBTOR_IBAN = "DE62888888880012345678";

    private FinTecSystemsApiClient mockApiClient = mock(FinTecSystemsApiClient.class);
    private SupplementalInformationHelper mockSupplementalInformationHelper =
            mock(SupplementalInformationHelper.class);
    private StrongAuthenticationState mockStrongAuthenticationState =
            mock(StrongAuthenticationState.class);

    private FinTecSystemsStorage mockStorage = mock(FinTecSystemsStorage.class);
    private PersistentLogin mockPersistentAgent = mock(PersistentLogin.class);

    private FinTechSystemsPaymentExecutor finTechSystemsPaymentExecutor;

    @Before
    public void setup() {
        finTechSystemsPaymentExecutor =
                new FinTechSystemsPaymentExecutor(
                        mockApiClient,
                        mockSupplementalInformationHelper,
                        mockStrongAuthenticationState,
                        TEST_REDIRECT_URL,
                        mockStorage,
                        mockPersistentAgent);
    }

    @Test
    public void shouldFillUniqueIdAfterPaymentCreate() {
        // given
        PaymentRequest paymentRequest = new PaymentRequest(buildTestPayment());
        when(mockApiClient.createPayment(paymentRequest))
                .thenReturn(
                        TestDataReader.readFromFile(
                                TestDataReader.CREATE_OK, FinTechSystemsPaymentResponse.class));

        // when
        PaymentResponse createResponse = finTechSystemsPaymentExecutor.create(paymentRequest);

        // then
        assertThat(createResponse.getPayment().getCreditor().getName()).isEqualTo("Creditor Name");
        assertThat(createResponse.getPayment().getCreditor().getAccountIdentifier().getIdentifier())
                .isEqualTo(CREDITOR_IBAN);
        assertThat(createResponse.getPayment().getExactCurrencyAmount())
                .isEqualTo(ExactCurrencyAmount.inEUR(1));
        assertThat(createResponse.getPayment().getCurrency()).isEqualTo("EUR");
        assertThat(createResponse.getPayment().getRemittanceInformation().getType())
                .isEqualTo(RemittanceInformationType.UNSTRUCTURED);
        assertThat(createResponse.getPayment().getRemittanceInformation().getValue())
                .isEqualTo("ReferenceToCreditor");
    }

    @Test
    public void shouldFillDebtorAfterSuccessfulPayment() {
        // given
        PaymentMultiStepRequest paymentMultiStepRequest =
                new PaymentMultiStepRequest(
                        buildTestPaymentWithUniqueId(), new SessionStorage(), null, null);

        when(mockApiClient.fetchSessionStatus(TEST_TRANSACTION_ID))
                .thenReturn(
                        TestDataReader.readFromFile(
                                TestDataReader.SESSION_OK, FinTechSystemsSession.class));
        when(mockApiClient.fetchPaymentStatus(paymentMultiStepRequest))
                .thenReturn(
                        TestDataReader.readFromFile(
                                TestDataReader.PAYMENT_OK, FinTechSystemsPayment.class));

        // when
        PaymentMultiStepResponse signResponse =
                finTechSystemsPaymentExecutor.sign(paymentMultiStepRequest);

        // then
        assertThat(signResponse.getPayment().getCreditor().getName()).isEqualTo("Creditor Name");
        assertThat(signResponse.getPayment().getCreditor().getAccountIdentifier().getIdentifier())
                .isEqualTo(CREDITOR_IBAN);
        assertThat(signResponse.getPayment().getDebtor().getAccountIdentifier())
                .isInstanceOf(IbanIdentifier.class);
        assertThat(signResponse.getPayment().getDebtor().getAccountIdentifier().getIdentifier())
                .isEqualTo(DEBTOR_IBAN);
        assertThat(signResponse.getPayment().getExactCurrencyAmount())
                .isEqualTo(ExactCurrencyAmount.inEUR(1));
        assertThat(signResponse.getPayment().getCurrency()).isEqualTo("EUR");
        assertThat(signResponse.getPayment().getRemittanceInformation().getType())
                .isEqualTo(RemittanceInformationType.UNSTRUCTURED);
        assertThat(signResponse.getPayment().getRemittanceInformation().getValue())
                .isEqualTo("ReferenceToCreditor");
    }

    private Payment buildTestPayment() {
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setValue("ReferenceToCreditor");
        remittanceInformation.setType(RemittanceInformationType.UNSTRUCTURED);

        Creditor creditor = new Creditor(new IbanIdentifier(CREDITOR_IBAN), "Creditor Name");

        return new Payment.Builder()
                .withCreditor(creditor)
                .withExactCurrencyAmount(ExactCurrencyAmount.inEUR(1))
                .withCurrency("EUR")
                .withRemittanceInformation(remittanceInformation)
                .withPaymentScheme(PaymentScheme.SEPA_CREDIT_TRANSFER)
                .build();
    }

    private Payment buildTestPaymentWithUniqueId() {
        Payment payment = buildTestPayment();
        payment.setUniqueId(TEST_TRANSACTION_ID);
        return payment;
    }
}

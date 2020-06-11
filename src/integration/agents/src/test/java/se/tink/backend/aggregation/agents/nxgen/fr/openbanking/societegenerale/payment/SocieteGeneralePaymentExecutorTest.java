package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.payment;

import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Collections;
import java.util.UUID;
import org.assertj.core.api.Assertions;
import org.iban4j.CountryCode;
import org.iban4j.Iban;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentRejectedException;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.SocieteGeneraleConstants;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.apiclient.SocieteGeneraleApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.executor.payment.SocieteGeneralePaymentExecutor;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.executor.payment.entities.BeneficiaryEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.executor.payment.entities.ConsentApprovalEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.executor.payment.entities.CreditTransferTransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.executor.payment.entities.PaymentEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.executor.payment.entities.PaymentInformationStatusCodeEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.executor.payment.entities.PaymentRequestLinkEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.executor.payment.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.executor.payment.rpc.GetPaymentResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepConstants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.payment.rpc.Reference;

public class SocieteGeneralePaymentExecutorTest {

    private SocieteGeneraleApiClient apiClient;

    private SessionStorage sessionStorage;

    private SupplementalInformationHelper supplementalInformationHelper;
    private StrongAuthenticationState strongAuthenticationState;

    private final String AUTHENTICATION_URL = "DUMMY_AUTHENTICATION_URL";
    private SocieteGeneralePaymentExecutor paymentExecutor;
    private GetPaymentResponse paymentResponse;

    @Before
    public void init() {
        apiClient = mock(SocieteGeneraleApiClient.class);
        sessionStorage = mock(SessionStorage.class);
        supplementalInformationHelper = mock(SupplementalInformationHelper.class);
        strongAuthenticationState = mock(StrongAuthenticationState.class);
        paymentResponse = mock(GetPaymentResponse.class);
        paymentExecutor =
                new SocieteGeneralePaymentExecutor(
                        apiClient,
                        AUTHENTICATION_URL,
                        sessionStorage,
                        supplementalInformationHelper,
                        strongAuthenticationState);
    }

    @Test
    public void verifyCreateShouldCallApiClientAndReturnPaymentResponse() throws PaymentException {
        PaymentRequest paymentRequest = createDomesticPayment();

        when(apiClient.createPayment(any()))
                .thenReturn(
                        new CreatePaymentResponse(
                                new PaymentRequestLinkEntity(
                                        new ConsentApprovalEntity(AUTHENTICATION_URL))));

        // when
        PaymentResponse paymentResponse = paymentExecutor.create(paymentRequest);

        // then
        Assertions.assertThat(paymentResponse.getPayment().getStatus())
                .isEqualTo(PaymentStatus.CREATED);
        verify(sessionStorage, times(1))
                .put(SocieteGeneraleConstants.StorageKeys.AUTH_URL, AUTHENTICATION_URL);
        verify(apiClient, times(1)).createPayment(any());
    }

    @Test
    public void verifySignShouldOpenThirdPartyAppOnInit() throws PaymentException {
        // given
        PaymentMultiStepRequest paymentRequest =
                new PaymentMultiStepRequest(
                        mock(Payment.class),
                        sessionStorage,
                        AuthenticationStepConstants.STEP_INIT,
                        Collections.emptyList(),
                        Collections.emptyList());

        when(sessionStorage.get(SocieteGeneraleConstants.StorageKeys.AUTH_URL))
                .thenReturn(AUTHENTICATION_URL);

        // when
        PaymentMultiStepResponse response = paymentExecutor.sign(paymentRequest);

        // then
        Assertions.assertThat(response.getStep())
                .isEqualTo(SocieteGeneraleConstants.PaymentSteps.POST_SIGN_STEP);
        verify(sessionStorage, times(1)).get(SocieteGeneraleConstants.StorageKeys.AUTH_URL);
        verify(supplementalInformationHelper, times(1)).openThirdPartyApp(any());
    }

    @Test
    public void verifySignShouldVerifyPaymentStatusOnPostSign() throws PaymentException {
        // given
        PaymentRequest paymentRequest = createDomesticPayment();
        PaymentMultiStepRequest paymentMultiStepRequest =
                new PaymentMultiStepRequest(
                        mock(Payment.class),
                        sessionStorage,
                        SocieteGeneraleConstants.PaymentSteps.POST_SIGN_STEP,
                        Collections.emptyList(),
                        Collections.emptyList());

        PaymentInformationStatusCodeEntity status =
                PaymentInformationStatusCodeEntity.valueOf("ACSC");
        when(apiClient.getPaymentStatus(any()))
                .thenReturn(
                        new GetPaymentResponse(
                                new PaymentEntity(
                                        null,
                                        BeneficiaryEntity.of(paymentRequest),
                                        status,
                                        null,
                                        CreditTransferTransactionEntity.of(paymentRequest),
                                        null)));
        // when
        PaymentMultiStepResponse response = paymentExecutor.sign(paymentMultiStepRequest);

        // then
        Assertions.assertThat(response.getStep())
                .isEqualTo(SocieteGeneraleConstants.PaymentSteps.CONFIRM_PAYMENT_STEP);
        verify(apiClient, times(1)).getPaymentStatus(any());
    }

    @Test
    public void verifySignShouldConfirmPaymentOnConfirmSign() throws PaymentException {
        // given
        PaymentRequest paymentRequest = createDomesticPayment();
        PaymentMultiStepRequest paymentMultiStepRequest =
                new PaymentMultiStepRequest(
                        mock(Payment.class),
                        sessionStorage,
                        SocieteGeneraleConstants.PaymentSteps.CONFIRM_PAYMENT_STEP,
                        Collections.emptyList(),
                        Collections.emptyList());

        PaymentInformationStatusCodeEntity status =
                PaymentInformationStatusCodeEntity.valueOf("ACSC");
        when(apiClient.confirmPayment(any()))
                .thenReturn(
                        new GetPaymentResponse(
                                new PaymentEntity(
                                        null,
                                        BeneficiaryEntity.of(paymentRequest),
                                        status,
                                        null,
                                        CreditTransferTransactionEntity.of(paymentRequest),
                                        null)));
        // when
        PaymentMultiStepResponse response = paymentExecutor.sign(paymentMultiStepRequest);

        // then
        Assertions.assertThat(response.getStep())
                .isEqualTo(AuthenticationStepConstants.STEP_FINALIZE);
        verify(apiClient, times(1)).confirmPayment(null);
    }

    @Test
    public void VerifySignShouldThrowExceptionIfPaymentIsPending() {
        // given
        PaymentRequest paymentRequest = createDomesticPayment();
        PaymentMultiStepRequest paymentMultiStepRequest =
                new PaymentMultiStepRequest(
                        mock(Payment.class),
                        sessionStorage,
                        SocieteGeneraleConstants.PaymentSteps.POST_SIGN_STEP,
                        Collections.emptyList(),
                        Collections.emptyList());

        PaymentInformationStatusCodeEntity status =
                PaymentInformationStatusCodeEntity.valueOf("ACTC");
        when(apiClient.getPaymentStatus(any()))
                .thenReturn(
                        new GetPaymentResponse(
                                new PaymentEntity(
                                        null,
                                        BeneficiaryEntity.of(paymentRequest),
                                        status,
                                        null,
                                        CreditTransferTransactionEntity.of(paymentRequest),
                                        null)));

        // when
        Throwable thrown = catchThrowable(() -> paymentExecutor.sign(paymentMultiStepRequest));

        // then
        Assertions.assertThat(thrown).isInstanceOf(PaymentAuthenticationException.class);
        verify(apiClient, times(1)).getPaymentStatus(any());
    }

    @Test
    public void VerifySignShouldThrowExceptionIfPaymentIsRejected() {
        // given
        PaymentRequest paymentRequest = createDomesticPayment();
        PaymentMultiStepRequest paymentMultiStepRequest =
                new PaymentMultiStepRequest(
                        mock(Payment.class),
                        sessionStorage,
                        SocieteGeneraleConstants.PaymentSteps.POST_SIGN_STEP,
                        Collections.emptyList(),
                        Collections.emptyList());

        PaymentInformationStatusCodeEntity status =
                PaymentInformationStatusCodeEntity.valueOf("RJCT");
        when(apiClient.getPaymentStatus(any()))
                .thenReturn(
                        new GetPaymentResponse(
                                new PaymentEntity(
                                        null,
                                        BeneficiaryEntity.of(paymentRequest),
                                        status,
                                        null,
                                        CreditTransferTransactionEntity.of(paymentRequest),
                                        null)));

        // when
        Throwable thrown = catchThrowable(() -> paymentExecutor.sign(paymentMultiStepRequest));

        // then
        Assertions.assertThat(thrown).isInstanceOf(PaymentRejectedException.class);
        verify(apiClient, times(1)).getPaymentStatus(any());
    }

    private PaymentRequest createDomesticPayment() {
        Iban sourceIban = new Iban.Builder().countryCode(CountryCode.FR).buildRandom();
        Iban destinationIban = new Iban.Builder().countryCode(CountryCode.FR).buildRandom();
        AccountIdentifier creditorAccountIdentifier = new IbanIdentifier(sourceIban.toString());
        Creditor creditor = new Creditor(creditorAccountIdentifier, "Creditor Name");

        AccountIdentifier debtorAccountIdentifier = new IbanIdentifier(destinationIban.toString());
        Debtor debtor = new Debtor(debtorAccountIdentifier);

        Reference reference = new Reference("Message", "ReferenceToCreditor");

        Amount amount = Amount.inEUR(1);
        LocalDate executionDate = LocalDate.now();
        return new PaymentRequest(
                new Payment.Builder()
                        .withCreditor(creditor)
                        .withDebtor(debtor)
                        .withAmount(amount)
                        .withExecutionDate(executionDate)
                        .withReference(reference)
                        .withUniqueId(UUID.randomUUID().toString())
                        .build());
    }
}

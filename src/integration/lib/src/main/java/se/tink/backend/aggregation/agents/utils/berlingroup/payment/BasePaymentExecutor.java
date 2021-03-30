package se.tink.backend.aggregation.agents.utils.berlingroup.payment;

import static java.util.Objects.nonNull;

import java.util.Collections;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentCancelledException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentRejectedException;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.entities.AccountEntity;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.entities.AmountEntity;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.enums.PaymentAuthenticationMode;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.rpc.CreateRecurringPaymentRequest;
import se.tink.backend.aggregation.agents.utils.remittanceinformation.RemittanceInformationValidator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepConstants;
import se.tink.backend.aggregation.nxgen.controllers.payment.CreateBeneficiaryMultiStepRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.CreateBeneficiaryMultiStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.FetchablePaymentExecutor;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentExecutor;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentListRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentListResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.transfer.enums.RemittanceInformationType;
import se.tink.libraries.transfer.rpc.PaymentServiceType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;

@AllArgsConstructor
@Slf4j
public class BasePaymentExecutor implements PaymentExecutor, FetchablePaymentExecutor {

    private final PaymentApiClient apiClient;
    private final PaymentAuthenticator authenticator;
    private final Credentials credentials;
    private final PaymentAuthenticationMode paymentAuthenticationMode;

    @Override
    public PaymentResponse create(PaymentRequest paymentRequest) throws PaymentException {

        CreatePaymentRequest createPaymentRequest;

        if (PaymentServiceType.PERIODIC.equals(
                paymentRequest.getPayment().getPaymentServiceType())) {
            createPaymentRequest = getCreateRecurringPaymentRequest(paymentRequest.getPayment());
        } else {
            createPaymentRequest = getCreatePaymentRequest(paymentRequest.getPayment());
        }

        CreatePaymentResponse createPaymentResponse =
                apiClient.createPayment(createPaymentRequest, paymentRequest);

        // for EMBEDDED authenticator is required. For REDIRECT no need of Authenticator
        if (paymentAuthenticationMode.equals(PaymentAuthenticationMode.EMBEDDED)) {
            authenticator.authenticatePayment(credentials, createPaymentResponse);
        }

        return createPaymentResponse.toTinkPayment(paymentRequest.getPayment());
    }

    @Override
    public PaymentMultiStepResponse sign(PaymentMultiStepRequest paymentMultiStepRequest)
            throws PaymentException, AuthenticationException {

        Payment payment = paymentMultiStepRequest.getPayment();

        PaymentResponse paymentResponse = fetch(paymentMultiStepRequest);
        PaymentStatus paymentStatus = paymentResponse.getPayment().getStatus();
        log.info("Payment id={} sign status={}", payment.getId(), paymentStatus);

        switch (paymentStatus) {
            case SIGNED:
            case PAID:
                return new PaymentMultiStepResponse(
                        paymentResponse,
                        AuthenticationStepConstants.STEP_FINALIZE,
                        Collections.emptyList());
            case REJECTED:
                throw new PaymentRejectedException("Payment rejected by Bank");
            case CANCELLED:
                throw new PaymentCancelledException("Payment Cancelled by PSU");

            default:
                log.error("Payment was not signed even after waiting for SCA");
                throw new PaymentAuthorizationException();
        }
    }

    @Override
    public PaymentResponse fetch(PaymentRequest paymentRequest) {
        return apiClient
                .fetchPaymentStatus(paymentRequest)
                .toTinkPayment(paymentRequest.getPayment());
    }

    @Override
    public CreateBeneficiaryMultiStepResponse createBeneficiary(
            CreateBeneficiaryMultiStepRequest createBeneficiaryMultiStepRequest) {
        throw new NotImplementedException(
                "createBeneficiary not yet implemented for " + this.getClass().getName());
    }

    @Override
    public PaymentResponse cancel(PaymentRequest paymentRequest) {
        throw new NotImplementedException(
                "cancel not yet implemented for " + this.getClass().getName());
    }

    @Override
    public PaymentListResponse fetchMultiple(PaymentListRequest paymentListRequest) {
        throw new NotImplementedException(
                "fetchMultiple not yet implemented for " + this.getClass().getName());
    }

    private CreatePaymentRequest getCreatePaymentRequest(Payment payment) {

        return CreatePaymentRequest.builder()
                .creditorAccount(getAccountEntity(payment.getCreditor().getAccountNumber()))
                .debtorAccount(getAccountEntity(payment.getDebtor().getAccountNumber()))
                .instructedAmount(getAmountEntity(payment))
                .creditorName(payment.getCreditor().getName())
                .remittanceInformationUnstructured(getUnstructuredRemittance(payment))
                .requestedExecutionDate(payment.getExecutionDate())
                .build();
    }

    private CreatePaymentRequest getCreateRecurringPaymentRequest(Payment payment) {

        return CreateRecurringPaymentRequest.builder()
                .creditorAccount(getAccountEntity(payment.getCreditor().getAccountNumber()))
                .debtorAccount(getAccountEntity(payment.getDebtor().getAccountNumber()))
                .instructedAmount(getAmountEntity(payment))
                .creditorName(payment.getCreditor().getName())
                .remittanceInformationUnstructured(getUnstructuredRemittance(payment))
                .frequency(payment.getFrequency().toString())
                .startDate(payment.getStartDate())
                // optional attributes
                .endDate(payment.getEndDate())
                .executionRule(
                        payment.getExecutionRule() != null
                                ? payment.getExecutionRule().toString()
                                : null)
                .dayOfExecution(
                        nonNull(payment.getDayOfExecution())
                                ? String.valueOf(payment.getDayOfExecution())
                                : null)
                .build();
    }

    private AmountEntity getAmountEntity(Payment payment) {
        return new AmountEntity(
                String.valueOf(payment.getExactCurrencyAmount().getDoubleValue()),
                payment.getExactCurrencyAmount().getCurrencyCode());
    }

    private String getUnstructuredRemittance(Payment payment) {
        RemittanceInformation remittanceInformation = payment.getRemittanceInformation();

        RemittanceInformationValidator.validateSupportedRemittanceInformationTypesOrThrow(
                remittanceInformation, null, RemittanceInformationType.UNSTRUCTURED);

        return Optional.ofNullable(remittanceInformation.getValue()).orElse("");
    }

    private AccountEntity getAccountEntity(String accountNumber) {
        return new AccountEntity(accountNumber);
    }
}

package se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.executor.payment;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.payment.DuplicatePaymentException;
import se.tink.backend.aggregation.agents.exceptions.payment.InsufficientFundsException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentValidationException;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.AbancaApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.AbancaConstants.PaymentKeys;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.AbancaConstants.PaymentValues;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.AbancaConstants.ResponseErrorCodes;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.executor.payment.entity.AccountInfoEntity;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.executor.payment.entity.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.executor.payment.entity.PaymentAttributesEntity;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.executor.payment.entity.PaymentDataEntity;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.executor.payment.errors.ErrorEntity;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.executor.payment.errors.PaymentErrorEntity;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.executor.payment.rpc.SepaPaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.executor.payment.rpc.SepaPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.fetcher.transactionalaccount.entity.account.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.fetcher.transactionalaccount.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.utils.remittanceinformation.RemittanceInformationValidator;
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
import se.tink.backend.aggregation.nxgen.controllers.signing.SigningStepConstants;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.payments.common.model.PaymentScheme;
import se.tink.libraries.signableoperation.enums.InternalStatus;
import se.tink.libraries.transfer.enums.RemittanceInformationType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;

@Slf4j
public class AbancaPaymentExecutor implements PaymentExecutor, FetchablePaymentExecutor {

    private final AbancaApiClient apiClient;
    private final SessionStorage sessionStorage;

    public AbancaPaymentExecutor(AbancaApiClient apiClient, SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public PaymentResponse fetch(PaymentRequest paymentRequest) throws PaymentException {
        SepaPaymentResponse response = getPaymentFromSessionStorage();
        return response.toTinkPayment(paymentRequest.getPayment(), response);
    }

    @Override
    public PaymentListResponse fetchMultiple(PaymentListRequest paymentListRequest) {
        return new PaymentListResponse(
                paymentListRequest.getPaymentRequestList().stream()
                        .map(req -> new PaymentResponse(req.getPayment()))
                        .collect(Collectors.toList()));
    }

    @Override
    public PaymentResponse create(PaymentRequest paymentRequest) throws PaymentException {
        String message = "";
        Payment payment = paymentRequest.getPayment();

        String debtorAccountNumber = payment.getDebtor().getAccountNumber();
        String debtorAccountId = getAccount(debtorAccountNumber).getId();

        SepaPaymentRequest request = createPaymentRequest(payment);
        SepaPaymentResponse transferResponse;
        try {
            transferResponse = apiClient.createPayment(debtorAccountId, request);
        } catch (HttpResponseException e) {
            HttpResponse response = e.getResponse();
            final List<ErrorEntity> errors = response.getBody(PaymentErrorEntity.class).getErrors();

            if (Optional.ofNullable(errors.get(0)).isPresent()) {
                ErrorEntity errorEntity = errors.get(0);
                if (ResponseErrorCodes.ACCOUNT_BLOCKED_FOR_TRANSFER.equals(errorEntity.getCode())) {
                    throw new PaymentAuthorizationException(
                            InternalStatus.ACCOUNT_BLOCKED_FOR_TRANSFER);
                }
                if (ResponseErrorCodes.DUPLICATED_TRANSFER.equals(errorEntity.getCode())) {
                    throw new DuplicatePaymentException();
                }
                if (ResponseErrorCodes.INSUFFICIENT_BALANCE_ERROR.equals(errorEntity.getCode())) {
                    throw new InsufficientFundsException();
                }
                message =
                        String.format(
                                "Error message: httpStatus: %s, code: %s, title: %s, meta: %s",
                                response.getStatus(),
                                errorEntity.getCode(),
                                errorEntity.getTitle(),
                                errorEntity.getDetails());
                log.warn(message);
            }
            throw new PaymentException(message, InternalStatus.BANK_ERROR_CODE_NOT_HANDLED_YET);
        }
        addToSessionStorage(transferResponse);
        return transferResponse.toTinkPayment(payment, transferResponse);
    }

    /**
     * This method always returns signed payments because Abanca does not require additional
     * confirmation after creating a payment.
     */
    @Override
    public PaymentMultiStepResponse sign(PaymentMultiStepRequest paymentMultiStepRequest)
            throws PaymentException, AuthenticationException {
        final Payment payment = paymentMultiStepRequest.getPayment();
        payment.setStatus(PaymentStatus.PAID);

        return new PaymentMultiStepResponse(payment, SigningStepConstants.STEP_FINALIZE);
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

    private SepaPaymentRequest createPaymentRequest(Payment payment) {
        RemittanceInformation remittanceInformation = payment.getRemittanceInformation();
        RemittanceInformationValidator.validateSupportedRemittanceInformationTypesOrThrow(
                remittanceInformation, null, RemittanceInformationType.UNSTRUCTURED);

        return new SepaPaymentRequest(
                new PaymentDataEntity(
                        PaymentKeys.TRANSFER_REQUEST,
                        PaymentAttributesEntity.builder()
                                .remoteAccount(
                                        new AccountInfoEntity(
                                                payment.getCreditor().getAccountNumber(),
                                                AccountIdentifierType.IBAN.toString()))
                                .concept(remittanceInformation.getValue())
                                .amount(new AmountEntity(payment.getExactCurrencyAmount()))
                                .recipientName(payment.getCreditor().getName())
                                .operationType(getOperationType(payment.getPaymentScheme()))
                                .build()));
    }

    private String getOperationType(PaymentScheme paymentScheme) {
        if (!Optional.ofNullable(paymentScheme).isPresent()) {
            return PaymentValues.SEPA;
        }
        return paymentScheme.equals(PaymentScheme.SEPA_CREDIT_TRANSFER)
                ? PaymentValues.SEPA
                : PaymentValues.SEPA_INSTANT;
    }

    private AccountsResponse getAccounts() {
        return sessionStorage
                .get("ACCOUNTS", AccountsResponse.class)
                .orElseGet(apiClient::fetchAccounts);
    }

    private AccountEntity getAccount(String debtorAccountNumber) throws PaymentValidationException {
        return findDebtorAccount(debtorAccountNumber)
                .orElseThrow(
                        () ->
                                new PaymentValidationException(
                                        String.format(
                                                "Account with number %s is unavailable for you. Please try again.",
                                                debtorAccountNumber)));
    }

    private Optional<AccountEntity> findDebtorAccount(String debtorAccountNumber) {
        return getAccounts().getData().stream()
                .filter(
                        accountEntity ->
                                accountEntity
                                        .getAttributes()
                                        .getIdentifier()
                                        .getNumber()
                                        .equals(debtorAccountNumber.trim()))
                .findFirst();
    }

    private void addToSessionStorage(SepaPaymentResponse paymentResponse) {
        sessionStorage.put(PaymentKeys.PAYMENT, paymentResponse);
    }

    private SepaPaymentResponse getPaymentFromSessionStorage() throws PaymentException {
        return sessionStorage
                .get(PaymentKeys.PAYMENT, SepaPaymentResponse.class)
                .orElseThrow(() -> new PaymentException("Payment not found"));
    }
}

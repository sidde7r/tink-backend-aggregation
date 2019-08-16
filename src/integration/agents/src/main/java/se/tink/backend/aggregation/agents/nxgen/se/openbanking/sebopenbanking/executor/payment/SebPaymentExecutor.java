package se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.executor.payment;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.contexts.SupplementalRequester;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.BankIdException;
import se.tink.backend.aggregation.agents.exceptions.errors.BankIdError;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.SebApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.SebConstants;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.SebConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.SebConstants.FormValues;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.executor.payment.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.executor.payment.entities.CreditorAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.executor.payment.entities.DebtorAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.executor.payment.entities.PaymentProduct;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.executor.payment.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.executor.payment.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.executor.payment.rpc.PaymentSigningRequest;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.executor.payment.rpc.PaymentStatusResponse;
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
import se.tink.backend.aggregation.nxgen.controllers.signing.Signer;
import se.tink.backend.aggregation.nxgen.controllers.signing.SigningStepConstants;
import se.tink.backend.aggregation.nxgen.controllers.signing.multifactor.bankid.BankIdSigningController;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;
import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.payment.enums.PaymentType;
import se.tink.libraries.payment.rpc.Payment;

public class SebPaymentExecutor implements PaymentExecutor, FetchablePaymentExecutor {

    private final SupplementalRequester supplementalRequester;
    private SebApiClient apiClient;

    public SebPaymentExecutor(SebApiClient apiClient, SupplementalRequester supplementalRequester) {
        this.apiClient = apiClient;
        this.supplementalRequester = supplementalRequester;
    }

    @Override
    public PaymentResponse create(PaymentRequest paymentRequest) {
        final Payment payment = paymentRequest.getPayment();
        final PaymentType type = getPaymentType(payment);

        final String paymentProduct =
                getPaymentProduct(type, payment.getCreditor().getAccountIdentifierType())
                        .getValue();

        final DebtorAccountEntity debtorAccountEntity = DebtorAccountEntity.of(paymentRequest);
        final CreditorAccountEntity creditorAccountEntity =
                CreditorAccountEntity.paymentProductsMapper
                        .get(PaymentProduct.fromString(paymentProduct))
                        .apply(payment.getCreditor().getAccountNumber());
        final AmountEntity amountEntity = AmountEntity.of(paymentRequest);
        final CreatePaymentRequest createPaymentRequest =
                new CreatePaymentRequest.Builder()
                        .withTemplateId(paymentProduct)
                        .withCreditorAccount(creditorAccountEntity)
                        .withCreditorAccountMessage(FormValues.CREDITORS_MESSAGE)
                        .withDebtorAccount(debtorAccountEntity)
                        .withDebtorrAccountMessage(FormValues.DEBTORS_MESSAGE)
                        .withExecutionDate(getCurrentDate())
                        .withAmount(amountEntity)
                        .withRemittanceInformationUnstructured(payment.getReference().getValue())
                        .withCreditorName(payment.getCreditor().getName())
                        .build();

        CreatePaymentResponse createPaymentResponse =
                apiClient.createPaymentInitiation(createPaymentRequest, paymentProduct);

        PaymentStatus paymentStatus =
                SebPaymentStatus.mapToTinkPaymentStatus(
                        SebPaymentStatus.fromString(
                                apiClient
                                        .getPaymentStatus(
                                                createPaymentResponse.getPaymentId(),
                                                paymentProduct)
                                        .getTransactionStatus()));

        return createPaymentResponse.toTinkPaymentResponse(paymentProduct, type, paymentStatus);
    }

    @Override
    public PaymentResponse fetch(PaymentRequest paymentRequest) {
        final Payment payment = paymentRequest.getPayment();
        final String paymentId = payment.getUniqueId();
        final PaymentType paymentType = payment.getType();
        final String paymentProduct =
                getPaymentProduct(paymentType, payment.getCreditor().getAccountIdentifierType())
                        .getValue();

        return apiClient
                .getPayment(paymentId, paymentProduct)
                .toTinkPaymentResponse(
                        paymentProduct, paymentId, paymentType, fetchStatus(paymentRequest));
    }

    @Override
    public PaymentMultiStepResponse sign(PaymentMultiStepRequest paymentMultiStepRequest) {
        PaymentStatus paymentStatus;
        String nextStep;
        final Payment payment = paymentMultiStepRequest.getPayment();
        final String paymentProduct =
                getPaymentProduct(
                                payment.getType(), payment.getCreditor().getAccountIdentifierType())
                        .getValue();
        final String paymentId = payment.getUniqueId();

        switch (paymentMultiStepRequest.getStep()) {
            case SigningStepConstants.STEP_INIT:
                PaymentStatusResponse paymentStatusResponse =
                        apiClient.getPaymentStatus(paymentId, paymentProduct);
                if (paymentStatusResponse.isReadyForSigning()) {
                    apiClient.signPayment(
                            paymentId,
                            paymentProduct,
                            new PaymentSigningRequest(FormValues.MOBILT_BANK_ID));
                    nextStep = SigningStepConstants.STEP_SIGN;
                    break;
                } else {
                    try {
                        // Waiting for payment to be ready to sign
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    return new PaymentMultiStepResponse(
                            payment, SigningStepConstants.STEP_INIT, new ArrayList<>());
                }
            case SigningStepConstants.STEP_SIGN:
                try {
                    getSigner().sign(paymentMultiStepRequest);
                } catch (AuthenticationException e) {
                    if (e instanceof BankIdException) {
                        BankIdError bankIdError = ((BankIdException) e).getError();
                        switch (bankIdError) {
                            case CANCELLED:
                                throw new IllegalStateException(
                                        "BankId signing cancelled by the user.", e);

                            case NO_CLIENT:
                                throw new IllegalStateException(
                                        "No BankId client when trying to sign the payment.", e);

                            case TIMEOUT:
                                throw new IllegalStateException("BankId signing timed out.", e);

                            case INTERRUPTED:
                                throw new IllegalStateException("BankId signing interrupded.", e);

                            case UNKNOWN:
                            default:
                                throw new IllegalStateException(
                                        "Unknown problem when signing payment with BankId.", e);
                        }
                    }
                }
                nextStep = SigningStepConstants.STEP_FINALIZE;
                break;

            default:
                throw new IllegalStateException(
                        String.format("Unknown step %s", paymentMultiStepRequest.getStep()));
        }
        paymentStatus =
                SebPaymentStatus.mapToTinkPaymentStatus(
                        SebPaymentStatus.fromString(
                                apiClient
                                        .getPaymentStatus(paymentId, paymentProduct)
                                        .getTransactionStatus()));
        payment.setStatus(paymentStatus);

        return new PaymentMultiStepResponse(payment, nextStep, new ArrayList<>());
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
        return new PaymentListResponse(
                paymentListRequest.getPaymentRequestList().stream()
                        .map(paymentRequest -> new PaymentResponse(paymentRequest.getPayment()))
                        .collect(Collectors.toList()));
    }

    private PaymentType getPaymentType(Payment payment) {
        return payment.getCreditor().getAccountIdentifierType().equals(Type.IBAN)
                        && !payment.getCreditor().getAccountNumber().startsWith(SebConstants.MARKET)
                ? PaymentType.SEPA
                : PaymentType.DOMESTIC;
    }

    private PaymentProduct getPaymentProduct(PaymentType paymentType, Type creditorAccountType) {
        switch (paymentType) {
            case SEPA:
                return PaymentProduct.SEPA_CREDIT_TRANSFER;
            case INTERNATIONAL:
                throw new IllegalStateException(ErrorMessages.CROSS_BORDER_PAYMENT_NOT_SUPPORTED);
            case DOMESTIC:
                return getDomesticPaymentProduct(creditorAccountType);
            default:
                throw new IllegalStateException(ErrorMessages.UNKNOWN_PAYMENT_PRODUCT);
        }
    }

    private String getCurrentDate() {
        return LocalDate.now().format(DateTimeFormatter.ofPattern(FormValues.DATE_FORMAT));
    }

    private PaymentProduct getDomesticPaymentProduct(Type creditorAccountType) {
        switch (creditorAccountType) {
            case SE_BG:
                return PaymentProduct.SWEDISH_DOMESTIC_PRIVATE_BNAKGIROS;
            case SE_PG:
                return PaymentProduct.SWEDISH_DOMESTIC_PRIVATE_PLUSGIROS;
            default:
                return PaymentProduct.SWEDISH_DOMESTIC_PRIVATE_CREDIT_TRANSFERS;
        }
    }

    private Signer getSigner() {
        return new BankIdSigningController(supplementalRequester, new SebBankIdSigner(this));
    }

    public PaymentStatus fetchStatus(PaymentRequest paymentRequest) {
        final Payment payment = paymentRequest.getPayment();
        final PaymentType type =
                Optional.ofNullable(paymentRequest.getPayment().getType())
                        .orElse(getPaymentType(payment));
        final String paymentProduct =
                getPaymentProduct(type, payment.getCreditor().getAccountIdentifierType())
                        .getValue();

        return SebPaymentStatus.mapToTinkPaymentStatus(
                SebPaymentStatus.fromString(
                        apiClient
                                .getPaymentStatus(payment.getUniqueId(), paymentProduct)
                                .getTransactionStatus()));
    }
}

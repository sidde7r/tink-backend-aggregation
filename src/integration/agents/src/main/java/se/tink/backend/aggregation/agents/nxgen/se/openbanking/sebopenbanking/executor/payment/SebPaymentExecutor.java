package se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.executor.payment;

import com.google.common.util.concurrent.Uninterruptibles;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.contexts.SupplementalRequester;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.exceptions.payment.ReferenceValidationException;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.SebApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.SebConstants.FormValues;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.executor.payment.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.executor.payment.entities.CreditorAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.executor.payment.entities.DebtorAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.executor.payment.entities.RemittanceInformationStructuredEntity;
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
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.payment.enums.PaymentType;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.transfer.enums.RemittanceInformationType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;

public class SebPaymentExecutor implements PaymentExecutor, FetchablePaymentExecutor {

    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final SupplementalRequester supplementalRequester;
    private SebApiClient apiClient;

    public SebPaymentExecutor(SebApiClient apiClient, SupplementalRequester supplementalRequester) {
        this.apiClient = apiClient;
        this.supplementalRequester = supplementalRequester;
    }

    @Override
    public PaymentResponse create(PaymentRequest paymentRequest) throws PaymentException {
        final Payment payment = paymentRequest.getPayment();
        final PaymentType type = SebPaymentUtil.getPaymentType(payment);

        final String paymentProduct =
                SebPaymentUtil.getPaymentProduct(
                                type, payment.getCreditor().getAccountIdentifierType())
                        .getValue();

        CreatePaymentResponse createPaymentResponse = createPayment(paymentRequest, paymentProduct);
        PaymentStatus paymentStatus =
                getPaymentStatusAfterCreatingPayment(
                        paymentRequest, createPaymentResponse.getPaymentId(), paymentProduct);

        return createPaymentResponse.toTinkPaymentResponse(paymentProduct, type, paymentStatus);
    }

    @Override
    public PaymentResponse fetch(PaymentRequest paymentRequest) throws PaymentException {
        logger.info("fetching payment");

        final Payment payment = paymentRequest.getPayment();
        final String paymentId = payment.getUniqueId();
        final PaymentType paymentType = payment.getType();
        final String paymentProduct =
                SebPaymentUtil.getPaymentProduct(
                                paymentType, payment.getCreditor().getAccountIdentifierType())
                        .getValue();

        return apiClient
                .getPayment(paymentId, paymentProduct)
                .toTinkPaymentResponse(
                        paymentProduct, paymentId, paymentType, fetchStatus(paymentRequest));
    }

    @Override
    public PaymentMultiStepResponse sign(PaymentMultiStepRequest paymentMultiStepRequest)
            throws PaymentException, AuthenticationException {
        logger.info("Signing payment");

        String nextStep;
        final Payment payment = paymentMultiStepRequest.getPayment();
        final String paymentProduct =
                SebPaymentUtil.getPaymentProduct(
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
                    // SEB api docs suggest 5s polling interval.
                    // "RCVD, Received, The payment has been received by SEB. To determine if
                    // payment is ready for signing, check the links block. Recommended polling
                    // interval is 5 seconds;"
                    // https://developer.sebgroup.com/node/6054
                    Uninterruptibles.sleepUninterruptibly(5, TimeUnit.SECONDS);
                    return new PaymentMultiStepResponse(
                            payment, SigningStepConstants.STEP_INIT, new ArrayList<>());
                }
            case SigningStepConstants.STEP_SIGN:
                getSigner().sign(paymentMultiStepRequest);
                nextStep = SigningStepConstants.STEP_FINALIZE;
                break;
            default:
                throw new IllegalStateException(
                        String.format("Unknown step %s", paymentMultiStepRequest.getStep()));
        }

        payment.setStatus(getPaymentStatus(paymentId, paymentProduct));
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

    private CreatePaymentResponse createPayment(
            PaymentRequest paymentRequest, String paymentProduct) throws PaymentException {
        final Payment payment = paymentRequest.getPayment();

        final DebtorAccountEntity debtorAccountEntity =
                DebtorAccountEntity.of(paymentRequest.getPayment().getDebtor().getAccountNumber());
        final CreditorAccountEntity creditorAccountEntity =
                CreditorAccountEntity.create(
                        payment.getCreditor().getAccountNumber(), paymentProduct);

        final AmountEntity amountEntity = AmountEntity.of(paymentRequest);

        CreatePaymentRequest createPaymentRequest =
                new CreatePaymentRequest.Builder()
                        .withTemplateId(paymentProduct)
                        .withCreditorAccount(creditorAccountEntity)
                        .withDebtorAccount(debtorAccountEntity)
                        .withExecutionDate(
                                SebPaymentUtil.getExecutionDateOrCurrentDate(
                                        payment, paymentProduct))
                        .withAmount(amountEntity)
                        .withCreditorName(payment.getCreditor().getName())
                        .build();

        RemittanceInformation remittanceInformation =
                SebPaymentUtil.validateAndGetRemittanceInformation(paymentProduct, payment);
        if (RemittanceInformationType.OCR.equals(remittanceInformation.getType())) {
            createPaymentRequest.setRemittanceInformationStructured(
                    new RemittanceInformationStructuredEntity()
                            .createOCRRemittanceInformation(remittanceInformation.getValue()));
        } else {
            SebPaymentUtil.validateUnStructuredRemittanceInformation(
                    payment.getRemittanceInformation().getValue());
            createPaymentRequest.setRemittanceInformationUnstructured(
                    payment.getRemittanceInformation().getValue());
        }

        return apiClient.createPaymentInitiation(createPaymentRequest, paymentProduct);
    }

    private PaymentStatus getPaymentStatusAfterCreatingPayment(
            PaymentRequest paymentRequest, String paymentId, String paymentProduct)
            throws PaymentException {
        getPaymentStatus(paymentId, paymentProduct);

        try {
            PaymentStatusResponse paymentStatusResponse =
                    apiClient.getPaymentStatus(paymentId, paymentProduct);
            return SebPaymentStatus.mapToTinkPaymentStatus(
                    SebPaymentStatus.fromString(paymentStatusResponse.getTransactionStatus()));
        } catch (ReferenceValidationException exception) {
            // Remove the retry when the customers move to RI.
            // https://tinkab.atlassian.net/browse/PAY1-1216
            paymentRequest
                    .getPayment()
                    .getRemittanceInformation()
                    .setType(RemittanceInformationType.UNSTRUCTURED);
            createPayment(paymentRequest, paymentProduct);
        }

        return getPaymentStatus(paymentId, paymentProduct);
    }

    private Signer getSigner() {
        return new BankIdSigningController(supplementalRequester, new SebBankIdSigner(apiClient));
    }

    public PaymentStatus fetchStatus(PaymentRequest paymentRequest) throws PaymentException {
        final Payment payment = paymentRequest.getPayment();
        final PaymentType type =
                Optional.ofNullable(paymentRequest.getPayment().getType())
                        .orElse(SebPaymentUtil.getPaymentType(payment));
        final String paymentProduct =
                SebPaymentUtil.getPaymentProduct(
                                type, payment.getCreditor().getAccountIdentifierType())
                        .getValue();
        return getPaymentStatus(payment.getUniqueId(), paymentProduct);
    }

    private PaymentStatus getPaymentStatus(String paymentId, String paymentProduct)
            throws PaymentException {
        PaymentStatusResponse paymentStatusResponse =
                apiClient.getPaymentStatus(paymentId, paymentProduct);

        return SebPaymentStatus.mapToTinkPaymentStatus(
                SebPaymentStatus.fromString(paymentStatusResponse.getTransactionStatus()));
    }
}

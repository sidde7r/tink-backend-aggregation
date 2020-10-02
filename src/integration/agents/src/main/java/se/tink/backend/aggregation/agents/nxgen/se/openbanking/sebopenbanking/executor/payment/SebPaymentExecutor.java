package se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.executor.payment;

import com.google.common.base.Strings;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.contexts.SupplementalRequester;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.exceptions.payment.ReferenceValidationException;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.SebApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.SebConstants;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.SebConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.SebConstants.FormValues;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.SebConstants.PaymentValue;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.executor.payment.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.executor.payment.entities.CreditorAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.executor.payment.entities.DebtorAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.executor.payment.entities.PaymentProduct;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.executor.payment.entities.RemittanceInformationStructuredEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.executor.payment.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.executor.payment.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.executor.payment.rpc.PaymentSigningRequest;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.executor.payment.rpc.PaymentStatusResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.utils.SebDateUtil;
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
import se.tink.backend.aggregation.utils.accountidentifier.IntraBankChecker;
import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.payment.enums.PaymentType;
import se.tink.libraries.payment.rpc.Payment;

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
        final PaymentType type = getPaymentType(payment);

        final String paymentProduct =
                getPaymentProduct(type, payment.getCreditor().getAccountIdentifierType())
                        .getValue();

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
                        .withExecutionDate(getExecutionDateOrCurrentDate(payment, paymentProduct))
                        .withAmount(amountEntity)
                        .withCreditorName(payment.getCreditor().getName())
                        .build();

        if (shouldAddRemittanceInformationStructured(paymentProduct)) {
            RemittanceInformationStructuredEntity remittanceInformation =
                    new RemittanceInformationStructuredEntity()
                            .createOCRRemittanceInformation(payment.getReference().getValue());
            createPaymentRequest.setRemittanceInformationStructured(remittanceInformation);
        } else {
            createPaymentRequest.setRemittanceInformationUnstructured(
                    getRemittanceInformationUnStructured(payment.getReference().getValue()));
        }

        CreatePaymentResponse createPaymentResponse =
                apiClient.createPaymentInitiation(createPaymentRequest, paymentProduct);

        PaymentStatus paymentStatus =
                getPaymentStatus(createPaymentResponse.getPaymentId(), paymentProduct);

        return createPaymentResponse.toTinkPaymentResponse(paymentProduct, type, paymentStatus);
    }

    @Override
    public PaymentResponse fetch(PaymentRequest paymentRequest) throws PaymentException {
        logger.info("fetching payment");

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
    public PaymentMultiStepResponse sign(PaymentMultiStepRequest paymentMultiStepRequest)
            throws PaymentException, AuthenticationException {
        logger.info("Signing payment");

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

    public PaymentType getPaymentType(Payment payment) {
        return payment.getCreditor().getAccountIdentifierType().equals(Type.IBAN)
                        && !payment.getCreditor().getAccountNumber().startsWith(SebConstants.MARKET)
                ? PaymentType.SEPA
                : PaymentType.DOMESTIC;
    }

    public PaymentProduct getPaymentProduct(PaymentType paymentType, Type creditorAccountType) {
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

    private String getExecutionDateOrCurrentDate(Payment payment, String paymentProduct) {
        switch (paymentProduct) {
            case SebConstants.PaymentProduct.SWEDISH_DOMESTIC_PRIVATE_BNAKGIROS:
            case SebConstants.PaymentProduct.SWEDISH_DOMESTIC_PRIVATE_PLUSGIROS:
                return SebDateUtil.getTransferDateForBgPg(payment.getExecutionDate());
            case SebConstants.PaymentProduct.SWEDISH_DOMESTIC_PRIVATE_CREDIT_TRANSFERS:
                return SebDateUtil.getTransferDate(
                        payment.getExecutionDate(),
                        IntraBankChecker.isSwedishMarketIntraBank(
                                payment.getDebtor().getAccountIdentifier(),
                                payment.getCreditor().getAccountIdentifier()));
            case SebConstants.PaymentProduct.SEPA_CREDIT_TRANSFER:
                return SebDateUtil.getTransferDateForSepa(payment.getExecutionDate());
            default:
                throw new IllegalStateException(ErrorMessages.UNKNOWN_PAYMENT_PRODUCT);
        }
    }

    private boolean shouldAddRemittanceInformationStructured(String paymentProduct) {
        if (StringUtils.containsAny(
                paymentProduct,
                PaymentProduct.SWEDISH_DOMESTIC_PRIVATE_BANKGIROS.getValue(),
                PaymentProduct.SWEDISH_DOMESTIC_PRIVATE_PLUSGIROS.getValue())) {
            return true;
        }
        return false;
    }

    private String getRemittanceInformationUnStructured(String message) throws PaymentException {
        if (Strings.isNullOrEmpty(message)) {
            return message;
        }

        if (message.length() > PaymentValue.MAX_DEST_MSG_LEN) {
            throw new ReferenceValidationException(
                    String.format(
                            ErrorMessages.PAYMENT_REF_TOO_LONG, PaymentValue.MAX_DEST_MSG_LEN));
        }

        return message;
    }

    private PaymentProduct getDomesticPaymentProduct(Type creditorAccountType) {
        switch (creditorAccountType) {
            case SE_BG:
                return PaymentProduct.SWEDISH_DOMESTIC_PRIVATE_BANKGIROS;
            case SE_PG:
                return PaymentProduct.SWEDISH_DOMESTIC_PRIVATE_PLUSGIROS;
            default:
                return PaymentProduct.SWEDISH_DOMESTIC_PRIVATE_CREDIT_TRANSFERS;
        }
    }

    private Signer getSigner() {
        return new BankIdSigningController(
                supplementalRequester, new SebBankIdSigner(this, apiClient));
    }

    public PaymentStatus fetchStatus(PaymentRequest paymentRequest) throws PaymentException {
        final Payment payment = paymentRequest.getPayment();
        final PaymentType type =
                Optional.ofNullable(paymentRequest.getPayment().getType())
                        .orElse(getPaymentType(payment));
        final String paymentProduct =
                getPaymentProduct(type, payment.getCreditor().getAccountIdentifierType())
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

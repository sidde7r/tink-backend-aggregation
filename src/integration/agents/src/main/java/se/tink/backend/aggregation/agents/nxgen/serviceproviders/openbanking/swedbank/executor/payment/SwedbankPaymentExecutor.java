package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.executor.payment;

import static java.util.Collections.emptyList;
import static se.tink.backend.aggregation.nxgen.controllers.signing.SigningStepConstants.STEP_FINALIZE;
import static se.tink.backend.aggregation.nxgen.controllers.signing.SigningStepConstants.STEP_INIT;
import static se.tink.backend.aggregation.nxgen.controllers.signing.SigningStepConstants.STEP_SIGN;

import java.util.ArrayList;
import java.util.List;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentRejectedException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.common.SwedbankOpenBankingPaymentApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.executor.payment.SwedbankPaymentSigner.MissingExtendedBankIdException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.executor.payment.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.executor.payment.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.executor.payment.enums.SwedbankPaymentStatus;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.executor.payment.enums.SwedbankPaymentType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.executor.payment.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.executor.payment.rpc.CreatePaymentRequest.CreatePaymentRequestBuilder;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.executor.payment.rpc.PaymentStatusResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.executor.payment.util.SwedbankDateUtil;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.executor.payment.util.SwedbankRemittanceInformationUtil;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.util.AccountTypePair;
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
import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.payment.rpc.Payment;

public class SwedbankPaymentExecutor implements PaymentExecutor, FetchablePaymentExecutor {
    private final SwedbankOpenBankingPaymentApiClient apiClient;
    private final List<PaymentResponse> createdPaymentsList = new ArrayList<>();
    private final SwedbankPaymentSigner swedbankPaymentSigner;

    public SwedbankPaymentExecutor(
            SwedbankOpenBankingPaymentApiClient apiClient,
            SwedbankPaymentSigner swedbankPaymentSigner) {
        this.apiClient = apiClient;
        this.swedbankPaymentSigner = swedbankPaymentSigner;
    }

    @Override
    public PaymentResponse create(PaymentRequest paymentRequest) throws PaymentException {
        final Payment payment = paymentRequest.getPayment();
        AccountEntity creditor = AccountEntity.creditorOf(paymentRequest);
        AccountEntity debtor = AccountEntity.debtorOf(paymentRequest);
        AmountEntity amount = AmountEntity.amountOf(paymentRequest);
        SwedbankRemittanceInformationUtil remittanceInformationUtil =
                SwedbankRemittanceInformationUtil.of(paymentRequest);

        CreatePaymentRequestBuilder builder =
                CreatePaymentRequest.builder()
                        .creditorAccount(creditor)
                        .debtorAccount(debtor)
                        .instructedAmount(amount)
                        .executionDate(SwedbankDateUtil.getExecutionDateOrCurrentDate(payment))
                        .remittanceInformationStructured(
                                remittanceInformationUtil.getRemittanceInformationStructured())
                        .remittanceInformationUnstructured(
                                remittanceInformationUtil.getRemittanceInformationUnStructured())
                        .debtorAccountStatementText(payment.getCreditor().getName());

        if (Type.SE.equals(paymentRequest.getPayment().getCreditor().getAccountIdentifierType())) {
            builder.creditorFriendlyName(payment.getCreditor().getName());
        }
        CreatePaymentRequest createPaymentRequest = builder.build();

        AccountTypePair accountTypePair =
                new AccountTypePair(paymentRequest.getPayment().getCreditorAndDebtorAccountType());

        SwedbankPaymentType paymentType = SwedbankPaymentType.getPaymentType(accountTypePair);

        PaymentResponse paymentResponse =
                apiClient
                        .createPayment(
                                createPaymentRequest,
                                SwedbankPaymentType.SE_DOMESTIC_CREDIT_TRANSFERS)
                        .toTinkPaymentResponse(
                                creditor, debtor, amount, paymentType, accountTypePair);

        createdPaymentsList.add(paymentResponse);

        return paymentResponse;
    }

    @Override
    public PaymentResponse fetch(PaymentRequest paymentRequest) throws PaymentException {
        return apiClient
                .getPayment(
                        paymentRequest.getPayment().getUniqueId(),
                        SwedbankPaymentType.SE_DOMESTIC_CREDIT_TRANSFERS)
                .toTinkPaymentResponse(
                        paymentRequest.getPayment(),
                        apiClient
                                .getPaymentStatus(
                                        paymentRequest.getPayment().getUniqueId(),
                                        SwedbankPaymentType.SE_DOMESTIC_CREDIT_TRANSFERS)
                                .getTransactionStatus());
    }

    @Override
    public PaymentMultiStepResponse sign(PaymentMultiStepRequest paymentMultiStepRequest)
            throws PaymentException {
        final Payment payment = paymentMultiStepRequest.getPayment();
        final String paymentId = payment.getUniqueId();

        switch (paymentMultiStepRequest.getStep()) {
            case STEP_INIT:
                final boolean authorizationResult = swedbankPaymentSigner.authorize(paymentId);
                final String step = authorizationResult ? STEP_SIGN : STEP_INIT;
                return new PaymentMultiStepResponse(payment, step, emptyList());
            case STEP_SIGN:
                return signPayment(paymentMultiStepRequest);
            default:
                throw new IllegalStateException(
                        String.format("Unknown step %s", paymentMultiStepRequest.getStep()));
        }
    }

    private PaymentMultiStepResponse signPayment(PaymentMultiStepRequest request)
            throws PaymentException {
        final Payment payment = request.getPayment();
        final String paymentId = payment.getUniqueId();
        try {
            swedbankPaymentSigner.sign(request);
            if (isPaymentInPendingStatus(paymentId)) {
                return new PaymentMultiStepResponse(payment, STEP_INIT, emptyList());
            }
        } catch (MissingExtendedBankIdException | PaymentRejectedException exception) {
            // fallback to redirect flow if we're missing extended BankID
            swedbankPaymentSigner.signWithRedirect(paymentId);
        }
        payment.setStatus(getTinkPaymentStatus(paymentId));
        return new PaymentMultiStepResponse(payment, STEP_FINALIZE, emptyList());
    }

    private boolean isPaymentInPendingStatus(String paymentId) throws PaymentException {
        return getTinkPaymentStatus(paymentId).equals(PaymentStatus.PENDING);
    }

    private PaymentStatus getTinkPaymentStatus(String paymentId) throws PaymentException {
        final PaymentStatusResponse paymentStatusResponse = getPaymentStatus(paymentId);
        return SwedbankPaymentStatus.fromString(paymentStatusResponse.getTransactionStatus())
                .getTinkPaymentStatus();
    }

    private PaymentStatusResponse getPaymentStatus(String paymentId) throws PaymentException {
        return apiClient.getPaymentStatus(
                paymentId, SwedbankPaymentType.SE_DOMESTIC_CREDIT_TRANSFERS);
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
        return new PaymentListResponse(createdPaymentsList);
    }
}

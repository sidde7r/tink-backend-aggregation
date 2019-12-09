package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.executor.payment;

import java.util.ArrayList;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.BankdataApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.BankdataConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.BankdataConstants.PaymentRequests;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.BankdataConstants.SIGNING_STEPS;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.configuration.BankdataConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.executor.payment.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.executor.payment.entities.CreditorEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.executor.payment.entities.DebtorEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.executor.payment.enums.BankdataPaymentStatus;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.executor.payment.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.executor.payment.rpc.PaymentStatusResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.util.DateUtils;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.util.TypePair;
import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationStepConstants;
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
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.payment.enums.PaymentType;
import se.tink.libraries.payment.rpc.Payment;

public class BankdataPaymentExecutorSelector implements PaymentExecutor, FetchablePaymentExecutor {

    private BankdataApiClient apiClient;
    private SessionStorage sessionStorage;
    private BankdataConfiguration configuration;

    public BankdataPaymentExecutorSelector(
            BankdataApiClient apiClient,
            SessionStorage sessionStorage,
            BankdataConfiguration configuration) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
        this.configuration = configuration;
    }

    @Override
    public PaymentResponse create(PaymentRequest paymentRequest) throws PaymentException {

        PaymentType type =
                BankdataConstants.PAYMENT_TYPE_MAPPER
                        .translate(
                                new TypePair(
                                        paymentRequest
                                                .getPayment()
                                                .getCreditorAndDebtorAccountType()))
                        .orElse(PaymentType.UNDEFINED);

        return createPayment(paymentRequest, type);
    }

    private PaymentResponse createPayment(PaymentRequest paymentRequest, PaymentType type)
            throws PaymentException {
        AmountEntity amountEntity =
                new AmountEntity(
                        paymentRequest.getPayment().getAmount().getCurrency(),
                        paymentRequest.getPayment().getAmount().getValue().toString());

        DebtorEntity debtorEntity = DebtorEntity.of(paymentRequest, type);
        CreditorEntity creditorEntity = CreditorEntity.of(paymentRequest, type);

        CreatePaymentRequest createPaymentRequest =
                new CreatePaymentRequest.Builder()
                        .withCreditor(creditorEntity)
                        .withDebtor(debtorEntity)
                        .withAmount(amountEntity)
                        .withRequestedExecutionDate(
                                DateUtils.convertToDateViaInstant(
                                        paymentRequest.getPayment().getExecutionDate()))
                        .withCreditorName(paymentRequest.getPayment().getCreditor().getName())
                        .withEndToEndIdentification(PaymentRequests.IDENTIFICATION)
                        .build();

        return apiClient
                .createPayment(createPaymentRequest, type)
                .toTinkPayment(
                        paymentRequest.getPayment().getDebtor().getAccountNumber(),
                        paymentRequest.getPayment().getCreditor().getAccountNumber(),
                        type);
    }

    @Override
    public PaymentResponse fetch(PaymentRequest paymentRequest) {
        String paymentId = paymentRequest.getPayment().getUniqueId();
        PaymentType type = paymentRequest.getPayment().getType();

        return apiClient.fetchPayment(paymentId, type).toTinkPayment(paymentId, type);
    }

    @Override
    public PaymentMultiStepResponse sign(PaymentMultiStepRequest paymentMultiStepRequest) {
        Payment payment = paymentMultiStepRequest.getPayment();
        PaymentStatus paymentStatus;
        String nextStep;
        switch (paymentMultiStepRequest.getStep()) {
            case AuthenticationStepConstants.STEP_INIT:
                URL signingUrl = apiClient.getSigningPaymentUrl(payment.getUniqueId());
                sessionStorage.put(payment.getUniqueId(), signingUrl.toString());
                nextStep = SIGNING_STEPS.CHECK_STATUS_STEP;
                break;
            case SIGNING_STEPS.CHECK_STATUS_STEP:
                String paymentProduct =
                        BankdataConstants.TYPE_TO_DOMAIN_MAPPER.get(payment.getType());
                PaymentStatusResponse paymentStatusResponse =
                        apiClient.getPaymentStatus(paymentProduct, payment.getUniqueId());
                paymentStatus =
                        BankdataPaymentStatus.fromString(
                                        paymentStatusResponse.getTransactionStatus())
                                .getPaymentStatus();
                nextStep = AuthenticationStepConstants.STEP_FINALIZE;
                payment.setStatus(paymentStatus);
                break;
            default:
                throw new IllegalStateException(
                        String.format("Unknown step %s", paymentMultiStepRequest.getStep()));
        }
        return new PaymentMultiStepResponse(payment, nextStep, new ArrayList<>());
    }

    @Override
    public CreateBeneficiaryMultiStepResponse createBeneficiary(
            CreateBeneficiaryMultiStepRequest createBeneficiaryMultiStepRequest) {
        throw new NotImplementedException(
                "Create beneficiary not yet implemented for " + this.getClass().getName());
    }

    @Override
    public PaymentResponse cancel(PaymentRequest paymentRequest) {
        throw new NotImplementedException(
                "Cancel not yet implemented for " + this.getClass().getName());
    }

    @Override
    public PaymentListResponse fetchMultiple(PaymentListRequest paymentListRequest) {
        return paymentListRequest.getPaymentRequestList().stream()
                .map(this::fetch)
                .collect(
                        Collectors.collectingAndThen(
                                Collectors.toList(), PaymentListResponse::new));
    }
}

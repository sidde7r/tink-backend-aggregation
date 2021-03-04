package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.executor.payment;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.FabricApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.FabricConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.executor.payment.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.executor.payment.entities.InstructedAmountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.executor.payment.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.executor.payment.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.executor.payment.rpc.CreateRecurringPaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.executor.payment.rpc.PaymentAuthorizationStatus;
import se.tink.backend.aggregation.agents.utils.remittanceinformation.RemittanceInformationValidator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
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
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.transfer.enums.RemittanceInformationType;
import se.tink.libraries.transfer.rpc.PaymentServiceType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;

public class FabricPaymentExecutor implements PaymentExecutor, FetchablePaymentExecutor {

    private final FabricApiClient apiClient;
    private final List<PaymentResponse> paymentResponses = new ArrayList<>();
    private final FabricPaymentSessionStorage sessionStorage;
    private final StrongAuthenticationState strongAuthenticationState;
    private final FabricPaymentController fabricPaymentController;
    private static final Logger logger = LoggerFactory.getLogger(FabricPaymentExecutor.class);

    public FabricPaymentExecutor(
            FabricApiClient apiClient,
            SupplementalInformationHelper supplementalInformationHelper,
            SessionStorage sessionStorage,
            StrongAuthenticationState strongAuthenticationState) {
        this.apiClient = apiClient;
        this.sessionStorage = new FabricPaymentSessionStorage(sessionStorage);
        this.strongAuthenticationState = strongAuthenticationState;
        this.fabricPaymentController =
                new FabricPaymentController(
                        supplementalInformationHelper, strongAuthenticationState);
    }

    @Override
    public PaymentResponse fetch(PaymentRequest paymentRequest) {
        return apiClient
                .getPayment(paymentRequest.getPayment().getUniqueId())
                .toTinkPaymentResponse(paymentRequest.getPayment());
    }

    @Override
    public PaymentListResponse fetchMultiple(PaymentListRequest paymentListRequest) {
        return new PaymentListResponse(paymentResponses);
    }

    @Override
    public PaymentResponse create(PaymentRequest paymentRequest) throws PaymentException {
        sessionStorage.put(FabricConstants.QueryKeys.STATE, strongAuthenticationState.getState());

        sessionStorage.updatePaymentServiceIfNeeded(paymentRequest.getPayment());
        sessionStorage.updatePaymentProductIfNeeded(paymentRequest.getPayment());

        CreatePaymentRequest createPaymentRequest;
        if (PaymentServiceType.PERIODIC.equals(
                paymentRequest.getPayment().getPaymentServiceType())) {
            createPaymentRequest = getCreateRecurringPaymentRequest(paymentRequest);
        } else {
            createPaymentRequest = getCreatePaymentRequest(paymentRequest);
        }

        CreatePaymentResponse payment = apiClient.createPayment(createPaymentRequest);

        sessionStorage.put(
                FabricConstants.StorageKeys.LINK, payment.getLinks().getScaRedirect().getHref());
        sessionStorage.put(FabricConstants.StorageKeys.PAYMENT_ID, payment.getPaymentId());

        return payment.toTinkPaymentResponse(paymentRequest.getPayment());
    }

    private CreatePaymentRequest getCreatePaymentRequest(PaymentRequest paymentRequest) {
        AccountEntity creditorEntity = AccountEntity.creditorOf(paymentRequest);
        AccountEntity debtorEntity = AccountEntity.debtorOf(paymentRequest);
        InstructedAmountEntity instructedAmountEntity = InstructedAmountEntity.of(paymentRequest);

        RemittanceInformation remittanceInformation =
                paymentRequest.getPayment().getRemittanceInformation();
        RemittanceInformationValidator.validateSupportedRemittanceInformationTypesOrThrow(
                remittanceInformation, null, RemittanceInformationType.UNSTRUCTURED);

        CreatePaymentRequest.CreatePaymentRequestBuilder createPaymentRequest =
                CreatePaymentRequest.builder()
                        .debtorAccount(debtorEntity)
                        .instructedAmount(instructedAmountEntity)
                        .creditorAccount(creditorEntity)
                        .creditorName(paymentRequest.getPayment().getCreditor().getName())
                        .remittanceInformationUnstructured(remittanceInformation.getValue());
        return createPaymentRequest.build();
    }

    private CreatePaymentRequest getCreateRecurringPaymentRequest(PaymentRequest paymentRequest) {
        Payment payment = paymentRequest.getPayment();

        AccountEntity creditorEntity = AccountEntity.creditorOf(paymentRequest);
        AccountEntity debtorEntity = AccountEntity.debtorOf(paymentRequest);
        InstructedAmountEntity instructedAmountEntity = InstructedAmountEntity.of(paymentRequest);

        RemittanceInformation remittanceInformation =
                paymentRequest.getPayment().getRemittanceInformation();
        RemittanceInformationValidator.validateSupportedRemittanceInformationTypesOrThrow(
                remittanceInformation, null, RemittanceInformationType.UNSTRUCTURED);

        CreateRecurringPaymentRequest.CreateRecurringPaymentRequestBuilder
                createRecurringPaymentRequest =
                        CreateRecurringPaymentRequest.builder()
                                .debtorAccount(debtorEntity)
                                .instructedAmount(instructedAmountEntity)
                                .creditorAccount(creditorEntity)
                                .creditorName(paymentRequest.getPayment().getCreditor().getName())
                                .remittanceInformationUnstructured(remittanceInformation.getValue())
                                .frequency(payment.getFrequency().toString())
                                .startDate(payment.getStartDate().toString());
        // optional attributes
        if (Optional.ofNullable(payment.getEndDate()).isPresent()) {
            createRecurringPaymentRequest.endDate(payment.getEndDate().toString());
        }
        if (Optional.ofNullable(payment.getExecutionRule()).isPresent()) {
            createRecurringPaymentRequest.executionRule(payment.getExecutionRule().toString());
        }

        return createRecurringPaymentRequest.build();
    }

    @Override
    public PaymentMultiStepResponse sign(PaymentMultiStepRequest paymentMultiStepRequest)
            throws PaymentException, AuthenticationException {

        logger.info(paymentMultiStepRequest.getStep());

        String redirectUrl = sessionStorage.get(FabricConstants.StorageKeys.LINK);

        if (redirectUrl != null) {
            fabricPaymentController.redirectSCA(redirectUrl);
            logger.info("SupplementalInformation received and continue to getPaymentStatus");
            sessionStorage.put(FabricConstants.StorageKeys.LINK, null);
        }
        String paymentId = sessionStorage.get(FabricConstants.StorageKeys.PAYMENT_ID);

        List<String> authorisationIdList =
                apiClient.getPaymentAuthorizations(paymentId).getAuthorisationIds();
        if (authorisationIdList.isEmpty()) {
            logger.warn("Payment does not have authorisation resources");
        } else {
            PaymentAuthorizationStatus authorizationStatus =
                    apiClient.getPaymentAuthorizationStatus(paymentId);
            logger.info(String.format("scaStatus: %s", authorizationStatus.getScaStatus()));
        }

        CreatePaymentResponse createPaymentResponse = apiClient.getPaymentStatus(paymentId);
        logger.info(
                "Transaction Status: {} after SCA ", createPaymentResponse.getTransactionStatus());
        return fabricPaymentController.response(createPaymentResponse, paymentMultiStepRequest);
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
}

package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.executor.payment;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.FabricApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.FabricConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.executor.payment.rpc.FabricPaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.executor.payment.rpc.FabricPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.executor.payment.rpc.PaymentAuthorizationStatus;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.executor.payment.rpc.RecurringPaymentRequest;
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
import se.tink.libraries.transfer.rpc.PaymentServiceType;

public class FabricPaymentExecutor implements PaymentExecutor, FetchablePaymentExecutor {

    private final FabricApiClient apiClient;
    private final List<PaymentResponse> paymentResponses = new ArrayList<>();
    private final SessionStorage sessionStorage;
    private final StrongAuthenticationState strongAuthenticationState;
    private final FabricPaymentController fabricPaymentController;
    private static final Logger logger = LoggerFactory.getLogger(FabricPaymentExecutor.class);

    public FabricPaymentExecutor(
            FabricApiClient apiClient,
            SupplementalInformationHelper supplementalInformationHelper,
            SessionStorage sessionStorage,
            StrongAuthenticationState strongAuthenticationState) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
        this.strongAuthenticationState = strongAuthenticationState;
        this.fabricPaymentController =
                new FabricPaymentController(
                        supplementalInformationHelper, strongAuthenticationState);
    }

    @Override
    public PaymentResponse fetch(PaymentRequest paymentRequest) {
        return apiClient
                .getPayment(paymentRequest.getPayment())
                .toTinkPaymentResponse(paymentRequest.getPayment());
    }

    @Override
    public PaymentListResponse fetchMultiple(PaymentListRequest paymentListRequest) {
        return new PaymentListResponse(paymentResponses);
    }

    @Override
    public PaymentResponse create(PaymentRequest paymentRequest) throws PaymentException {
        sessionStorage.put(FabricConstants.QueryKeys.STATE, strongAuthenticationState.getState());

        FabricPaymentRequest fabricPaymentRequest;
        if (PaymentServiceType.PERIODIC.equals(
                paymentRequest.getPayment().getPaymentServiceType())) {
            fabricPaymentRequest = RecurringPaymentRequest.createFrom(paymentRequest);
        } else {
            fabricPaymentRequest = FabricPaymentRequest.createFrom(paymentRequest);
        }

        FabricPaymentResponse payment =
                apiClient.createPayment(fabricPaymentRequest, paymentRequest.getPayment());

        sessionStorage.put(
                FabricConstants.StorageKeys.LINK, payment.getLinks().getScaRedirect().getHref());

        return payment.toTinkPaymentResponse(paymentRequest.getPayment());
    }

    public PaymentResponse delete(PaymentRequest paymentRequest) {
        return apiClient
                .deletePayment(paymentRequest.getPayment())
                .toTinkPaymentResponseDelete(paymentRequest.getPayment());
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

        List<String> authorisationIdList =
                apiClient
                        .getPaymentAuthorizations(paymentMultiStepRequest.getPayment())
                        .getAuthorisationIds();
        if (authorisationIdList.isEmpty()) {
            logger.warn("Payment does not have authorisation resources");
        } else {
            PaymentAuthorizationStatus authorizationStatus =
                    apiClient.getPaymentAuthorizationStatus(paymentMultiStepRequest.getPayment());
            logger.info(String.format("scaStatus: %s", authorizationStatus.getScaStatus()));
        }

        FabricPaymentResponse fabricPaymentResponse =
                apiClient.getPaymentStatus(paymentMultiStepRequest.getPayment());
        logger.info(
                "Transaction Status: {} after SCA ", fabricPaymentResponse.getTransactionStatus());
        return fabricPaymentController.response(fabricPaymentResponse, paymentMultiStepRequest);
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

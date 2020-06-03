package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.executor.payment;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.FabricConstants.Timer.WAITING_FOR_SUPPLEMENTAL_INFORMATION_TIMER;

import com.google.api.client.repackaged.com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentRejectedException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.FabricConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.executor.payment.enums.FabricPaymentStatus;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.executor.payment.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepConstants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.payment.enums.PaymentType;

public class FabricPaymentController {

    private final SupplementalInformationHelper supplementalInformationHelper;
    private final StrongAuthenticationState strongAuthenticationState;
    private final SessionStorage sessionStorage;
    private static final Logger logger = LoggerFactory.getLogger(FabricPaymentController.class);

    FabricPaymentController(
            SupplementalInformationHelper supplementalInformationHelper,
            StrongAuthenticationState strongAuthenticationState,
            SessionStorage sessionStorage) {
        this.supplementalInformationHelper = supplementalInformationHelper;
        this.strongAuthenticationState = strongAuthenticationState;
        this.sessionStorage = sessionStorage;
    }

    void openThirdPartyApp(String redirectUrl) {
        ThirdPartyAppAuthenticationPayload payload = getAppPayload(new URL(redirectUrl));
        Preconditions.checkNotNull(payload);
        this.supplementalInformationHelper.openThirdPartyApp(payload);
    }

    private ThirdPartyAppAuthenticationPayload getAppPayload(URL authorizeUrl) {
        return ThirdPartyAppAuthenticationPayload.of(authorizeUrl);
    }

    void waitForSupplementalInformation() {
        this.supplementalInformationHelper.waitForSupplementalInformation(
                strongAuthenticationState.getSupplementalKey(),
                WAITING_FOR_SUPPLEMENTAL_INFORMATION_TIMER,
                TimeUnit.MINUTES);
    }

    PaymentMultiStepResponse response(
            CreatePaymentResponse createPaymentResponse,
            PaymentMultiStepRequest paymentMultiStepRequest)
            throws PaymentException {
        FabricPaymentStatus paymentStatus =
                FabricPaymentStatus.fromString(createPaymentResponse.getTransactionStatus());
        String scaStatus = createPaymentResponse.getScaStatus();
        String psuMessage = createPaymentResponse.getPsuMessage();
        logger.info("PSU Message: {}", psuMessage);
        switch (paymentStatus) {
                // After signing PIS
            case ACCP:
            case ACSC:
            case ACSP:
            case ACTC:
            case ACWC:
            case ACWP:
                return responseSignedPayment(
                        createPaymentResponse, paymentMultiStepRequest.getPayment().getType());
            case RCVD:
            case PDNG:
                return responseUnsignedPayment(paymentMultiStepRequest, createPaymentResponse);
            case RJCT:
            case CANC:
                return responseReject(scaStatus);
            default:
                logger.error(
                        "Payment failed. Invalid Payment status from Fabric paymentStatus={}",
                        paymentStatus);
                throw new PaymentException("Payment failed");
        }
    }

    PaymentMultiStepResponse responseReject(String scaStatus)
            throws PaymentAuthenticationException, PaymentRejectedException {
        if (FabricConstants.ScaStatus.SCA_FAILED.equalsIgnoreCase(scaStatus)) {
            throw new PaymentAuthenticationException(
                    "Payment authentication failed.", new PaymentRejectedException());
        } else {
            throw new PaymentRejectedException();
        }
    }

    private PaymentMultiStepResponse responseUnsignedPayment(
            PaymentMultiStepRequest paymentMultiStepRequest,
            CreatePaymentResponse createPaymentResponse) {

        return createPaymentResponse.getLinks() == null
                ? responseEmptyLinksInResponse(paymentMultiStepRequest, createPaymentResponse)
                : responseRedirectURLs(createPaymentResponse);
    }

    private PaymentMultiStepResponse responseEmptyLinksInResponse(
            PaymentMultiStepRequest paymentMultiStepRequest,
            CreatePaymentResponse createPaymentResponse) {
        sessionStorage.put(FabricConstants.StorageKeys.LINK, null);
        return new PaymentMultiStepResponse(
                createPaymentResponse.toTinkPaymentResponse(
                        paymentMultiStepRequest.getPayment().getUniqueId(),
                        paymentMultiStepRequest.getPayment().getType()),
                FabricConstants.PaymentStep.IN_PROGRESS,
                new ArrayList<>());
    }

    private PaymentMultiStepResponse responseRedirectURLs(
            CreatePaymentResponse createPaymentResponse) {
        logger.info(createPaymentResponse.getLinks().getStatus().getHref());
        logger.info(createPaymentResponse.getLinks().getScaStatus().getHref());
        logger.info(createPaymentResponse.getLinks().getUpdatePsuAuthentication().getHref());
        String psuAuthenticationStatus = createPaymentResponse.getScaStatus();
        if (FabricConstants.ScaStatus.IDENTIFICATION_REQUIRED.equalsIgnoreCase(
                        psuAuthenticationStatus)
                || FabricConstants.ScaStatus.AUTHENTICATION_REQUIRED.equalsIgnoreCase(
                        psuAuthenticationStatus)) {
            sessionStorage.put(
                    FabricConstants.StorageKeys.LINK,
                    createPaymentResponse.getLinks().getUpdatePsuAuthentication().getHref());

        } else if (createPaymentResponse.getLinks().getScaRedirect() != null) {
            sessionStorage.put(
                    FabricConstants.StorageKeys.LINK,
                    createPaymentResponse.getLinks().getScaRedirect().getHref());
        } else if (createPaymentResponse.getLinks().getUpdatePsuAuthentication() != null) {
            sessionStorage.put(
                    FabricConstants.StorageKeys.LINK,
                    createPaymentResponse.getLinks().getUpdatePsuAuthentication().getHref());
        }
        return new PaymentMultiStepResponse(
                createPaymentResponse.toTinkPaymentResponse(
                        createPaymentResponse.getPaymentId(), PaymentType.SEPA),
                FabricConstants.PaymentStep.IN_PROGRESS,
                new ArrayList<>());
    }

    private PaymentMultiStepResponse responseSignedPayment(
            CreatePaymentResponse createPaymentResponse, PaymentType paymentType) {

        if (FabricConstants.ScaStatus.SCA_FINALISED.equalsIgnoreCase(
                        createPaymentResponse.getScaStatus())
                || FabricConstants.ScaStatus.EXEMPTED.equalsIgnoreCase(
                        createPaymentResponse.getScaStatus())) {
            return new PaymentMultiStepResponse(
                    createPaymentResponse.toTinkPaymentResponse(paymentType),
                    AuthenticationStepConstants.STEP_FINALIZE,
                    new ArrayList<>());
        } else {
            return responseIntermediatePaymentStates(createPaymentResponse, paymentType);
        }
    }

    private PaymentMultiStepResponse responseIntermediatePaymentStates(
            CreatePaymentResponse createPaymentResponse, PaymentType paymentType) {

        String redirectURL = null;
        if (createPaymentResponse.getLinks() != null) {
            if (createPaymentResponse.getLinks().getScaRedirect() != null) {
                redirectURL = createPaymentResponse.getLinks().getScaRedirect().getHref();

            } else if (createPaymentResponse.getLinks().getUpdatePsuAuthentication() != null) {
                redirectURL =
                        createPaymentResponse.getLinks().getUpdatePsuAuthentication().getHref();
            }

            // redirect URl from Bank should be null for intermediate states, If
            // not null then it may be bug on CBI globe
            if (redirectURL != null) {
                logger.warn("IntermediatePaymentStates redirectURl was NOT null, check logs");
            }
        }
        sessionStorage.put(FabricConstants.StorageKeys.LINK, redirectURL);

        return new PaymentMultiStepResponse(
                createPaymentResponse.toTinkPaymentResponse(paymentType),
                FabricConstants.PaymentStep.IN_PROGRESS,
                new ArrayList<>());
    }
}

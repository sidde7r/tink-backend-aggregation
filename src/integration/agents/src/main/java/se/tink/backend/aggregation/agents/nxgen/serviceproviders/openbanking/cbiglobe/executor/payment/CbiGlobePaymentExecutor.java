package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.executor.payment;

import com.google.common.util.concurrent.Uninterruptibles;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthorizationCancelledByUserException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthorizationTimeOutException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentCancelledException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentRejectedException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeConstants.PisStatus;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.client.CbiGlobePaymentApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.errorhandle.ErrorResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.executor.payment.enums.CbiGlobePaymentStatus;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.executor.payment.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.executor.payment.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepConstants;
import se.tink.backend.aggregation.nxgen.controllers.payment.CreateBeneficiaryMultiStepRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.CreateBeneficiaryMultiStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentExecutor;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.transfer.rpc.PaymentServiceType;

@RequiredArgsConstructor
@Slf4j
public class CbiGlobePaymentExecutor implements PaymentExecutor {

    // We poll for the "9 minutes". Since this will not count the redirect waits, most of this time
    // will be probably spent between redirects/after last redirect, as it should.
    private static final int SECONDS_SLEEP_BETWEEN_CALLS = 3;
    private static final int MAX_POLL_ATTEMPTS = 9 * 60 / SECONDS_SLEEP_BETWEEN_CALLS;

    protected final CbiGlobePaymentApiClient paymentApiClient;
    private final SupplementalInformationController supplementalInformationController;
    private final CbiStorage storage;
    private final CbiGlobePaymentRequestBuilder paymentRequestBuilder;

    @Override
    public PaymentResponse create(PaymentRequest paymentRequest) {
        CreatePaymentRequest createPaymentRequest =
                PaymentServiceType.PERIODIC.equals(
                                paymentRequest.getPayment().getPaymentServiceType())
                        ? paymentRequestBuilder.getCreateRecurringPaymentRequest(
                                paymentRequest.getPayment())
                        : paymentRequestBuilder.getCreatePaymentRequest(
                                paymentRequest.getPayment());

        CreatePaymentResponse createPaymentResponse =
                paymentApiClient.createPayment(createPaymentRequest, paymentRequest.getPayment());

        // In default flow, redirect, this doesn't do much.
        // But it is useful for iccrea extension into decoupled flow.
        prepareAuthorization(createPaymentResponse);

        return createPaymentResponse.toTinkPaymentResponse(paymentRequest.getPayment());
    }

    protected void prepareAuthorization(CreatePaymentResponse createPaymentResponse) {
        storage.saveScaLinkForPayments(
                createPaymentResponse.getLinks().getUpdatePsuAuthenticationRedirect().getHref());
    }

    // PIS flow in CBI works (by default) as follows:
    // - we receive a redirect url in CreatePaymentResponse
    // - we use this redirect to 'authenticate'
    // - after that is done, we will be able to get second redirect url by fetching payment status
    // - this is not always preset right off the bat, we might need to wait few seconds
    // - we then use that second redirect url to 'authorize' the payment
    // - then we poll for status till it is successful, or some time elapses

    // We implement this behaviour by calling payment status repeatedly in a time limited loop,
    // and based on the status, url presence, etc, we know where we are.
    @Override
    public PaymentMultiStepResponse sign(PaymentMultiStepRequest paymentRequest) {
        for (int i = 0; i < MAX_POLL_ATTEMPTS; i++) {
            performRedirectIfNeeded();

            CreatePaymentResponse paymentStatusResponse =
                    fetchPaymentStatusOrThrowException(paymentRequest);

            boolean isSuccess = false;

            CbiGlobePaymentStatus cbiGlobePaymentStatus =
                    CbiGlobePaymentStatus.fromString(paymentStatusResponse.getTransactionStatus());
            switch (cbiGlobePaymentStatus) {
                case ACCP:
                case ACSC:
                case ACSP:
                case ACTC:
                case ACWC:
                case ACWP:
                    // For most banks, successes can only appear alongside one of these statuses.
                    isSuccess = checkIfPaymentIsSuccesful(paymentStatusResponse);
                    break;
                case RCVD:
                case PDNG:
                    // For just one bank (so far), it is possible that RCVD is treated as success,
                    // due to the way they operate.
                    // Thus we need a way of signaling the success & returning proper response after
                    // the switch.
                    isSuccess =
                            checkIfPaymentInIntermediateStatusIsSuccessful(paymentStatusResponse);
                    break;
                case RJCT:
                    // Finished, but not nicely
                    handleReject(paymentStatusResponse);
                    break;
                default:
                    // Totally unexpected, should never happen.
                    log.error(
                            "Payment failed. Invalid Payment status returned by CBI Globe cbiGlobePaymentStatus={}",
                            cbiGlobePaymentStatus);
                    throw new PaymentException("Payment failed");
            }

            if (isSuccess) {
                return buildSuccessfulPaymentResponse(paymentRequest, paymentStatusResponse);
            } else {
                extractRedirectUrlFromIntermediateResponseIfPresent(paymentStatusResponse);
            }

            // Sleep for a bit before next call, to not spam the API too much.
            Uninterruptibles.sleepUninterruptibly(SECONDS_SLEEP_BETWEEN_CALLS, TimeUnit.SECONDS);
        }

        log.warn("9 minutes (not couting redirects) of polling elapsed! Marking as AuthTimeOut");
        throw new PaymentAuthorizationTimeOutException();
    }

    protected void performRedirectIfNeeded() {
        if (storage.getScaLinkForPayments() == null) {
            // No redirect url in storage, nothing to do!
            return;
        }

        // Send the user away, wait for him to return, or quit if he leaves
        Map<String, String> supplementalInfo =
                supplementalInformationController
                        .openThirdPartyAppSync(
                                ThirdPartyAppAuthenticationPayload.of(
                                        new URL(storage.getScaLinkForPayments())))
                        .orElseThrow(PaymentAuthorizationTimeOutException::new);

        // Clear storage to avoid multiple redirects to the same url
        storage.clearScaLinkForPayments();

        // Check what came back.
        // If we received a callback frmk Not-OK url, then it is definitely a failure.
        if (!QueryValues.SUCCESS.equals(supplementalInfo.get(QueryKeys.RESULT))) {
            throw new PaymentAuthorizationException();
        }
    }

    private CreatePaymentResponse fetchPaymentStatusOrThrowException(
            PaymentMultiStepRequest paymentRequest) {
        try {
            return paymentApiClient.getPaymentStatus(paymentRequest.getPayment());
        } catch (HttpResponseException httpResponseException) {
            // This errors indicate that payment no longer exists - usually user clicked cancel on
            // redirect site
            ErrorResponse errorResponse =
                    ErrorResponse.createFrom(httpResponseException.getResponse());
            if (errorResponse != null
                    && (errorResponse.errorManagementDescriptionEquals(
                                    "Operation not allowed: authentication required.")
                            || errorResponse.tppMessagesContainsError(
                                    "GENERIC_ERROR", "Unknown Payment Identifier"))) {
                log.warn("Payment could not be found when asking for status!");
                throw new PaymentAuthorizationCancelledByUserException();
            } else {
                throw httpResponseException;
            }
        }
    }

    protected boolean checkIfPaymentIsSuccesful(CreatePaymentResponse paymentStatusResponse) {
        String scaStatus = paymentStatusResponse.getScaStatus();
        String psuAuthenticationStatus = paymentStatusResponse.getPsuAuthenticationStatus();

        // Throw best matching exception fast if definitely failure:
        if (PisStatus.FAILED.equalsIgnoreCase(scaStatus)) {
            throw logAndPrepareException(paymentStatusResponse);
        }

        return PisStatus.AUTHENTICATED.equalsIgnoreCase(psuAuthenticationStatus)
                && PisStatus.VERIFIED.equalsIgnoreCase(scaStatus);
    }

    private PaymentCancelledException logAndPrepareException(
            CreatePaymentResponse paymentStatusResponse) {
        log.error(
                "Payment cancelled by user: psuAuthenticationStatus={} , scaStatus={}",
                paymentStatusResponse.getPsuAuthenticationStatus(),
                paymentStatusResponse.getScaStatus());
        return new PaymentCancelledException();
    }

    private void extractRedirectUrlFromIntermediateResponseIfPresent(
            CreatePaymentResponse paymentStatusResponse) {
        // Status was not judged to be the final one, so we look for subsequent redirect urls
        String redirectURL = null;
        if (paymentStatusResponse.getLinks() != null) {
            if (paymentStatusResponse.getLinks().getScaRedirect() != null) {
                redirectURL = paymentStatusResponse.getLinks().getScaRedirect().getHref();
            } else if (paymentStatusResponse.getLinks().getUpdatePsuAuthenticationRedirect()
                    != null) {
                redirectURL =
                        paymentStatusResponse
                                .getLinks()
                                .getUpdatePsuAuthenticationRedirect()
                                .getHref();
            }
        }
        storage.saveScaLinkForPayments(redirectURL);
    }

    protected boolean checkIfPaymentInIntermediateStatusIsSuccessful(
            CreatePaymentResponse paymentStatusResponse) {
        // This method only exist to let BPM have special logic for peculiar bank behaviour
        return false;
    }

    private void handleReject(CreatePaymentResponse createPaymentResponse) {
        String psuAuthenticationStatus = createPaymentResponse.getPsuAuthenticationStatus();

        if (PisStatus.AUTHENTICATION_FAILED.equalsIgnoreCase(psuAuthenticationStatus)) {
            log.error(
                    "PSU Authentication failed, psuAuthenticationStatus={}",
                    psuAuthenticationStatus);
            throw new PaymentAuthenticationException(
                    "Payment authentication failed.", new PaymentRejectedException());
        } else {
            log.error(
                    "Payment rejected by ASPSP: psuAuthenticationStatus={} , scaStatus={}",
                    psuAuthenticationStatus,
                    createPaymentResponse.getScaStatus());
            throw new PaymentRejectedException();
        }
    }

    protected PaymentMultiStepResponse buildSuccessfulPaymentResponse(
            PaymentMultiStepRequest paymentRequest, CreatePaymentResponse paymentStatusResponse) {
        return new PaymentMultiStepResponse(
                paymentStatusResponse.toTinkPaymentResponse(paymentRequest.getPayment()),
                AuthenticationStepConstants.STEP_FINALIZE);
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

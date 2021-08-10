package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.executor.payment;

import com.github.rholder.retry.RetryException;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.WaitStrategies;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentCancelledException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentRejectedException;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.StarlingApiClient;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.executor.payment.auth.PaymentHeaderComposer;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.executor.payment.auth.PaymentMessageSigner;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.executor.payment.rpc.InstructLocalPaymentRequest;
import se.tink.backend.aggregation.agents.utils.remittanceinformation.RemittanceInformationValidator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepConstants;
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
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.signableoperation.enums.InternalStatus;
import se.tink.libraries.transfer.enums.RemittanceInformationType;

@Slf4j
public class StarlingPaymentExecutor implements PaymentExecutor, FetchablePaymentExecutor {
    private static final int SLEEP_TIME_SECOND = 2;
    private static final int RETRY_ATTEMPTS = 270;

    private final StarlingApiClient client;
    private final StarlingPaymentAuthenticationController starlingPaymentAuthenticatorController;
    private final PaymentMessageSigner paymentMessageSigner;

    public StarlingPaymentExecutor(
            StarlingApiClient client,
            StarlingPaymentAuthenticationController starlingPaymentAuthenticationController,
            PaymentMessageSigner paymentMessageSigner) {
        this.client = client;
        this.starlingPaymentAuthenticatorController = starlingPaymentAuthenticationController;
        this.paymentMessageSigner = paymentMessageSigner;
    }

    @Override
    public PaymentResponse fetch(PaymentRequest paymentRequest) {
        return client.fetchPayment(paymentRequest.getPayment().getUniqueId());
    }

    @Override
    public PaymentResponse create(PaymentRequest paymentRequest) throws PaymentException {
        RemittanceInformationValidator.validateSupportedRemittanceInformationTypesOrThrow(
                paymentRequest.getPayment().getRemittanceInformation(),
                RemittanceInformationType.REFERENCE);
        OAuth2Token token = starlingPaymentAuthenticatorController.exchangeToken();
        client.saveOAuthToken(token);

        String accountNumber = paymentRequest.getPayment().getDebtor().getAccountNumber();
        URL accountCategoryUri = client.getAccountCategoryUri(accountNumber);

        InstructLocalPaymentRequest instructLocalPaymentRequest =
                InstructLocalPaymentRequest.fromPaymentRequest(paymentRequest);
        PaymentHeaderComposer signaturer =
                new PaymentHeaderComposer.Builder()
                        .withPayload(instructLocalPaymentRequest)
                        .withBearer(token.toAuthorizeHeader())
                        .withPath(accountCategoryUri.toUri().getPath())
                        .build();
        return client.createPayment(
                instructLocalPaymentRequest,
                accountCategoryUri,
                signaturer.getSignedHeaders(paymentMessageSigner));
    }

    @Override
    public PaymentMultiStepResponse sign(PaymentMultiStepRequest paymentMultiStepRequest)
            throws PaymentException {
        String paymentId = paymentMultiStepRequest.getPayment().getUniqueId();
        PaymentStatus paymentStatus;
        log.info(
                "Start to Get Payment Status every {} Seconds for a total of {} times.",
                SLEEP_TIME_SECOND,
                RETRY_ATTEMPTS);
        try {
            paymentStatus =
                    PAYMENT_STATUS_RETRYOR.call(
                            () -> client.fetchPayment(paymentId).getPayment().getStatus());

        } catch (ExecutionException e) {
            log.error(e.getMessage());
            throw new PaymentException(e.getMessage(), e.getCause());
        } catch (RetryException e) {
            throw new PaymentAuthorizationException(
                    "SCA timeout", InternalStatus.PAYMENT_AUTHORIZATION_TIMEOUT);
        }

        if (paymentStatus == PaymentStatus.PENDING) {
            throw new PaymentCancelledException();
        }

        if (paymentStatus != PaymentStatus.SIGNED) {
            throw new PaymentRejectedException();
        }
        paymentMultiStepRequest.getPayment().setStatus(paymentStatus);
        return new PaymentMultiStepResponse(
                paymentMultiStepRequest.getPayment(), AuthenticationStepConstants.STEP_FINALIZE);
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
        throw new NotImplementedException(
                "fetchMultiple not yet implemented for " + this.getClass().getName());
    }

    private static final Retryer<PaymentStatus> PAYMENT_STATUS_RETRYOR =
            RetryerBuilder.<PaymentStatus>newBuilder()
                    .retryIfResult(status -> status == PaymentStatus.PENDING)
                    .withWaitStrategy(WaitStrategies.fixedWait(SLEEP_TIME_SECOND, TimeUnit.SECONDS))
                    .withStopStrategy(StopStrategies.stopAfterAttempt(RETRY_ATTEMPTS))
                    .build();
}

package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.executor.payment.sign;

import com.github.rholder.retry.RetryException;
import com.github.rholder.retry.Retryer;
import java.util.concurrent.ExecutionException;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsConstants.CredentialKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsConstants.Storage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.executor.payment.entities.SibsPSUDataEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.executor.payment.entities.dictionary.SibsPaymentType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.executor.payment.entities.dictionary.SibsTransactionStatus;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.executor.payment.rpc.SibsPaymentUpdateRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.utils.SibsUtils;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepRequest;
import se.tink.libraries.payment.rpc.Payment;

public class SibsDecoupledSignPaymentStrategy extends AbstractSibsSignPaymentStrategy {

    private static final long SLEEP_TIME = 10L;
    private static final int RETRY_ATTEMPTS = 60;

    protected final Credentials credentials;

    public SibsDecoupledSignPaymentStrategy(SibsBaseApiClient apiClient, Credentials credentials) {
        super(apiClient);
        this.credentials = credentials;
    }

    @Override
    protected void executeSignStrategy(
            PaymentMultiStepRequest paymentMultiStepRequest,
            SibsPaymentType paymentType,
            Payment payment) {

        SibsPaymentUpdateRequest request = new SibsPaymentUpdateRequest();
        request.setPsuData(new SibsPSUDataEntity());
        request.getPsuData().setPassword(credentials.getField(CredentialKeys.PSU_ID));

        String psuUpdateUrl =
                paymentMultiStepRequest.getStorage().get(Storage.PAYMENT_REDIRECT_URI);

        apiClient.updatePaymentforPsuId(psuUpdateUrl, request);
    }

    @Override
    protected SibsTransactionStatus verifyStatusAfterSigning(
            PaymentMultiStepRequest paymentMultiStepRequest,
            SibsPaymentType paymentType,
            Payment payment)
            throws PaymentException {
        Retryer<SibsTransactionStatus> consentStatusRetryer =
                SibsUtils.getPaymentStatusRetryer(SLEEP_TIME, RETRY_ATTEMPTS);
        SibsTransactionStatus status = null;
        try {
            status =
                    consentStatusRetryer.call(
                            () -> getCurrentStatus(paymentMultiStepRequest, paymentType));
            checkStatusAfterSign(status);
            payment.setStatus(status.getTinkStatus());
        } catch (RetryException | ExecutionException e) {
            throw new PaymentException("Payment status verification fails.", e);
        }
        return status;
    }
}

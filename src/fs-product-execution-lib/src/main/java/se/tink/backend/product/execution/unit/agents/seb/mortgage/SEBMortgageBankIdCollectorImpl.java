package se.tink.backend.product.execution.unit.agents.seb.mortgage;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import se.tink.backend.product.execution.log.ProductExecutionLogger;
import se.tink.backend.product.execution.unit.agents.seb.mortgage.model.GetLoanStatusSignRequest;
import se.tink.backend.product.execution.unit.agents.seb.mortgage.model.GetLoanStatusSignResponse;
import se.tink.backend.product.execution.utils.RateLimitedCountdown;
import se.tink.backend.utils.LogUtils;

public class SEBMortgageBankIdCollectorImpl implements SEBMortgageBankIdCollector {
    private static final ProductExecutionLogger log = new ProductExecutionLogger(SEBMortgageBankIdCollectorImpl.class);
    private final SEBMortgageApiClient apiClient;
    private final Provider<RateLimitedCountdown> bankIdCollectCountdown;

    @Inject
    public SEBMortgageBankIdCollectorImpl(
            SEBMortgageApiClient apiClient,
            @Named("SEBMortgage.bankIdCollectCountdown") Provider<RateLimitedCountdown> bankIdCollectCountdown) {
        this.apiClient = apiClient;
        this.bankIdCollectCountdown = bankIdCollectCountdown;
    }

    @Override
    public GetLoanStatusSignResponse.BankIdStatus collect(String applicationId) {
        GetLoanStatusSignRequest request = new GetLoanStatusSignRequest(applicationId);

        // Each time we start a collect session, we want a new collect countdown
        RateLimitedCountdown rateLimitedCountdown = bankIdCollectCountdown.get();

        GetLoanStatusSignResponse.BankIdStatus status = null;
        // Acquire next iteration, if we should do another
        while (rateLimitedCountdown.acquireIsMore()) {
            GetLoanStatusSignResponse bankIdResponse = apiClient.getMortgageStatusSign(request);
            status = bankIdResponse.getStatus();
            switch (status) {
            case EXPIRED_TRANSACTION:
            case NO_CLIENT:
            case ERROR:
                // We're done with some error, log it and return
                log.error(ProductExecutionLogger
                        .newBuilder()
                        .withMessage(String.format("Got unwanted BankID status from SEB: %s (%s)", bankIdResponse, request)));
                return status;
            case USER_VALIDATION_ERROR:
                // We're done with some error, log it and return
                log.warn(ProductExecutionLogger
                        .newBuilder()
                        .withMessage(String.format("Got user validation error from SEB: %s (%s)", bankIdResponse, request)));
                return status;
            case COMPLETE:
            case USER_CANCEL:
                // We're done by some user intended events

                log.info(ProductExecutionLogger
                        .newBuilder()
                        .withMessage(String.format("Got final BankID status from SEB: %s (%s)", bankIdResponse, request)));
                return status;
            case USER_SIGN:
            case OUTSTANDING_TRANSACTION:
            case STARTED:
                // Continue looping

                log.debug(ProductExecutionLogger
                        .newBuilder()
                        .withMessage(String.format("Got waiting-for-sign BankID status from SEB: %s (%s)", bankIdResponse, request)));
                break;
            }
        }

        log.error(String.format("BankID timeout when doing SEB mortgage signing: %s (%s)", status, request));
        return status != null ? status : GetLoanStatusSignResponse.BankIdStatus.ERROR;
    }

}

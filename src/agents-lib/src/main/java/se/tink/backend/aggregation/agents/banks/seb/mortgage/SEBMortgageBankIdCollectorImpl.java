package se.tink.backend.aggregation.agents.banks.seb.mortgage;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import se.tink.backend.aggregation.agents.banks.seb.mortgage.model.GetLoanStatusSignRequest;
import se.tink.backend.aggregation.agents.banks.seb.mortgage.model.GetLoanStatusSignResponse;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.utils.RateLimitedCountdown;

public class SEBMortgageBankIdCollectorImpl implements SEBMortgageBankIdCollector {
    private final AggregationLogger log;
    private final SEBMortgageApiClient apiClient;
    private final Provider<RateLimitedCountdown> bankIdCollectCountdown;

    @Inject
    public SEBMortgageBankIdCollectorImpl(
            AggregationLogger log,
            SEBMortgageApiClient apiClient,
            @Named("SEBMortgage.bankIdCollectCountdown") Provider<RateLimitedCountdown> bankIdCollectCountdown) {
        this.log = log;
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
                log.error(String.format("Got unwanted BankID status from SEB: %s (%s)", bankIdResponse, request));
                return status;
            case USER_VALIDATION_ERROR:
                // We're done with some error, log it and return
                log.warn(String.format("Got user validation error from SEB: %s (%s)", bankIdResponse, request));
                return status;
            case COMPLETE:
            case USER_CANCEL:
                // We're done by some user intended events
                log.info(String.format("Got final BankID status from SEB: %s (%s)", bankIdResponse, request));
                return status;
            case USER_SIGN:
            case OUTSTANDING_TRANSACTION:
            case STARTED:
                // Continue looping
                log.debug(String.format(
                        "Got waiting-for-sign BankID status from SEB: %s (%s)",
                        bankIdResponse, request));
                break;
            }
        }

        log.error(String.format("BankID timeout when doing SEB mortgage signing: %s (%s)", status, request));
        return status != null ? status : GetLoanStatusSignResponse.BankIdStatus.ERROR;
    }

}

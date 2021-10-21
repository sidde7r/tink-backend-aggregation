package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.filter;

import java.util.Objects;
import javax.ws.rs.core.MediaType;
import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.authenticator.rpc.TokenErrorResponse;
import se.tink.backend.aggregation.nxgen.http.filter.filters.randomretry.AbstractRandomRetryFilter;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

public class VolksbankRetryFilter extends AbstractRandomRetryFilter {

    /**
     * @param maxNumRetries Number of additional retries to be performed.
     * @param retrySleepMilliseconds Time in milliseconds that will be spent sleeping between
     */
    public VolksbankRetryFilter(int maxNumRetries, long retrySleepMilliseconds) {
        super(maxNumRetries, retrySleepMilliseconds);
    }

    @Override
    protected boolean shouldRetry(HttpResponse response) {
        return response.getStatus() == HttpStatus.SC_INTERNAL_SERVER_ERROR
                && isNotInvalidRequestError(response);
    }

    private boolean isNotInvalidRequestError(HttpResponse response) {
        checkErrorResponseBodyType(response);
        TokenErrorResponse errorResponse = response.getBody(TokenErrorResponse.class);
        if (!Objects.isNull(errorResponse)) {
            return !errorResponse.isInvalidRequest();
        }
        return true;
    }

    private void checkErrorResponseBodyType(HttpResponse response) {
        if (!MediaType.APPLICATION_JSON_TYPE.isCompatible(response.getType())) {
            throw BankServiceError.BANK_SIDE_FAILURE.exception(
                    "Invalid error response format : " + response.getBody(String.class));
        }
    }
}

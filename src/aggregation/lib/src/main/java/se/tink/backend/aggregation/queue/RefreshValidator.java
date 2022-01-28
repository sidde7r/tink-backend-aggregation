package se.tink.backend.aggregation.queue;

import se.tink.libraries.credentials.service.RefreshInformationRequest;
import se.tink.libraries.queue.sqs.exception.ExpiredMessageException;
import se.tink.libraries.queue.sqs.exception.RateLimitException;

public interface RefreshValidator {

    void validate(RefreshInformationRequest request)
            throws RateLimitException, ExpiredMessageException;
}

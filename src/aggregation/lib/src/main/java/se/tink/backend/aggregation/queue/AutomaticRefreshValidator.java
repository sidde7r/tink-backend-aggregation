package se.tink.backend.aggregation.queue;

import com.google.inject.Inject;
import java.time.ZoneId;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.libraries.credentials.service.RefreshInformationRequest;
import se.tink.libraries.queue.sqs.exception.ExpiredMessageException;
import se.tink.libraries.queue.sqs.exception.RateLimitException;
import se.tink.libraries.rate_limit_service.RateLimitService;

public class AutomaticRefreshValidator implements RefreshValidator {

    private final LocalDateTimeSource localDateTimeSource;

    @Inject
    public AutomaticRefreshValidator(LocalDateTimeSource localDateTimeSource) {
        this.localDateTimeSource = localDateTimeSource;
    }

    @Override
    public void validate(RefreshInformationRequest request)
            throws RateLimitException, ExpiredMessageException {

        String providerName = request.getProvider().getName();
        if (RateLimitService.INSTANCE.hasReceivedRateLimitNotificationRecently(providerName)) {
            throw new RateLimitException(
                    String.format(
                            "Provider %s was rate limited recently. Rejecting execution to requeue.",
                            providerName));
        }

        if (request.getExpiryDate() != null
                && localDateTimeSource
                        .now(ZoneId.systemDefault())
                        .isAfter(request.getExpiryDate())) {
            throw new ExpiredMessageException(
                    String.format(
                            "Message expired, refresh id:%s, expiry date: %s",
                            request.getRefreshId(), request.getExpiryDate()));
        }
    }
}

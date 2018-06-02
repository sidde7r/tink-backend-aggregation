package se.tink.backend.insights.identity;

import java.util.List;
import java.util.Optional;
import se.tink.backend.core.FraudDetails;
import se.tink.backend.insights.core.valueobjects.UserId;
import se.tink.libraries.identity.model.IdentityEvent;

public interface IdentityQueryService {
    List<IdentityEvent> getFraudIdentityEvents(UserId userId, String localeStr, String currencyCode);

    Optional<FraudDetails> getFraudAddressDetails(UserId userId);

}

package se.tink.backend.api;

import se.tink.backend.consent.core.Consent;
import se.tink.backend.consent.core.UserConsent;
import se.tink.backend.consent.rpc.ConsentListResponse;
import se.tink.backend.consent.rpc.ConsentRequest;
import se.tink.backend.consent.rpc.UserConsentListResponse;
import se.tink.backend.core.User;

public interface ConsentService {
    ConsentListResponse available(User user);

    UserConsentListResponse list(User user);

    UserConsent consent(User user, ConsentRequest request);

    UserConsent details(User user, String id);

    Consent describe(User user, String key);
}

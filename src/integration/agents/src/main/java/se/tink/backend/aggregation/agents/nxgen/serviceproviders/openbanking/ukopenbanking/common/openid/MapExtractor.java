package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.util.Map;
import java.util.Optional;

interface MapExtractor {

    static Optional<String> getCallbackElement(Map<String, String> callbackData, String key) {
        Preconditions.checkNotNull(callbackData, "Callback data cannot be null!");
        String value = callbackData.getOrDefault(key, null);
        if (Strings.isNullOrEmpty(value)) {
            return Optional.empty();
        }
        return Optional.of(value);
    }
}

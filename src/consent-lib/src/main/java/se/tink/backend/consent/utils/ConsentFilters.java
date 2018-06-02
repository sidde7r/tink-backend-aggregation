package se.tink.backend.consent.utils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import se.tink.backend.consent.core.cassandra.CassandraConsent;
import se.tink.backend.consent.core.cassandra.CassandraUserConsent;

/**
 * Utility class for filtering for filtering of consents.
 */
public class ConsentFilters {

    /**
     * Take a list of consents and filter out the latest consents by key and locale.
     * Example:
     * - Input  => {Key=T&C, Locale = en_US, Version = 1.0.0}, {}Key=T&C, Locale = en_US, Version = 1.0.1}
     * - Output => {Key=T&C, Locale = en_US, Version = 1.0.1}
     */
    public static List<CassandraConsent> filterLatestConsents(Iterable<CassandraConsent> consents) {
        Map<String, CassandraConsent> result = Maps.newHashMap();

        for (CassandraConsent consent : consents) {

            String key = consent.getKey() + consent.getLocale();

            CassandraConsent existing = result.get(key);

            if (existing == null) {
                result.put(key, consent);
            } else if (consent.isUpdateFor(existing)) {
                result.put(key, consent);
            }
        }

        return Lists.newArrayList(result.values());
    }

    /**
     * Filter out the latest events for an user by key, locale and version. This is useful if a user has accepted and
     * then withdrawn a consent.
     */
    public static List<CassandraUserConsent> filterLatestUserConsents(List<CassandraUserConsent> userConsents) {
        Map<String, CassandraUserConsent> result = Maps.newHashMap();

        for (CassandraUserConsent userConsent : userConsents) {

            String key = userConsent.getKey() + userConsent.getLocale() + userConsent.getVersion();

            CassandraUserConsent existing = result.get(key);

            if (existing == null) {
                result.put(key, userConsent);
            } else if (userConsent.getTimestamp().after(existing.getTimestamp())) {
                result.put(key, userConsent);
            }
        }

        return Lists.newArrayList(result.values());
    }
}

package se.tink.backend.consent.cache;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Multimaps;
import com.google.inject.Inject;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import se.tink.backend.consent.config.ConsentCacheConfiguration;
import se.tink.backend.consent.core.cassandra.CassandraConsent;
import se.tink.backend.consent.repository.cassandra.ConsentRepository;
import se.tink.backend.consent.utils.ConsentFilters;

public class ConsentCache {
    private final Supplier<ImmutableList<CassandraConsent>> allConsents;
    private final Supplier<ImmutableListMultimap<String, CassandraConsent>> latestConsentsByLocale;

    @Inject
    public ConsentCache(ConsentRepository consentRepository, ConsentCacheConfiguration configuration) {
        final int duration = configuration.getDuration();
        final TimeUnit timeUnit = configuration.getTimeUnit();

        allConsents = Suppliers.memoizeWithExpiration(
                () -> ImmutableList.copyOf(consentRepository.findAll()), duration, timeUnit);

        latestConsentsByLocale = Suppliers.memoizeWithExpiration(
                () -> Multimaps.index(ConsentFilters.filterLatestConsents(consentRepository.findAll()),
                        CassandraConsent::getLocale), duration, timeUnit);
    }

    /**
     * Return only the latest consents. If versions 1.0.0, 1.0.1 and 1.0.2 are available for a specific key then
     * version 1.0.2 be returned.
     */
    public ImmutableList<CassandraConsent> getLatestByLocale(String locale) {
        return latestConsentsByLocale.get().get(locale);
    }

    public Optional<CassandraConsent> getLatestByKeyAndLocale(String key, String locale) {
        return latestConsentsByLocale.get().get(locale).stream().filter(c -> Objects.equals(c.getKey(), key))
                .findFirst();
    }

    public Optional<CassandraConsent> get(String key, String version, String locale) {
        return allConsents.get().stream().filter(c -> Objects.equals(c.getKey(), key)
                && Objects.equals(c.getVersion(), version)
                && Objects.equals(c.getLocale(), locale)).findFirst();
    }
}

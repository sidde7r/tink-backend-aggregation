package se.tink.backend.system.cronjob;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import io.dropwizard.lifecycle.Managed;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import se.tink.backend.common.dao.ProviderDao;
import se.tink.backend.common.utils.MetricsUtils;
import se.tink.backend.core.Provider;
import se.tink.backend.utils.LogUtils;
import se.tink.libraries.metrics.LastUpdateGauge;
import se.tink.libraries.metrics.MetricId;
import se.tink.libraries.metrics.MetricRegistry;

/**
 * Reports all providers' refresh frequency, if isLeader is true.
 */
public class ProviderRefreshFrequencyMetricsReporter implements Managed {

    private static final LogUtils log = new LogUtils(ProviderRefreshFrequencyMetricsReporter.class);

    private Map<MetricId, Double> activeProviders = Maps.newHashMap();
    private ProviderDao providerDao;
    private AtomicBoolean isLeader;
    private Timer timer; // It is essential to _not_ instantiate this one here. It will start a non-daemon thread which
                         // will block shutting down the JVM...
    private AtomicBoolean started = new AtomicBoolean(false);
    private MetricRegistry metricRegistry;

    private static MetricId generateProviderRefreshFrequencyMetricName(final Provider provider) {
        return MetricId.newId("refresh_frequency")
                .label("provider", MetricsUtils.cleanMetricName(provider.getName()));
    }

    /**
     * Constructor.
     * 
     * @param providerDao
     *            a providers DAO. It supplies recently updated providers. This is important to make this
     *            class refresh newly added providers and handle providers removal.
     * @param isLeader
     *            a boolean that says whether this class should submit refresh frequencies to Metrics or not. Only
     *            leader submits that.
     * @param metricRegistry
     *            the registry on which the gauges are registered.
     */
    public ProviderRefreshFrequencyMetricsReporter(ProviderDao providerDao, AtomicBoolean isLeader,
            MetricRegistry metricRegistry) {
        this.providerDao = providerDao;
        this.isLeader = isLeader;
        this.metricRegistry = metricRegistry;
    }

    @Override
    public void start() throws Exception {
        if (started.compareAndSet(false, true)) {
            timer = new Timer();

            timer.schedule(new TimerTask() {

                @Override
                public void run() {
                    log.debug("Reporting refresh frequency.");

                    if (isLeader.get()) {
                        log.trace("I'm leader. Using actual providers.");
                        updateGauges(providerDao.getProviders());
                    } else {
                        log.trace("I'm not leader. Using empty list of providers to disable reporting.");
                        updateGauges(ImmutableList.<Provider> of());
                    }
                }

                private void updateGauges(List<Provider> providers) {

                    // Create a view that can be diffed against activeProviders.

                    final Map<MetricId, Double> providersFrequencyByName = Maps.transformValues(
                            Maps.uniqueIndex(providers,
                                    ProviderRefreshFrequencyMetricsReporter::generateProviderRefreshFrequencyMetricName),
                            Provider::getCurrentRefreshFrequency);
                    
                    // Diff against current set up Gauges (stored in activeProviders).

                    MapDifference<MetricId, Double> providerDiff = Maps.difference(
                            providersFrequencyByName, activeProviders);

                    Map<MetricId, Double> introducedProviders = providerDiff.entriesOnlyOnLeft();
                    Set<MetricId> removedProviders = providerDiff.entriesOnlyOnRight().keySet();
                    Map<MetricId, Double> providersToUpdate = providerDiff.entriesInCommon();
                    
                    // Remove unused providers
                    
                    for (MetricId metricName : removedProviders) {
                        metricRegistry.remove(metricName);
                        activeProviders.remove(metricName);
                    }
                    
                    // Add new providers
                    
                    for (final Entry<MetricId, Double> entry : introducedProviders.entrySet()) {
                        MetricId metricId = entry.getKey();
                        Double value = entry.getValue();

                        activeProviders.put(metricId, value);

                        LastUpdateGauge lastUpdateGauge = metricRegistry.lastUpdateGauge(metricId);
                        lastUpdateGauge.update(value);
                    }

                    // Update preexisting providers.

                    activeProviders.putAll(providersToUpdate);

                }
                
            }, TimeUnit.MILLISECONDS.convert(1, TimeUnit.MINUTES));
        }
    }

    @Override
    public void stop() throws Exception {
        timer.cancel();
        started.set(false);
    }


}

package se.tink.backend.system.cli.seeding;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.util.Modules;
import io.dropwizard.setup.Bootstrap;
import java.util.Map;
import java.util.Objects;
import net.sourceforge.argparse4j.inf.Namespace;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.backend.system.cli.ServiceContextCommand;
import se.tink.backend.system.cli.helper.traversal.CommandLineInterfaceUserTraverser;
import se.tink.backend.system.controllers.ProductController;
import se.tink.backend.system.guice.configuration.SystemServiceModule;
import se.tink.backend.utils.LogUtils;
import se.tink.libraries.metrics.Counter;
import se.tink.libraries.metrics.Metric;
import se.tink.libraries.metrics.MetricId;
import se.tink.libraries.metrics.MetricRegistry;
import se.tink.libraries.uuid.UUIDUtils;

public class RefreshProductsCommand extends ServiceContextCommand<ServiceConfiguration> {

    private static final LogUtils log = new LogUtils(RefreshProductsCommand.class);

    public RefreshProductsCommand() {
        super("refresh-products", "");
    }

    public class RefreshProductsCommandModule extends AbstractModule {
        @Override
        protected void configure() {
            ProductRefreshConfiguration runConfiguration = new ProductRefreshConfiguration(
                    ProductRefreshConfiguration.Scope.REFRESH_PRODUCTS_COMMAND);
            runConfiguration.setDryRun(Boolean.getBoolean("dryRun"));
            runConfiguration.setVerbose(Boolean.getBoolean("verbose"));

            bind(ProductRefreshConfiguration.class).toInstance(runConfiguration);
        }
    }

    @Override
    protected void run(Bootstrap<ServiceConfiguration> bootstrap, Namespace namespace,
            ServiceConfiguration configuration, Injector injector, ServiceContext serviceContext) throws Exception {

        Injector systemServiceInjector = injector.createChildInjector(Modules.override(new SystemServiceModule())
                .with(new RefreshProductsCommandModule()));

        final ProductController productController = systemServiceInjector.getInstance(ProductController.class);

        injector.getInstance(UserRepository.class).streamAll()
                .compose(new CommandLineInterfaceUserTraverser(10))
                .forEach(user -> productController.refreshExpiringProducts(UUIDUtils.fromTinkUUID(user.getId())));

        logRefreshProductMetrics(injector.getInstance(MetricRegistry.class));
    }

    private void logRefreshProductMetrics(MetricRegistry metricRegistry) {
        log.info("Total refresh products statistics:");
        ImmutableMap<MetricId, Metric> metrics = metricRegistry.getMetrics();

        Map<String, Long> typeCount = Maps.newHashMap();
        Map<String, Long> providerCount = Maps.newHashMap();
        Map<String, Long> outcomeCount = Maps.newHashMap();

        Map<String, Long> typeProviderCount = Maps.newHashMap();
        Map<String, Long> typeOutcomeCount = Maps.newHashMap();
        Map<String, Long> providerOutcomeCount = Maps.newHashMap();

        for (MetricId metricId : metrics.keySet()) {
            // We only care about the refresh metrics
            if (!Objects.equals(metricId.getMetricName(), ProductController.METRIC_REFRESH_PRODUCTS.getMetricName())) {
                continue;
            }

            Map<String, String> labels = metricId.getLabels();

            Counter metric = (Counter) metrics.get(metricId);
            long count = metric.getCount();

            increaseGrouping(typeCount, labels.get("type"), count);
            increaseGrouping(outcomeCount, labels.get("outcome"), count);
            increaseGrouping(providerCount, labels.get("provider"), count);

            increaseGrouping(typeProviderCount, labels.get("type") + "_" + labels.get("provider"), count);
            increaseGrouping(typeOutcomeCount, labels.get("type") + "_" + labels.get("outcome"), count);
            increaseGrouping(providerOutcomeCount, labels.get("provider") + "_" + labels.get("outcome"), count);

            log.info(metricId.toString() + " = " + count);
        }

        log.info("types=" + typeCount.toString());
        log.info("providers=" + providerCount.toString());
        log.info("outcomes=" + outcomeCount.toString());

        log.info("typesProviders="+typeProviderCount.toString());
        log.info("typesOutcome="+typeOutcomeCount.toString());
        log.info("providersOutcome="+providerOutcomeCount.toString());
    }

    private void increaseGrouping(Map<String, Long> grouping, String label, long count) {
        Long previousValue = grouping.get(label);

        if (previousValue == null) {
            previousValue = 0L;
        }

        grouping.put(label, count + previousValue);
    }
}

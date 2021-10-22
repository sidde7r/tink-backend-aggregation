package se.tink.backend.aggregation.workers.ratelimit;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.dropwizard.lifecycle.Managed;
import java.lang.invoke.MethodHandles;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.assertj.core.util.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Provider;
import se.tink.libraries.concurrency.ListenableThreadPoolExecutor;
import se.tink.libraries.concurrency.NamedRunnable;
import se.tink.libraries.metrics.core.MetricId;
import se.tink.libraries.metrics.registry.MetricRegistry;
import se.tink.libraries.tracing.lib.api.Tracing;

/**
 * https://tinkab.atlassian.net/wiki/spaces/AGGDOCS/pages/1579778096/Refresh+rate-limiting+batching+rejection
 */
public class RateLimitedExecutorService implements Managed {
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final ImmutableMap<String, Double> PROVIDERS_WITH_OVERRIDDEN_RATE_LIMITER =
            ImmutableMap.<String, Double>builder()
                    .put("fraud.CreditSafeAgent", 0.1)
                    .put("abnamro.ics.IcsAgent", 8.)
                    .put("other.CSNAgent", 0.1)
                    .put("nxgen.dk.banks.danskebank.DanskeBankDKAgent", 0.01)
                    .put("nxgen.nl.openbanking.triodos.TriodosAgent", 0.02)
                    .put("nxgen.no.banks.danskebank.DanskeBankNOAgent", 0.01)
                    .put("nxgen.se.openbanking.handelsbanken.HandelsbankenSEAgent", 0.02)
                    .put("nxgen.se.openbanking.nordea.NordeaSeAgent", 0.01)
                    .put("nxgen.se.banks.swedbank.fallback.SwedbankFallbackAgent.java", 0.05)
                    .put("nxgen.se.openbanking.swedbank.SwedbankAgent", 0.03)
                    .put("nxgen.se.openbanking.alandsbanken.AlandsbankenAgent", 0.03)
                    .put("nxgen.se.openbanking.business.danskebank.DanskebankBusinessAgent", 0.03)
                    .put("nxgen.se.openbanking.danskebank.DanskebankV31Agent", 0.03)
                    .put(
                            "nxgen.se.openbanking.business.handelsbanken.HandelsbankenSEBusinessAgent",
                            0.03)
                    .put("nxgen.se.openbanking.business.nordea.NordeaSeBusinessAgent", 0.03)
                    .put("nxgen.se.openbanking.business.seb.SebSEBusinessAgent", 0.03)
                    .put("nxgen.se.openbanking.entercard.coop.CoopMastercardAgent", 0.03)
                    .put("nxgen.se.openbanking.entercard.mervarde.MervardeMasterCardAgent", 0.03)
                    .put("nxgen.se.openbanking.entercard.moregolf.MoreGolfMastercardAgent", 0.03)
                    .put("nxgen.se.openbanking.entercard.remember.RememberMastercardAgent", 0.03)
                    .put("nxgen.se.openbanking.lansforsakringar.LansforsakringarAgent", 0.03)
                    .put("nxgen.se.openbanking.nordnet.NordnetAgent", 0.03)
                    .put("nxgen.se.openbanking.resursbank.ResursBankAgent", 0.03)
                    .put("nxgen.se.openbanking.sbab.SbabAgent", 0.03)
                    .put("nxgen.se.openbanking.sebkort.circlek.CircleKMastercardSeAgent", 0.03)
                    .put("nxgen.se.openbanking.sebkort.eurocard.EurocardSeAgent", 0.03)
                    .put("nxgen.se.openbanking.sebkort.finnair.FinnairMastercardSeAgent", 0.03)
                    .put("nxgen.se.openbanking.sebkort.ingo.IngoMastercardSeAgent", 0.03)
                    .put("nxgen.se.openbanking.sebkort.nk.NkNyckelnMastercardSeAgent", 0.03)
                    .put(
                            "nxgen.se.openbanking.sebkort.nordicchoiceclub.NordicChoiceClubSeAgent",
                            0.03)
                    .put("nxgen.se.openbanking.sebkort.opel.OpelMastercardSeAgent", 0.03)
                    .put(
                            "nxgen.se.openbanking.sebkort.quintessentially.QuintessentiallyTheCreditCardSeAgent",
                            0.03)
                    .put("nxgen.se.openbanking.sebkort.saab.SaabMastercardSeAgent", 0.03)
                    .put("nxgen.se.openbanking.sebkort.sas.SasEurobonusMastercardSeAgent", 0.03)
                    .put("nxgen.se.openbanking.sebkort.sj.SjPrioMastercardSeAgent", 0.03)
                    .put("nxgen.se.openbanking.sebkort.wallet.WalletMastercardSeAgent", 0.03)
                    .put("nxgen.se.openbanking.sebopenbanking.SebAgent", 0.03)
                    .put("nxgen.se.openbanking.skandia.SkandiaAgent", 0.03)
                    .put("nxgen.se.openbanking.volvofinans.VolvoFinansAgent", 0.03)
                    .put("nxgen.se.openbanking.icabanken.IcaBankenAgent", 0.02)
                    .put("nxgen.nl.creditcards.ICS.ICSAgent", 0.005)
                    .put("nxgen.nl.banks.openbanking.rabobank.RabobankAgent", 0.005)
                    .put("nxgen.nl.openbanking.knab.KnabAgent", 0.02)
                    .put("nxgen.nl.openbanking.abnamro.AbnAmroAgent", 0.03)
                    .put("nxgen.nl.openbanking.ing.IngAgent", 0.03)
                    .put("nxgen.uk.creditcards.amex.v62.AmericanExpressV62UKAgent", 0.03)
                    .put("nxgen.uk.openbanking.nationwide.NationwideV31Agent", 0.05)
                    .put("nxgen.uk.openbanking.revolut.RevolutV31Agent", 0.033)
                    .put("nxgen.uk.openbanking.santander.SantanderV31Agent", 0.033)
                    .put("nxgen.demo.openbanking.demobank.DemobankAgent", 2.)
                    .put("nxgen.fi.openbanking.spankki.SPankkiAgent", 0.025)
                    .put("nxgen.pt.openbanking.activobank.ActivoBankAgent", 0.025)
                    .put("nxgen.pt.openbanking.atlanticoeuropa.AtlanticoEuropaAgent", 0.025)
                    .put("nxgen.pt.openbanking.bancobpi.BancoBpiAgent", 0.025)
                    .put("nxgen.pt.openbanking.bancoctt.BancoCttAgent", 0.025)
                    .put("nxgen.pt.openbanking.bancomontepio.BancoMontepioAgent", 0.025)
                    .put("nxgen.pt.openbanking.bankinter.BankinterAgent", 0.025)
                    .put("nxgen.pt.openbanking.big.BigAgent", 0.025)
                    .put("nxgen.pt.openbanking.bpg.BpgAgent", 0.025)
                    .put("nxgen.pt.openbanking.caixa.CaixaRedirectAgent", 0.025)
                    .put("nxgen.pt.openbanking.caixacrl.CaixaCrlAgent", 0.025)
                    .put("nxgen.pt.openbanking.cemah.CemahAgent", 0.025)
                    .put("nxgen.pt.openbanking.cofidis.CofidisAgent", 0.025)
                    .put("nxgen.pt.openbanking.creditoagricola.CreditoAgricolaAgent", 0.025)
                    .put("nxgen.pt.openbanking.eurobic.EurobicAgent", 0.025)
                    .put("nxgen.pt.openbanking.millenniumbcp.MillenniumBcpAgent", 0.025)
                    .put("nxgen.pt.openbanking.novobanco.NovoBancoAgent", 0.025)
                    .put("nxgen.pt.openbanking.novobancoacores.NovoBancoAcoresAgent", 0.025)
                    .put("nxgen.pt.openbanking.santander.SantanderAgent", 0.025)
                    .put("nxgen.pt.openbanking.unicre.UnicreAgent", 0.025)
                    .put("nxgen.serviceproviders.openbanking.revolut.RevolutEEAAgent", 0.033)
                    .build();

    private final MetricRegistry metricRegistry;

    private LoadingCache<Provider, RateLimitedExecutorProxy>
            rateLimitedRefreshInformationRequestExecutorByProvider;
    private ListenableThreadPoolExecutor<Runnable> executorService;
    private final AtomicReference<ProviderRateLimiterFactory> rateLimiterFactory;
    private int maxQueuedItems;

    public RateLimitedExecutorService(
            ListenableThreadPoolExecutor<Runnable> executorService,
            MetricRegistry metricRegistry,
            int maxQueuedItems) {
        this.executorService = executorService;
        this.metricRegistry = metricRegistry;
        this.maxQueuedItems = maxQueuedItems;

        this.rateLimiterFactory =
                new AtomicReference<>(
                        new CachingProviderRateLimiterFactory(
                                new LoggingProviderRateLimiterFactory(
                                        new OverridingProviderRateLimiterFactory(
                                                PROVIDERS_WITH_OVERRIDDEN_RATE_LIMITER,
                                                new DefaultProviderRateLimiterFactory(0.1)))));

        logger.info(
                String.format("Rate limiter factory on initialization: %s", rateLimiterFactory));
    }

    private LoadingCache<Provider, RateLimitedExecutorProxy> buildRateLimittedProxyCache(
            final AtomicReference<ProviderRateLimiterFactory> providerRateLimiterFactory) {
        return CacheBuilder.newBuilder()
                .expireAfterAccess(
                        96, TimeUnit.HOURS) // Must be longer than the time it takes to process the
                // ratelimitter queue for a single provider. That is, if we queue
                // up all CreditSafe refreshes in a single batch, we must be sure
                // that it finishes all those batches in X hours.
                .removalListener(
                        (RemovalListener<Provider, RateLimitedExecutorProxy>)
                                notification -> {
                                    RateLimitedExecutorProxy executor = notification.getValue();

                                    if (!MoreExecutors.shutdownAndAwaitTermination(
                                            executor, 2, TimeUnit.MINUTES)) {
                                        logger.error(
                                                String.format(
                                                        "Could gracefully shut down RateLimitedExecutorProxy for provider '%s'.",
                                                        notification.getKey().getName()));
                                    }
                                })
                .build(
                        new CacheLoader<Provider, RateLimitedExecutorProxy>() {
                            @Override
                            public RateLimitedExecutorProxy load(final Provider provider)
                                    throws Exception {
                                MetricId.MetricLabels labels =
                                        new MetricId.MetricLabels()
                                                .add(
                                                        "provider_type",
                                                        provider.getType().name().toLowerCase())
                                                .add("provider", provider.getName());
                                logger.info(
                                        "Provider {} has hash-code {}",
                                        provider.getName(),
                                        provider.hashCode());
                                return new RateLimitedExecutorProxy(
                                        () ->
                                                RateLimitedExecutorProxy.RateLimiters.from(
                                                        providerRateLimiterFactory
                                                                .get()
                                                                .buildFor(provider.getClassName())),
                                        executorService,
                                        new ThreadFactoryBuilder()
                                                .setNameFormat(
                                                        provider.getName() + "-rate-limiter-%d")
                                                .build(),
                                        metricRegistry,
                                        labels,
                                        maxQueuedItems);
                            }
                        });
    }

    public void execute(final NamedRunnable namedRunnable, final Provider provider)
            throws Exception {
        final RateLimitedExecutorProxy executorProxy =
                rateLimitedRefreshInformationRequestExecutorByProvider.get(provider);
        executorProxy.execute(Tracing.wrapRunnable(namedRunnable));
    }

    @VisibleForTesting
    long getCacheSize() {
        return rateLimitedRefreshInformationRequestExecutorByProvider.size();
    }

    @Override
    public void start() throws Exception {
        rateLimitedRefreshInformationRequestExecutorByProvider =
                buildRateLimittedProxyCache(rateLimiterFactory);
    }

    @Override
    public void stop() throws Exception {
        if (rateLimitedRefreshInformationRequestExecutorByProvider != null) {
            rateLimitedRefreshInformationRequestExecutorByProvider.invalidateAll();
            rateLimitedRefreshInformationRequestExecutorByProvider.cleanUp(); // just in case
        }
    }

    public void setRateLimiterFactory(ProviderRateLimiterFactory rateLimiterFactory) {
        ProviderRateLimiterFactory loggedFactory =
                new LoggingProviderRateLimiterFactory(rateLimiterFactory);

        // We must wrap all these in CachingProviderRateLimiterFactory so that each call to a
        // factory with the same
        // provider returns the same RateLimiter instance.
        ProviderRateLimiterFactory cachedFactor =
                new CachingProviderRateLimiterFactory(loggedFactory);

        ProviderRateLimiterFactory oldFactory = this.rateLimiterFactory.getAndSet(cachedFactor);

        logger.info(String.format("Old provider rate limiter factory: %s", oldFactory));
        logger.info(String.format("New provider rate limiter factory: %s", cachedFactor));
    }
}

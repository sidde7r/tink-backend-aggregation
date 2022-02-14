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

    private static final double MULTIPLIER = 1.25;
    private static final double SIBS_PERMITS_PER_SECOND = 0.01;

    private static final ImmutableMap<String, Double> PROVIDERS_WITH_OVERRIDDEN_RATE_LIMITER =
            ImmutableMap.<String, Double>builder()
                    .put("fraud.CreditSafeAgent", 0.12 * MULTIPLIER)
                    .put("abnamro.ics.IcsAgent", 10. * MULTIPLIER)
                    .put("other.CSNAgent", 0.12 * MULTIPLIER)
                    .put("nxgen.dk.banks.danskebank.DanskeBankDKAgent", 0.015 * MULTIPLIER)
                    .put("nxgen.nl.openbanking.triodos.TriodosAgent", 0.03 * MULTIPLIER)
                    .put("nxgen.no.banks.danskebank.DanskeBankNOAgent", 0.015 * MULTIPLIER)
                    .put(
                            "nxgen.serviceproviders.openbanking.sparebank.SparebankAgent",
                            0.003 * MULTIPLIER)
                    .put(
                            "nxgen.se.openbanking.handelsbanken.HandelsbankenSEAgent",
                            0.03 * MULTIPLIER)
                    .put("nxgen.se.openbanking.nordea.NordeaSeAgent", 0.015 * MULTIPLIER)
                    .put(
                            "nxgen.se.banks.swedbank.fallback.SwedbankFallbackAgent.java",
                            0.08 * MULTIPLIER)
                    .put("nxgen.se.openbanking.swedbank.SwedbankAgent", 0.02 * MULTIPLIER)
                    .put("nxgen.se.openbanking.alandsbanken.AlandsbankenAgent", 0.04 * MULTIPLIER)
                    .put(
                            "nxgen.se.openbanking.business.danskebank.DanskebankBusinessAgent",
                            0.04 * MULTIPLIER)
                    .put("nxgen.se.openbanking.danskebank.DanskebankV31Agent", 0.04 * MULTIPLIER)
                    .put(
                            "nxgen.se.openbanking.business.handelsbanken.HandelsbankenSEBusinessAgent",
                            0.04 * MULTIPLIER)
                    .put(
                            "nxgen.se.openbanking.business.nordea.NordeaSeBusinessAgent",
                            0.04 * MULTIPLIER)
                    .put("nxgen.se.openbanking.business.seb.SebSEBusinessAgent", 0.04 * MULTIPLIER)
                    .put(
                            "nxgen.se.openbanking.entercard.coop.CoopMastercardAgent",
                            0.04 * MULTIPLIER)
                    .put(
                            "nxgen.se.openbanking.entercard.mervarde.MervardeMasterCardAgent",
                            0.04 * MULTIPLIER)
                    .put(
                            "nxgen.se.openbanking.entercard.moregolf.MoreGolfMastercardAgent",
                            0.04 * MULTIPLIER)
                    .put(
                            "nxgen.se.openbanking.entercard.remember.RememberMastercardAgent",
                            0.04 * MULTIPLIER)
                    .put(
                            "nxgen.se.openbanking.lansforsakringar.LansforsakringarAgent",
                            0.04 * MULTIPLIER)
                    .put("nxgen.se.openbanking.nordnet.NordnetAgent", 0.04 * MULTIPLIER)
                    .put("nxgen.se.openbanking.resursbank.ResursBankAgent", 0.04 * MULTIPLIER)
                    .put("nxgen.se.openbanking.sbab.SbabAgent", 0.04 * MULTIPLIER)
                    .put(
                            "nxgen.se.openbanking.sebkort.circlek.CircleKMastercardSeAgent",
                            0.015 * MULTIPLIER)
                    .put("nxgen.se.openbanking.sebkort.eurocard.EurocardSeAgent", 0.04 * MULTIPLIER)
                    .put(
                            "nxgen.se.openbanking.sebkort.finnair.FinnairMastercardSeAgent",
                            0.04 * MULTIPLIER)
                    .put(
                            "nxgen.se.openbanking.sebkort.ingo.IngoMastercardSeAgent",
                            0.04 * MULTIPLIER)
                    .put(
                            "nxgen.se.openbanking.sebkort.nk.NkNyckelnMastercardSeAgent",
                            0.04 * MULTIPLIER)
                    .put(
                            "nxgen.se.openbanking.sebkort.nordicchoiceclub.NordicChoiceClubSeAgent",
                            0.04 * MULTIPLIER)
                    .put(
                            "nxgen.se.openbanking.sebkort.opel.OpelMastercardSeAgent",
                            0.02 * MULTIPLIER)
                    .put(
                            "nxgen.se.openbanking.sebkort.quintessentially.QuintessentiallyTheCreditCardSeAgent",
                            0.04 * MULTIPLIER)
                    .put(
                            "nxgen.se.openbanking.sebkort.saab.SaabMastercardSeAgent",
                            0.04 * MULTIPLIER)
                    .put(
                            "nxgen.se.openbanking.sebkort.sas.SasEurobonusMastercardSeAgent",
                            0.015 * MULTIPLIER)
                    .put(
                            "nxgen.se.openbanking.sebkort.sj.SjPrioMastercardSeAgent",
                            0.015 * MULTIPLIER)
                    .put(
                            "nxgen.se.openbanking.sebkort.wallet.WalletMastercardSeAgent",
                            0.04 * MULTIPLIER)
                    .put("nxgen.se.openbanking.sebopenbanking.SebAgent", 0.04 * MULTIPLIER)
                    .put("nxgen.se.openbanking.skandia.SkandiaAgent", 0.04 * MULTIPLIER)
                    .put("nxgen.se.openbanking.volvofinans.VolvoFinansAgent", 0.04 * MULTIPLIER)
                    .put("nxgen.se.openbanking.icabanken.IcaBankenAgent", 0.03 * MULTIPLIER)
                    .put("nxgen.nl.creditcards.ICS.ICSAgent", 0.007 * MULTIPLIER)
                    .put("nxgen.nl.banks.openbanking.rabobank.RabobankAgent", 0.007 * MULTIPLIER)
                    .put("nxgen.nl.openbanking.knab.KnabAgent", 0.03 * MULTIPLIER)
                    .put("nxgen.nl.openbanking.abnamro.AbnAmroAgent", 0.04 * MULTIPLIER)
                    .put("nxgen.nl.openbanking.ing.IngAgent", 0.04 * MULTIPLIER)
                    .put(
                            "nxgen.uk.creditcards.amex.v62.AmericanExpressV62UKAgent",
                            0.04 * MULTIPLIER)
                    .put("nxgen.uk.openbanking.ukob.cashplus.CashplusAgent", 0.015 * MULTIPLIER)
                    .put(
                            "nxgen.uk.openbanking.handelsbanken.HandelsbankenCorporateAgent",
                            0.007 * MULTIPLIER)
                    .put(
                            "nxgen.uk.openbanking.ukob.lloydsgroup.lloyds.LloydsV31Agent",
                            0.04 * MULTIPLIER)
                    .put(
                            "nxgen.uk.openbanking.ukob.lloydsgroup.lloyds.LloydsV31BusinessAgent",
                            0.04 * MULTIPLIER)
                    .put(
                            "nxgen.uk.openbanking.ukob.nationwide.NationwideV31Agent",
                            0.07 * MULTIPLIER)
                    .put("nxgen.uk.openbanking.ukob.revolut.RevolutV31Agent", 0.04 * MULTIPLIER)
                    .put("nxgen.uk.openbanking.ukob.santander.SantanderV31Agent", 0.04 * MULTIPLIER)
                    .put(
                            "nxgen.serviceproviders.openbanking.amex.AmericanExpressAgent",
                            0.03 * MULTIPLIER)
                    .put("nxgen.demo.openbanking.demobank.DemobankAgent", 3. * MULTIPLIER)
                    .put(
                            "nxgen.fi.openbanking.handelsbanken.HandelsbankenAgent",
                            0.003 * MULTIPLIER)
                    .put("nxgen.fi.openbanking.nordea.NordeaFiAgent", 0.001 * MULTIPLIER)
                    .put("nxgen.fi.openbanking.spankki.SPankkiAgent", 0.003 * MULTIPLIER)
                    .put(
                            "nxgen.pt.openbanking.activobank.ActivoBankAgent",
                            SIBS_PERMITS_PER_SECOND * MULTIPLIER)
                    .put(
                            "nxgen.pt.openbanking.atlanticoeuropa.AtlanticoEuropaAgent",
                            SIBS_PERMITS_PER_SECOND * MULTIPLIER)
                    .put(
                            "nxgen.pt.openbanking.bancobpi.BancoBpiAgent",
                            SIBS_PERMITS_PER_SECOND * MULTIPLIER)
                    .put(
                            "nxgen.pt.openbanking.bancoctt.BancoCttAgent",
                            SIBS_PERMITS_PER_SECOND * MULTIPLIER)
                    .put(
                            "nxgen.pt.openbanking.bancomontepio.BancoMontepioAgent",
                            SIBS_PERMITS_PER_SECOND * MULTIPLIER)
                    .put(
                            "nxgen.pt.openbanking.bankinter.BankinterAgent",
                            SIBS_PERMITS_PER_SECOND * MULTIPLIER)
                    .put("nxgen.pt.openbanking.big.BigAgent", SIBS_PERMITS_PER_SECOND * MULTIPLIER)
                    .put("nxgen.pt.openbanking.bpg.BpgAgent", SIBS_PERMITS_PER_SECOND * MULTIPLIER)
                    .put(
                            "nxgen.pt.openbanking.caixa.CaixaRedirectAgent",
                            SIBS_PERMITS_PER_SECOND * MULTIPLIER)
                    .put(
                            "nxgen.pt.openbanking.caixacrl.CaixaCrlAgent",
                            SIBS_PERMITS_PER_SECOND * MULTIPLIER)
                    .put(
                            "nxgen.pt.openbanking.cemah.CemahAgent",
                            SIBS_PERMITS_PER_SECOND * MULTIPLIER)
                    .put(
                            "nxgen.pt.openbanking.cofidis.CofidisAgent",
                            SIBS_PERMITS_PER_SECOND * MULTIPLIER)
                    .put(
                            "nxgen.pt.openbanking.creditoagricola.CreditoAgricolaAgent",
                            SIBS_PERMITS_PER_SECOND * MULTIPLIER)
                    .put(
                            "nxgen.pt.openbanking.eurobic.EurobicAgent",
                            SIBS_PERMITS_PER_SECOND * MULTIPLIER)
                    .put(
                            "nxgen.pt.openbanking.millenniumbcp.MillenniumBcpAgent",
                            SIBS_PERMITS_PER_SECOND * MULTIPLIER)
                    .put(
                            "nxgen.pt.openbanking.novobanco.NovoBancoAgent",
                            SIBS_PERMITS_PER_SECOND * MULTIPLIER)
                    .put(
                            "nxgen.pt.openbanking.novobancoacores.NovoBancoAcoresAgent",
                            SIBS_PERMITS_PER_SECOND * MULTIPLIER)
                    .put(
                            "nxgen.pt.openbanking.santander.SantanderAgent",
                            SIBS_PERMITS_PER_SECOND * MULTIPLIER)
                    .put(
                            "nxgen.pt.openbanking.unicre.UnicreAgent",
                            SIBS_PERMITS_PER_SECOND * MULTIPLIER)
                    .put("nxgen.fr.openbanking.bpcegroup.BpceGroupAgent", 0.06 * MULTIPLIER)
                    .put("nxgen.fr.openbanking.boursorama.BoursoramaAgent", 0.06 * MULTIPLIER)
                    .put(
                            "nxgen.serviceproviders.openbanking.revolut.RevolutEEAAgent",
                            0.04 * MULTIPLIER)
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
        logger.info("Executing runnable for provider {}", provider.getName());
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

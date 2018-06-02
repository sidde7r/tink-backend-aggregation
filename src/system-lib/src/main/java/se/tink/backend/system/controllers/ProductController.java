package se.tink.backend.system.controllers;

import com.google.inject.Inject;
import com.google.inject.Provider;
import java.util.List;
import java.util.UUID;
import org.joda.time.DateTime;
import se.tink.backend.common.dao.ProductDAO;
import se.tink.libraries.metrics.MetricId;
import se.tink.libraries.metrics.MetricRegistry;
import se.tink.libraries.uuid.UUIDUtils;
import se.tink.backend.core.product.ProductArticle;
import se.tink.backend.guice.annotations.Now;
import se.tink.backend.system.cli.seeding.ProductRefreshConfiguration;
import se.tink.backend.system.product.mortgage.MortgageProductRefresher;
import se.tink.backend.system.product.savings.SavingsProductRefresher;
import se.tink.backend.utils.LogUtils;
import se.tink.libraries.date.ThreadSafeDateFormat;

public class ProductController {
    public static final MetricId METRIC_REFRESH_PRODUCTS = MetricId.newId("refresh_products");

    private final ProductDAO productDAO;
    private final MortgageProductRefresher mortgageProductRefresher;
    private final SavingsProductRefresher savingsProductRefresher;
    private final Provider<DateTime> now;
    private final ProductRefreshConfiguration configuration;
    private final MetricRegistry metricRegistry;
    private static final LogUtils log = new LogUtils(ProductController.class);

    @Inject
    public ProductController(
            ProductDAO productDAO,
            MortgageProductRefresher mortgageProductRefresher,
            SavingsProductRefresher savingsProductRefresher,
            @Now Provider<DateTime> now,
            ProductRefreshConfiguration configuration,
            MetricRegistry metricRegistry) {
        this.productDAO = productDAO;
        this.mortgageProductRefresher = mortgageProductRefresher;
        this.savingsProductRefresher = savingsProductRefresher;
        this.now = now;
        this.configuration = configuration;
        this.metricRegistry = metricRegistry;
    }

    /**
     * Will refresh all products that are active and close to expire belonging to userId
     */
    public void refreshExpiringProducts(UUID userId) {
        List<ProductArticle> productArticles = productDAO.findAllActiveArticlesByUserId(userId);

        for (ProductArticle productArticle : productArticles) {
            try {
                refreshExpiringProducts(productArticle);
            } catch (Exception e) {
                measure(productArticle, "failed");
                log.error(UUIDUtils.toTinkUUID(userId),
                        String.format("Couldn't refresh expiring product: %s.",
                                productArticle.getInstanceId().toString()),
                        e);
            }
        }
    }

    private void measure(ProductArticle productArticle, String outcome) {
        metricRegistry.meter(METRIC_REFRESH_PRODUCTS
                .label("type", productArticle.getType().name())
                .label("provider", productArticle.getProviderName())
                .label("outcome", outcome)
                .label("dryrun", Boolean.toString(configuration.isDryRun()))
                .label("scope", configuration.getScope().name())).inc();
    }

    private void refreshExpiringProducts(ProductArticle productArticle) throws Exception {
        if (!hasExpiringValidTo(productArticle)) {
            measure(productArticle, "not_expiring");

            if (configuration.isVerbose()) {
                log.debug(UUIDUtils.toTinkUUID(productArticle.getUserId()), String.format(
                        "Product is not expiring yet. Skipping refresh. (productInstanceId: %s, validTo: %s)",
                        productArticle.getInstanceId(),
                        ThreadSafeDateFormat.FORMATTER_SECONDS.format(productArticle.getValidTo())));
            }

            return;
        }

        boolean executed = false;
        switch (productArticle.getType()) {
        case SAVINGS_ACCOUNT:
            executed = savingsProductRefresher.refresh(productArticle);
            break;
        case MORTGAGE:
            executed = mortgageProductRefresher.refresh(productArticle);
            break;
        case RESIDENCE_VALUATION:
            executed = false;
            break;
        }

        measure(productArticle, executed ? "executed" : "skipped");
    }

    /**
     * We should have at least two days left of validity to avoid product turning invalid soon. Valid to dates that are
     * null (if there would be such) are considered expiring.
     */
    private boolean hasExpiringValidTo(ProductArticle productArticle) {
        DateTime validTo = new DateTime(productArticle.getValidTo());
        DateTime aboutToExpire = now.get().plusDays(2);

        return validTo.isBefore(aboutToExpire);
    }
}

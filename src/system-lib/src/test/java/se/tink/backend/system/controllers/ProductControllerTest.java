package se.tink.backend.system.controllers;

import com.google.common.collect.Lists;
import com.google.inject.Provider;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.joda.time.DateTime;
import org.junit.Test;
import se.tink.backend.common.dao.ProductDAO;
import se.tink.libraries.metrics.Counter;
import se.tink.libraries.metrics.MetricId;
import se.tink.libraries.metrics.MetricRegistry;
import se.tink.backend.core.product.ProductArticle;
import se.tink.backend.core.product.ProductInstance;
import se.tink.backend.core.product.ProductTemplate;
import se.tink.backend.core.product.ProductType;
import se.tink.backend.system.cli.seeding.ProductRefreshConfiguration;
import se.tink.backend.system.product.mortgage.MortgageProductRefresher;
import se.tink.backend.system.product.savings.SavingsProductRefresher;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class ProductControllerTest {

    private static final UUID USER_ID = UUID.randomUUID();
    private static final Provider<DateTime> NOW = () -> {
        return DateTime.parse("2017-02-16"); // For testing purposes, always create same date
    };

    @Test
    public void noRefreshWhenNoProducts() throws Exception {
        List<ProductArticle> productArticles = Collections.emptyList();

        new ProductController(
                mockProductDao(USER_ID, productArticles),
                throwingMortgageRefresher(), // Throws if refresh is called
                throwingSavingsRefresher(), // Throws if refresh is called
                NOW, new ProductRefreshConfiguration(ProductRefreshConfiguration.Scope.SYSTEM_SERVICE),
                mockMetricRegistry())
                .refreshExpiringProducts(USER_ID);
    }

    @Test
    public void refreshesSavingsAndMortgageProducts() throws Exception {
        // User articles in db
        List<ProductArticle> productArticles = Lists.newArrayList(
                createProductArticle(ProductType.SAVINGS_ACCOUNT, NOW.get(), NOW.get()),
                createProductArticle(ProductType.MORTGAGE, NOW.get(), NOW.get()),
                createProductArticle(ProductType.MORTGAGE, NOW.get(), NOW.get()));

        ProductDAO productDao = mockProductDao(USER_ID, productArticles);
        MortgageProductRefresher mortgageRefresher = mock(MortgageProductRefresher.class);
        SavingsProductRefresher savingsRefresher = mock(SavingsProductRefresher.class);

        new ProductController(productDao, mortgageRefresher, savingsRefresher, NOW,
                new ProductRefreshConfiguration(ProductRefreshConfiguration.Scope.SYSTEM_SERVICE), mockMetricRegistry())
                .refreshExpiringProducts(USER_ID);

        // Ensure all product articles were refreshed once by correct refresher
        verify(savingsRefresher, times(1)).refresh(productArticles.get(0));
        verify(mortgageRefresher, times(1)).refresh(productArticles.get(1));
        verify(mortgageRefresher, times(1)).refresh(productArticles.get(2));
        verifyNoMoreInteractions(savingsRefresher, mortgageRefresher);
    }

    @Test
    public void refreshesOnlyProductsThatAreAboutToExpire() throws Exception {
        List<ProductArticle> productArticles = Lists.newArrayList(
                createProductArticle(ProductType.SAVINGS_ACCOUNT, NOW.get(), NOW.get().plusDays(1)),
                createProductArticle(ProductType.SAVINGS_ACCOUNT, NOW.get(), NOW.get().plusDays(3)),
                createProductArticle(ProductType.MORTGAGE, NOW.get(), NOW.get().plusDays(1)),
                createProductArticle(ProductType.MORTGAGE, NOW.get(), NOW.get().plusDays(3)));

        ProductDAO productDao = mockProductDao(USER_ID, productArticles);
        MortgageProductRefresher mortgageRefresher = mock(MortgageProductRefresher.class);
        SavingsProductRefresher savingsRefresher = mock(SavingsProductRefresher.class);

        new ProductController(productDao, mortgageRefresher, savingsRefresher, NOW,
                new ProductRefreshConfiguration(ProductRefreshConfiguration.Scope.SYSTEM_SERVICE), mockMetricRegistry())
                .refreshExpiringProducts(USER_ID);

        // Only products with less than two days of validity left should be refreshed
        verify(savingsRefresher, times(1)).refresh(productArticles.get(0));
        verify(mortgageRefresher, times(1)).refresh(productArticles.get(2));
        verifyNoMoreInteractions(savingsRefresher, mortgageRefresher);
    }

    private static ProductDAO mockProductDao(UUID userId, List<ProductArticle> productArticles) {
        ProductDAO productDao = mock(ProductDAO.class);

        when(productDao.findAllActiveArticlesByUserId(userId))
                .thenReturn(productArticles);

        return productDao;
    }

    private MetricRegistry mockMetricRegistry() {
        MetricRegistry mock = mock(MetricRegistry.class);

        when(mock.meter(any(MetricId.class)))
                .thenReturn(mock(Counter.class));

        return mock;
    }

    private static ProductArticle createProductArticle(ProductType type, DateTime validFrom, DateTime validTo) {
        ProductInstance instance = new ProductInstance();
        instance.setValidFrom(validFrom != null ? validFrom.toDate() : null);
        instance.setValidTo(validTo != null ? validTo.toDate() : null);

        ProductTemplate template = new ProductTemplate();
        template.setType(type);

        return new ProductArticle(template, instance);
    }

    private static MortgageProductRefresher throwingMortgageRefresher() throws Exception {
        MortgageProductRefresher refresher = mock(MortgageProductRefresher.class);
        doThrow(UnsupportedOperationException.class).when(refresher).refresh(any(ProductArticle.class));
        return refresher;
    }

    private static SavingsProductRefresher throwingSavingsRefresher() {
        SavingsProductRefresher refresher = mock(SavingsProductRefresher.class);
        doThrow(UnsupportedOperationException.class).when(refresher).refresh(any(ProductArticle.class));
        return refresher;
    }
}

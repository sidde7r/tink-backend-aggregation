package se.tink.backend.system.product.savings;

import com.google.inject.Provider;
import java.util.Date;
import org.joda.time.DateTime;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import se.tink.backend.common.dao.ProductDAO;
import se.tink.backend.core.product.ProductArticle;
import se.tink.backend.core.product.ProductInstance;
import se.tink.backend.core.product.ProductTemplate;
import se.tink.backend.core.product.ProductType;
import se.tink.backend.system.cli.seeding.ProductRefreshConfiguration;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

public class SavingsProductRefresherTest {

    private static final Provider<DateTime> NOW = () -> {
        return DateTime.parse("2017-02-16"); // For testing purposes, always create same date
    };

    @Test(expected = IllegalArgumentException.class)
    public void validatesProductType() {
        ProductDAO mockedProductDao = mock(ProductDAO.class);

        new SavingsProductRefresher(mockedProductDao, NOW,
                new ProductRefreshConfiguration(ProductRefreshConfiguration.Scope.SYSTEM_SERVICE))
                .refresh(createProductArticle(ProductType.MORTGAGE));

        // Shouldn't save any product of incorrect type
        verifyZeroInteractions(mockedProductDao);
    }

    @Test
    public void validToIsUpdated() {
        ProductDAO mockedProductDao = mock(ProductDAO.class);
        ArgumentCaptor<ProductArticle> productCaptor = ArgumentCaptor.forClass(ProductArticle.class);
        ArgumentCaptor<Date> dateCaptor = ArgumentCaptor.forClass(Date.class);

        ProductArticle productArticle = createProductArticle(ProductType.SAVINGS_ACCOUNT);
        new SavingsProductRefresher(mockedProductDao, NOW,
                new ProductRefreshConfiguration(ProductRefreshConfiguration.Scope.SYSTEM_SERVICE))
                .refresh(productArticle);

        // Ensure we saved the product once
        verify(mockedProductDao, times(1)).updateValidTo(productCaptor.capture(), dateCaptor.capture());

        // We should update the same product object instance (e.g. not a new product id or something like that)
        assertThat(productCaptor.getValue()).isSameAs(productArticle);

        // Ensure product was saved with one month from now
        assertThat(dateCaptor.getValue()).isEqualTo(
                NOW.get().plusMonths(1).toDate());
    }

    private static ProductArticle createProductArticle(ProductType productType) {
        ProductInstance instance = new ProductInstance();
        instance.setValidFrom(DateTime.now().toDate()); // An arbitrary validFrom
        instance.setValidTo(DateTime.now().plusDays(3).toDate()); // An arbitrary validTo

        ProductTemplate template = new ProductTemplate();
        template.setType(productType);

        return new ProductArticle(template, instance);
    }
}

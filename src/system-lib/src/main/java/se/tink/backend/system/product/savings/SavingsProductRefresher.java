package se.tink.backend.system.product.savings;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Provider;
import java.util.Date;
import java.util.Objects;
import org.joda.time.DateTime;
import se.tink.backend.common.dao.ProductDAO;
import se.tink.libraries.uuid.UUIDUtils;
import se.tink.backend.core.product.ProductArticle;
import se.tink.backend.core.product.ProductType;
import se.tink.backend.guice.annotations.Now;
import se.tink.backend.system.cli.seeding.ProductRefreshConfiguration;
import se.tink.backend.utils.LogUtils;

public class SavingsProductRefresher implements ProductRefresher {
    private final ProductDAO productDAO;
    private final Provider<DateTime> now;
    private final ProductRefreshConfiguration configuration;
    private static final LogUtils log = new LogUtils(SavingsProductRefresher.class);

    @Inject
    public SavingsProductRefresher(
            ProductDAO productDAO,
            @Now Provider<DateTime> now,
            ProductRefreshConfiguration configuration) {
        this.productDAO = productDAO;
        this.now = now;
        this.configuration = configuration;
    }

    /**
     * Currently we only move date of product article forward on savings accounts.
     * No refreshing needed of product properties.
     * <p>
     * We add 1 month, so that we don't need to update these products that often.
     */
    @Override
    public boolean refresh(ProductArticle productArticle) {
        validateProductType(productArticle);
        Date newValidToDate = now.get().plusMonths(1).toDate();

        if (configuration.isVerbose()) {
            log.debug(UUIDUtils.toTinkUUID(productArticle.getUserId()), String.format(
                    "Refresh SAVINGS productId: %s. New validTo date: %s",
                    productArticle.getInstanceId().toString(), newValidToDate));
        }

        if (configuration.isDryRun()) {
            log.debug(UUIDUtils.toTinkUUID(productArticle.getUserId()), String.format(
                    "Dry run SAVINGS productId: %s.",
                    productArticle.getInstanceId().toString()));
            return false;
        }

        productDAO.updateValidTo(productArticle, newValidToDate);
        return true;
    }

    private static void validateProductType(ProductArticle productArticle) {
        Preconditions.checkArgument(Objects.equals(
                productArticle.getType(),
                ProductType.SAVINGS_ACCOUNT));
    }
}

package se.tink.backend.common.application.mortgage;

import com.google.common.base.Preconditions;
import java.util.Objects;
import se.tink.backend.core.product.ProductArticle;
import se.tink.backend.core.product.ProductPropertyKey;
import se.tink.backend.core.product.ProductType;

public class MortgageProductUtils {
    private final ProductArticle product;

    public MortgageProductUtils(ProductArticle product) {
        Preconditions.checkArgument(Objects.equals(product.getType(), ProductType.MORTGAGE));
        this.product = product;
    }

    public double getInterestRateIncludingDiscount() {
        double newInterestRate = ((Number) product.getProperty(ProductPropertyKey.INTEREST_RATE)).doubleValue();

        if (product.hasProperty(ProductPropertyKey.INTEREST_RATE_DISCOUNT)) {
            newInterestRate -= ((Number) product.getProperty(ProductPropertyKey.INTEREST_RATE_DISCOUNT)).doubleValue();
        }

        return newInterestRate;
    }
}

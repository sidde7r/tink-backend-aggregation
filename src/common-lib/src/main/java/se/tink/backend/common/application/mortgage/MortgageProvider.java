package se.tink.backend.common.application.mortgage;

import java.util.Optional;
import com.google.common.base.Strings;
import se.tink.backend.core.Application;
import se.tink.backend.core.product.ProductArticle;

public enum MortgageProvider {
    SEB_BANKID, SBAB_BANKID, UNKNOWN;

    /**
     * So that we can get a non-null enum to switch/case on regardless of provider or product article being null or not
     */
    public static MortgageProvider fromApplication(Application application) {
        Optional<ProductArticle> productArticle = application.getProductArticle();

        if (!productArticle.isPresent()) {
            return UNKNOWN;
        }

        return fromProductArticle(productArticle.get());
    }

    public static MortgageProvider fromProductArticle(ProductArticle productArticle) {
        String providerName = productArticle.getProviderName();

        if (Strings.isNullOrEmpty(providerName)) {
            return UNKNOWN;
        }

        switch (providerName) {
        case "seb-bankid":
            return SEB_BANKID;
        case "sbab-bankid":
            return SBAB_BANKID;
        default:
            return UNKNOWN;
        }
    }
}

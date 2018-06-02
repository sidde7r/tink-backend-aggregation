package se.tink.backend.common.application;

import com.google.common.base.Objects;
import java.util.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import java.util.List;
import javax.annotation.Nullable;
import se.tink.libraries.uuid.UUIDUtils;
import se.tink.backend.core.ApplicationField;
import se.tink.backend.core.ApplicationForm;
import se.tink.backend.core.enums.ApplicationFieldName;
import se.tink.backend.core.enums.ProviderColorCode;
import se.tink.backend.core.product.ProductArticle;
import se.tink.backend.core.product.ProductType;

public class ProductUtils {

    private static ProductArticle getChosenProductArticle(final String chosenProductId, List<ProductArticle> articles) {
        return Iterables.find(articles, productArticle -> {
            if (productArticle == null) {
                return false;
            }

            String articleId = UUIDUtils.toTinkUUID(productArticle.getInstanceId());
            if (Objects.equal(chosenProductId, articleId)) {
                return true;
            }
            return false;
        });
    }

    public static boolean isSwitchMortgageProviderSEB(ApplicationForm form, List<ProductArticle> articles) {
        Optional<ApplicationField> field = form.getField(ApplicationFieldName.MORTGAGE_PRODUCT);
        if (field.isPresent()) {
            final String chosenProductId = field.get().getValue();

            ProductArticle productArticle = getChosenProductArticle(chosenProductId, articles);
            if (productArticle != null) {
                if (Objects.equal(ProductType.MORTGAGE, productArticle.getType()) &&
                        Objects.equal("seb-bankid", productArticle.getProviderName())) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isSwitchMortgageProviderSBAB(ApplicationForm form, List<ProductArticle> articles) {
        Optional<ApplicationField> field = form.getField(ApplicationFieldName.MORTGAGE_PRODUCT);
        if (field.isPresent()) {
            final String chosenProductId = field.get().getValue();

            ProductArticle productArticle = getChosenProductArticle(chosenProductId, articles);
            if (productArticle != null) {
                if (Objects.equal(ProductType.MORTGAGE, productArticle.getType()) &&
                        Objects.equal("sbab-bankid", productArticle.getProviderName())) {
                    return true;
                }
            }
        }
        return false;
    }

    public static String getProviderNameForSwitchMortgage(ApplicationForm form, List<ProductArticle> articles) {
        Optional<ApplicationField> field = form.getField(ApplicationFieldName.MORTGAGE_PRODUCT);
        if (field.isPresent()) {
            final String chosenProductId = field.get().getValue();

            ProductArticle productArticle = getChosenProductArticle(chosenProductId, articles);
            if (productArticle != null) {
                if (Objects.equal(ProductType.MORTGAGE, productArticle.getType()) &&
                        Objects.equal("sbab-bankid", productArticle.getProviderName())) {

                    return productArticle.getProviderName();
                }
            }
        }
        return null;
    }

    public static String getColorCodeForProvider(String providerName) {
        switch (providerName) {
        case "sbab":
        case "sbab-bankid":
            return ProviderColorCode.SBAB;
        case "seb":
        case "seb-bankid":
            return ProviderColorCode.SEB;
        default:
            return "#FFFFFF";
        }
    }
}

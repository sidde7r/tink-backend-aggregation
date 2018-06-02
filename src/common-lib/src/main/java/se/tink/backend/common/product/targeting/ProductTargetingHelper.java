package se.tink.backend.common.product.targeting;

import com.google.common.base.Function;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import se.tink.backend.common.application.PropertyUtils;
import se.tink.backend.common.workers.fraud.FraudUtils;
import se.tink.backend.core.FraudAddressContent;
import se.tink.backend.core.FraudDetails;
import se.tink.backend.core.FraudDetailsContentType;
import se.tink.libraries.application.ApplicationFieldOptionValues;
import se.tink.backend.core.product.ProductFilter;

public class ProductTargetingHelper {
    public static Optional<FraudDetails> getMostRecentFraudEntry(Profile profile, FraudDetailsContentType type) {
        List<FraudDetails> details = profile.getFraudDetails(type);

        return FraudUtils.getMostRecentFraudDetails(details);
    }
    
    public static Optional<String> getPropertyType(Profile profile) {
        Optional<FraudDetails> details = getMostRecentFraudEntry(profile, FraudDetailsContentType.ADDRESS);

        if (!details.isPresent()) {
            return Optional.empty();
        }

        FraudAddressContent content = (FraudAddressContent) details.get().getContent();
        boolean apartment = PropertyUtils.isApartment(content, profile.getFraudDetails());
        
        if (apartment) {
            return Optional.of(ApplicationFieldOptionValues.APARTMENT);
        } else {
            return Optional.of(ApplicationFieldOptionValues.HOUSE);
        }
    }

    public static final Function<ProductFilter, UUID> FILTER_TO_TEMPLATE_ID = ProductFilter::getTemplateId;
}

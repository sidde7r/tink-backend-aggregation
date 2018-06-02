package se.tink.backend.common.product.targeting;

import com.google.common.base.MoreObjects;
import java.util.Optional;
import com.google.common.base.Predicate;
import java.util.Map;
import java.util.Objects;
import se.tink.backend.core.FraudDetails;
import se.tink.backend.core.FraudDetailsContentType;
import se.tink.backend.core.FraudStatus;
import se.tink.libraries.application.ApplicationFieldOptionValues;
import se.tink.backend.core.product.ProductFilterRule;

public class PropertyTypePredicate implements Predicate<Profile> {
    private final String criteria;
    private final String type;

    public PropertyTypePredicate(ProductFilterRule rule) {
        @SuppressWarnings("unchecked")
        Map<String, String> payload = (Map<String, String>) rule.getPayload();
        this.criteria = payload.get("criteria");
        this.type = payload.get("type");
    }

    @Override
    public boolean apply(Profile profile) {
        boolean invert = Objects.equals(criteria, "exclude");
        return hasPropertyType(profile) ^ invert;
    }

    private boolean hasPropertyType(Profile profile) {
        switch (type) {
        case "house":
            return hasHouse(profile);
        case "apartment":
            return hasApartment(profile);
        default:
            return false;
        }
    }

    private boolean hasHouse(Profile profile) {
        Optional<FraudDetails> details = ProductTargetingHelper.getMostRecentFraudEntry(profile,
                FraudDetailsContentType.REAL_ESTATE_ENGAGEMENT);

        if (!details.isPresent()) {
            return false;
        }

        return !Objects.equals(details.get().getStatus(), FraudStatus.EMPTY);
    }

    private boolean hasApartment(Profile profile) {
        Optional<String> propertyType = ProductTargetingHelper.getPropertyType(profile);
        return propertyType.isPresent() && Objects.equals(propertyType.get(), ApplicationFieldOptionValues.APARTMENT);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("criteria", criteria)
                .add("type", type)
                .toString();
    }
}

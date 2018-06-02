package se.tink.backend.common.product.targeting;

import com.google.common.base.CharMatcher;
import com.google.common.base.MoreObjects;
import java.util.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import java.util.Map;
import se.tink.backend.core.FraudAddressContent;
import se.tink.backend.core.FraudDetails;
import se.tink.backend.core.FraudDetailsContentType;
import se.tink.backend.core.product.ProductFilterRule;

public class PostalCodePredicate implements Predicate<Profile> {
    private final int min;
    private final int max;

    public PostalCodePredicate(ProductFilterRule rule) {
        @SuppressWarnings("unchecked")
        Map<String, Integer> payload = (Map<String, Integer>) rule.getPayload();

        this.min = payload.containsKey("min") ? payload.get("min") : Integer.MIN_VALUE;
        this.max = payload.containsKey("max") ? payload.get("max") : Integer.MAX_VALUE;
    }

    @Override
    public boolean apply(Profile profile) {

        Optional<FraudDetails> details = ProductTargetingHelper.getMostRecentFraudEntry(profile,
                FraudDetailsContentType.ADDRESS);

        if (!details.isPresent()) {
            return false;
        }

        FraudAddressContent content = (FraudAddressContent) details.get().getContent();

        if (Strings.isNullOrEmpty(content.getPostalcode())) {
            return false;
        }

        int postalCode = Integer.valueOf(CharMatcher.DIGIT.retainFrom(content.getPostalcode()));
        return postalCode >= min && postalCode <= max;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("min", min)
                .add("max", max)
                .toString();
    }
}

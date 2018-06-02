package se.tink.backend.common.product.targeting;

import com.google.common.base.MoreObjects;
import java.util.Optional;
import com.google.common.base.Predicate;
import java.util.Map;
import se.tink.backend.core.FraudCreditScoringContent;
import se.tink.backend.core.FraudDetails;
import se.tink.backend.core.FraudDetailsContentType;
import se.tink.backend.core.product.ProductFilterRule;

public class CreditScorePredicate implements Predicate<Profile> {
    private final int min;
    private final int max;

    public CreditScorePredicate(ProductFilterRule rule) {
        @SuppressWarnings("unchecked")
        Map<String, Integer> payload = (Map<String, Integer>) rule.getPayload();

        this.min = payload.containsKey("min") ? payload.get("min") : -100;
        this.max = payload.containsKey("max") ? payload.get("max") : 100;
    }

    @Override
    public boolean apply(Profile profile) {

        Optional<FraudDetails> details = ProductTargetingHelper.getMostRecentFraudEntry(profile,
                FraudDetailsContentType.SCORING);

        if (!details.isPresent()) {
            return false;
        }

        FraudCreditScoringContent content = (FraudCreditScoringContent) details.get().getContent();
        int score = content.getScore();
        return score >= min && score <= max;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("min", min)
                .add("max", max)
                .toString();
    }
}

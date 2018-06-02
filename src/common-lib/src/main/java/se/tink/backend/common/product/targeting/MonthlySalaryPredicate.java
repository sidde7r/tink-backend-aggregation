package se.tink.backend.common.product.targeting;

import com.google.common.base.MoreObjects;
import java.util.Optional;
import com.google.common.base.Predicate;
import java.util.Map;
import se.tink.backend.core.FraudDetails;
import se.tink.backend.core.FraudDetailsContentType;
import se.tink.backend.core.FraudIncomeContent;
import se.tink.backend.core.product.ProductFilterRule;

public class MonthlySalaryPredicate implements Predicate<Profile> {
    private final int min;
    private final int max;

    public MonthlySalaryPredicate(ProductFilterRule rule) {
        @SuppressWarnings("unchecked")
        Map<String, Integer> payload = (Map<String, Integer>) rule.getPayload();

        this.min = payload.containsKey("min") ? payload.get("min") : 0;
        this.max = payload.containsKey("max") ? payload.get("max") : Integer.MAX_VALUE;
    }

    @Override
    public boolean apply(Profile profile) {

        Optional<FraudDetails> details = ProductTargetingHelper.getMostRecentFraudEntry(profile,
                FraudDetailsContentType.INCOME);

        if (!details.isPresent()) {
            return false;
        }

        FraudIncomeContent incomeContent = (FraudIncomeContent) details.get().getContent();
        int monthlySalary = (int) Math.round(incomeContent.getIncomeByService() / 12d);
        return monthlySalary >= min && monthlySalary <= max;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("min", min)
                .add("max", max)
                .toString();
    }
}

package se.tink.backend.common.product.targeting;

import com.google.common.base.MoreObjects;
import com.google.common.base.Predicate;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import se.tink.backend.common.i18n.SocialSecurityNumber;
import se.tink.backend.core.FraudDetails;
import se.tink.backend.core.FraudDetailsContentType;
import se.tink.backend.core.FraudIdentityContent;
import se.tink.backend.core.product.ProductFilterRule;

public class AgePredicate implements Predicate<Profile> {
    private final int min;
    private final int max;
    private final LocalDate today = LocalDate.now(ZoneId.of("CET"));

    public AgePredicate(ProductFilterRule rule) {
        @SuppressWarnings("unchecked")
        Map<String, Integer> payload = (Map<String, Integer>) rule.getPayload();

        this.min = payload.containsKey("min") ? payload.get("min") : 0;
        this.max = payload.containsKey("max") ? payload.get("max") : 100;
    }

    @Override
    public boolean apply(Profile profile) {

        List<FraudDetails> identityDetails = profile.getFraudDetails(FraudDetailsContentType.IDENTITY);

        if (identityDetails.isEmpty()) {
            return false;
        }

        FraudIdentityContent identityContent = (FraudIdentityContent) identityDetails.get(0).getContent();
        SocialSecurityNumber.Sweden ssn = new SocialSecurityNumber.Sweden(identityContent.getPersonIdentityNumber());

        if (!ssn.isValid()) {
            return false;
        }

        int age = ssn.getAge(today);

        return age >= min && age <= max;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("min", min)
                .add("max", max)
                .toString();
    }
}

package se.tink.backend.common.product.targeting;

import com.google.common.base.MoreObjects;
import com.google.common.base.Predicate;
import se.tink.backend.core.product.ProductFilterRule;

public class LocalePredicate implements Predicate<Profile> {
    private final String locale;

    public LocalePredicate(ProductFilterRule rule) {
        this.locale = (String) rule.getPayload();
    }

    @Override
    public boolean apply(Profile profile) {
        return locale.equalsIgnoreCase(profile.getUser().getProfile().getLocale());
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("locale", locale)
                .toString();
    }
}

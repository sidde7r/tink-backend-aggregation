package se.tink.backend.common.product.targeting;

import com.google.common.base.MoreObjects;
import com.google.common.base.Predicate;
import java.util.Map;
import java.util.Objects;
import se.tink.backend.core.product.ProductFilterRule;

public class ProviderPredicate implements Predicate<Profile> {
    private final String criteria;
    private final String provider;

    public ProviderPredicate(ProductFilterRule rule) {
        @SuppressWarnings("unchecked")
        Map<String, String> payload = (Map<String, String>) rule.getPayload();
        this.criteria = payload.get("criteria");
        this.provider = payload.get("provider");
    }

    @Override
    public boolean apply(Profile profile) {
        boolean invert = Objects.equals(criteria, "exclude");
        return hasProvider(profile) ^ invert;
    }

    private boolean hasProvider(Profile profile) {
        return profile.getCredentials().containsKey(provider);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("criteria", criteria)
                .add("provider", provider)
                .toString();
    }
}

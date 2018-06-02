package se.tink.backend.common.product.targeting;

import com.google.common.base.MoreObjects;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import se.tink.backend.core.enums.FeatureFlags;
import se.tink.backend.core.product.ProductFilterRule;

public class FeatureFlagPredicate implements Predicate<Profile> {

    private final String flag;
    private final FeatureFlags.FeatureFlagGroup group;

    public FeatureFlagPredicate(ProductFilterRule rule) {
        this.flag = (String) rule.getPayload();
        this.group = getGroup(flag);
    }

    private FeatureFlags.FeatureFlagGroup getGroup(String flag) {
        try {
            return FeatureFlags.FeatureFlagGroup.valueOf(flag);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public boolean apply(Profile profile) {
        if (group != null) {
            return group.isFlagInGroup(profile.getUser().getFlags());
        } else if (!Strings.isNullOrEmpty(flag)) {
            return profile.getUser().getFlags().contains(flag);
        }

        return false;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("flag", flag)
                .add("group", group != null ? group.name() : null)
                .toString();
    }
}

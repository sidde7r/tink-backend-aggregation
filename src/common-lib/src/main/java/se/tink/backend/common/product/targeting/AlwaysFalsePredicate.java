package se.tink.backend.common.product.targeting;

import com.google.common.base.MoreObjects;
import com.google.common.base.Predicate;

public class AlwaysFalsePredicate implements Predicate<Profile> {
    @Override
    public boolean apply(Profile profile) {
        return false;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).toString();
    }
}

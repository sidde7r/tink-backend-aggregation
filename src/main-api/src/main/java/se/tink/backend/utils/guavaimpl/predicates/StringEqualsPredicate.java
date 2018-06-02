package se.tink.backend.utils.guavaimpl.predicates;

import com.google.common.base.Predicate;

import javax.annotation.Nullable;

public class StringEqualsPredicate implements Predicate<String> {

    private String equals;
    public StringEqualsPredicate(String equals) {
        if (equals == null) {
            throw new NullPointerException("Need non-null string");
        }
        this.equals = equals;
    }

    @Override
    public boolean apply(@Nullable String s) {
        return equals.equals(s);
    }
}

package se.tink.backend.aggregation.agents.nxgen.framework.validation;

import java.util.function.Predicate;

/** A rule is uniquely identified and recognized by its string identifier. */
public interface ValidationRule<Validatee> {
    String getRuleIdentifier();

    Predicate<Validatee> getCriterion();

    String getMessage(final Validatee validatee);
}

package se.tink.backend.aggregation.agents.consent;

@FunctionalInterface
public interface ToScope<INPUT_TYPE, SCOPE extends Scope> {

    SCOPE convert(INPUT_TYPE input);
}

package se.tink.backend.aggregation.agents.framework.wiremock.utils.parsingrules;

import java.util.List;
import java.util.function.Predicate;
import se.tink.backend.aggregation.agents.framework.wiremock.utils.parsingrules.parsingoperations.ParsingOperation;

public final class ParsingRule implements ParsingOperation, Predicate<String> {

    private final Predicate<String> matchingRule;
    private final ParsingOperation parsingOperation;

    private ParsingRule(Predicate<String> matchingRule, ParsingOperation parsingOperation) {
        this.matchingRule = matchingRule;
        this.parsingOperation = parsingOperation;
    }

    @Override
    public boolean test(final String line) {
        return matchingRule.test(line);
    }

    @Override
    public List<String> performOperation(String line) {
        return parsingOperation.performOperation(line);
    }

    public static ParsingRule of(
            final Predicate<String> whenPredicate, ParsingOperation doOperation) {
        return new ParsingRule(whenPredicate, doOperation);
    }
}

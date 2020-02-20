package se.tink.backend.aggregation.agents.framework.wiremock.utils.parsingrules.parsingoperations;

import java.util.Collections;
import java.util.List;

public final class RemoveLine implements ParsingOperation {

    @Override
    public List<String> performOperation(final String line) {
        return Collections.emptyList();
    }
}

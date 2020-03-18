package se.tink.backend.aggregation.agents.framework.wiremock.utils.parsingrules.parsingoperations;

import java.util.List;

public interface ParsingOperation {

    List<String> performOperation(final String line);
}

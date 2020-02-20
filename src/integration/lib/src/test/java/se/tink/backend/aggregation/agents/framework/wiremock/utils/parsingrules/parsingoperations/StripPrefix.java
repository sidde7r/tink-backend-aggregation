package se.tink.backend.aggregation.agents.framework.wiremock.utils.parsingrules.parsingoperations;

import java.util.Collections;
import java.util.List;

public final class StripPrefix implements ParsingOperation {

    @Override
    public List<String> performOperation(final String line) {
        final String prefixIndex = getPrefixIndex(line);
        return Collections.singletonList(line.substring(prefixIndex.length() + 3).trim());
    }

    private String getPrefixIndex(final String line) {
        return line.split(" ")[0];
    }
}

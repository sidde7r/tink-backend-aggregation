package se.tink.backend.aggregation.agents.framework.wiremock.utils.parsingrules.parsingoperations;

import com.google.common.collect.ImmutableList;
import java.util.List;

public final class InsertEmptyLineBefore implements ParsingOperation {

    private static final String EMPTY_LINE = "";

    @Override
    public List<String> performOperation(final String line) {
        return ImmutableList.of(EMPTY_LINE, line);
    }
}

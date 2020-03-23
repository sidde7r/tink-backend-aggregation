package se.tink.backend.aggregation.agents.framework.wiremock.utils.parsingrules.parsingoperations;

import com.google.common.collect.ImmutableList;
import java.util.List;

public final class RequestResponseSubstitution implements ParsingOperation {

    private static final String EMPTY_LINE = "";

    private final String substituteWith;

    public RequestResponseSubstitution(String substituteWith) {
        this.substituteWith = substituteWith + " %s";
    }

    @Override
    public List<String> performOperation(final String line) {
        return ImmutableList.of(EMPTY_LINE, String.format(substituteWith, getPrefixIndex(line)));
    }

    private String getPrefixIndex(final String line) {
        return line.split(" ")[0];
    }
}

package se.tink.backend.aggregation.agents.framework.wiremock.parsing;

import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import com.google.common.collect.ImmutableList;

public interface BodyParser {

    ImmutableList<StringValuePattern> getStringValuePatterns(
            final String body, final String mediaType);
}

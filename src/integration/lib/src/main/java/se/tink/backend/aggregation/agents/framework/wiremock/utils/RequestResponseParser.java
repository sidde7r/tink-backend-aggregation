package se.tink.backend.aggregation.agents.framework.wiremock.utils;

import java.util.Set;
import se.tink.backend.aggregation.agents.framework.wiremock.entities.HTTPRequest;
import se.tink.backend.aggregation.agents.framework.wiremock.entities.HTTPResponse;
import se.tink.libraries.pair.Pair;

public interface RequestResponseParser {

    Set<Pair<HTTPRequest, HTTPResponse>> parseRequestResponsePairs();
}

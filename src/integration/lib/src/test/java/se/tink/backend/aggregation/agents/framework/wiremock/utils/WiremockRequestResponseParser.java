package se.tink.backend.aggregation.agents.framework.wiremock.utils;

import java.util.List;
import se.tink.backend.aggregation.agents.framework.wiremock.entities.HTTPRequest;
import se.tink.backend.aggregation.agents.framework.wiremock.entities.HTTPResponse;
import se.tink.libraries.pair.Pair;

public interface WiremockRequestResponseParser {

    List<Pair<HTTPRequest, HTTPResponse>> parseRequestResponsePairs();
}

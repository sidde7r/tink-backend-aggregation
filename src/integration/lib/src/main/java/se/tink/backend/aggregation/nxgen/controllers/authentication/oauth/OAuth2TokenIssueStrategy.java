package se.tink.backend.aggregation.nxgen.controllers.authentication.oauth;

import java.util.Map;

public interface OAuth2TokenIssueStrategy {

    OAuth2Token issueToken(Map<String, String> authorizationResponseParams);
}

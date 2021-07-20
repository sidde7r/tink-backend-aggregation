package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.authenticator.dto.requests.token;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.authenticator.dto.common.ScopeDetailsEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.common.dto.requests.RequestHeaderEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
@SuperBuilder
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TokenRequest {

    // Used only in POST API - for GET API - real headers are sent.
    RequestHeaderEntity requestHeader;

    // Used only in POST API - for GET API - real headers are sent.
    @JsonProperty("is_user_session")
    Boolean isUserSession;

    // Used only in POST API - for GET API - real headers are sent.
    @JsonProperty("user_ip")
    String userIp;

    @JsonProperty("scope_details")
    ScopeDetailsEntity scopeDetails;

    @JsonProperty("redirect_uri")
    String redirectUri;

    @JsonProperty("client_id")
    String clientId;

    String scope;

    @JsonProperty("refresh_token")
    String refreshToken;

    @JsonProperty("exchange_token")
    String exchangeToken;

    @JsonProperty("grant_type")
    String grantType;

    @JsonProperty("Code")
    String code;

    @JsonProperty("user_agent")
    String userAgent;
}

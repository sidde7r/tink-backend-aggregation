package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.common.dto.requests;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@SuperBuilder
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@ToString
public class RequestHeaderEntity {
    // common
    String sendDate;
    String ipAddress;
    String userAgent;
    String tppId;
    String requestId;
    String apiKey;
    Boolean isDirectPsu;
    String callbackURL;

    // data fetch
    String token;

    // Currently used only in BNP Paribas
    @JsonProperty("client_id")
    String clientId;

    // Used for consent / authorize
    String psuContextIdentifierValue;
    String psuIdentifierType;
    String psuContextIdentifierType;
    String psuIdentifierValue;

    Boolean isCompanyContext;
}

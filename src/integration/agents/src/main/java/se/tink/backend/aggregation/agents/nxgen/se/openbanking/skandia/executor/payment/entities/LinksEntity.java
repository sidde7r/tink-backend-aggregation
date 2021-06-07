package se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.executor.payment.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
@JsonInclude(Include.NON_NULL)
public class LinksEntity {

    private final LinkDetailsEntity scaRedirect;
    private final LinkDetailsEntity scaOAuth;
    private final LinkDetailsEntity startAuthorisation;
    private final LinkDetailsEntity self;
    private final LinkDetailsEntity status;
    private final LinkDetailsEntity scaStatus;

    @JsonCreator
    public LinksEntity(
            @JsonProperty("scaRedirect") LinkDetailsEntity scaRedirect,
            @JsonProperty("scaOAuth") LinkDetailsEntity scaOAuth,
            @JsonProperty("startAuthorisation") LinkDetailsEntity startAuthorisation,
            @JsonProperty("self") LinkDetailsEntity self,
            @JsonProperty("status") LinkDetailsEntity status,
            @JsonProperty("scaStatus") LinkDetailsEntity scaStatus) {
        this.scaRedirect = scaRedirect;
        this.scaOAuth = scaOAuth;
        this.startAuthorisation = startAuthorisation;
        this.self = self;
        this.status = status;
        this.scaStatus = scaStatus;
    }
}

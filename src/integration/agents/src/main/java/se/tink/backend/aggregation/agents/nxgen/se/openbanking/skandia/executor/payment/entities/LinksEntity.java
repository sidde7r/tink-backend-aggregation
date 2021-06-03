package se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.executor.payment.entities;

import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class LinksEntity {

    private LinkDetailsEntity scaRedirect;
    private LinkDetailsEntity scaOAuth;
    private LinkDetailsEntity startAuthorisation;
    private LinkDetailsEntity self;
    private LinkDetailsEntity status;
    private LinkDetailsEntity scaStatus;
}

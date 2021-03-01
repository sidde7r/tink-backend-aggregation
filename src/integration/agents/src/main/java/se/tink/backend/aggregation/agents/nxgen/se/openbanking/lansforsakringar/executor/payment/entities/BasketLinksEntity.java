package se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.executor.payment.entities;

import lombok.Setter;
import se.tink.backend.aggregation.annotations.JsonObject;

@Setter
@JsonObject
public class BasketLinksEntity {

    private LinkDetailsEntity self;
    private LinkDetailsEntity startAuthorisation;

    public String getAuthorizationUrl() {
        return startAuthorisation.getHref();
    }
}

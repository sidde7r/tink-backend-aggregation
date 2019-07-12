package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fidor.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fidor.fetcher.transactionalaccount.entities.LinkEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LinksEntity {

    @JsonProperty("startAuthorisationWithTransactionAuthorisation")
    private LinkEntity autorizationLink;

    public String getAutorizationLink() {
        return autorizationLink.getHref();
    }
}

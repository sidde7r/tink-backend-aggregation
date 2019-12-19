package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc;

import java.util.Optional;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public abstract class AbstractBankIdResponse {

    private LinksEntity links;

    public LinksEntity getLinks() {
        return Optional.ofNullable(links).orElseThrow(IllegalStateException::new);
    }
}

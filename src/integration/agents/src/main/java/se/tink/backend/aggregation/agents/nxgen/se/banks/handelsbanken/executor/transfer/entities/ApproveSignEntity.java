package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.transfer.entities;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.entities.Link;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ApproveSignEntity {

    private Link link;

    public Link getLink() {
        return link;
    }
}
